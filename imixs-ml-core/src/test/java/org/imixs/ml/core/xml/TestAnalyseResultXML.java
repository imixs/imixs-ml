package org.imixs.ml.core.xml;

import java.io.File;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

import org.imixs.ml.xml.XMLAnalyseCategory;
import org.imixs.ml.xml.XMLAnalyseEntity;
import org.imixs.ml.xml.XMLAnalyseResult;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.exceptions.PluginException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

/**
 * Test the jaxb xml structure of a XMLAnalyseResult object
 * 
 * @author rsoika
 */
public class TestAnalyseResultXML {
    XMLAnalyseResult xmlObj=null;
    
    
    /**
     * init a test object
     * 
     * @throws ModelException
     */
    @Before
    public void setup() throws PluginException, ModelException {

        xmlObj = new XMLAnalyseResult();
        XMLAnalyseEntity xmlentity= new XMLAnalyseEntity("person","Wayne Rooney");
        XMLAnalyseCategory xmlcategory= new XMLAnalyseCategory("sports",0.999);

        xmlObj.getCategories().add(xmlcategory);
        xmlObj.getEntities().add(xmlentity);
    }
    
    /**
     * write XMLAnalyseText as xml to file
     */
    @Test
    public void testXMLOutput() {
  
        // now write back to file
        File file = null;
        try {

            file = new File("src/test/resources/analyseresult.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(XMLAnalyseResult.class);
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

    /**
     * write XMLAnalyseText as json to file
     * 
     * 
     * TODO does not run with Jakarta ee 10 - because of jackson-module-jaxb-annotations
     */
    @Test
    @Ignore
    public void testJSONOutput() {

        try {

            ObjectMapper mapper = new ObjectMapper();
            AnnotationIntrospector introspector = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
            mapper.setAnnotationIntrospector(introspector);
            String result;
            result = mapper.writeValueAsString(xmlObj);
            
            Assert.assertNotNull(result);
            System.out.println(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
       

    }

    
}
