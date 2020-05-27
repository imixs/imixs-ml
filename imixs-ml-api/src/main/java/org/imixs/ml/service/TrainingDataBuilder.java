package org.imixs.ml.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.event.Event;

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
 * <li>analyzerEntityEvents - optional list of AnalyzerEntityEvents
 * </ul>
 * 
 * @author rsoika
 */
public class TrainingDataBuilder {

    private static Logger logger = Logger.getLogger(TrainingDataBuilder.class.getName());

    private List<String> itemNames = null;
    private ItemCollection doc = null;
    private Event<AnalyzeEntityEvent> analyzerEntityEvents = null;
    private XMLTrainingData trainingData=null;

    public TrainingDataBuilder(String text,ItemCollection doc,List<String> itemNames) {
        super();
        this.itemNames = itemNames; 
        this.doc=doc;
        trainingData = new XMLTrainingData();
        trainingData.setText(text);
    }


    public TrainingDataBuilder setAnalyzerEntityEvents(Event<AnalyzeEntityEvent> analyzerEntityEvents) {
        this.analyzerEntityEvents = analyzerEntityEvents;
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
                List<XMLTrainingEntity> trainingEntities = createTraingEntities(trainingData.getText(), values.get(0), itemName);

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
     * 
     * 
     * @param text
     * @param entity
     * @param label
     * @return
     */
    public List<XMLTrainingEntity> createTraingEntities(String text, Object entity, String label) {
        boolean debug = logger.isLoggable(Level.FINE);

        if (text == null || text.isEmpty()) {
            return null;
        }
        if (entity == null || entity.toString().isEmpty()) {
            return null;
        }

        List<XMLTrainingEntity> result = new ArrayList<XMLTrainingEntity>();
        if (debug) {
            logger.finest(".......analyzing: " + label + " value= " + entity + " object class="
                    + entity.getClass().getSimpleName());
        }

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

        if (debug) {
            logger.finest("...... entity variants for : '" + entity.toString() + "'");
            for (String entityVariant : enityVariants) {
                logger.finest("......    " + entityVariant);
            }
        }

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

  

}
