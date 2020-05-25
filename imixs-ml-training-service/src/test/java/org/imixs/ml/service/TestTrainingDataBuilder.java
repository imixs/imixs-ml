package org.imixs.ml.service;

import java.util.List;

import org.imixs.ml.data.xml.XMLTrainingData;
import org.imixs.ml.data.xml.XMLTrainingEntity;
import org.imixs.workflow.ItemCollection;
import org.junit.Test;

import junit.framework.Assert;

/**
 * Test class for the XMLTrainingService
 * 
 * @author rsoika
 */
public class TestTrainingDataBuilder {

    /**
     * This test verifies the creation of a training entity.
     * 
     * It is important to ensure that the start/stop positions are as expected by
     * spaCy.
     * 
     * <p>
     * {@code
     * 
     * ("Uber blew through $1 million a week", [(0, 4, 'ORG')]),
       ("Android Pay expands to Canada", [(0, 11, 'PRODUCT'), (23, 30, 'GPE')]),
       ("Spotify steps up Asia expansion", [(0, 8, "ORG"), (17, 21, "LOC")]),
       ("Google Maps launches location sharing", [(0, 11, "PRODUCT")]),
       ("Google rebrands its business apps", [(0, 6, "ORG")])])
     * 
     * }
     * 
     * See: https://spacy.io/usage/training
     * 
     */
    @Test
    public void testCreateXMLTrainingEntity() {

        TrainingDataBuilder trainingDataBuilder = new TrainingDataBuilder(null, null, null);

        List<XMLTrainingEntity> trainingEntities = null;

        trainingEntities = trainingDataBuilder.createTraingEntities("Uber blew through $1 million a week", "Uber",
                "ORG");
        Assert.assertEquals(0, trainingEntities.get(0).getStart());
        Assert.assertEquals(4, trainingEntities.get(0).getStop());
        Assert.assertEquals("ORG", trainingEntities.get(0).getLabel());

        trainingEntities = trainingDataBuilder.createTraingEntities("Android Pay expands to Canada", "Android Pay",
                "PRODUCT");
        Assert.assertEquals(0, trainingEntities.get(0).getStart());
        Assert.assertEquals(11, trainingEntities.get(0).getStop());

        trainingEntities = trainingDataBuilder.createTraingEntities("Spotify steps up Asia expansion", "Asia", "LOC");
        Assert.assertEquals(17, trainingEntities.get(0).getStart());
        Assert.assertEquals(21, trainingEntities.get(0).getStop());

        trainingEntities = trainingDataBuilder.createTraingEntities("Android Pay expands to Canada", "Canada", "GPE");
        Assert.assertEquals(23, trainingEntities.get(0).getStart());
        Assert.assertEquals(29, trainingEntities.get(0).getStop());

        trainingEntities = trainingDataBuilder
                .createTraingEntities("they pretend to care about your feelings, those horses", "horses", "LABEL");
        Assert.assertEquals(48, trainingEntities.get(0).getStart());
        Assert.assertEquals(54, trainingEntities.get(0).getStop());

        // test with more than one match
        trainingEntities = trainingDataBuilder
                .createTraingEntities("horses pretend to care about your feelings, those horses", "horses", "LABEL");
        Assert.assertEquals(2, trainingEntities.size());

        Assert.assertEquals(0, trainingEntities.get(0).getStart());
        Assert.assertEquals(6, trainingEntities.get(0).getStop());

        Assert.assertEquals(50, trainingEntities.get(1).getStart());
        Assert.assertEquals(56, trainingEntities.get(1).getStop());

    }

    /**
     * This test verifies the creation of a TraingDat object with a content containg
     * special characters.
     * 
     */
    @Test
    public void testBuilder() {

        ItemCollection doc = new ItemCollection();
        doc.replaceItemValue("test", "{some text}");

        String[] items = { "test" };

        String text = "some text in a special textblock.\nWith line\nAnd with some text{END}";

        XMLTrainingData trainingData = new TrainingDataBuilder(text, doc, items).build();

        Assert.assertEquals("some text in a special textblock. With line And with some text END ",
                trainingData.getText());

        List<XMLTrainingEntity> trainingEntities = trainingData.getEntities();
        Assert.assertEquals(2, trainingEntities.size());

        Assert.assertEquals(0, trainingEntities.get(0).getStart());
        Assert.assertEquals(9, trainingEntities.get(0).getStop());

        Assert.assertEquals(53, trainingEntities.get(1).getStart());
        Assert.assertEquals(62, trainingEntities.get(1).getStop());

    }

    /**
     * This test verifies the computes of start stop positions in a text. This test
     * is based on the example provided by spacy Named Entity Recognition 101:
     * https://spacy.io/usage/linguistic-features#named-entities-101
     * 
     * <p>
     * The test expects the following results
     * 
     * <pre>
     *     "Apple is looking at buying U.K. startup for $1 billion"
     *     
     *     Apple        0   5  
     *     U.K.        27  31  
     *     $1 billion  44  54
     * 
     * </pre>
     */
    @Test
    public void testBuilderStartStop() {

        ItemCollection doc = new ItemCollection();
        doc.replaceItemValue("org", "Apple");
        doc.replaceItemValue("gpe", "U.K.");
        doc.replaceItemValue("money", "$1 billion");

        String[] items = { "org","gpe","money" };

        String text = "Apple is looking at buying U.K. startup for $1 billion";

        XMLTrainingData trainingData = new TrainingDataBuilder(text, doc, items).build();

       

        List<XMLTrainingEntity> trainingEntities = trainingData.getEntities();
        Assert.assertEquals(3, trainingEntities.size());

        Assert.assertEquals(0, trainingEntities.get(0).getStart());
        Assert.assertEquals(5, trainingEntities.get(0).getStop());

        Assert.assertEquals(27, trainingEntities.get(1).getStart());
        Assert.assertEquals(31, trainingEntities.get(1).getStop());

        Assert.assertEquals(44, trainingEntities.get(2).getStart());
        Assert.assertEquals(54, trainingEntities.get(2).getStop());
    }
    
    
   

    @Test
    public void testCleanTextdata() {

        String result = null;

        result = XMLTrainingData.cleanTextdata("some {special} \"text\"!");
        Assert.assertEquals("some special text !", result);

        // test new lines
        result = XMLTrainingData.cleanTextdata("some\n{special} \"text\"!");
        Assert.assertEquals("some special text !", result);
        
        // test strip of multiple spaces
        result = XMLTrainingData.cleanTextdata("hello     there");
        Assert.assertEquals("hello there", result);

    }

}
