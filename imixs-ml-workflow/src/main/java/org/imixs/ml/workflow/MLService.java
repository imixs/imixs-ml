package org.imixs.ml.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.imixs.ml.core.MLClient;
import org.imixs.ml.core.MLConfig;
import org.imixs.ml.core.MLEntity;
import org.imixs.ml.core.MLTrainingResult;
import org.imixs.ml.events.EntityObjectEvent;
import org.imixs.ml.training.TrainingDataBuilder;
import org.imixs.ml.xml.XMLAnalyseEntity;
import org.imixs.ml.xml.XMLAnalyseResult;
import org.imixs.ml.xml.XMLTrainingData;
import org.imixs.workflow.FileData;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.Model;
import org.imixs.workflow.engine.EventLogService;
import org.imixs.workflow.engine.ModelService;
import org.imixs.workflow.engine.ProcessingEvent;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.util.XMLParser;

import util.LocaleHelper;

/**
 * The MLService reacts on Processing events and updates the ml.status item. If
 * no ml.status item exists, and ml.items is not empty, than the status is set
 * to 'suggest'. If the status is 'suggest' and the current event is 'public'
 * than the status is set to 'confirmed'
 * <p>
 * If the status was already confirmed, the workitem type is 'archive', than the
 * ml status is set to 'training' and a eventLog entry is created to indicate
 * that this workitem can be send to the training service. (Note: the training
 * service can reject the workitem for training if the data is insufficient of
 * quality)
 * 
 * @author rsoika
 *
 */
@Stateless
@LocalBean
public class MLService implements Serializable {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(MLService.class.getName());

    public static final String ITEM_ML_DEFINITIONS = "ml.definitions";
    public static final String ITEM_ML_ENDPOINT = "ml.endpoint";
    public static final String ITEM_ML_ITEMS = "ml.items";
    public static final String ITEM_ML_LOCALES = "ml.locales";
    public static final String ITEM_ML_STATUS = "ml.status";
    public static final String ITEM_ML_MODEL = "ml.model";
    public static final String ITME_ML_OPTIONS = "ml.options";

    public static final String ML_STATUS_SUGGEST = "suggest";
    public static final String ML_STATUS_CONFIRMED = "confirmed";
    public static final String ML_STATUS_TRAINING = "training";

    public static final String EVENTLOG_TOPIC_TRAINING = "ml.training";

    // enabled
    @Inject
    @ConfigProperty(name = MLTrainingScheduler.ML_TRAINING_SCHEDULER_ENABLED, defaultValue = "false")
    boolean trainingSchedulerEnabled;

    @Inject
    protected Event<EntityObjectEvent> entityObjectEvents;

    @Inject
    protected ModelService modelService;

    @Inject
    protected WorkflowService workflowService;

    @Inject
    protected EventLogService eventLogService;

