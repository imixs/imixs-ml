package org.imixs.ml.api;

import java.util.List;
import java.util.logging.Logger;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import org.imixs.melman.FormAuthenticator;
import org.imixs.melman.WorkflowClient;
import org.imixs.workflow.ItemCollection;

@ApplicationPath("api")
public class TrainingApplication extends Application {

    public static final String ITEM_WORKFLOW_ENDPOINT = "workflow.endpoint";
    public static final String ITEM_WORKFLOW_USER = "workflow.userid";
    public static final String ITEM_WORKFLOW_PASSWORD = "workflow.password";
    public static final String ITEM_WORKFLOW_QUERY = "workflow.query";
    public static final String ITEM_WORKFLOW_PAGEINDEX = "workflow.pageindex";
    public static final String ITEM_WORKFLOW_PAGESIZE = "workflow.pagesize";
    public static final String ITEM_ENTITIES = "workflow.entities";
    public static final String ITEM_LOCALES = "workflow.locale";
    
    public static final String ITEM_TIKA_OPTIONS = "tika.options";
    public static final String ITEM_TIKA_OCR_MODE = "tika.ocrmode";
    
    public static final String ITEM_ML_TRAINING_ENDPOINT = "ml.training.endpoint";
    public static final String ITEM_ML_TRAINING_MODEL = "ml.training.model";
    public static final String ITEM_ML_TRAINING_QUALITYLEVEL = "ml.training.quality";
    public static final String ITEM_ML_TRAINING_ITERATIONS = "ml.training.iterations";
    public static final String ITEM_ML_TRAINING_DROP_OUT_RATE = "ml.training.dropoutrate";
    public static final String ITEM_ML_TRAINING_OCR = "ml.training.ocr";
    
    public static final String ITEM_ML_TRAINING_FILEPATTERN="ml.training.filepattern";
    public static final String ITME_ML_OPTIONS = "ml.options";

    
    public static final String ITEM_ML_VALIDATION_ENDPOINT = "ml.validation.endpoint";
    public static final String ITEM_ML_VALIDATION_MODEL = "ml.validation.model";
    public static final String ITEM_ML_VALIDATION_OCR = "ml.validation.ocr";
    public static final String ITEM_ML_VALIDATION_FILEPATTERN="ml.validation.filepattern";

    
    
    private static Logger logger = Logger.getLogger(TrainingApplication.class.getName());

    
    public static WorkflowClient buildWorkflowClient(ItemCollection config) {

        // properties.get("target.url");
        String target = config.getItemValueString(ITEM_WORKFLOW_ENDPOINT);
        logger.info("...workflow.endpoint="+target);
        logger.info("...workflow.userid="+config.getItemValueString(ITEM_WORKFLOW_USER));
        
        WorkflowClient worklowClient = new WorkflowClient(target);
        // register the authenticator
        FormAuthenticator formAuth = new FormAuthenticator(target, config.getItemValueString(ITEM_WORKFLOW_USER),
                config.getItemValueString(ITEM_WORKFLOW_PASSWORD));
        worklowClient.registerClientRequestFilter(formAuth);

        return worklowClient;
    }

    /**
     * This method appends the item query param to an url based on a config list of
     * itemnames.
     * <p>
     * The method tests for | character. If found only the first part is taken as
     * the item name
     * 
     * @param url
     * @param entities
     * @return
     */
    public static String appendItenNames(String url, List<String> itemNames) {

        String queryParam = "&items=$file,$snapshotid,$uniqueid";
        if (itemNames != null) {
            for (String itemName : itemNames) {
                itemName = itemName.toLowerCase().trim();
                // if the itemName contains a | character than we do a mapping here.....
                if (itemName.contains("|")) {
                    itemName = itemName.substring(0, itemName.indexOf('|')).trim();

                }
                queryParam = queryParam + "," + itemName;

            }
        }
        url = url + queryParam;
        return url;
    }
}