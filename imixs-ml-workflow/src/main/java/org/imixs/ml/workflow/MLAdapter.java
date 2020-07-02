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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.imixs.ml.core.MLClient;
import org.imixs.ml.events.EntityTextEvent;
import org.imixs.ml.xml.XMLAnalyseEntity;
import org.imixs.ml.xml.XMLTrainingData;
import org.imixs.workflow.FileData;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.SignalAdapter;
import org.imixs.workflow.engine.WorkflowService;
import org.imixs.workflow.exceptions.AdapterException;
import org.imixs.workflow.exceptions.PluginException;
import org.imixs.workflow.util.XMLParser;

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
 * workflow result item named 'ml.config'.
 * <p>
 * Example:
 * 
 * <pre>
 * {@code
<item name="ml.config">
    <endpoint>https://localhost:8111/api/resource/</endpoint>
    <locales>DE,UK</locales>
</item>
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
<item name="ml.entity">
    <name>_invoicetotal</name>
    <type>currency</type>
</item>
<item name="ml.entity">
    <name>_cdtr_bic</name>
    <type>text</type>
    <mapping>bic</mapping>
</item>
 * }
 * </pre>
 * 
 * @author Ralph Soika
 * @version 1.0
 *
 */

public class MLAdapter implements SignalAdapter {

    public static final String ML_API_ENDPOINT = "ml.endpoint";
    public static final String ML_LOCALES = "ml.locales";

    public static final String ML_ENTITY = "ml.entity";

    public static final int API_EVENT_SUCCESS = 110;
    public static final int API_EVENT_FAILURE = 90;

    private static Logger logger = Logger.getLogger(MLAdapter.class.getName());

    @Inject
    @ConfigProperty(name = ML_API_ENDPOINT, defaultValue = "")
    private String mlDefaultAPIEndpoint;

    @Inject
    @ConfigProperty(name = ML_LOCALES, defaultValue = "DE,UK")
    private String mlDefaultAPILocales;

    @Inject
    private WorkflowService workflowService;

    @Inject
    private Event<EntityTextEvent> entityTextEvents = null;

