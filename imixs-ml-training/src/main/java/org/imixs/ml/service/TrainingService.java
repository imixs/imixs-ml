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

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.imixs.melman.RestAPIException;
import org.imixs.melman.WorkflowClient;
import org.imixs.ml.api.TrainingApplication;
import org.imixs.ml.core.MLClient;
import org.imixs.ml.core.MLConfig;
import org.imixs.ml.core.MLContentBuilder;
import org.imixs.ml.core.MLEntity;
import org.imixs.ml.core.MLTrainingResult;
import org.imixs.ml.events.EntityObjectEvent;
import org.imixs.ml.training.TrainingDataBuilder;
import org.imixs.ml.xml.XMLAnalyseResult;
import org.imixs.ml.xml.XMLTrainingData;
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
 * @version 2.0
 * @author rsoika
 */

@Stateless
public class TrainingService {
    private static Logger logger = Logger.getLogger(TrainingService.class.getName());
    public static final String FILE_ATTRIBUTE_TEXT = "text";
    public static final String ITEM_ML_ITEMS = "ml.items";
    public static final String ITEM_ML_DEFINITIONS = "ml.definitions";

    @Inject
    TikaHelperService tikaService;

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
    public MLTrainingResult trainWorkitemData(ItemCollection config, ItemCollection workitem,
            WorkflowClient workflowClient) {
        boolean debug = logger.isLoggable(Level.FINE);
        int qualityResult = -1;
        MLTrainingResult trainingResult = null;
        Pattern mlFilenamePattern = null;

        logger.info("=======================START ======================================");
        logger.info("...create new training data for: " + workitem.getUniqueID());

        String model = config.getItemValueString(TrainingApplication.ITEM_ML_TRAINING_MODEL);
        String mlOptions = config.getItemValueString(TrainingApplication.ITME_ML_OPTIONS);
        String mlOCR = config.getItemValueString(TrainingApplication.ITEM_ML_TRAINING_OCR);
        List<String> trainingItemNames = config.getItemValue(TrainingApplication.ITEM_ENTITIES);
        List<String> tikaOptions = config.getItemValue(TrainingApplication.ITEM_TIKA_OPTIONS);
        String qualityLevel = config.getItemValueString(TrainingApplication.ITEM_ML_TRAINING_QUALITYLEVEL);

        if (qualityLevel.isEmpty()) {
            qualityLevel = "LOW"; // default level!
        }
        // parse optional filename regex pattern...
        String _FilenamePattern = config.getItemValueString(TrainingApplication.ITEM_ML_TRAINING_FILEPATTERN);
        if (_FilenamePattern != null && !_FilenamePattern.isEmpty()) {
            logger.fine("......apply filename.pattern=" + _FilenamePattern);
            mlFilenamePattern = Pattern.compile(_FilenamePattern);
        }

        // build locales....
        List<String> sLocales = config.getItemValue(TrainingApplication.ITEM_LOCALES);
        List<Locale> locals = new ArrayList<Locale>();
        for (String _locale : sLocales) {
            Locale aLocale = null;
            // split parts?
            if (_locale.contains("_")) {
                String[] localParts = _locale.split("_");
                // language country?
                if (localParts.length >= 1) {
                    aLocale = new Locale(localParts[0], localParts[1]);
                } else {
                    logger.warning("Wrong Locale Configuration: " + _locale);
                }
            } else {
                // simple language locale
                aLocale = new Locale(_locale);
            }

            if (aLocale != null) {
                locals.add(aLocale);
                if (debug) {
                    logger.finest("......suporting locale " + aLocale);
                }
            }
        }

        try {

            // update ocr information only if defined in xml config item
            // 'ml.training.ocr'...
            if ("true".equalsIgnoreCase(mlOCR)) {
                workitem = doVerifyOCRContent(workitem, mlFilenamePattern, workflowClient, tikaOptions);
            }

            // build the ml content....
            String ocrText = new MLContentBuilder(workitem, null, false, mlFilenamePattern).build();

            // String ocrText = getTextContent(workitem, mlFilenamePattern, workflowClient,
            // ocrMode, tikaOptions);

            if (ocrText == null || ocrText.isEmpty()) {
                logger.severe("...document '" + workitem.getUniqueID()
                + "' No text found!");
                logger.info("=======================FINISHED====================================");
                return new MLTrainingResult(XMLTrainingData.TRAININGDATA_QUALITY_BAD, null);
            }

            logger.fine("extracted text content to be analysed=");
            logger.fine(ocrText);
            
            XMLTrainingData trainingData= generateTraingDataSet( ocrText, workitem, trainingItemNames, locals );

            // compute stats rate for found entities
//            List<String> entitysFound = new ArrayList<String>();
//            for (XMLTrainingEntity trainingEntity : trainingData.getEntities()) {
//                if (!entitysFound.contains(trainingEntity.getLabel())) {
//                    entitysFound.add(trainingEntity.getLabel());
//                }
//            }

            qualityResult = trainingData.getQuality();
            // we only send the training data in case of quality level is sufficient
            if (XMLTrainingData.TRAININGDATA_QUALITY_BAD == trainingData.getQuality()) {
                logger.severe("...document '" + workitem.getUniqueID()
                        + "' TRAININGDATA_QUALITY_LEVEL=BAD - document will be ignored!");

            } else {
                if (XMLTrainingData.TRAININGDATA_QUALITY_LOW == trainingData.getQuality()
                        && "GOOD".equalsIgnoreCase(qualityLevel)) {
                    logger.severe("...document '" + workitem.getUniqueID()
                            + "' TRAININGDATA_QUALITY_LOW but GOOD is required - document will be ignored!");
                    qualityResult = XMLTrainingData.TRAININGDATA_QUALITY_BAD;
                } else {
                    logger.info("...document '" + workitem.getUniqueID() + "' TRAININGDATA_QUALITY_LEVEL="
                            + qualityResult + "...");
                }
            }

            // trainingData if quality level is sufficient
            if (qualityResult != XMLTrainingData.TRAININGDATA_QUALITY_BAD) {
                // log the XMLTrainingData object....
                if (debug) {
                    printXML(trainingData);
                }
                String serviceEndpoint = config.getItemValueString(TrainingApplication.ITEM_ML_TRAINING_ENDPOINT);
                MLClient mlClient = new MLClient(serviceEndpoint);
                String resultData = mlClient.postTrainingData(trainingData, model, mlOptions);
                trainingResult = new MLTrainingResult(qualityResult, resultData);

            } else {
                trainingResult = new MLTrainingResult(qualityResult, null);
            }

        } catch (PluginException | RestAPIException e1) {
            logger.severe("Error parsing documents: " + e1.getMessage());
        }
        logger.info("=======================FINISHED====================================");

        return trainingResult;

    }

    
    
