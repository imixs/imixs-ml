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
import java.util.Set;
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

    @Inject
    protected Event<AnalyzeEntityEvent> analyzerEntityEvents;

    /**
     * This method is used to extract the text contained in a document and search
     * for 'known' entities within the text. If a valuable training data set can be
     * build the training data is send to the Imixs-ML service.
     * <p>
     * The method returns a TraingStats object providing statistical data.
     * <p>
     * The parameter stats holds a reference to a ItemCollection collecting
     * statistical data.
     * 
     * @param doc
     * @param items - String list with items
     */
    public void trainWorkitemData(ItemCollection doc, WorkflowClient workflowClient, String[] itemNames,
            ItemCollection stats) {

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
                            itemName = itemName.toLowerCase().trim();
                            // if the itemName contains a | character than we do a mapping here.....
                            if (itemName.contains("|")) {
                                String entityName = itemName.substring(itemName.indexOf('|') + 1).trim();
                                // replace item in workitem....
                                doc.replaceItemValue(entityName,
                                        doc.getItemValue(itemName.substring(0, itemName.indexOf('|')).trim()));
                                itemName = entityName;
                            }

                            @SuppressWarnings("unchecked")
                            List<Object> values = doc.getItemValue(itemName);
                            if (values != null && values.size() > 0) {
                                List<XMLTrainingEntity> trainingEntities = createTraingEntities(content, values.get(0),
                                        itemName);

                                if (trainingEntities != null) {
                                    for (XMLTrainingEntity trainingEntity : trainingEntities) {
                                        logger.info("......found entity " + trainingEntity.getLabel() + " = '"
                                                + trainingEntity.getValue() + "' at " + " " + trainingEntity.getStart()
                                                + "," + trainingEntity.getStop());
                                        trainingData.addTrainingEntity(trainingEntity);

                                        // update entity stats...
                                        stats.replaceItemValue("item.count." + itemName,
                                                stats.getItemValueInteger("item.count." + itemName) + 1);
                                    }
                                }
                            }
                        }

                        if (trainingData.getEntities().size() == 0) {
                            logger.severe("......no entities found in '" + file.getName() + "' (" + doc.getUniqueID()+")");
                            logger.finest("<CONTENT>" + content + "</CONTENT>");

                            stats.replaceItemValue("doc.failures", stats.getItemValueInteger("doc.failures") + 1);
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
                        logger.severe("......no content found in '" + file.getName() + "' (" + doc.getUniqueID()+")");
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
    public List<XMLTrainingEntity> createTraingEntities(String text, Object entity, String label) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        if (entity == null || entity.toString().isEmpty()) {
            return null;
        }
        List<XMLTrainingEntity> result = new ArrayList<XMLTrainingEntity>();
        logger.finest(".......analyzing: " + label + " value= " + entity + " object class="
                + entity.getClass().getSimpleName());

        // adapt value formats...
        // fire event
        Set<String> enityVariants = new HashSet<String>();

        if (analyzerEntityEvents != null) {
            analyzerEntityEvents.fire(new AnalyzeEntityEvent(entity, enityVariants));
        } else {
            logger.warning("CDI Support is missing - AnalyzeEntityEvent Not Supported!");
        }

        // if the EntityAdapters provide no value, than we are adding the plain string
        // value only..
        if (enityVariants.size() == 0) {
            enityVariants.add(entity.toString());
        }

        logger.info("...... entity variants for : '" + entity.toString() + "'");
        for (String entityVariant : enityVariants) {
            logger.info("......    " + entityVariant);
        }

        // test all variants...
        for (String entityVariant : enityVariants) {

            // find all matches....
            int indexPos = 0;
            while (true) {
                int start = text.indexOf(entityVariant, indexPos);
                if (start > -1) {
                    XMLTrainingEntity trainingEntity = new XMLTrainingEntity();
                    trainingEntity.setLabel(label);
                    trainingEntity.setValue(entityVariant);
                    trainingEntity.setStart(start);
                    trainingEntity.setStop(start + entityVariant.length());
                    result.add(trainingEntity);
                    indexPos = trainingEntity.getStop();
                } else {
                    // no more matches
                    break;
                }
            }
        }

        return result;

    }

}
