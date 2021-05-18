package org.imixs.ml.core;

import java.util.HashMap;
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
        setName((String) map.get("name"));
        this.itemType = (String) map.get("itemType");
        this.itemName = (String) map.get("itemName");
        if (map.containsKey("length")) {
            setLength(Integer.parseInt((String) map.get("length")));
        }
        if (map.containsKey("required")) {
            this.required = Boolean.getBoolean((String) map.get("required"));
        }

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