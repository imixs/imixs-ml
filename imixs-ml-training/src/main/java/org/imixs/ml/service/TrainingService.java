/*******************************************************************************
 *  Imixs Workflow Technology
 *  Copyright (C) 2001, 2008 Imixs Software Solutions GmbH,  
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
 *  Contributors:  
 *  	Imixs Software Solutions GmbH - initial API and implementation
 *  	Ralph Soika
 *******************************************************************************/
package org.imixs.ml.service;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.imixs.archive.ocr.OCRService;
import org.imixs.melman.RestAPIException;
import org.imixs.melman.WorkflowClient;
import org.imixs.ml.api.TrainingApplication;
import org.imixs.ml.core.MLClient;
import org.imixs.ml.events.EntityObjectEvent;
import org.imixs.ml.training.TrainingDataBuilder;
import org.imixs.ml.xml.XMLTrainingData;
import org.imixs.ml.xml.XMLTrainingEntity;
import org.imixs.workflow.FileData;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.exceptions.PluginException;

/**
 * The TrainingService loads a list of documents and creates a TraingData object
 * for each document.
 * <p>
 * The TrainingService extracts the text contained in a document attached to a
 * snapshot workitem. The file content is created by a OCR scan.
 * <p>
 * If a valuable training data set can be build for a document, than the
 * XMLTraingData object is send to the Imixs-ML service to train a ml-model.
 * 
 * 
 * @version 1.0
 * @author rsoika
 */

@Stateless
public class TrainingService {
    private static Logger logger = Logger.getLogger(TrainingService.class.getName());

    @Inject
    OCRService ocrService;

    @Inject
    protected Event<EntityObjectEvent> entityObjectEvents;

    /**
     * This method is used to extract the text contained in a snapshot document and
     * search for 'known' entities within the text. If a valuable training data set
     * can be build the training data is send to the Imixs-ML service.
     * <p>
     * The method generates statistical data.
     * 
     * @param doc            - a workitem providing the attachments and the entity
     *                       data
     * @param items          - String list with items
     * @param stats          - an itemCollection to collect statistical data.
     * @param workflowClient - a rest client instance
     */
    @SuppressWarnings("unchecked")
    public void trainWorkitemData(ItemCollection config, ItemCollection doc, WorkflowClient workflowClient,
            ItemCollection stats) {

        logger.info("......create new training data for: " + doc.getUniqueID());

        List<String> itemNames = config.getItemValue(TrainingApplication.ITEM_ENTITIES);
        List<String> tikaOptions = config.getItemValue(TrainingApplication.ITEM_TIKA_OPTIONS);
        String ocrMode = config.getItemValueString(TrainingApplication.ITEM_TIKA_OCR_MODE);
        
        // build locales....
        List<String> sLocales=config.getItemValue(TrainingApplication.ITEM_LOCALES);
        Set<Locale> locals=new HashSet<Locale>();
        for (String _locale: sLocales) {
            Locale aLocale=new Locale(_locale);
            locals.add(aLocale);
            logger.info("......suporting locale " + aLocale);
        }

        ItemCollection snapshot = null;
        try {
            // first load the snapshot
            String snapshotID = doc.getItemValueString("$snapshotid");
            if (!snapshotID.isEmpty()) {
                snapshot = workflowClient.getDocument(snapshotID);
            }

            if (snapshot == null) {
                logger.warning("Unable to load snapshot for document " + doc.getUniqueID());
                return;
            }

            ocrService.extractText(snapshot,null, ocrMode, tikaOptions);

            // now we load the attachments...
            List<FileData> files = snapshot.getFileData();
            if (files != null && files.size() > 0) {
                for (FileData file : files) {

                    logger.info("...analyzing content of '" + file.getName() + "'.....");
                    ItemCollection metadata = new ItemCollection(file.getAttributes());
                    String content = metadata.getItemValueString("content");
                    // clean content string....
                    // content=TrainingDataBuilder.cleanTextdata(content);

                    if (!content.isEmpty()) {

                        // build training data set...
                        XMLTrainingData trainingData = new TrainingDataBuilder(content, doc, itemNames, locals)
                                .setAnalyzerEntityEvents(entityObjectEvents).build();

                        // update entity stats...
                        for (XMLTrainingEntity trainingEntity : trainingData.getEntities()) {
                            if (stats != null) {
                                stats.replaceItemValue("item.count." + trainingEntity.getLabel(),
                                        stats.getItemValueInteger("item.count." + trainingEntity.getLabel()) + 1);

                            }
                        }
                        // compute stats rate for found entites
                        List<String> entitysFound = new ArrayList<String>();
                        for (XMLTrainingEntity trainingEntity : trainingData.getEntities()) {
                            if (!entitysFound.contains(trainingEntity.getLabel())) {
                                entitysFound.add(trainingEntity.getLabel());
                            }
                        }

                        if (trainingData.getEntities().size() == 0) {
                            logger.severe("......no entities found (" + doc.getUniqueID() + ")");
                            logger.finest("<CONTENT>" + trainingData.getText() + "</CONTENT>");
                            if (stats != null) {
                                stats.replaceItemValue("doc.failures", stats.getItemValueInteger("doc.failures") + 1);
                            }
                        } else {
                            // we only send the training data in case of all entities types are found
                            // This means we train optimal training data only
                            if (entitysFound.size() == itemNames.size()) {
                                // log the XMLTrainingData object....
                                printXML(trainingData);

                                String serviceEndpoint = config
                                        .getItemValueString(TrainingApplication.ITEM_ML_TRAINING_ENDPOINT);

                                MLClient mlClient = new MLClient();
                                mlClient.postTrainingData(trainingData, serviceEndpoint);

                                // postTrainingData(trainingData, serviceEndpoint);
                            } else {
                                double rate = entitysFound.size() / itemNames.size() * 100;
                                logger.warning("...document '" + doc.getUniqueID() + "' has bad quality: "
                                        + (Math.round(rate * 100.0) / 100.0) + "% - will be ignored!");
                                stats.replaceItemValue("doc.ignore", stats.getItemValueInteger("doc.ignore") + 1);
                            }

                        }

                    } else {
                        logger.severe("......no content found in '" + file.getName() + "' (" + doc.getUniqueID() + ")");
                        stats.replaceItemValue("doc.failures", stats.getItemValueInteger("doc.failures") + 1);
                    }

                }

            } else {
                logger.severe("......no files found for " + doc.getUniqueID());
                stats.replaceItemValue("doc.failures", stats.getItemValueInteger("doc.failures") + 1);
            }
        } catch (PluginException | RestAPIException e1) {
            logger.severe("Error parsing documents: " + e1.getMessage());
        }

    }

