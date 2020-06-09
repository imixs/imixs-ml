package org.imixs.ml.service;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

import org.imixs.ml.adapters.AnalyzeEntityEvent;
import org.imixs.ml.adapters.DateAdapter;
import org.imixs.ml.xml.XMLTrainingData;
import org.imixs.ml.xml.XMLTrainingEntity;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.exceptions.PluginException;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

/**
 * Test class for the XMLTrainingService
 * 
 * @author rsoika
 */
public class TestTrainingDataBuilder {

    private  Set<Locale> locals=null;
    private static Logger logger = Logger.getLogger(TestTrainingDataBuilder.class.getName());


    @Before
    public void setup() throws PluginException {
        locals=new HashSet<Locale>();
        locals.add(Locale.UK);
        locals.add(Locale.GERMAN);

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

        TrainingDataBuilder trainingDataBuilder = new TrainingDataBuilder(null, null, null,locals);

        List<XMLTrainingEntity> trainingEntities = null;

        trainingEntities = trainingDataBuilder.createTrainingEntities("Uber blew through $1 million a week", "Uber",
                "ORG",locals);
        Assert.assertEquals(0, trainingEntities.get(0).getStart());
        Assert.assertEquals(4, trainingEntities.get(0).getStop());
        Assert.assertEquals("ORG", trainingEntities.get(0).getLabel());

        trainingEntities = trainingDataBuilder.createTrainingEntities("Android Pay expands to Canada", "Android Pay",
                "PRODUCT",locals);
        Assert.assertEquals(0, trainingEntities.get(0).getStart());
        Assert.assertEquals(11, trainingEntities.get(0).getStop());

        trainingEntities = trainingDataBuilder.createTrainingEntities("Spotify steps up Asia expansion", "Asia", "LOC",locals);
        Assert.assertEquals(17, trainingEntities.get(0).getStart());
        Assert.assertEquals(21, trainingEntities.get(0).getStop());

        trainingEntities = trainingDataBuilder.createTrainingEntities("Android Pay expands to Canada", "Canada", "GPE",locals);
        Assert.assertEquals(23, trainingEntities.get(0).getStart());
        Assert.assertEquals(29, trainingEntities.get(0).getStop());

        trainingEntities = trainingDataBuilder
                .createTrainingEntities("they pretend to care about your feelings, those horses", "horses", "LABEL",locals);
        Assert.assertEquals(48, trainingEntities.get(0).getStart());
        Assert.assertEquals(54, trainingEntities.get(0).getStop());

        // test with more than one match
        trainingEntities = trainingDataBuilder
                .createTrainingEntities("horses pretend to care about your feelings, those horses", "horses", "LABEL",locals);
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

        List<String> items = Arrays.asList(new String[] { "test" });

        String text = "some text in a special textblock.\nWith line\nAnd with some text{END}";

        XMLTrainingData trainingData = new TrainingDataBuilder(text, doc, items,locals).build();

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

        List<String> items = Arrays.asList(new String[] { "org", "gpe", "money" });

        String text = "Apple is looking at buying U.K. startup for $1 billion";

        XMLTrainingData trainingData = new TrainingDataBuilder(text, doc, items,locals).build();

        List<XMLTrainingEntity> trainingEntities = trainingData.getEntities();
        Assert.assertEquals(3, trainingEntities.size());

        Assert.assertEquals(0, trainingEntities.get(0).getStart());
        Assert.assertEquals(5, trainingEntities.get(0).getStop());

        Assert.assertEquals(27, trainingEntities.get(1).getStart());
        Assert.assertEquals(31, trainingEntities.get(1).getStop());

        Assert.assertEquals(44, trainingEntities.get(2).getStart());
        Assert.assertEquals(54, trainingEntities.get(2).getStop());
    }

    /**
     * This test verifies the cleanTextdata method provided by the XMLTrainingData
     * class. The goal of this method is to remove unsupported characters.
     */
    @Test
    public void testCleanTextdata() {

        String result = null;

        result = XMLTrainingData.cleanTextdata("some {special} \"text\"!");
        Assert.assertEquals("some special text !", result);

        // test new lines
        result = XMLTrainingData.cleanTextdata("some\n{special} \"text\"!");
        Assert.assertEquals("some special text !", result);

        // test strip of multiple spaces
        result = XMLTrainingData.cleanTextdata("hello    \n     there");
        Assert.assertEquals("hello there", result);

    }

    /**
     * Test overlapping entities.
     * <p>
     * We have a text where two different text variants match. We expect that only
     * the better match is returned and the shorter match is removed from the list.
     * <pre>
     * text = "Invoice Date: 02.06.2020";
       _invoicedate=02.06.2020 (14,24)
       _invoicedate=02.06.20 (14,22)
     * </pre>
     * 
     */
    @Test
    public void testcleanOvelappingEntities() {
        DateAdapter dateAdapter = new DateAdapter();
        String text = "Invoice Date: 02.06.2020";

        List<String> items = Arrays.asList(new String[] { "_invoicedate" });

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 2);
        cal.set(Calendar.MONTH, 5);
        cal.set(Calendar.YEAR, 2020);

        Set<String> enityVariants = new HashSet<String>();
        dateAdapter.onEvent(new AnalyzeEntityEvent(cal.getTime(), enityVariants,locals));

        ItemCollection doc = new ItemCollection();
        doc.setItemValue("_invoicedate", cal.getTime());

        TrainingDataBuilder builder = new TrainingDataBuilder(text, doc, items,locals);

        // Here we simulate a XML training entity list....
        List<XMLTrainingEntity> trainingEntites = builder.collectTrainingEntities(text, enityVariants, "_invoice");
        // we expect in a first 2 matches (02.06.20 , 02.06.2020)
        Assert.assertEquals(2, trainingEntites.size());

        logger.info("===> collected TrainingEntites:");
        for (XMLTrainingEntity entity : trainingEntites) {
            logger.info(".... found " + entity.getValue() + " at " + entity.getStart() + "," + entity.getStop());
        }

        // now we clean overlapping...
        builder.cleanOvelappingEntities(trainingEntites);
        // finally we expect one match! (02.06.2020)
        Assert.assertEquals(1, trainingEntites.size());
        logger.info("===> Cleaned TrainingEntites:");
        for (XMLTrainingEntity entity : trainingEntites) {
            logger.info(".... found " + entity.getValue() + " at " + entity.getStart() + "," + entity.getStop());
        }
    }

}
