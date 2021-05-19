/*  
 *  Imixs-Workflow 
 *  
 *  Copyright (C) 2001-2020 Imixs Software Solutions GmbH,  
 *  http://www.imixs.com
 *  
 *  This program is free software; you can redistribute it and/or 
 *  modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation; either version 2 
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 *  General Public License for more details.
 *  
 *  You can receive a copy of the GNU General Public
 *  License at http://www.gnu.org/licenses/gpl.html
 *  
 *  Project: 
 *      https://www.imixs.org
 *      https://github.com/imixs/imixs-workflow
 *  
 *  Contributors:  
 *      Imixs Software Solutions GmbH - Project Management
 *      Ralph Soika - Software Developer
 */

package org.imixs.ml.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.imixs.ml.core.MLClient;
import org.imixs.ml.core.MLConfig;
import org.imixs.ml.core.MLContentBuilder;
import org.imixs.ml.core.MLEntity;
import org.imixs.ml.events.EntityTextEvent;
import org.imixs.ml.xml.XMLAnalyseEntity;
import org.imixs.ml.xml.XMLAnalyseResult;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.SignalAdapter;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.exceptions.AdapterException;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.exceptions.ProcessingErrorException;
import org.imixs.workflow.util.XMLParser;

import util.LocaleHelper;

/**
 * This adapter class is used for ml analysis based on the Imixs-ML project.
 * <p>
 * The MLAdaper automatically analyses the text content of a attached fields,
 * and stores entities found in the text into corresponding items.
 * <p>
 * By the parameters 'ml.endpoint' and 'ml.locales' the MLAdapter can be
 * configured in the imixs.properties or by environment variables
 * (ML_API_ENDPOINT' and 'ML_LOCALES')
 * <p>
 * The Adapter can be optional configured through the model by defining a
 * workflow result tag named 'ml-config'.
 * <p>
 * Example:
 * 
 * <pre>
 * {@code
<ml-config>
    <endpoint>https://localhost:8111/api/training/{model}</endpoint>
    <locales>de_DE,en_GB</locales>
</ml-config>
 * }
 * </pre>
 * <p>
 * Per default the MLAdapter takes all entities into the current workitem if an
 * item with the name did not yet exist.
 * <p>
 * To configure the behavior of the entity adaption optional configuration via
 * the workflow model is possible:
 * <p>
 * 
 * <pre>
 * {@code
<ml-config name="entity">
    <name>_invoicetotal</name>
    <type>currency</type>
</ml-entity>
<ml-config name="entity">
    <name>_cdtr_bic</name>
    <type>text</type>
    <mapping>bic</mapping>
</ml-entity>
 * }
 * </pre>
 * 
 * @author Ralph Soika
 * @version 1.0
 *
 */

public class MLAdapter implements SignalAdapter {

    public static final String ML_ENTITY = "entity";
    public static final String API_ERROR = "API_ERROR";

    public static final int API_EVENT_SUCCESS = 110;
    public static final int API_EVENT_FAILURE = 90;

    private static Logger logger = Logger.getLogger(MLAdapter.class.getName());

    @Inject
    @ConfigProperty(name = MLConfig.ML_SERVICE_ENDPOINT)
    Optional<String> mlDefaultAPIEndpoint;

    @Inject
    @ConfigProperty(name = MLConfig.ML_MODEL, defaultValue = "imixs-model")
    String mlDefaultModel;

    @Inject
    @ConfigProperty(name = MLConfig.ML_LOCALES, defaultValue = "de_DE,en_GB")
    private String mlDefaultLocales;
    
    @Inject
    @ConfigProperty(name = MLConfig.ML_TRAINING_QUALITYLEVEL, defaultValue = "PARTIAL")
    String mlDefaultQualityLevel;

    @Inject
    private WorkflowService workflowService;

    @Inject
    private MLService mlService;

    @Inject
    private Event<EntityTextEvent> entityTextEvents = null;