    /**
     * This method posts a text from an attachment to the Imixs-ML Analyse service
     * endpoint
     */
    public ItemCollection execute(ItemCollection document, ItemCollection event) throws AdapterException {
        String mlAPIEndpoint = null;
        Set<Locale> locals = new HashSet<Locale>();

        Map<String, EntityDefinition> enityDefinitions = null;
        boolean debug = logger.isLoggable(Level.FINE);
        debug = true;

        logger.info("...running api adapter...");

        // read configuration either form the model or imixs.properties....
        try {
            ItemCollection evalItemCollection = workflowService.evalWorkflowResult(event, document, false);
            ItemCollection mlConfig = XMLParser.parseItemStructure(evalItemCollection.getItemValueString("ml.config"));
            mlAPIEndpoint = parseMLEndpointByModel(mlConfig);
            locals = parseMLLocalesByModel(mlConfig);

            // test if the model provides optional entity definitions.
            enityDefinitions = parseEntityDefinitionsByModel(evalItemCollection);
        } catch (PluginException e) {
            logger.warning("Unable to parse item definitions for 'ml.config' and 'ml.entity', verify model - "
                    + e.getMessage());
        }

        // analyse file content....

        List<FileData> files = document.getFileData();
        if (files != null && files.size() > 0) {
            for (FileData file : files) {

                logger.info("...analyzing content of '" + file.getName() + "'.....");
                ItemCollection metadata = new ItemCollection(file.getAttributes());
                String ocrText = metadata.getItemValueString("text");

                // clean content string....
                ocrText = XMLTrainingData.cleanTextdata(ocrText);

                MLClient mlClient = new MLClient();
                List<XMLAnalyseEntity> result = mlClient.postAnalyseData(ocrText, mlAPIEndpoint);
                /*
                 * We now have a list of XMLAnalyseEntities possible matching the same item. In
                 * the following we group matching items by itemName and fire a
                 * EntityObjectEvent to test if an adapter provides a unique value.
                 */
                Map<String, List<String>> groupedEntityList = groupTextValues(result);

                // analyse the entities....
                for (Map.Entry<String, List<String>> entity : groupedEntityList.entrySet()) {

                    String entityName = entity.getKey();
                    List<String> itemValueList = entity.getValue();
                    // do we have an entityDefinition for this entity?
                    EntityDefinition entityDef = enityDefinitions.get(entityName);
                    if (entityDef == null) {
                        // create dummy
                        entityDef = new EntityDefinition(entityName, null, entityName);
                    }

                    // set the item value if the workItem did not have a value for this entity....
                    if (document.isItemEmpty(entityDef.getItemName())) {

                        // fire entityTextEvents so that an adapter can resolve the text value into a
                        // object
                        EntityTextEvent entityTextEvent = new EntityTextEvent(itemValueList, locals,
                                entityDef.getItemType());
                        entityTextEvents.fire(entityTextEvent);

                        // test if we found an object
                        if (entityTextEvent.getItemValue() != null) {
                            // set the value
                            if (debug) {
                                logger.info("Best match=" + entityTextEvent.getItemValue());
                            }
                            document.setItemValue(entityDef.getItemName(), entityTextEvent.getItemValue());
                        } else {
                            // set the first text value as is
                            document.setItemValue(entityDef.getItemName(), entity.getValue().iterator().next());
                        }

                    }

                }
                // update the ml.item list with all new assigned items....
                document.setItemValue("ml.items", groupedEntityList.keySet());

            }

        } else {
            logger.finest("......no files found for " + document.getUniqueID());
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
    private String parseMLEndpointByModel(ItemCollection mlConfig) {
        boolean debug = logger.isLoggable(Level.FINE);
        debug = true;
        String mlAPIEndpoint = null;

        // test if the model provides a MLEndpoint. If not, the adapter uses the
        // mlDefaultAPIEndpoint
        mlAPIEndpoint = mlConfig.getItemValueString("endpoint");
        if (mlAPIEndpoint.isEmpty()) {
            mlAPIEndpoint = mlDefaultAPIEndpoint;
        }
        if (debug) {
            logger.info("......ml api endpoint " + mlAPIEndpoint);
        }

        return mlAPIEndpoint;

    }

    /**
     * This helper method parses the locales either provided by a model definition
     * or a imixs.property or an environment variable
     * 
     * @param mlConfig
     * @return
     */
    private Set<Locale> parseMLLocalesByModel(ItemCollection mlConfig) {
        boolean debug = logger.isLoggable(Level.FINE);
        debug = true;
        Set<Locale> locals = new HashSet<Locale>();

        // test if the model provides locales. If not, the adapter uses the
        // mlDefaultAPILocales
        String mlAPILocales = mlConfig.getItemValueString("locales");
        if (mlAPILocales.isEmpty()) {
            mlAPILocales = mlDefaultAPILocales;
        }

        // translate locales..
        String[] sLocales = mlAPILocales.split(",");

        for (String _locale : sLocales) {
            Locale aLocale = new Locale(_locale);
            locals.add(aLocale);
            if (debug) {
                logger.info("......suporting locale " + aLocale);
            }
        }
        return locals;
    }

    /**
     * This method parses the workflow result for optional entity definitions
     */
    @SuppressWarnings("unchecked")
    private Map<String, EntityDefinition> parseEntityDefinitionsByModel(ItemCollection evalItemCollection) {
        boolean debug = logger.isLoggable(Level.FINE);
        debug = true;

        Map<String, EntityDefinition> result = new HashMap<String, EntityDefinition>();

        // test if the model provides optional entity definitions.
        if (evalItemCollection.hasItem(ML_ENTITY)) {
            if (debug) {
                logger.info("......processing " + ML_ENTITY);
            }
            // extract the create subprocess definitions...
            List<String> entityDevList = evalItemCollection.getItemValue(ML_ENTITY);

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
                        // add definition into the definition map...
                        result.put(name, new EntityDefinition(name, type, mapping));
                    }
                } catch (PluginException e) {
                    logger.warning("Invalid ml.entity definition - verify model!");
                }
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
    private Map<String, List<String>> groupTextValues(List<XMLAnalyseEntity> data) {

        HashMap<String, List<String>> result = new HashMap<String, List<String>>();

        if (data != null) {
            for (XMLAnalyseEntity entity : data) {
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

    /**
     * Helper class providing properties of a entity definition
     * 
     * @author rsoika
     *
     */
    class EntityDefinition {
        private String name;
        private String itemType;
        private String itemName;

        public EntityDefinition(String name, String itemType, String itemName) {
            super();
            this.name = name;
            this.itemType = itemType;
            this.itemName = itemName;
        }

        public String getName() {
            return name;
        }

        public String getItemType() {
            return itemType;
        }

        public String getItemName() {
            return itemName;
        }

    }
}
