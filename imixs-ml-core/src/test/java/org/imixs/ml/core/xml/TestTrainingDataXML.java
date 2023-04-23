package org.imixs.ml.core.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

import org.imixs.ml.core.MLEntity;
import org.imixs.ml.training.TrainingDataBuilder;
import org.imixs.ml.xml.XMLTrainingCategory;
import org.imixs.ml.xml.XMLTrainingData;
import org.imixs.ml.xml.XMLTrainingEntity;
import org.imixs.workflow.ItemCollection;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test the jaxb xml structure of a XMLTrainingData object
 * 
 * @author rsoika
 */
public class TestTrainingDataXML {

    /**
     * write xml to file
     */
    @Test
    public void testConvertTrainingDataObject() {

        XMLTrainingData xmlObj = new XMLTrainingData();
        xmlObj.setText("Wayne Rooney is a famous football player!");
        XMLTrainingEntity xmlTe = new XMLTrainingEntity();
        xmlTe.setLabel("person");
        xmlTe.setStart(0);
        xmlTe.setStop(13);
        xmlTe.setValue("Wayne Rooney");
        xmlObj.getEntities().add(xmlTe);
        XMLTrainingCategory xmlCa = new XMLTrainingCategory();
        xmlCa.setEnclosed(true);
        xmlCa.setLabel("sports");
        xmlObj.getCategories().add(xmlCa);

        // now write back to file
        File file = null;
        try {

            file = new File("src/test/resources/trainingdata.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(XMLTrainingData.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            // output pretty printed
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            jaxbMarshaller.marshal(xmlObj, file);
            jaxbMarshaller.marshal(xmlObj, System.out);

        } catch (JAXBException e) {
            e.printStackTrace();
            Assert.fail();
        }
        Assert.assertNotNull(file);

    }

    /**
     * Test the build process
     */
    @Test
    public void testBuildTrainingDataObject() {

        String text = "Wayne Rooney is a famous football player!";

        List<Locale> locals = new ArrayList<Locale>();
        locals.add(new Locale("de", "DE"));

        List<MLEntity> mlEntities = new ArrayList<MLEntity>();
        mlEntities.add(new MLEntity("person", "text", "person", 255, true));

        ItemCollection workitem = new ItemCollection();
        workitem.setItemValue("person", "Wayne Rooney");

        TrainingDataBuilder builder = new TrainingDataBuilder(text, workitem, mlEntities, locals);

        XMLTrainingData trainingData = builder.build();

        Assert.assertNotNull(trainingData);

        Assert.assertEquals(XMLTrainingData.TRAININGDATA_QUALITY_GOOD, trainingData.getQuality());

        List<XMLTrainingEntity> entities = trainingData.getEntities();
        // we expect one entity
        Assert.assertEquals(1, entities.size());

        XMLTrainingEntity entity = entities.get(0);
        // test start stop position
        Assert.assertEquals(0, entity.getStart());
        Assert.assertEquals(12, entity.getStop());
    }

    /**
     * Test the build process with a text where the entity is listed more than once!
     */
    @Test
    public void testBuildTrainingDataObjectMultipleApperances() {

        String text = "Wayne Rooney is a famous football player! Wayne Rooney is a star in england.";

        List<Locale> locals = new ArrayList<Locale>();
        locals.add(new Locale("de", "DE"));

        List<MLEntity> mlEntities = new ArrayList<MLEntity>();
        mlEntities.add(new MLEntity("person", "text", "person", 255, true));

        ItemCollection workitem = new ItemCollection();
        workitem.setItemValue("person", "Wayne Rooney");

        TrainingDataBuilder builder = new TrainingDataBuilder(text, workitem, mlEntities, locals);

        XMLTrainingData trainingData = builder.build();

        Assert.assertNotNull(trainingData);

        Assert.assertEquals(XMLTrainingData.TRAININGDATA_QUALITY_GOOD, trainingData.getQuality());

        List<XMLTrainingEntity> entities = trainingData.getEntities();
        // we expect one entity
        Assert.assertEquals(2, entities.size());

        XMLTrainingEntity entity = entities.get(0);
        // test start stop position
        Assert.assertEquals(0, entity.getStart());
        Assert.assertEquals(12, entity.getStop());
    }

}