    /**
     * This method posts a text from an attachment to the Imixs-ML Analyse service
     * endpoint
     */
    public ItemCollection execute(ItemCollection document, ItemCollection event) throws AdapterException {
        String mlAPIEndpoint = null;
        String mlModelName = null;
        String mlLocals = null;
        //String mlQuality = null;
        Pattern mlFilenamePattern = null;
        List<Locale> locals = new ArrayList<Locale>();
        List<MLEntity> mlEntities = null;
        boolean debug = logger.isLoggable(Level.FINE);
        debug = true;

        logger.finest("...running api adapter...");

        // read optional configuration form the model or imixs.properties....
        try {
            ItemCollection mlConfig = workflowService.evalWorkflowResult(event, "ml-config", document, false);

            mlAPIEndpoint = parseMLEndpointByBPMN(mlConfig);
            mlModelName = parseMLModelByBPMN(mlConfig);
            mlLocals = parseMLLocalesByBPMN(mlConfig);
            //mlQuality = parseMLQualityByBPMN(mlConfig);
            // parse optional filename regex pattern...
            String _FilenamePattern = parseMLFilePatternByBPMN(mlConfig);
            if (_FilenamePattern != null && !_FilenamePattern.isEmpty()) {
                mlFilenamePattern = Pattern.compile(_FilenamePattern);
            }

            // convert locals definitions into a List of Locales
            locals = LocaleHelper.parseLocales(mlLocals);
            // test if the model provides optional entity definitions.
            mlEntities = parseMLEntitiesByBPMN(mlConfig);
        } catch (PluginException e) {
            logger.warning("Unable to parse item definitions for 'ml-config', verify model - " + e.getMessage());
        }

        // do we have a valid endpoint?
        if (mlAPIEndpoint == null || mlAPIEndpoint.isEmpty()) {
            throw new ProcessingErrorException(MLAdapter.class.getSimpleName(), API_ERROR,
                    "imixs-ml service endpoint is empty!");
        }
        // add the analyzse/ resource
        if (mlAPIEndpoint.indexOf("/analyse") > -1) {
            throw new AdapterException(MLAdapter.class.getSimpleName(), API_ERROR,
                    "imixs-ml wrong service endpoint - should not contain \"/analyzse\" resource!");
        }

        // build the ml content....
        String mlContent = new MLContentBuilder(document, null, false, mlFilenamePattern).build();

        // if we have ocr text content than we call the ml api endpoint
        if (!mlContent.isEmpty()) {
            // create a MLClient for the current service endpoint
            MLClient mlClient = new MLClient(mlAPIEndpoint);
            XMLAnalyseResult result = mlClient.postAnalyseData(mlContent, mlModelName);

            if (result == null) {
                // interrupt current transaction
                throw new ProcessingErrorException(MLAdapter.class.getSimpleName(), API_ERROR,
                        "imixs-ml api error at endpoint: " + mlAPIEndpoint + "!");
            }

            /*
             * We now have a list of XMLAnalyseEntity objects possible matching the same
             * item. In the following we group matching items by itemName and fire a
             * EntityObjectEvent to test if an adapter provides a unique value.
             */
            Map<String, List<String>> groupedEntityList = groupTextValues(result);

            // analyse the entities....
            for (Map.Entry<String, List<String>> mlEntity : groupedEntityList.entrySet()) {

                String mlEntityName = mlEntity.getKey();

                // is this entity listed in our configuration?
                MLEntity entityDef= MLConfig.findMLEntityByName(mlEntityName,mlEntities);
                if (entityDef!=null) {
                    // Do we have an entityDefinition for this entity?
                    // If not we do ignore this ml item! issue #34                    
                    if (document.isItemEmpty(entityDef.getItemName())) {
                        List<String> itemValueList = mlEntity.getValue();
                        // fire entityTextEvents so that an adapter can resolve the text into a
                        // object
                        EntityTextEvent entityTextEvent = new EntityTextEvent(itemValueList, locals,
                                entityDef.getItemType(), entityDef.getLength());
                        entityTextEvents.fire(entityTextEvent);

                        Object _resultValueObject = null;
                        // test if we found an object
                        if (entityTextEvent.getItemValue() != null) {
                            // set the value
                            if (debug) {
                                logger.info("Best match=" + entityTextEvent.getItemValue());
                            }
                            _resultValueObject = entityTextEvent.getItemValue();
                        } else {
                            // if type=text or type="" then  set the first text value as is
                            if (entityDef.getItemType().isEmpty() || "text".equals(entityDef.getItemType())) {
                                _resultValueObject = mlEntity.getValue().iterator().next();
                            }
                        }
                        // if it is a text string than cut the length to the allowed maxium text length
                        if (_resultValueObject instanceof String) {
                            if (_resultValueObject.toString().length() > entityDef.getLength()) {
                                _resultValueObject = _resultValueObject.toString().substring(0, entityDef.getLength())
                                        .trim();
                            }
                        }
                        document.setItemValue(entityDef.getItemName(), _resultValueObject);
                    }
                }
            }

            // Finally we store the mlItems, the MLModel name and the mlLocale definitions
            // for each Service endpoint into
            // the item 'ml.definitions'

            ItemCollection mlDefinition = new ItemCollection();
            mlDefinition.setItemValue(MLService.ITEM_ML_ENDPOINT, mlAPIEndpoint);
            mlDefinition.setItemValue(MLService.ITEM_ML_MODEL, mlModelName);
            //mlDefinition.setItemValue(MLService.ITEM_ML_ITEMS, entityDefinitions.keySet());
            mlDefinition.setItemValue(MLService.ITEM_ML_ITEMS,MLConfig.implodeMLEntityList(mlEntities) );
            mlDefinition.setItemValue(MLService.ITEM_ML_LOCALES, mlLocals);
            //mlDefinition.setItemValue(MLService.ITEM_ML_QUALITY, mlQuality);
            mlService.updateMLDefinition(document, mlDefinition);

        } else {
            logger.finest("......no ml content found to be analysed for " + document.getUniqueID());
        }

        return document;
    }