    /**
     * This method is used to analyze test data on an existing model. The method extracts the text
     * contained in a snapshot document and sends the text to the Imixs-ML service
     * to be analyzed. The method returnd a XMLAnalyseResult object with the anlyze result
     * 
     * @param doc            - a workitem providing the attachments and the entity
     *                       data
     * @param items          - String list with items
     * @param workflowClient - a rest client instance
     */
    @SuppressWarnings("unchecked")
    public XMLAnalyseResult analyzeWorkitemData(ItemCollection config, ItemCollection workitem, WorkflowClient workflowClient) {
        logger.info("......anaysing: " + workitem.getUniqueID());
        Pattern mlFilenamePattern = null;
        List<String> tikaOptions = config.getItemValue(TrainingApplication.ITEM_TIKA_OPTIONS);
        String serviceEndpoint = config.getItemValueString(TrainingApplication.ITEM_ML_VALIDATION_ENDPOINT);
        String model = config.getItemValueString(TrainingApplication.ITEM_ML_VALIDATION_MODEL);
        String mlOCR = config.getItemValueString(TrainingApplication.ITEM_ML_VALIDATION_OCR);
        // parse optional filename regex pattern...
        String _FilenamePattern = config.getItemValueString(TrainingApplication.ITEM_ML_VALIDATION_FILEPATTERN);
        if (_FilenamePattern != null && !_FilenamePattern.isEmpty()) {
            logger.info("......apply filename.pattern=" + _FilenamePattern);
            mlFilenamePattern = Pattern.compile(_FilenamePattern);
        }
        try {

            // update ocr information only if defined in xml config item
            // 'ml.training.ocr'...
            if ("true".equalsIgnoreCase(mlOCR)) {
                workitem = doVerifyOCRContent(workitem, mlFilenamePattern, workflowClient, tikaOptions);
            }

            // build the ml content....
            String ocrText = new MLContentBuilder(workitem, null, false, mlFilenamePattern).build();

            // String ocrText = getTextContent(doc, mlFilenamePattern, workflowClient,
            // ocrMode, tikaOptions);
            if (ocrText != null && !ocrText.isEmpty()) {
                MLClient mlClient = new MLClient(serviceEndpoint);
                XMLAnalyseResult result = mlClient.postAnalyseData(ocrText, model);
                return result;
            }
        } catch (PluginException | RestAPIException e1) {
            logger.severe("Error parsing documents: " + e1.getMessage());
        }
        return null;
    }

    

 

