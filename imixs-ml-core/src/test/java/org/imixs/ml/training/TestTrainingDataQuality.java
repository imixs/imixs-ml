package org.imixs.ml.training;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.imixs.ml.core.MLEntity;
import org.imixs.ml.xml.XMLTrainingData;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.exceptions.PluginException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class to test the computation of the TraingData quality level by
 * simulating different szenarios.
 * 
 * @author rsoika
 */
public class TestTrainingDataQuality {

    private List<Locale> locals = null;

    @Before
    public void setup() throws PluginException {
        locals = new ArrayList<Locale>();
        locals.add(Locale.UK);
        locals.add(Locale.GERMANY);
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

        List<MLEntity> mlEntities = new ArrayList<MLEntity>();
        mlEntities.add(new MLEntity("name", null, null, 0, true));
        mlEntities.add(new MLEntity("city", null, null, 0, true));
        mlEntities.add(new MLEntity("company", null, null, 0, true));

        String text = "anna is working at IBM in munich";

        XMLTrainingData trainingData = new TrainingDataBuilder(text, doc, mlEntities, locals).build();
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

        List<MLEntity> mlEntities = new ArrayList<MLEntity>();
        mlEntities.add(new MLEntity("name", null, null, 0, true));
        mlEntities.add(new MLEntity("city", null, null, 0, true));
        mlEntities.add(new MLEntity("company", null, null, 0, true));

        String text = "anna is working at IBM in munich";

        XMLTrainingData trainingData = new TrainingDataBuilder(text, doc, mlEntities, locals).build();
        Assert.assertEquals(XMLTrainingData.TRAININGDATA_QUALITY_LEVEL_PARTIAL, trainingData.getQuality());

        // ##########################################################
        // second variant also the text does not contain a company
        // ##########################################################
        doc = new ItemCollection();
        doc.replaceItemValue("dummy", "xxx");
        doc.replaceItemValue("name", "anna");
        doc.replaceItemValue("city", "munich");
        // no company - doc.replaceItemValue("company", "IBM");

        text = "anna is working in munich";

        trainingData = new TrainingDataBuilder(text, doc, mlEntities, locals).build();
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

        List<MLEntity> mlEntities = new ArrayList<MLEntity>();
        mlEntities.add(new MLEntity("name", null, null, 0, true));
        mlEntities.add(new MLEntity("city", null, null, 0, true));
        mlEntities.add(new MLEntity("company", null, null, 0, true));

        /* no company in traing data */
        String text = "anna is working at RedHat in munich";

        XMLTrainingData trainingData = new TrainingDataBuilder(text, doc, mlEntities, locals).build();
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

        List<MLEntity> mlEntities = new ArrayList<MLEntity>();
        mlEntities.add(new MLEntity("name", null, null, 0, true));
        mlEntities.add(new MLEntity("city", null, null, 0, true));
        mlEntities.add(new MLEntity("firma|company", null, null, 0, true));

        String text = "anna is working at IBM in munich";

        XMLTrainingData trainingData = new TrainingDataBuilder(text, doc, mlEntities, locals).build();
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

        List<MLEntity> mlEntities = new ArrayList<MLEntity>();
        mlEntities.add(new MLEntity("name", null, null, 0, true));
        mlEntities.add(new MLEntity("stadt|city", null, null, 0, true));
        mlEntities.add(new MLEntity("company", null, null, 0, true));

        String text = "anna is working at IBM in munich";

        XMLTrainingData trainingData = new TrainingDataBuilder(text, doc, mlEntities, locals).build();
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

        List<MLEntity> mlEntities = new ArrayList<MLEntity>();
        mlEntities.add(new MLEntity("name", null, null, 0, true));
        mlEntities.add(new MLEntity("stadt | city", null, null, 0, true));
        mlEntities.add(new MLEntity("firma|company", null, null, 0, true));

        /* no company in traing data */
        String text = "anna is working at RedHat in munich";

        XMLTrainingData trainingData = new TrainingDataBuilder(text, doc, mlEntities, locals).build();
        Assert.assertEquals(XMLTrainingData.TRAININGDATA_QUALITY_LEVEL_BAD, trainingData.getQuality());
    }
}