    /**
     * This helper method parses the ml api endpoint either provided by a model
     * definition or a imixs.property or an environment variable
     * 
     * @param mlConfig
     * @return
     */
    private String parseMLEndpointByBPMN(ItemCollection mlConfig) {
        boolean debug = logger.isLoggable(Level.FINE);
        debug = true;
        String mlAPIEndpoint = null;

        // test if the model provides a MLEndpoint. If not, the adapter uses the
        // mlDefaultAPIEndpoint
        mlAPIEndpoint = null;
        if (mlConfig != null) {
            mlAPIEndpoint = mlConfig.getItemValueString("endpoint");
        }

        // switch to default api endpoint?
        if (mlAPIEndpoint == null || mlAPIEndpoint.isEmpty()) {
            // set defautl api endpoint if defined
            if (mlDefaultAPIEndpoint.isPresent() && !mlDefaultAPIEndpoint.get().isEmpty()) {
                mlAPIEndpoint = mlDefaultAPIEndpoint.get();
            }
        }
        if (debug) {
            logger.info("......ml api endpoint " + mlAPIEndpoint);
        }

        if (!mlAPIEndpoint.endsWith("/")) {
            mlAPIEndpoint = mlAPIEndpoint + "/";
        }

        return mlAPIEndpoint;

    }

    /**
     * This helper method parses the ml model name either provided by a model
     * definition or a imixs.property or an environment variable
     * 
     * @param mlConfig
     * @return
     */
    private String parseMLModelByBPMN(ItemCollection mlConfig) {
        boolean debug = logger.isLoggable(Level.FINE);
        debug = true;
        String mlModel = null;

        // test if the model provides a MLModel name. If not, the adapter uses the
        // mlDefaultAPIEndpoint
        if (mlConfig != null) {
            mlModel = mlConfig.getItemValueString("model");
        }

        // switch to default api endpoint?
        if (mlModel == null || mlModel.isEmpty()) {
            // set defautl model if defined
            mlModel = mlDefaultModel;
        }
        if (debug) {
            logger.info("......ml model = " + mlModel);
        }

        return mlModel;

    }
    
    
    
    /**
     * This helper method parses the ml model name either provided by a model
     * definition or a imixs.property or an environment variable
     * 
     * @param mlConfig
     * @return
     */
//    private String parseMLQualityByBPMN(ItemCollection mlConfig) {
//        boolean debug = logger.isLoggable(Level.FINE);
//        debug = true;
//        String mlQuality = null;
//
//        // test if the model provides a MLModel name. If not, the adapter uses the
//        // mlDefaultAPIEndpoint
//        if (mlConfig != null) {
//            mlQuality = mlConfig.getItemValueString("quality");
//        }
//
//        // switch to default api endpoint?
//        if (mlQuality == null || mlQuality.isEmpty()) {
//            // set defautl model if defined
//            mlQuality = mlDefaultQualityLevel;
//        }
//        if (debug) {
//            logger.info("......ml quality = " + mlQuality);
//        }
//
//        return mlQuality;
//
//    }

