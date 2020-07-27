package org.imixs.ml.service;

import java.util.ArrayList;
import java.util.List;

import org.imixs.ml.xml.XMLTrainingData;
import org.imixs.ml.xml.XMLTrainingEntity;
import org.imixs.workflow.ItemCollection;
import org.junit.Test;

import junit.framework.Assert;

/**
 * Test class to test the TrainingService
 * 
 * @author rsoika
 */
public class TestTrainingService {

    /**
     * This test verifies the method computeTrainingDataQuality
     * <p>
     * Full match without any item name mapping
     * <p>
     * All items are contained in the workitem and in the training data
     */
    @Test
    public void testComputeTrainingDataQualityFullMatch() {

        TrainingService trainingService = new TrainingService();

        // build a full match with no item name mapping

        ItemCollection workitem = new ItemCollection();
        workitem.setItemValue("name", "anna").setItemValue("city", "munich").setItemValue("company", "IBM");

        List<String> trainingItemNames = new ArrayList<String>();
        trainingItemNames.add("name");
        trainingItemNames.add("city");
        trainingItemNames.add("company");

        XMLTrainingData trainingData = new XMLTrainingData();
        List<XMLTrainingEntity> entities = new ArrayList<XMLTrainingEntity>();
        XMLTrainingEntity trainingEntity = new XMLTrainingEntity();
        trainingEntity.setLabel("name");
        entities.add(trainingEntity);
        trainingEntity = new XMLTrainingEntity();
        trainingEntity.setLabel("city");
        entities.add(trainingEntity);
        trainingEntity = new XMLTrainingEntity();
        trainingEntity.setLabel("company");
        entities.add(trainingEntity);

        trainingData.setEntities(entities);

        int quality = trainingService.computeTrainingDataQuality(workitem, trainingItemNames, trainingData);

        Assert.assertEquals(TrainingService.TRAININGDATA_QUALITY_LEVEL_FULL, quality);

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

        TrainingService trainingService = new TrainingService();

        // build a full match with no item name mapping

        ItemCollection workitem = new ItemCollection();
        workitem.setItemValue("name", "anna").setItemValue("city", "munich");
        // no company! .setItemValue("company", "IBM");

        List<String> trainingItemNames = new ArrayList<String>();
        trainingItemNames.add("name");
        trainingItemNames.add("city");
        trainingItemNames.add("company");

        XMLTrainingData trainingData = new XMLTrainingData();
        List<XMLTrainingEntity> entities = new ArrayList<XMLTrainingEntity>();
        XMLTrainingEntity trainingEntity = new XMLTrainingEntity();
        trainingEntity.setLabel("name");
        entities.add(trainingEntity);
        trainingEntity = new XMLTrainingEntity();
        trainingEntity.setLabel("city");
        entities.add(trainingEntity);
        trainingEntity = new XMLTrainingEntity();
        trainingEntity.setLabel("company");
        entities.add(trainingEntity);

        trainingData.setEntities(entities);

        int quality = trainingService.computeTrainingDataQuality(workitem, trainingItemNames, trainingData);

        Assert.assertEquals(TrainingService.TRAININGDATA_QUALITY_LEVEL_PARTIAL, quality);

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

        TrainingService trainingService = new TrainingService();

        // build a full match with no item name mapping

        ItemCollection workitem = new ItemCollection();
        workitem.setItemValue("name", "anna").setItemValue("city", "munich").setItemValue("company", "IBM");

        List<String> trainingItemNames = new ArrayList<String>();
        trainingItemNames.add("name");
        trainingItemNames.add("city");
        trainingItemNames.add("company");

        XMLTrainingData trainingData = new XMLTrainingData();
        List<XMLTrainingEntity> entities = new ArrayList<XMLTrainingEntity>();
        XMLTrainingEntity trainingEntity = new XMLTrainingEntity();
        trainingEntity.setLabel("name");
        entities.add(trainingEntity);
        trainingEntity = new XMLTrainingEntity();
        trainingEntity.setLabel("city");
        entities.add(trainingEntity);

        /* no company in traing data */
        // trainingEntity = new XMLTrainingEntity();
        // trainingEntity.setLabel("company");
        // entities.add(trainingEntity);

        trainingData.setEntities(entities);

        int quality = trainingService.computeTrainingDataQuality(workitem, trainingItemNames, trainingData);

        Assert.assertEquals(TrainingService.TRAININGDATA_QUALITY_LEVEL_BAD, quality);

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

        TrainingService trainingService = new TrainingService();

        // build a full match with no item name mapping

        ItemCollection workitem = new ItemCollection();
        workitem.setItemValue("name", "anna").setItemValue("city", "munich").setItemValue("firma", "IBM");

        List<String> trainingItemNames = new ArrayList<String>();
        trainingItemNames.add("name");
        trainingItemNames.add("city");
        trainingItemNames.add("firma|company");

        XMLTrainingData trainingData = new XMLTrainingData();
        List<XMLTrainingEntity> entities = new ArrayList<XMLTrainingEntity>();
        XMLTrainingEntity trainingEntity = new XMLTrainingEntity();
        trainingEntity.setLabel("name");
        entities.add(trainingEntity);
        trainingEntity = new XMLTrainingEntity();
        trainingEntity.setLabel("city");
        entities.add(trainingEntity);
        trainingEntity = new XMLTrainingEntity();
        trainingEntity.setLabel("company");
        entities.add(trainingEntity);

        trainingData.setEntities(entities);

        int quality = trainingService.computeTrainingDataQuality(workitem, trainingItemNames, trainingData);

        Assert.assertEquals(TrainingService.TRAININGDATA_QUALITY_LEVEL_FULL, quality);

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

        TrainingService trainingService = new TrainingService();

        // build a full match with no item name mapping

        ItemCollection workitem = new ItemCollection();
        workitem.setItemValue("name", "anna").setItemValue("stadt", "munich");
        // no company! .setItemValue("company", "IBM");

        List<String> trainingItemNames = new ArrayList<String>();
        trainingItemNames.add("name");
        trainingItemNames.add("stadt|city");
        trainingItemNames.add("company");

        XMLTrainingData trainingData = new XMLTrainingData();
        List<XMLTrainingEntity> entities = new ArrayList<XMLTrainingEntity>();
        XMLTrainingEntity trainingEntity = new XMLTrainingEntity();
        trainingEntity.setLabel("name");
        entities.add(trainingEntity);
        trainingEntity = new XMLTrainingEntity();
        trainingEntity.setLabel("city");
        entities.add(trainingEntity);
        trainingEntity = new XMLTrainingEntity();
        trainingEntity.setLabel("company");
        entities.add(trainingEntity);

        trainingData.setEntities(entities);

        int quality = trainingService.computeTrainingDataQuality(workitem, trainingItemNames, trainingData);

        Assert.assertEquals(TrainingService.TRAININGDATA_QUALITY_LEVEL_PARTIAL, quality);

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

        TrainingService trainingService = new TrainingService();

        // build a full match with no item name mapping

        ItemCollection workitem = new ItemCollection();
        workitem.setItemValue("name", "anna").setItemValue("stadt", "munich").setItemValue("firma", "IBM");

        List<String> trainingItemNames = new ArrayList<String>();
        trainingItemNames.add("name");
        trainingItemNames.add("stadt | city");
        trainingItemNames.add("firma|company");

        XMLTrainingData trainingData = new XMLTrainingData();
        List<XMLTrainingEntity> entities = new ArrayList<XMLTrainingEntity>();
        XMLTrainingEntity trainingEntity = new XMLTrainingEntity();
        trainingEntity.setLabel("name");
        entities.add(trainingEntity);
        trainingEntity = new XMLTrainingEntity();
        trainingEntity.setLabel("city");
        entities.add(trainingEntity);

        /* no company in traing data */
        // trainingEntity = new XMLTrainingEntity();
        // trainingEntity.setLabel("company");
        // entities.add(trainingEntity);

        trainingData.setEntities(entities);

        int quality = trainingService.computeTrainingDataQuality(workitem, trainingItemNames, trainingData);

        Assert.assertEquals(TrainingService.TRAININGDATA_QUALITY_LEVEL_BAD, quality);

    }
}
