package org.imixs.ml.core.xml;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.imixs.ml.xml.XMLTrainingCategory;
import org.imixs.ml.xml.XMLTrainingData;
import org.imixs.ml.xml.XMLTrainingEntity;
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
        
        XMLTrainingData xmlObj=new XMLTrainingData();
        xmlObj.setText("Wayne Rooney is a famous football player!");
        XMLTrainingEntity xmlTe=new XMLTrainingEntity();
        xmlTe.setLabel("person");
        xmlTe.setStart(0);
        xmlTe.setStop(13);
        xmlTe.setValue("Wayne Rooney");
        xmlObj.getEntities().add(xmlTe);
        XMLTrainingCategory xmlCa=new XMLTrainingCategory();
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

}