    /**
     * This helper method parses the ml model name either provided by a model
     * definition or a imixs.property or an environment variable
     * 
     * @param mlConfig
     * @return
     */
    private String parseMLFilePatternByBPMN(ItemCollection mlConfig) {
        boolean debug = logger.isLoggable(Level.FINE);
        debug = true;
        String filePattern = null;

        // test if the model provides a MLModel name. If not, the adapter uses the
        // mlDefaultAPIEndpoint
        if (mlConfig != null) {
            filePattern = mlConfig.getItemValueString("filename.pattern");
        }

        if (debug) {
            logger.info("......ml file.pattern = " + filePattern);
        }

        return filePattern;

    }

    /**
     * This helper method loads the locale definition form a config workitem. If no
     * locale definitions are found, the locales provided the imixs.property
     * 'ML_LOCALES' are taken.
     * 
     * @param mlConfig - configuration workitem with the item 'locales'
     * @return comma separated list of Locale definitions
     */
    private String parseMLLocalesByBPMN(ItemCollection mlConfig) {
        // test if the model provides locales. If not, the adapter uses the
        // mlDefaultAPILocales
        String mlAPILocales = null;
        if (mlConfig != null) {
            mlAPILocales = mlConfig.getItemValueString("locales");
        }
        if (mlAPILocales == null || mlAPILocales.isEmpty()) {
            mlAPILocales = mlDefaultLocales;
        }

        return mlAPILocales;
    }

    /**
     * This method parses the workflow result for optional entity definitions
     */
    @SuppressWarnings("unchecked")
    private List<MLEntity> parseMLEntitiesByBPMN(ItemCollection mlConfig) {

        List<String> entityDevList = mlConfig.getItemValue("entity");

        if (entityDevList.size() == 0) {
            throw new ProcessingErrorException(MLAdapter.class.getSimpleName(), API_ERROR,
                    "missing ml-config entity definitions!");
        }

       // Map<String, EntityDefinition> result = new HashMap<String, EntityDefinition>();
        List<MLEntity> result = new ArrayList<MLEntity>();

        for (String entityDev : entityDevList) {

            if (entityDev.trim().isEmpty()) {
                // no definition
                continue;
            }
            try {
                // evaluate the item content (XML format expected here!)
                ItemCollection entityData = XMLParser.parseItemStructure(entityDev);

                if (entityData != null) {
                    String name = entityData.getItemValueString("name");
                    String type = entityData.getItemValueString("type");
                    String mapping = entityData.getItemValueString("mapping");
                    int length = entityData.getItemValueInteger("length");
                    boolean forceTraining=true;
                    if (entityData.hasItem("required")) {
                        forceTraining=entityData.getItemValueBoolean("required");
                    }
                    // add definition into the definition map...
                    //result.put(name, new EntityDefinition(name, type, mapping, length,forceTraining));
                    result.add(new MLEntity(name, type, mapping, length,forceTraining));
                }
            } catch (PluginException e) {
                logger.warning("Invalid ml.config definition with unexpected entity element - verify model!");
            }

        }

        return result;
    }

    /**
     * This method groups a List of XMLAnalyseEntity by the label name. The method
     * builds a Map with the key 'label' containing all entities found in given data
     * set.
     * 
     * @param data - list of XMLAnalyseEntity
     * @return grouped text values
     */
    private Map<String, List<String>> groupTextValues(XMLAnalyseResult data) {

        HashMap<String, List<String>> result = new HashMap<String, List<String>>();

        if (data != null) {
            for (XMLAnalyseEntity entity : data.getEntities()) {
                String itemName = entity.getLabel();
                String itemValue = entity.getText();
                // do we have a label and a value?
                if (itemName != null && !itemName.isEmpty() && itemValue != null && !itemValue.isEmpty()) {
                    List<String> textList = result.get(itemName);
                    if (textList == null) {
                        // create list object
                        textList = new ArrayList<String>();
                    }
                    // store value
                    textList.add(itemValue);
                    result.put(itemName.trim(), textList);
                }
            }
        }
        return result;
    }

  
   
}
