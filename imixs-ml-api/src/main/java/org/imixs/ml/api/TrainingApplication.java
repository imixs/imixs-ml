package org.imixs.ml.api;

import java.util.List;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.imixs.melman.FormAuthenticator;
import org.imixs.melman.WorkflowClient;
import org.imixs.workflow.ItemCollection;

@ApplicationPath("api")
public class TrainingApplication extends Application {
    
    public static final String ITEM_TRAGET_URL="target.url";
    public static final String ITEM_TRAGET_USER="target.userid";
    public static final String ITEM_TRAGET_PASSWORD="target.password";
    public static final String ITEM_TRAGET_QUERY="target.query";
    public static final String ITEM_TRAGET_PAGEINDEX="target.pageindex";
    public static final String ITEM_TRAGET_PAGESIZE="target.pagesize";
    public static final String ITEM_ENTITIES="entities";
    public static final String ITEM_TIKA_OPTIONS="tika.options";

    
    public static WorkflowClient buildWorkflowClient(ItemCollection config) {
        
        // properties.get("target.url");
        String target=config.getItemValueString(ITEM_TRAGET_URL);
        
        WorkflowClient worklowClient = new WorkflowClient(target);
        // register the authenticator
        FormAuthenticator formAuth = new FormAuthenticator(target, config.getItemValueString(ITEM_TRAGET_USER),
                config.getItemValueString(ITEM_TRAGET_PASSWORD));
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
        for (String itemName : itemNames) {
            itemName = itemName.toLowerCase().trim();
            // if the itemName contains a | character than we do a mapping here.....
            if (itemName.contains("|")) {
                itemName = itemName.substring(0, itemName.indexOf('|')).trim();

            }
            queryParam = queryParam + "," + itemName;

        }
        url = url + queryParam;
        return url;
    }
}