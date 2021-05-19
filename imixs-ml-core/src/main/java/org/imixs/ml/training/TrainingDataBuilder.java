package org.imixs.ml.training;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.event.Event;

import org.imixs.ml.core.MLEntity;
import org.imixs.ml.events.EntityObjectEvent;
import org.imixs.ml.xml.XMLTrainingData;
import org.imixs.ml.xml.XMLTrainingEntity;
import org.imixs.workflow.ItemCollection;

/**
 * The TrainingDataBuilder builds a new XMLTraingData object based on a given
 * Workitem applied to a textual content.
 * <p>
 * The TrainingDataBuilder provides the following properties:
 * <ul>
 * <li>itemNames - list of items to be analyzed
 * <li>content - a text content form a file or workitem
 * <li>workitem - the workitem containing the entity data
 * <li>entityObjectEvents - optional list of EntityObjectEvents to adapt the
 * values of a workitem
 * <li>locals - a list of locales to be applied to entityObjectEvents
 * </ul>
 * The method build can be called to build a new XMLTrainingData object
 * 
 * @author rsoika
 */
public class TrainingDataBuilder {

    private static Logger logger = Logger.getLogger(TrainingDataBuilder.class.getName());

    private List<MLEntity> mlEntities = null;
    private ItemCollection workitem = null;
    private Event<EntityObjectEvent> entityObjectEvents = null;
    private XMLTrainingData trainingData = null;
    private List<Locale> locals = null;

    public TrainingDataBuilder(String text, ItemCollection workitem, List<MLEntity> mlEntities, List<Locale> locals) {
        super();
        this.mlEntities = mlEntities;
        if (workitem != null) {
            this.workitem = (ItemCollection) workitem.clone();
        } else {
            this.workitem = null;
        }
        this.locals = locals;
        trainingData = new XMLTrainingData();
        trainingData.setText(text);
    }

    public TrainingDataBuilder setAnalyzerEntityEvents(Event<EntityObjectEvent> analyzerEntityEvents) {
        this.entityObjectEvents = analyzerEntityEvents;
        return this;
    }

    /**
     * This method build a new XMLTraingData object and based on the given text
     * content and the current workitem. The method also computes the quality level
     * of the trainingData.
     * 
     * @return - new XMLTrainingData object
     */
    public XMLTrainingData build() {
        boolean debug = logger.isLoggable(Level.FINE);

        debug = true;

        // set quality level to full and reduce the level during the build process
        trainingData.setQuality(XMLTrainingData.TRAININGDATA_QUALITY_GOOD);
        // now lets see if we find some of our item values....
        for (MLEntity mlEntity:  mlEntities) {
            String itemName = mlEntity.getItemName().toLowerCase().trim();
            // if the itemName contains a | character than we do a mapping here.....
            if (itemName.contains("|")) {
                String entityName = itemName.substring(itemName.indexOf('|') + 1).trim();
                // replace item in workitem....
                workitem.replaceItemValue(entityName,
                        workitem.getItemValue(itemName.substring(0, itemName.indexOf('|')).trim()));
                itemName = entityName;
            }

            if (!workitem.isItemEmpty(itemName)) {
                // we only analyze the first value...
                Object value = workitem.getItemValue(itemName).get(0);
                List<XMLTrainingEntity> trainingEntities = createTrainingEntities(trainingData.getText(), value,
                        itemName, locals);

                if (trainingEntities!=null && trainingEntities.size()>0) {
                    for (XMLTrainingEntity trainingEntity : trainingEntities) {
                        trainingData.addTrainingEntity(trainingEntity);
                        if (debug) {
                            logger.finest("......found entity " + trainingEntity.getLabel() + " = '"
                                    + trainingEntity.getValue() + "' at " + " " + trainingEntity.getStart() + ","
                                    + trainingEntity.getStop());
                        }
                    }
                } else {
                    // no matching value was found for this entity, the quality level is bad if the entity has
                    // the required flag.
                    if (mlEntity.isRequired()) {
                        trainingData.setQuality(XMLTrainingData.TRAININGDATA_QUALITY_BAD);
                    }
                }

            } else {
                // workitem does not have a value for this entity!
                // if the quality level is still FULL, than we downgrade the level to PARTIAL
                if (trainingData.getQuality() == XMLTrainingData.TRAININGDATA_QUALITY_GOOD) {
                    trainingData.setQuality(XMLTrainingData.TRAININGDATA_QUALITY_LOW);
                }
            }
        }

        // if the trainingData contains no entities set quality to TRAININGDATA_QUALITY_LEVEL_BAD
        if (trainingData.getEntities().size()==0) {
            // no training entities for this workitem value were found
            trainingData.setQuality(XMLTrainingData.TRAININGDATA_QUALITY_BAD);
        }
        
        return trainingData;
    }

