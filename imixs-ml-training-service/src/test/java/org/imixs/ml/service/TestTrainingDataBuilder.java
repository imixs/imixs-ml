package org.imixs.ml.service;

import java.util.List;

import org.imixs.ml.data.xml.XMLTrainingEntity;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

/**
 * Test class for the XMLTrainingService
 * 
 * @author rsoika
 */
public class TestTrainingDataBuilder {
    protected TrainingDataBuilder trainingDataBuilder = null;

    @Before
    public void setup() {
        trainingDataBuilder = new TrainingDataBuilder(null);

    }

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

}
