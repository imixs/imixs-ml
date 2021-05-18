package org.imixs.ml.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The MLConfig class provides environment names and helper method to manage
 * MLEntiy objects.
 * 
 * @author rsoika
 *
 */
public class MLConfig {
    public static final String ML_SERVICE_ENDPOINT = "ml.service.endpoint";
    public static final String ML_MODEL = "ml.model";
    public static final String ML_LOCALES = "ml.locales";
    public static final String ML_TRAINING_QUALITYLEVEL = "ml.training.quality";

    /**
     * Convert the List of ItemCollections back into a List of Map elements
     * 
     * @param workitem
     */
    @SuppressWarnings({ "rawtypes" })
    public static List<Map> implodeMLEntityList(List<MLEntity> entities) {
        List<Map> result = new ArrayList<Map>();
        // convert MLEntity objects into a List of Map
        if (entities != null) {
            // iterate over all order items..
            for (MLEntity entiryDefItem : entities) {
                result.add(entiryDefItem.getAllItems());
            }
        }
        return result;
    }

    /**
     * Converts a map list of map Object into a list of MLEntity objects.
     * <p>
     * Because of migration we need to check if the given list contains Map objects
     * (expected) or only Strings (in this case we have a workitem containing a old
     * mlDefinition).
     * 
     * @param mapList
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<MLEntity> explodeMLEntityList(List<?> mapList) {
        List<MLEntity> result = new ArrayList<MLEntity>();
        for (Object map : mapList) {
            // because of migration we test the list object here
            if (map instanceof String) {
                // this is an old entity we need to create a dummy EntityDefinition
                result.add(new MLEntity(map.toString(), null, null, 0, true));
            } else {
                // this is what we expect since version 1.1.2
                result.add(new MLEntity((Map<String, Object>) map));
            }
        }
        return result;
    }

    /**
     * This method finds a MLEntity by name form a list of MLEntity objects
     * 
     * @param name
     * @param mlEntities
     * @return
     */
    public static MLEntity findMLEntityByName(String name, List<MLEntity> mlEntities) {
        for (MLEntity edef : mlEntities) {
            if (edef.getName().equalsIgnoreCase(name)) {
                return edef;
            }
        }
        return null;
    }

    /**
     * Returns a list of all entity names for a given MLEntity List
     * 
     * @param entityDefinitions
     * @return
     */
    public static List<String> getAllEntityNames(List<MLEntity> entityDefinitions) {
        List<String> result = new ArrayList<String>();
        for (MLEntity edef : entityDefinitions) {
            result.add(edef.getName());
        }
        return result;
    }
}