    /**
     * WorkflowEvent listener to update the ml.status property.
     * <p>
     * If no ml.status item exists, and ml.item is not empty, than the status is set
     * to 'suggest'
     * <p>
     * If the status is 'suggest' and the current event is 'public' than the status
     * is set to 'confirmed'
     * 
     * @param processingEvent
     */
    public void onWorkflowEvent(@Observes ProcessingEvent processingEvent) {
        if (processingEvent == null)
            return;

        ItemCollection workitem = processingEvent.getDocument();

        // skip if not a workItem...
        if (workitem != null && !workitem.getItemValueString("type").startsWith("workitem")) {
            return;
        }

        int eventType = processingEvent.getEventType();

        List<ItemCollection> mlDefinitionList = getMLDefinitions(workitem);
        boolean bUpdateDefinitions = false;
        for (ItemCollection mlDefinition : mlDefinitionList) {
            List<MLEntity> mlEntities = MLConfig.explodeMLEntityList(mlDefinition.getItemValue(ITEM_ML_ITEMS));
            // set initial status?
            if (ProcessingEvent.AFTER_PROCESS == eventType) {
                if (mlEntities.size() > 0 && mlDefinition.getItemValueString(ITEM_ML_STATUS).isEmpty()) {
                    mlDefinition.setItemValue(ITEM_ML_STATUS, ML_STATUS_SUGGEST);
                    bUpdateDefinitions = true;
                    continue;
                }
            }

            // set confirmed status?
            if (ProcessingEvent.BEFORE_PROCESS == eventType) {
                if (mlEntities.size() > 0
                        && ML_STATUS_SUGGEST.equals(mlDefinition.getItemValueString(ITEM_ML_STATUS))) {
                    // test if we have a public event
                    Model model;
                    try {

                        model = modelService.getModelByWorkitem(workitem);
                        ItemCollection event = model.getEvent(workitem.getTaskID(), workitem.getEventID());

                        // load an optional ml-config
                        ItemCollection mlConfig = workflowService.evalWorkflowResult(event, "ml-config", workitem,
                                false);
                        // if ml-config status is defined updat the status flag....
                        if (mlConfig != null && mlConfig.hasItem("status")) {
                            // update the status flag
                            mlDefinition.setItemValue(ITEM_ML_STATUS, mlConfig.getItemValueString("status"));
                            bUpdateDefinitions = true;
                            continue;
                        } else {
                            // default behavior - set confirmed status to 'confirmed'...
                            // set only if event is userControlled != No
                            if (!"0".equals(event.getItemValueString("keypublicresult"))) {
                                // update status
                                mlDefinition.setItemValue(ITEM_ML_STATUS, ML_STATUS_CONFIRMED);
                                bUpdateDefinitions = true;
                                continue;
                            }
                        }
                    } catch (ModelException | PluginException e) {
                        logger.warning("unable to parse current bpmn event: " + e.getMessage());
                    }
                }
            }

            // set training status?
            if (ProcessingEvent.AFTER_PROCESS == eventType && trainingSchedulerEnabled
                    && ML_STATUS_CONFIRMED.equals(mlDefinition.getItemValueString(ITEM_ML_STATUS))
                    && "workitemarchive".equals(workitem.getType())) {

                // update status...
                mlDefinition.setItemValue(ITEM_ML_STATUS, ML_STATUS_TRAINING);
                // ... and create a eventLog entry
                eventLogService.createEvent(EVENTLOG_TOPIC_TRAINING, workitem.getUniqueID());
                bUpdateDefinitions = true;
                continue;
            }
        }

        // update the ml.definitions?
        if (bUpdateDefinitions) {
            updateMLDefinitions(processingEvent.getDocument(), mlDefinitionList);
        }
    }

    /**
     * This method returns a string with all the text content of all documents
     * attached to a workitem.
     * 
     * @return
     */
    @SuppressWarnings("rawtypes")
    public String getAllDocumentText(ItemCollection workitem) {
        if (workitem == null) {
            return null;
        }

        String result = "";
        List<FileData> fileDataList = workitem.getFileData();

        for (FileData fileData : fileDataList) {
            List fileText = (List) fileData.getAttribute("text");
            if (fileText != null && fileText.size() > 0) {
                result = result + fileText.get(0) + " ";
            }
        }

        return result;
    }

