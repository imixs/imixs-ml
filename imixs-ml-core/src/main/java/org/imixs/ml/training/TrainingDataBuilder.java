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

import org.imixs.ml.events.EntityObjectEvent;
import org.imixs.ml.xml.XMLTrainingData;
import org.imixs.ml.xml.XMLTrainingEntity;
import org.imixs.workflow.ItemCollection;

/**
 * The TrainingDataBuilder builds a new XMLTraingData object based on a given
 * document applied to a content.
 * <p>
 * The TrainingDataBuilder provides the following properties:
 * <ul>
 * <li>itemNames - list of items to be analyzed
 * <li>content - a text content form a file
 * <li>doc - the document containing the entity data
 * <li>entityObjectEvents - optional list of EntityObjectEvent
 * </ul>
 * 
 * @author rsoika
 */
public class TrainingDataBuilder {

    private static Logger logger = Logger.getLogger(TrainingDataBuilder.class.getName());

    private List<String> itemNames = null;
    private ItemCollection doc = null;
    private Event<EntityObjectEvent> entityObjectEvents = null;
    private XMLTrainingData trainingData = null;
    private Set<Locale> locals=null;

    public TrainingDataBuilder(String text, ItemCollection doc, List<String> itemNames,Set<Locale> locals) {
        super();
        this.itemNames = itemNames;
        this.doc = doc;
        this.locals=locals;
        trainingData = new XMLTrainingData();
        trainingData.setText(text);
    }

    public TrainingDataBuilder setAnalyzerEntityEvents(Event<EntityObjectEvent> analyzerEntityEvents) {
        this.entityObjectEvents = analyzerEntityEvents;
        return this;
    }

    public XMLTrainingData build() {
        boolean debug = logger.isLoggable(Level.FINE);

        debug = true;

        // now lets see if we find some of our item values....
        for (String itemName : itemNames) {
            itemName = itemName.toLowerCase().trim();
            // if the itemName contains a | character than we do a mapping here.....
            if (itemName.contains("|")) {
                String entityName = itemName.substring(itemName.indexOf('|') + 1).trim();
                // replace item in workitem....
                doc.replaceItemValue(entityName, doc.getItemValue(itemName.substring(0, itemName.indexOf('|')).trim()));
                itemName = entityName;
            }

            @SuppressWarnings("unchecked")
            List<Object> values = doc.getItemValue(itemName);
            if (values != null && values.size() > 0) {
                List<XMLTrainingEntity> trainingEntities = createTrainingEntities(trainingData.getText(), values.get(0),
                        itemName,locals);

                if (trainingEntities != null) {
                    for (XMLTrainingEntity trainingEntity : trainingEntities) {
                        trainingData.addTrainingEntity(trainingEntity);
                        if (debug) {
                            logger.info("......found entity " + trainingEntity.getLabel() + " = '"
                                    + trainingEntity.getValue() + "' at " + " " + trainingEntity.getStart() + ","
                                    + trainingEntity.getStop());
                        }
                    }
                }
            }
        }

        return trainingData;
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
    public List<XMLTrainingEntity> createTrainingEntities(String text, Object entity, String label,Set<Locale> locals) {
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
            entityObjectEvents.fire(new EntityObjectEvent(entity, enityTextVariants,locals));
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
    public List<XMLTrainingEntity> collectTrainingEntities(String text, Set<String> enityVariants, String label) {
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
    public void cleanOvelappingEntities(List<XMLTrainingEntity> trainingEntites) {

        List<XMLTrainingEntity> overlapps=new ArrayList<XMLTrainingEntity>();
        
        for (XMLTrainingEntity analyzeEntity: trainingEntites) {
            // analyze
            for (XMLTrainingEntity entity: trainingEntites) {
                if (!entity.equals(analyzeEntity)) {
                    if (entity.getStart()>= analyzeEntity.getStart() && entity.getStop()<=analyzeEntity.getStop()) {
                        overlapps.add(entity);
                    }
                }
                
                
            }
        }
        
        // remove overlapping entities....
        for (Iterator<XMLTrainingEntity> iter = trainingEntites.listIterator(); iter.hasNext(); ) {
            XMLTrainingEntity a = iter.next();
            if (overlapps.contains(a)) {
                iter.remove();
            }
        }
        
    }

}