    /**
     * This method is used to test an existing model. The method extracts the text
     * contained in a snapshot document and sends the text to the Imixs-ML service
     * to be analyzed. The resuls are printed out.
     * 
     * @param doc            - a workitem providing the attachments and the entity
     *                       data
     * @param items          - String list with items
     * @param workflowClient - a rest client instance
     */
    @SuppressWarnings("unchecked")
    public void testWorkitemData(ItemCollection config, ItemCollection doc, WorkflowClient workflowClient) {

        logger.info("......anaysing: " + doc.getUniqueID());

        List<String> tikaOptions = config.getItemValue(TrainingApplication.ITEM_TIKA_OPTIONS);
        String ocrMode = config.getItemValueString(TrainingApplication.ITEM_TIKA_OCR_MODE);

        ItemCollection snapshot = null;
        try {
            // first load the snapshot
            String snapshotID = doc.getItemValueString("$snapshotid");
            if (!snapshotID.isEmpty()) {
                snapshot = workflowClient.getDocument(snapshotID);
            }

            if (snapshot == null) {
                logger.warning("Unable to load snapshot for document " + doc.getUniqueID());
                return;
            }

            ocrService.extractText(snapshot,null, ocrMode, tikaOptions);

            // now we load the attachments...
            List<FileData> files = snapshot.getFileData();
            if (files != null && files.size() > 0) {
                for (FileData file : files) {

                    logger.info("...analyzing content of '" + file.getName() + "'.....");
                    ItemCollection metadata = new ItemCollection(file.getAttributes());
                    String content = metadata.getItemValueString("content");

                    // clean content string....
                    content = XMLTrainingData.cleanTextdata(content);
                    
                    String serviceEndpoint = config
                            .getItemValueString(TrainingApplication.ITEM_ML_ANALYSE_ENDPOINT);

                    MLClient mlClient = new MLClient();
                    mlClient.postAnalyseData(content, serviceEndpoint);

                    // postAnalyzeData(content, serviceEndpoint);

                }

            } else {
                logger.severe("......no files found for " + doc.getUniqueID());

            }
        } catch (PluginException | RestAPIException e1) {
            logger.severe("Error parsing documents: " + e1.getMessage());
        }

    }

    public void printXML(XMLTrainingData trainingData) {

        JAXBContext context;
        try {
            context = JAXBContext.newInstance(XMLTrainingData.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringWriter out = new StringWriter();
            marshaller.marshal(trainingData, out);
            String xml = out.toString();
            logger.info(xml);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

}