    /**
     * This method sends a given workitem to each ML Training Service defined in the
     * item 'ml.definitions'.
     * <p>
     * The method is called by the MLTrainingScheduler.
     * <p>
     * 
     * @return a TrainingResult object containing the quality level and the Rest
     *         Service response
     * 
     * @param uid
     */
    public MLTrainingResult trainWorkitem(String uid) {
        MLTrainingResult trainingResult = null;
        // load the workitem
        ItemCollection workitem = workflowService.getWorkItem(uid);
        if (workitem == null) {
            throw new IllegalArgumentException("Invalid workitem uid '" + uid + "!");
        }

        // iterate over all ml definitions....
        List<ItemCollection> mlDefinitionList = getMLDefinitions(workitem);
        for (ItemCollection mlDefinition : mlDefinitionList) {

            String mlStatus = mlDefinition.getItemValueString(ITEM_ML_STATUS);
            if (!ML_STATUS_TRAINING.equals(mlStatus)) {
                // skip this definition
                continue;
            }
            String mlEndpoint = mlDefinition.getItemValueString(ITEM_ML_ENDPOINT);
            String mlModel = mlDefinition.getItemValueString(ITEM_ML_MODEL);
            String mlLocals = mlDefinition.getItemValueString(ITEM_ML_LOCALES);
            String mlOptions = mlDefinition.getItemValueString(ITME_ML_OPTIONS);
            // String mlQuality = mlDefinition.getItemValueString(ITEM_ML_QUALITY);
            logger.info("...train " + mlEndpoint + " model: " + mlModel);

            // send workitem to training service
            MLClient mlClient = new MLClient(mlEndpoint);
            String content = getAllDocumentText(workitem);
            List<MLEntity> mlEntities = MLConfig.explodeMLEntityList(mlDefinition.getItemValue(ITEM_ML_ITEMS));
            // parse locales
            List<Locale> locales = LocaleHelper.parseLocales(mlLocals);

            // build training data set...
            XMLTrainingData trainingData = new TrainingDataBuilder(content, workitem, mlEntities, locales)
                    .setAnalyzerEntityEvents(entityObjectEvents).build();

            // verify the TRAININGDATA_QUALITY_LEVEL
            if (XMLTrainingData.TRAININGDATA_QUALITY_BAD == trainingData.getQuality()) {
                logger.warning("...document '" + workitem.getUniqueID()
                        + "' TRAININGDATA_QUALITY_LEVEL=BAD - document will be ignored!");
                return new MLTrainingResult(XMLTrainingData.TRAININGDATA_QUALITY_BAD, null);
            }

            // post training data...
            // validate if usefull data
            if (!trainingData.isEmpty()) {
                String resultData = mlClient.postTrainingData(trainingData, mlModel, mlOptions);
                trainingResult = new MLTrainingResult(trainingData.getQuality(), resultData);
            }
        }

        return trainingResult;
    }

    /**
     * This method updates the item 'ml.definitions' of a workitem holding a list of
     * ML Endpoint definitions. Each endpoint definition is defined by a set of
     * items stored in a map. The Map can be converted into a ItemCollection
     * <p>
     * If a endpoint definition with the same service endpoint and model name
     * already exists, the method overwrites this entry.
     * 
     * @param workitem              - the workitem holding a list of existing
     *                              endpoint definitions
     * @param newEndpointDefinition - the new Endpoint definition to be stored.
     */
    @SuppressWarnings("unchecked")
    public void updateMLDefinition(ItemCollection workitem, ItemCollection newMLDefinition) {

        if (!newMLDefinition.hasItem(ITEM_ML_ENDPOINT) || !newMLDefinition.hasItem(ITEM_ML_MODEL)) {
            throw new IllegalArgumentException("A ml definition must contain a least a ml.endpoint and a ml.model!");
        }

        List<Map<String, List<Object>>> mlDefinitionList = workitem.getItemValue(MLService.ITEM_ML_DEFINITIONS);

        // test if the list already have a definition for the current endpoint/model and
        // remove it....
        Iterator<Map<String, List<Object>>> iter = mlDefinitionList.iterator();
        while (iter.hasNext()) {
            ItemCollection aEndpointDev = new ItemCollection(iter.next());
            // do we have already
            if (newMLDefinition.getItemValueString(ITEM_ML_ENDPOINT)
                    .equals(aEndpointDev.getItemValueString(ITEM_ML_ENDPOINT))
                    && newMLDefinition.getItemValueString(ITEM_ML_MODEL)
                            .equals(aEndpointDev.getItemValueString(ITEM_ML_MODEL))) {
                iter.remove();
            }
        }

        // now we add the new definition
        mlDefinitionList.add(newMLDefinition.getAllItems());

        // update the ml.definitions..
        workitem.setItemValue(MLService.ITEM_ML_DEFINITIONS, mlDefinitionList);

    }