    /**
     * compute the training data quality
     * 
     * The training data quality depends on the entities found in the content. there
     * are two different quality modes possible.
     * <p>
     * <ul>
     * <li>FULL - all trainingItems in the workitem have a value and all values are
     * part of the traingData. This means a 100% match.
     * <li>PARTIAL - not all trainingItems in the workitem have a value, but all
     * values are part of the traingData. This means we have a partial match.
     * <li>BAD - not all item values of the workitem are part of the traingData.
     * This means the training object has a bad quality and can not be used for
     * training
     * 
     **/
//    private int computeTrainingDataQuality(ItemCollection workitem, List<String> trainingItemNames,
//            XMLTrainingData trainingData) {
//
//        // trainingItemNames can contain a | for a mapping betwen the itemName in the
//        // workitem and the training entity. for that reason we now build two lists. The
//        // first contains the normalized training items and the second contains the
//        // items with the workitem not empty.
//
//        List<String> normalizedTrainingEntities = new ArrayList<String>();
//        List<String> normalizedWorkitemEntities = new ArrayList<String>();
//
//        // now lets see if we find some of our item values....
//        for (String itemName : trainingItemNames) {
//            String trainingEntity = null;
//            String workitemEntity = null;
//
//            itemName = itemName.toLowerCase().trim();
//            // if the itemName contains a | character than we do a mapping here.....
//            if (itemName.contains("|")) {
//                trainingEntity = itemName.substring(itemName.indexOf('|') + 1).trim();
//                workitemEntity = itemName.substring(0, itemName.indexOf('|')).trim();
//            } else {
//                trainingEntity = itemName;
//                workitemEntity = itemName;
//            }
//
//            // is the workitemEntiy part of the workitem?
//            if (!workitem.isItemEmpty(workitemEntity) && !normalizedWorkitemEntities.contains(workitemEntity)) {
//                normalizedWorkitemEntities.add(workitemEntity);
//            }
//
//            for (XMLTrainingEntity _trainingEntity : trainingData.getEntities()) {
//                if (_trainingEntity.getLabel().equals(trainingEntity)
//                        && !normalizedTrainingEntities.contains(trainingEntity)) {
//                    normalizedTrainingEntities.add(trainingEntity);
//                }
//            }
//        }
//
//        // now we can compute the quality level of the traing data....
//        if (normalizedTrainingEntities.size() == normalizedWorkitemEntities.size()) {
//            return TRAININGDATA_QUALITY_LEVEL_FULL;
//        }
//
//        if (normalizedTrainingEntities.size() > normalizedWorkitemEntities.size()) {
//            return TRAININGDATA_QUALITY_LEVEL_PARTIAL;
//        }
//
//        return TRAININGDATA_QUALITY_LEVEL_BAD;
//    }

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
     * <p>
     * It may happen that two or more variants of the same entity overlap. Most ml
     * frameworks do not support this case. For that reason only the best matches
     * will be included in the Training list.
     * 
     * @param text
     * @param entity
     * @param label
     * @return
     */
    protected List<XMLTrainingEntity> createTrainingEntities(String text, Object entity, String label,
            List<Locale> locals) {
        boolean debug = logger.isLoggable(Level.FINE);

        if (text == null || text.isEmpty()) {
            return null;
        }
        if (entity == null || entity.toString().isEmpty()) {
            return null;
        }

        if (debug) {
            logger.finest(".......analyzing: " + label + " value= " + entity + " object class="
                    + entity.getClass().getSimpleName());
        }

        // adapt value formats...
        // fire event
        Set<String> enityTextVariants = new HashSet<String>();

        if (entityObjectEvents != null) {
            entityObjectEvents.fire(new EntityObjectEvent(entity, enityTextVariants, locals));
        } else {
            logger.warning("CDI Support is missing - AnalyzeEntityEvent Not Supported!");
        }

        // if the EntityAdapters provide no value, than we are adding the plain string
        // value only..
        if (enityTextVariants.size() == 0) {
            enityTextVariants.add(entity.toString());
        }

        if (debug) {
            logger.finest("...... entity variants for : '" + entity.toString() + "'");
            for (String entityVariant : enityTextVariants) {
                logger.finest("......    " + entityVariant);
            }
        }

        List<XMLTrainingEntity> result = collectTrainingEntities(text, enityTextVariants, label);
        // cleanup duplicates
        cleanOvelappingEntities(result);

        return result;

    }

    /**
     * This method builds a List of XMLTrainingEntity for all occurrences of
     * entityVariants in a given text
     * 
     * @param text
     * @param enityVariants
     * @param label
     * @return
     */
    protected List<XMLTrainingEntity> collectTrainingEntities(String text, Set<String> enityVariants, String label) {
        List<XMLTrainingEntity> result = new ArrayList<XMLTrainingEntity>();
        // test all variants...
        for (String entityVariant : enityVariants) {

            // clean the entityVariant text....
            entityVariant = XMLTrainingData.cleanTextdata(entityVariant).trim();

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

    /**
     * It may happen that two or more variants of the same entity overlap for the
     * same text. This is in most cases not supported from ml frameworks. This
     * method can be used to remove duplicates from a List of XMLTrainingEntity
     * objects.
     * <p>
     * This method is called by the createTrainingEntites.
     * 
     */
    protected void cleanOvelappingEntities(List<XMLTrainingEntity> trainingEntites) {

        List<XMLTrainingEntity> overlapps = new ArrayList<XMLTrainingEntity>();

        for (XMLTrainingEntity analyzeEntity : trainingEntites) {
            // analyze
            for (XMLTrainingEntity entity : trainingEntites) {
                if (!entity.equals(analyzeEntity)) {
                    if (entity.getStart() >= analyzeEntity.getStart() && entity.getStop() <= analyzeEntity.getStop()) {
                        overlapps.add(entity);
                    }
                }

            }
        }

        // remove overlapping entities....
        for (Iterator<XMLTrainingEntity> iter = trainingEntites.listIterator(); iter.hasNext();) {
            XMLTrainingEntity a = iter.next();
            if (overlapps.contains(a)) {
                iter.remove();
            }
        }

    }

}
