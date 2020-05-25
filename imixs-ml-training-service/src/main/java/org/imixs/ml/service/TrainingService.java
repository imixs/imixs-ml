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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.imixs.melman.RestAPIException;
import org.imixs.melman.WorkflowClient;
import org.imixs.ml.data.xml.XMLTrainingData;
import org.imixs.ml.data.xml.XMLTrainingEntity;
import org.imixs.workflow.FileData;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.documents.TikaDocumentService;
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
    TikaDocumentService tikaDocumentService;

    @Inject
    protected Event<AnalyzeEntityEvent> analyzerEntityEvents;

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
    public void trainWorkitemData(ItemCollection doc, String[] items, ItemCollection stats,
            WorkflowClient workflowClient) {

        logger.info("......create new training data for: " + doc.getUniqueID());

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

            tikaDocumentService.extractText(snapshot);

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
                        XMLTrainingData trainingData = new TrainingDataBuilder(content, doc, items)
                                .setAnalyzerEntityEvents(analyzerEntityEvents).build();

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
                            if (entitysFound.size() == items.length) {
                                // log the XMLTrainingData object....
                                printXML(trainingData);

                                String serviceEndpoint = "http://imixs-ml:8000/trainingdata/";
                                // String serviceEndpoint="http://imixs-ml:8000/trainingdatasingle/";
                                postTrainingData(trainingData, serviceEndpoint);
                            } else {
                                double rate = entitysFound.size() / items.length * 100;
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
    public void testWorkitemData(ItemCollection doc, String[] items, WorkflowClient workflowClient) {

        logger.info("......testing: " + doc.getUniqueID());

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

            tikaDocumentService.extractText(snapshot);

            // now we load the attachments...
            List<FileData> files = snapshot.getFileData();
            if (files != null && files.size() > 0) {
                for (FileData file : files) {

                    logger.info("...analyzing content of '" + file.getName() + "'.....");
                    ItemCollection metadata = new ItemCollection(file.getAttributes());
                    String content = metadata.getItemValueString("content");

                    // clean content string....
                    content = XMLTrainingData.cleanTextdata(content);
                    String serviceEndpoint = "http://imixs-ml:8000/analyze/";
                    postAnalyzeData(content, serviceEndpoint);

                }

            } else {
                logger.severe("......no files found for " + doc.getUniqueID());

            }
        } catch (PluginException | RestAPIException e1) {
            logger.severe("Error parsing documents: " + e1.getMessage());
        }

    }

    /**
     * This method converts a XMLTrainingData object into the spaCy JSON Format
     * <p>
     * 
     * <pre>
     * [
          {
            "text": "string",
            "entities": [
              {
                "label": "string",
                "start": 0,
                "stop": 0
              }
            ]
          }
        ]
     * </pre>
     * <p>
     * 
     * @param trainingData
     */
    private String convertToSpacyFormat(XMLTrainingData trainingData) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[{\"text\": \"");
        String text = trainingData.getText();
        stringBuilder.append(text).append("\",");
        stringBuilder.append("\"entities\": [");

        for (int i = 0; i < trainingData.getEntities().size(); i++) {
            XMLTrainingEntity trainingEnity = trainingData.getEntities().get(i);
            stringBuilder.append("{\"label\": \"" + trainingEnity.getLabel() + "\",");
            stringBuilder.append("\"start\": \"" + trainingEnity.getStart() + "\",");
            stringBuilder.append("\"stop\": \"" + trainingEnity.getStop() + "\"}");
            if (i < trainingData.getEntities().size() - 1) {
                stringBuilder.append(",");
            }
        }

        stringBuilder.append("]}]");
        return stringBuilder.toString();
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

    /**
     * This method posts a spacy json training string to the spacy service endpoint
     * 
     * @param trainingData
     */
    public void postTrainingData(XMLTrainingData trainingData, String serviceEndpoint) {

        String json = convertToSpacyFormat(trainingData);
        logger.info("...JSON=" + json);
        logger.info("...send json to spacy...");
        try {
            URL url = new URL(serviceEndpoint);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = json.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                logger.info("spacy result=" + response.toString());
            }

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    /**
     * This method posts a spacy json training string to the spacy service endpoint
     * 
     * @param trainingData
     */
    public void postAnalyzeData(String text, String serviceEndpoint) {

        // build the spacy format.....
        // {"text": "..."}
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{\"text\": \"").append(text).append("\"}");

        String json = stringBuilder.toString();
        logger.info("...JSON=" + json);
        logger.info("...send json to spacy...");
        try {
            URL url = new URL(serviceEndpoint);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = json.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                logger.info("spacy result=" + response.toString());
            }

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

}
