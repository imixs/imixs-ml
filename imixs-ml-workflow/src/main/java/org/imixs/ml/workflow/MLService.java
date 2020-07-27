package org.imixs.ml.workflow;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.IntPredicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.imixs.ml.core.MLClient;
import org.imixs.ml.events.EntityObjectEvent;
import org.imixs.ml.training.TrainingDataBuilder;
import org.imixs.ml.xml.XMLTrainingData;
import org.imixs.workflow.FileData;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.Model;
import org.imixs.workflow.engine.EventLogService;
import org.imixs.workflow.engine.ModelService;
import org.imixs.workflow.engine.ProcessingEvent;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.exceptions.ModelException;

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

    public static final String ITEM_ML_ITEMES = "ml.items";
    public static final String ITEM_ML_STATUS = "ml.status";

    public static final String ML_SERVICE_ENDPOINT = "ml.service.endpoint";
    public static final String ML_LOCALES = "ml.locales";

    public static final String ML_STATUS_SUGGEST = "suggest";
    public static final String ML_STATUS_CONFIRMED = "confirmed";
    public static final String ML_STATUS_TRAINING = "training";

    public static final String EVENTLOG_TOPIC_TRAINING = "ml.training";

    @Inject
    @ConfigProperty(name = ML_SERVICE_ENDPOINT, defaultValue = "")
    private String mlAPIEndpoint;

    @Inject
    @ConfigProperty(name = MLService.ML_LOCALES, defaultValue = "DE,UK")
    private String mlDefaultLocales;

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
    @SuppressWarnings("unchecked")
    public void onWorkflowEvent(@Observes ProcessingEvent processingEvent) {
        if (processingEvent == null)
            return;

        ItemCollection workitem = processingEvent.getDocument();

        // skip if not a workItem...
        if (workitem != null && !workitem.getItemValueString("type").startsWith("workitem"))
            return;

        int eventType = processingEvent.getEventType();

        // if it was an public event we set the ml.status to 'verified'
        List<String> mlItems = workitem.getItemValue(ITEM_ML_ITEMES);

        // set initial status?
        if (ProcessingEvent.AFTER_PROCESS == eventType) {
            if (mlItems.size() > 0 && workitem.getItemValueString(ITEM_ML_STATUS).isEmpty()) {
                workitem.setItemValue(ITEM_ML_STATUS, ML_STATUS_SUGGEST);
                return;
            }
        }

        // set confirmed status?
        if (ProcessingEvent.BEFORE_PROCESS == eventType) {
            if (mlItems.size() > 0 && ML_STATUS_SUGGEST.equals(workitem.getItemValueString(ITEM_ML_STATUS))) {
                // test if we have a public event
                Model model;
                try {
                    model = modelService.getModelByWorkitem(workitem);
                    ItemCollection event = model.getEvent(workitem.getTaskID(), workitem.getEventID());
                    // ad only activities with userControlled != No
                    if (!"0".equals(event.getItemValueString("keypublicresult"))) {
                        // update status
                        workitem.setItemValue(ITEM_ML_STATUS, ML_STATUS_CONFIRMED);
                        return;
                    }
                } catch (ModelException e) {
                    logger.warning("unable to parse current bpmn event: " + e.getMessage());
                }
            }

        }

        // set trainng status?
        if (ProcessingEvent.AFTER_PROCESS == eventType
                && ML_STATUS_CONFIRMED.equals(workitem.getItemValueString(ITEM_ML_STATUS))
                && "workitemarchive".equals(workitem.getType())) {

            // update status...
            workitem.setItemValue(ITEM_ML_STATUS, ML_STATUS_TRAINING);
            // ... and create a eventLog entry
            eventLogService.createEvent(EVENTLOG_TOPIC_TRAINING, workitem.getUniqueID());
        }

    }

    /**
     * This method strip control codes and the characters '{', '}' and '"' from a
     * text string.
     * <p>
     * The method also strips multiple space characters. 'A B' -> 'A B'
     * 
     * @param text
     * @return
     */
    public String cleanTextdata(String text) {

        // replace newline with a space
        String result = text.replaceAll("\n", " ");

        // strip control codes
        result = stripChars(result, c -> c > '\u001F' && c != '\u007F');

        // replace '{', '}' and '"' with a space
        result = result.replaceAll("[{}\"]", " ");

        // strip more than one space.
        result = result.replaceAll("[ ]{2,}", " ");

        return result;

    }

    private String stripChars(String s, IntPredicate include) {
        return s.codePoints().filter(include::test)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }

    /**
     * Retruns a string with all the document text
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
        // clean the text form unsupported characters
        return cleanTextdata(result);
    }

    /**
     * This method sends a given workitem to the ML Training Service via Rest API.
     * The method is called by the MLTrainingScheduler.
     * 
     * @param uid
     */
    @SuppressWarnings("unchecked")
    public void trainWorkitem(String uid) {
        // load the workitem
        ItemCollection workitem = workflowService.getWorkItem(uid);

        if (workitem != null) {
            // send workitem to training service
            MLClient mlClient = new MLClient();

            String content = getAllDocumentText(workitem);
            List<String> itemNames = workitem.getItemValue(ITEM_ML_ITEMES);

            // parse locales
            Set<Locale> locales = parseMLLocales();

            // build training data set...
            XMLTrainingData trainingData = new TrainingDataBuilder(content, workitem, itemNames, locales)
                    .setAnalyzerEntityEvents(entityObjectEvents).build();

            mlClient.postTrainingData(trainingData, mlAPIEndpoint);
        }

    }

    /**
     * This helper method parses the locales provided by the imixs.property or an
     * environment variable
     * 
     * 
     * @return
     */
    private Set<Locale> parseMLLocales() {
        boolean debug = logger.isLoggable(Level.FINE);
        debug = true;
        Set<Locale> locals = new HashSet<Locale>();

        // translate locales..
        String[] sLocales = mlDefaultLocales.split(",");

        for (String _locale : sLocales) {
            Locale aLocale = new Locale(_locale);
            locals.add(aLocale);
            if (debug) {
                logger.info("......suporting locale " + aLocale);
            }
        }
        return locals;
    }

}