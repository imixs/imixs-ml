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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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
 * The Adapter is configured through the model by defining a workflow result
 * item named 'ml_config'.
 * <p>
 * Example:
 * 
 * <pre>
 * {@code
<item name="ml_config">
    <api.endpoint>https://localhost:8111/api/resource/</api.endpoint>
    <locales>DE,UK</locales>
</item>
 * }
 * </pre>
 * 
 * Both parameters can also be set by environment variables
 * 'ML_API_ENDPOINT' and 'ML_LOCALES'
 * 
 * @author Ralph Soika
 * @version 1.0
 *
 */

public class MLAdapter implements SignalAdapter {

    public static final String ML_API_ENDPOINT = "ML_API_ENDPOINT";
    public static final String ML_LOCALES = "ML_LOCALES";

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

        logger.info("...running api adapter...");
        ItemCollection mlConfig = null;
        // get ml_config defintion...
        try {
            ItemCollection evalItemCollection = workflowService.evalWorkflowResult(event, document, false);
            mlConfig = XMLParser.parseItemStructure(evalItemCollection.getItemValueString("ml_config"));
        } catch (PluginException e) {
            logger.warning("Unable to parse ml_config definition, verify model!");
            mlConfig = new ItemCollection();
        }

        // test if the config provides a MLEndpoint. If not, the adapter uses the
        // mlDefaultAPIEndpoint
        String mlAPIEndpoint = mlConfig.getItemValueString("endpoint");
        if (mlAPIEndpoint.isEmpty()) {
            mlAPIEndpoint = mlDefaultAPIEndpoint;
        }

        // test if the config provides locales. If not, the adapter uses the
        // mlDefaultAPILocales
        String mlAPILocales = mlConfig.getItemValueString("locales");
        if (mlAPILocales.isEmpty()) {
            mlAPILocales = mlDefaultAPILocales;
        }
        String[] sLocales =mlAPILocales.split(",");
        Set<Locale> locals = new HashSet<Locale>();
        for (String _locale : sLocales) {
            Locale aLocale = new Locale(_locale);
            locals.add(aLocale);
            logger.info("......suporting locale " + aLocale);
        }

        // get the data object...

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

                List<String> mlItemList = new ArrayList<String>();

                /*
                 * We now have a list of XMLAnalyseEntities possible matching the same item. In
                 * the following we group matching items by itemName and fire a
                 * EntityObjectEvent to test if an adapter provides a unique value.
                 */
                if (result != null && result.size() > 0) {
                    // extract entities....
                    for (XMLAnalyseEntity entity : result) {
                        String itemName = entity.getLabel();

                        // set the entiy value if the workitem did not have a value for this entity....
                        if (document.isItemEmpty(itemName)) {
                            // fire entityTextEvents so that an adapter can resolve the text value into a
                            // object
                            EntityTextEvent entityTextEvent = new EntityTextEvent(entity.getText(), locals);
                            entityTextEvents.fire(entityTextEvent);

                            // test if we found an object
                            if (entityTextEvent.getItemValue() != null) {
                                // set the value
                                document.setItemValue(itemName, entityTextEvent.getItemValue());
                            } else {
                                // set the text value as is
                                document.setItemValue(itemName, entity.getText());
                            }

                            mlItemList.add(itemName);
                        }
                    }
                }
                // update the ml.item list with all new assigned items....
                document.setItemValue("ml.items", mlItemList);

            }

        } else {
            logger.finest("......no files found for " + document.getUniqueID());
        }

        return document;
    }

}
