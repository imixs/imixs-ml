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
import java.util.logging.Level;
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
     * @param config         - a config object providing the training configuration
     * @param workitem       - a workitem providing the data
     * @param workflowClient - a rest client instance
     * @return - quality result
     */
    @SuppressWarnings("unchecked")
    public int trainWorkitemData(ItemCollection config, ItemCollection workitem, WorkflowClient workflowClient) {
        boolean debug = logger.isLoggable(Level.FINE);
        int qualityResult = -1;

        logger.info("......create new training data for: " + workitem.getUniqueID());

        List<String> trainingItemNames = config.getItemValue(TrainingApplication.ITEM_ENTITIES);
        List<String> tikaOptions = config.getItemValue(TrainingApplication.ITEM_TIKA_OPTIONS);
        String ocrMode = config.getItemValueString(TrainingApplication.ITEM_TIKA_OCR_MODE);
        String qualityLevel = config.getItemValueString(TrainingApplication.ITEM_ML_TRAINING_QUALITYLEVEL);
        if (qualityLevel.isEmpty()) {
            qualityLevel="FULL"; // default level!
        }
        
        // build locales....
        List<String> sLocales = config.getItemValue(TrainingApplication.ITEM_LOCALES);
        Set<Locale> locals = new HashSet<Locale>();
        for (String _locale : sLocales) {
            Locale aLocale = new Locale(_locale);
            locals.add(aLocale);
            logger.finest("......suporting locale " + aLocale);
        }

        ItemCollection snapshot = null;
        try {
            // first load the snapshot
            String snapshotID = workitem.getItemValueString("$snapshotid");
            if (!snapshotID.isEmpty()) {
                snapshot = workflowClient.getDocument(snapshotID);
            }

            if (snapshot == null) {
                logger.warning("Unable to load snapshot for document " + workitem.getUniqueID());
                return XMLTrainingData.TRAININGDATA_QUALITY_LEVEL_BAD;
            }

            ocrService.extractText(snapshot, null, ocrMode, tikaOptions);

            // now we load the filedata ...
            List<FileData> files = snapshot.getFileData();
            if (files != null && files.size() > 0) {
                for (FileData file : files) {

                    logger.info("...analyzing content of '" + file.getName() + "'.....");
                    ItemCollection metadata = new ItemCollection(file.getAttributes());

                    // TODO replace "text" with OCRServcie.FILE_ATTRIBUTE_TEXT
                    String content = metadata.getItemValueString("text");
                    // clean content string....
                   
                    if (!content.isEmpty()) {
                        // build training data set...
                        XMLTrainingData trainingData = new TrainingDataBuilder(content, workitem, trainingItemNames,
                                locals).setAnalyzerEntityEvents(entityObjectEvents).build();

                        // compute stats rate for found entities
                        List<String> entitysFound = new ArrayList<String>();
                        for (XMLTrainingEntity trainingEntity : trainingData.getEntities()) {
                            if (!entitysFound.contains(trainingEntity.getLabel())) {
                                entitysFound.add(trainingEntity.getLabel());
                            }
                        }

                        // we only send the training data in case of quality level is sufficient
                        if (XMLTrainingData.TRAININGDATA_QUALITY_LEVEL_BAD == trainingData.getQuality()) {
                            logger.severe("...document '" + workitem.getUniqueID()
                                    + "' TRAININGDATA_QUALITY_LEVEL=BAD - document will be ignored!");

                            qualityResult = XMLTrainingData.TRAININGDATA_QUALITY_LEVEL_BAD;
                        } else if (XMLTrainingData.TRAININGDATA_QUALITY_LEVEL_PARTIAL == trainingData.getQuality()
                                && "FULL".equalsIgnoreCase(qualityLevel)) {
                            logger.severe("...document '" + workitem.getUniqueID()
                                    + "' TRAININGDATA_QUALITY_LEVEL=PARTIAL but FULL is required - document will be ignored!");
                            qualityResult = XMLTrainingData.TRAININGDATA_QUALITY_LEVEL_BAD;
                        } else {
                            // trainingData quality level is sufficient
                            if (XMLTrainingData.TRAININGDATA_QUALITY_LEVEL_PARTIAL == trainingData.getQuality()) {
                                logger.warning("...document '" + workitem.getUniqueID()
                                        + "' TRAININGDATA_QUALITY_LEVEL=PARTIAL ...");
                                qualityResult = XMLTrainingData.TRAININGDATA_QUALITY_LEVEL_PARTIAL;
                            }
                            if (XMLTrainingData.TRAININGDATA_QUALITY_LEVEL_FULL == trainingData.getQuality()) {
                                logger.info("...document '" + workitem.getUniqueID()
                                        + "' TRAININGDATA_QUALITY_LEVEL=FULL ...");
                                qualityResult = XMLTrainingData.TRAININGDATA_QUALITY_LEVEL_FULL;
                            }

                            // log the XMLTrainingData object....
                            if (debug) {
                                 printXML(trainingData);
                            }
                            String serviceEndpoint = config
                                    .getItemValueString(TrainingApplication.ITEM_ML_TRAINING_ENDPOINT);
                            MLClient mlClient = new MLClient();
                            mlClient.postTrainingData(trainingData, serviceEndpoint);
                        }

                    } else {
                        logger.severe(
                                "......no content found in '" + file.getName() + "' (" + workitem.getUniqueID() + ")");
                        qualityResult = XMLTrainingData.TRAININGDATA_QUALITY_LEVEL_BAD;
                    }

                }

            } else {
                logger.severe("......no files found for " + workitem.getUniqueID());
                qualityResult = XMLTrainingData.TRAININGDATA_QUALITY_LEVEL_BAD;
            }
        } catch (PluginException | RestAPIException e1) {
            logger.severe("Error parsing documents: " + e1.getMessage());
        }

        return qualityResult;

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

            ocrService.extractText(snapshot, null, ocrMode, tikaOptions);

            // now we load the attachments...
            List<FileData> files = snapshot.getFileData();
            if (files != null && files.size() > 0) {
                for (FileData file : files) {

                    logger.fine("...analyzing content of '" + file.getName() + "'.....");
                    ItemCollection metadata = new ItemCollection(file.getAttributes());
                    // TODO replace "text" with OCRServcie.FILE_ATTRIBUTE_TEXT
                    String content = metadata.getItemValueString("text");
                    String serviceEndpoint = config.getItemValueString(TrainingApplication.ITEM_ML_ANALYSE_ENDPOINT);
                    MLClient mlClient = new MLClient();
                    mlClient.postAnalyseData(content, serviceEndpoint);
                }

            } else {
                logger.severe("......no files found for " + doc.getUniqueID());

            }
        } catch (PluginException | RestAPIException e1) {
            logger.severe("Error parsing documents: " + e1.getMessage());
        }

    }

    /**
     * Log the training data into the server log
     * 
     * @param trainingData
     */
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