    /**
     * This method is used to validate of a training data set without updating the model
     * <p>
     * The method generates statistical data.
     * 
     * @param config         - a config object providing the training configuration
     * @param workitem       - a workitem providing the data
     * @param workflowClient - a rest client instance
     * @return - quality result
     */
    @SuppressWarnings("unchecked")
    public MLTrainingResult validateWorkitemData(ItemCollection config, ItemCollection workitem,
            WorkflowClient workflowClient) {
        boolean debug = logger.isLoggable(Level.FINE);
        int qualityResult = -1;
        MLTrainingResult trainingResult = null;
        Pattern mlFilenamePattern = null;

        logger.info("=======================START ======================================");
        logger.info("...validate training data for: " + workitem.getUniqueID());

        String model = config.getItemValueString(TrainingApplication.ITEM_ML_TRAINING_MODEL);
        String mlOCR = config.getItemValueString(TrainingApplication.ITEM_ML_TRAINING_OCR);
        List<String> trainingItemNames = config.getItemValue(TrainingApplication.ITEM_ENTITIES);
        List<String> tikaOptions = config.getItemValue(TrainingApplication.ITEM_TIKA_OPTIONS);
        String qualityLevel = config.getItemValueString(TrainingApplication.ITEM_ML_TRAINING_QUALITYLEVEL);

        if (qualityLevel.isEmpty()) {
            qualityLevel = "LOW"; // default level!
        }
        // parse optional filename regex pattern...
        String _FilenamePattern = config.getItemValueString(TrainingApplication.ITEM_ML_TRAINING_FILEPATTERN);
        if (_FilenamePattern != null && !_FilenamePattern.isEmpty()) {
            logger.fine("......apply filename.pattern=" + _FilenamePattern);
            mlFilenamePattern = Pattern.compile(_FilenamePattern);
        }

        // build locales....
        List<String> sLocales = config.getItemValue(TrainingApplication.ITEM_LOCALES);
        List<Locale> locals = new ArrayList<Locale>();
        for (String _locale : sLocales) {
            Locale aLocale = null;
            // split parts?
            if (_locale.contains("_")) {
                String[] localParts = _locale.split("_");
                // language country?
                if (localParts.length >= 1) {
                    aLocale = new Locale(localParts[0], localParts[1]);
                } else {
                    logger.warning("Wrong Locale Configuration: " + _locale);
                }
            } else {
                // simple language locale
                aLocale = new Locale(_locale);
            }

            if (aLocale != null) {
                locals.add(aLocale);
                if (debug) {
                    logger.finest("......suporting locale " + aLocale);
                }
            }
        }

        try {

            // update ocr information only if defined in xml config item
            // 'ml.training.ocr'...
            if ("true".equalsIgnoreCase(mlOCR)) {
                workitem = doVerifyOCRContent(workitem, mlFilenamePattern, workflowClient, tikaOptions);
            }

            // build the ml content....
            String ocrText = new MLContentBuilder(workitem, null, false, mlFilenamePattern).build();

            // String ocrText = getTextContent(workitem, mlFilenamePattern, workflowClient,
            // ocrMode, tikaOptions);

            if (ocrText == null || ocrText.isEmpty()) {
                logger.severe("...document '" + workitem.getUniqueID()
                + "' No text found!");
                logger.info("=======================FINISHED====================================");
                return new MLTrainingResult(XMLTrainingData.TRAININGDATA_QUALITY_BAD, null);
            }

            logger.fine("extracted text content to be analysed=");
            logger.fine(ocrText);
            
            XMLTrainingData trainingData= generateTraingDataSet( ocrText, workitem, trainingItemNames, locals );
            qualityResult = trainingData.getQuality();
            String serviceEndpoint = config.getItemValueString(TrainingApplication.ITEM_ML_TRAINING_ENDPOINT);
            MLClient mlClient = new MLClient(serviceEndpoint);
            String resultData = mlClient.postValidateData(trainingData, model);
            trainingResult = new MLTrainingResult(qualityResult, resultData);

        

        } catch (PluginException | RestAPIException e1) {
            logger.severe("Error parsing documents: " + e1.getMessage());
        }
        logger.info("=======================FINISHED====================================");

        return trainingResult;

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
        if (!workitem.getItemValueString(ITEM_ML_DEFINITIONS).isEmpty()) {
            List<Map<String, List<Object>>> mlDefinitions = workitem.getItemValue(ITEM_ML_DEFINITIONS);
            for (Map<String, List<Object>> aDef : mlDefinitions) {
                result.add(new ItemCollection(aDef));
            }
        }
        return result;
    }



