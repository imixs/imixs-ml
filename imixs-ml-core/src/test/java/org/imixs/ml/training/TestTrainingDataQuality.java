package org.imixs.ml.training;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.imixs.ml.xml.XMLTrainingData;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.exceptions.PluginException;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

/**
 * Test class to test the computation of the TraingData quality level by
 * simulating different szenarios.
 * 
 * @author rsoika
 */
public class TestTrainingDataQuality {

    private Set<Locale> locals = null;

    @Before
    public void setup() throws PluginException {
        locals = new HashSet<Locale>();
        locals.add(Locale.UK);
        locals.add(Locale.GERMAN);
    }

    /**
     * This test verifies the method computeTrainingDataQuality
     * <p>
     * Full match without any item name mapping
     * <p>
     * All items are contained in the workitem and in the training data
     */
    @Test
    public void testComputeTrainingDataQualityFullMatch() {

        ItemCollection doc = new ItemCollection();
        doc.replaceItemValue("dummy", "xxx");
        doc.replaceItemValue("name", "anna");
        doc.replaceItemValue("city", "munich");
        doc.replaceItemValue("company", "IBM");

        List<String> items = Arrays.asList(new String[] { "name", "city", "company" });

        String text = "anna is working at IBM in munich";

        XMLTrainingData trainingData = new TrainingDataBuilder(text, doc, items, locals).build();
        Assert.assertEquals(XMLTrainingData.TRAININGDATA_QUALITY_LEVEL_FULL, trainingData.getQuality());
    }

    /**
     * This test verifies the method computeTrainingDataQuality
     * <p>
     * partial match without any item name mapping.
     * <p>
     * The workitem does not contain a value for Company
     * 
     */
    @Test
    public void testComputeTrainingDataQualityPartialMatch() {

        ItemCollection doc = new ItemCollection();
        doc.replaceItemValue("dummy", "xxx");
        doc.replaceItemValue("name", "anna");
        doc.replaceItemValue("city", "munich");
        // no company - doc.replaceItemValue("company", "IBM");

        List<String> items = Arrays.asList(new String[] { "name", "city", "company" });

        String text = "anna is working at IBM in munich";

        XMLTrainingData trainingData = new TrainingDataBuilder(text, doc, items, locals).build();
        Assert.assertEquals(XMLTrainingData.TRAININGDATA_QUALITY_LEVEL_PARTIAL, trainingData.getQuality());
    }

    /**
     * This test verifies the method computeTrainingDataQuality
     * <p>
     * bad match without any item name mapping
     * <p>
     * The traingData object does not contain an entity for Company
     * 
     */
    @Test
    public void testComputeTrainingDataQualityBadMatch() {

        ItemCollection doc = new ItemCollection();
        doc.replaceItemValue("dummy", "xxx");
        doc.replaceItemValue("name", "anna");
        doc.replaceItemValue("city", "munich");
        doc.replaceItemValue("company", "IBM");

        List<String> items = Arrays.asList(new String[] { "name", "city", "company" });

        /* no company in traing data */
        String text = "anna is working at RedHat in munich";

        XMLTrainingData trainingData = new TrainingDataBuilder(text, doc, items, locals).build();
        Assert.assertEquals(XMLTrainingData.TRAININGDATA_QUALITY_LEVEL_BAD, trainingData.getQuality());
    }

    /**
     * This test verifies the method computeTrainingDataQuality
     * <p>
     * Full match with item name mapping
     * <p>
     * All items are contained in the workitem and in the training data
     */
    @Test
    public void testComputeTrainingDataQualityFullMatchWithItemNameMapping() {

        ItemCollection doc = new ItemCollection();
        doc.replaceItemValue("dummy", "xxx");
        doc.replaceItemValue("name", "anna");
        doc.replaceItemValue("city", "munich");
        doc.replaceItemValue("firma", "IBM");

        List<String> items = Arrays.asList(new String[] { "name", "city", "firma|company" });

        String text = "anna is working at IBM in munich";

        XMLTrainingData trainingData = new TrainingDataBuilder(text, doc, items, locals).build();
        Assert.assertEquals(XMLTrainingData.TRAININGDATA_QUALITY_LEVEL_FULL, trainingData.getQuality());
    }

    /**
     * This test verifies the method computeTrainingDataQuality
     * <p>
     * partial match with item name mapping.
     * <p>
     * The workitem does not contain a value for Company
     * 
     */
    @Test
    public void testComputeTrainingDataQualityPartialMatchWithItemNameMapping() {

        ItemCollection doc = new ItemCollection();
        doc.replaceItemValue("dummy", "xxx");
        doc.replaceItemValue("name", "anna");
        doc.replaceItemValue("stadt", "munich");
        // no company - doc.replaceItemValue("company", "IBM");

        List<String> items = Arrays.asList(new String[] { "name", "stadt|city", "company" });

        String text = "anna is working at IBM in munich";

        XMLTrainingData trainingData = new TrainingDataBuilder(text, doc, items, locals).build();
        Assert.assertEquals(XMLTrainingData.TRAININGDATA_QUALITY_LEVEL_PARTIAL, trainingData.getQuality());
    }

    /**
     * This test verifies the method computeTrainingDataQuality
     * <p>
     * bad match with item name mapping
     * <p>
     * The traingData object does not contain an entity for Company
     * 
     */
    @Test
    public void testComputeTrainingDataQualityBadMatchWithItemNameMapping() {

        ItemCollection doc = new ItemCollection();
        doc.replaceItemValue("dummy", "xxx");
        doc.replaceItemValue("name", "anna");
        doc.replaceItemValue("stadt", "munich");
        doc.replaceItemValue("firma", "IBM");

        List<String> items = Arrays.asList(new String[] { "name", "stadt | city", "firma|company" });

        /* no company in traing data */
        String text = "anna is working at RedHat in munich";

        XMLTrainingData trainingData = new TrainingDataBuilder(text, doc, items, locals).build();
        Assert.assertEquals(XMLTrainingData.TRAININGDATA_QUALITY_LEVEL_BAD, trainingData.getQuality());
    }
}