    /**
     * This method returns a list of ItemCollection object holding the
     * ml.definitions stored in a given workitem. Each endpoint definition is
     * defined by a set of items stored in a map. The Map can be conferted into a
     * ItemCollection
     * 
     **/
    @SuppressWarnings("unchecked")
    public List<ItemCollection> getMLDefinitions(ItemCollection workitem) {

        List<ItemCollection> result = new ArrayList<ItemCollection>();
        // test if ml.definitions object exists...
        if (!workitem.getItemValueString(MLService.ITEM_ML_DEFINITIONS).isEmpty()) {
            List<Map<String, List<Object>>> mlDefinitions = workitem.getItemValue(MLService.ITEM_ML_DEFINITIONS);
            for (Map<String, List<Object>> aDef : mlDefinitions) {
                result.add(new ItemCollection(aDef));
            }
        }
        return result;
    }

    /**
     * Analyse a text content by calling a ML Service Endpoint
     * <p>
     * The method returns a list of XMLAnalyseEntity extracted from the given text
     * or null if the request failed.
     * 
     * @param text            - text to be analyzed
     * @param serviceEndpoint - the ml API endpoint
     * @param mlModelName     - the ML model name to be used by the endpoint
     * @return XMLAnalyseResult
     **/
    public XMLAnalyseResult analyseTextByMLFramework(String text, String serviceEndpoint, String mlModelName) {
        // create a MLClient for the current service endpoint
        MLClient mlClient = new MLClient(serviceEndpoint);
        XMLAnalyseResult result = mlClient.postAnalyseData(text, mlModelName);
        return result;
    }

    /**
     * Analyse a text by searching for matches in a text with Regex Pattern
     * Recognition
     * <p>
     * The method expects a XMLAnalseEntity and does only apply a Regex if the
     * corresponding item is not yet part of the given XMLAnalseEntity.
     * 
     * 
     * @param text         - text to be analyzed
     * @param anylseResult - a optional existing XMLAnalyseResult instance
     * @return XMLAnalyseResult containing a list of XMLAnalyseEntity
     **/
    public XMLAnalyseResult analyseTextByRegex(String text, ItemCollection mlConfig, XMLAnalyseResult anylseResult) {
        // do we have alredy an anylseResult?
        if (anylseResult == null) {
            anylseResult = new XMLAnalyseResult();
        }
        // parse the mlConfig object for optional regex definitions
        List<String> regexDevList = mlConfig.getItemValueList("regex", String.class);
        if (regexDevList.size() == 0) {
            // no regex definitions found
            return anylseResult;
        }
        for (String regexDev : regexDevList) {
            if (regexDev.trim().isEmpty()) {
                // no definition
                continue;
            }
            try {
                // evaluate the item content (XML format expected here!)
                ItemCollection regexData = XMLParser.parseItemStructure(regexDev);

                if (regexData != null) {
                    String name = regexData.getItemValueString("name");
                    String patternString = regexData.getItemValueString("pattern");

                    // do we have already a result for this item?
                    List<XMLAnalyseEntity> mlEntityList = anylseResult.getEntities();
                    boolean found = false;
                    for (XMLAnalyseEntity analyseEntity : mlEntityList) {
                        if (name.equals(analyseEntity.getLabel()) && !analyseEntity.getText().isEmpty()) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        // no value found so far - so apply the regex pattern...
                        Pattern p = Pattern.compile(patternString); // the pattern to search for
                        Matcher m = p.matcher(text);
                        // now try to find at least one match
                        while (m.find()) {
                            String value = m.group();
                            logger.info("...apply Regex Pattern Recognition for '" + name + "'");
                            anylseResult.getEntities().add(new XMLAnalyseEntity(name, value));
                        }
                    }
                }
            } catch (PluginException e) {
                logger.warning("Invalid ml.config definition with unexpected regex element - verify model!");
            }

        }

        return anylseResult;
    }

    /**
     * This helper method converts a list of ItemCollections into a map and updates
     * the ML.definitions item.
     * 
     * @param workitem
     * @param mlDefinitionList
     */
    private void updateMLDefinitions(ItemCollection workitem, List<ItemCollection> mlDefinitionList) {
        List<Map<String, List<Object>>> newDefinitionList = new ArrayList<Map<String, List<Object>>>();
        for (ItemCollection mlDefinition : mlDefinitionList) {
            newDefinitionList.add(mlDefinition.getAllItems());
        }
        workitem.setItemValue(MLService.ITEM_ML_DEFINITIONS, newDefinitionList);
    }

}