    /**
     * Generates a XML Traning Data Set
     * 
     * @param ocrText
     * @param workitem
     * @param trainingItemNames
     * @param locals
     * @return
     */
    private XMLTrainingData generateTraingDataSet(String ocrText,ItemCollection workitem, List<String> trainingItemNames,List<Locale> locals ) {
        // we try to extract the mlEntities from the workitem
        // if this is not possible we take the data form the XML configuraiton
        List<MLEntity> mlEntities = null;
        List<ItemCollection> mlDefinitionList = getMLDefinitions(workitem);
        if (mlDefinitionList != null && mlDefinitionList.size() > 0) {
            ItemCollection mlDefinition = mlDefinitionList.get(0);
            mlEntities = MLConfig.explodeMLEntityList(mlDefinition.getItemValue(ITEM_ML_ITEMS));
        }
        if (mlEntities == null || mlEntities.size() == 0) {
            logger.info("migrating to dummy mlEntity definition set from XML configuration!");
            mlEntities = new ArrayList<MLEntity>();
            // build a dummy mlEntity list form the xml config..
            for (String aname : trainingItemNames) {
                if (!TrainingService.ITEM_ML_DEFINITIONS.equals(aname)) {
                    mlEntities.add(new MLEntity(aname, null, null, 0, false));
                }
            }
        }
    
        // build training data set...
        return  new TrainingDataBuilder(ocrText, workitem, mlEntities, locals)
                .setAnalyzerEntityEvents(entityObjectEvents).build();
    }



    /**
     * This method tests if we already have OCR text in the workitem. If not we load
     * the snapshot and post the files first to the tika service. In a normal setup
     * of Imixs-Office-Workfow this task should not be necessary here. But we need
     * to be able to parse old data.
     * 
     * 
     * @param workitem - workitem containing file attachments
     * @return text to be analyzed
     * @throws RestAPIException
     * @throws PluginException
     */
    private ItemCollection doVerifyOCRContent(ItemCollection workitem, Pattern mlFilenamePattern,
            WorkflowClient workflowClient, List<String> tikaOptions) throws RestAPIException, PluginException {

        // do we have file data?
        List<FileData> files = workitem.getFileData();

        if (files == null || files.size() == 0) {
            // no op
            return workitem;
        }

        // test if the workitem already have ocr content
        for (FileData file : files) {
            ItemCollection metadata = new ItemCollection(file.getAttributes());
            String _text = metadata.getItemValueString("text");
            if (!_text.isEmpty()) {
                // ocr contentent already exists
                return workitem;
            }
        }

        // no existing content - we need to ocr .....
        // first load the snapshot
        ItemCollection snapshot = null;
        String snapshotID = workitem.getItemValueString("$snapshotid");
        if (!snapshotID.isEmpty()) {
            snapshot = workflowClient.getDocument(snapshotID);
        }

        if (snapshot == null) {
            logger.warning("Unable to load snapshot for document " + workitem.getUniqueID());
            return workitem;

        }

        workitem = snapshot;
        files = workitem.getFileData();

        for (FileData fileData : files) {
            // add ocr content to each filedata ...
            try {
                String ocrContent;
                ocrContent = tikaService.doORCProcessing(fileData, tikaOptions);
                // store the ocrContent....
                List<Object> list = new ArrayList<Object>();
                list.add(ocrContent);
                fileData.setAttribute(FILE_ATTRIBUTE_TEXT, list);

            } catch (IOException e) {

                e.printStackTrace();
            }
        }

        return workitem;
    }
}
