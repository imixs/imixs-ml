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
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
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
 * The DocumentExtractorService is used to analyze the payload of a document and
 * extract the entity data to generate a training data set.
 * <p>
 * The analyzer extracts the text contained in a document attached to a workitem
 * and searches for 'known' entities within the text. If a valuable training
 * data set can be build the training data is send to the Imixs-ML service.
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

    /**
     * This method is used to extract the text contained in a document and search
     * for 'known' entities within the text. If a valuable traing data set can be
     * build the training data is send to the Imixs-ML service.
     * 
     * 
     * @param doc
     * @param items - String list with items
     */
    public void trainWorkitemData(ItemCollection doc, WorkflowClient workflowClient, String[] itemNames) {

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

                    XMLTrainingData trainingData = null;

                    logger.info("...analyzing content of '" + file.getName() + "'.....");
                    ItemCollection metadata = new ItemCollection(file.getAttributes());
                    String content = metadata.getItemValueString("content");
                    if (!content.isEmpty()) {
                        trainingData = new XMLTrainingData();
                        trainingData.setText(content);
                        logger.fine("file text content: " + content);

                        // now lets see if we find some of our intem values....
                        for (String itemName : itemNames) {
                            String value = doc.getItemValueString(itemName);

                            List<XMLTrainingEntity> trainingEntities = createTraingEntities(content, value, itemName);

                            if (trainingEntities != null) {
                                for (XMLTrainingEntity trainingEntity : trainingEntities) {
                                    logger.info(" ======>> found " + itemName + " = '" + value + "' at " + " "
                                            + trainingEntity.getStart() + "," + trainingEntity.getStop());
                                    trainingData.addTrainingEntity(trainingEntity);
                                }
                            }
                        }

                        if (trainingData.getEntities().size() == 0) {
                            logger.warning("No entities found in " + doc.getUniqueID() + " '" + file.getName() + "'");
                            logger.finest("<CONTENT>" + content + "</CONTENT>");
                        } else {

                            // log the XMLTrainingData object....
                            try {
                                JAXBContext context;
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

                    } else {
                        logger.fine("...no content found in '" + file.getName() + "'.....");
                    }

                }

            } else {
                logger.fine("...no files found for " + doc.getUniqueID());
            }
        } catch (PluginException | RestAPIException e1) {
            logger.severe("Error parsing documents: " + e1.getMessage());
        }

    }

    /**
     * This helper method computes the start/stop position of a substring in a text.
     * It is important to ensure that the start/stop positions are as expected by
     * spaCy.
     * <p>
     * Example: {@code
     * 
     * "they pretend to care about your feelings, those horses", "horses" = 48,54
     * 
     * }
     * <p>
     * An entity can be found more than once in a training text. For that reason the
     * method returns a list of trainingEntites with all matches!
     * 
     * 
     * @param text
     * @param entity
     * @param label
     * @return
     */
    public List<XMLTrainingEntity> createTraingEntities(String text, String entity, String label) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        if (entity == null || entity.isEmpty()) {
            return null;
        }

        List<XMLTrainingEntity> result = new ArrayList<XMLTrainingEntity>();
        int indexPos = 0;
        // find all matches....
        while (true) {
            int start = text.indexOf(entity, indexPos);
            if (start > -1) {
                XMLTrainingEntity trainingEntity = new XMLTrainingEntity();
                trainingEntity.setLabel(label);
                trainingEntity.setStart(start);
                trainingEntity.setStop(start + entity.length());
                result.add(trainingEntity);
                indexPos = trainingEntity.getStop();
            } else {
                // no more matches
                break;
            }

        }
        return result;

    }

}
