package org.imixs.ml.core.xml;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.imixs.ml.xml.XMLAnalyseData;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test the jaxb xml structure of a XMLAnalyseData object
 * 
 * @author rsoika
 */
public class TestAnalyseDataXML {

    /**
     * write xml to file
     */
    @Test
    public void testXMLOutput() {

        XMLAnalyseData xmlObj = new XMLAnalyseData("some test with <some> xml tags...");

        // now write back to file
        File file = null;
        try {

            file = new File("src/test/resources/analysedata.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(XMLAnalyseData.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            // XML output pretty printed
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
