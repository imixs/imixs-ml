package org.imixs.ml.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.imixs.ml.events.EntityTextEvent;

/**
 * An MLEntity describes the properties of a single entity to be analyzed or
 * trained by the MLService. An MLEntity defines the following fields:
 * <ul>
 * <li>name - name of the entity used by the ml model
 * <li>itemName - the name of the corresponding item in a workitem
 * <li>itemType - the type of the corresponding item in a workitem (e.g. text,
 * date, currency...)
 * <li>length - an optional maximum length the value can have
 * <li>required - if true the the item must be included in a training data set
 * to be used for training
 * </ul>
 * <p>
 * The MLEntity is used by the imixs-ml-workflow module to describe the entities
 * by a BPMN model.
 * 
 * @author rsoika
 *
 */
public class MLEntity {
    private String name;
    private String itemType;
    private String itemName;
    private int length;
    private boolean required;

    private static Logger logger = Logger.getLogger(MLEntity.class.getName());

    public MLEntity(String name, String itemType, String itemName, int length, boolean required) {
        super();
        setName(name);
        this.itemType = itemType;
        this.itemName = itemName;
        setLength(length);
        this.required = required;
    }

    public MLEntity(Map<String, Object> map) {
        super();

        // setName((String) map.get("name"));
        // this.itemType = (String) map.get("itemType");
        // this.itemName = (String) map.get("itemName");
        // if (map.containsKey("length")) {
        // setLength((int) map.get("length"));
        // }
        // if (map.containsKey("required")) {
        // this.required = (boolean) map.get("required");
        // }

        // Issue 62
        setName((String) getMapValue(map, "name"));
        this.itemType = (String) getMapValue(map, "itemType");
        this.itemName = (String) getMapValue(map, "itemName");
        if (map.containsKey("length")) {
            setLength((int) getMapValue(map, "length"));
        }
        if (map.containsKey("required")) {
            this.required = (boolean) getMapValue(map, "required");
        }

    }

    /**
     * Helper method to convert the map value form a MLEntity. This can be a simple
     * object or in case of previous XML conversion a ArraList of objects. In the
     * later case we take the first value of the list.
     * <p>
     * See also Github Issue 62
     * 
     * @param o
     * @return
     */
    @SuppressWarnings({ "rawtypes" })
    private Object getMapValue(Map<String, Object> map, String item) {
        Object o = map.get(item);
        Object result = o;
        if (result instanceof List) {
            result = ((List) o).get(0);
        }
        return result;
    }

    public String getName() {
        return name;
    }

    /**
     * Set the name of the mlEntity in lowercase
     * 
     * @param name
     */
    public void setName(String name) {
        if (name == null || name.isEmpty()) {
            logger.warning("Invalid ml.config entity definition - missing name!");
            this.name = name;
        } else {
            this.name = name.toLowerCase().trim();
        }
    }

    public String getItemType() {
        return itemType;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        if (length <= 0) {
            length = EntityTextEvent.TEXT_DEFAULT_MAXLENGTH;
        }

        this.length = length;
    }

    public String getItemName() {
        if (itemName == null || itemName.isEmpty()) {
            return name;
        } else {
            return itemName;
        }
    }

    /**
     * If true, than the item is required for training!
     * 
     * @return
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Returns a hashmap with all properties of the entity. The map can be used to
     * be stored in a ItemCollection.
     * 
     * @return
     */
    public Map<String, Object> getAllItems() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("name", name);
        result.put("itemType", itemType);
        result.put("itemName", itemName);
        result.put("length", length);
        result.put("required", required);

        return result;
    }
}