package org.imixs.ml.integration;

import java.io.StringWriter;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.imixs.ml.data.xml.XMLTrainingData;
import org.imixs.ml.data.xml.XMLTrainingEntity;
import org.imixs.ml.service.TrainingDataBuilder;
import org.imixs.ml.service.TrainingService;
import org.imixs.workflow.ItemCollection;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

/**
 * TestMLIntegrationTest is used to test againsed the python imixs-ml service
 * module with test data based on ItemCollection objects.
 * 
 * 
 * @author rsoika
 */
public class TestMLIntegrationTest {

    private static Logger logger = Logger.getLogger(TestMLIntegrationTest.class.getName());

    static String ML_SERVICE_API = "http://localhost:8000/docs";
    
    static String SERVICE_ENDPOINT="http://localhost:8000/trainingdatasingle/";
    //static String SERVICE_ENDPOINT="http://localhost:8000/trainingdata/";

    private IntegrationTest integrationTest = new IntegrationTest(ML_SERVICE_API);

    private TrainingService trainingService = null;
    String[] items = null;

    /**
     * Training Data
     * 
     * <pre>
     *     [
              ('what is the price of 11,00', {'entities': [(21, 26, 'iban')]}), 
              ('what is the price of 12,50', {'entities': [(21, 26, 'iban')]}), 
              ('what is the price of 20000,00', {'entities': [(21, 29, 'iban')]}), 
              ('what is the price of 10120,00', {'entities': [(21, 29, 'iban')]}), 
              ('what is the price of 333,00', {'entities': [(21, 27, 'iban')]}), 
              ('what is the price of 0,50', {'entities': [(21, 25, 'iban')]}), 
              ('what is the price of 10,40', {'entities': [(21, 26, 'iban')]}), 
              ('what is the price of 0,49', {'entities': [(21, 25, 'iban')]}), 
              ('what is the price of 0,10', {'entities': [(21, 25, 'iban')]}), 
              ('what is the price of 9,99', {'entities': [(21, 25, 'iban')]}), 
              ('what is the price of 155,55', {'entities': [(21, 27, 'iban')]}), 
              ('what is the price of 200,00', {'entities': [(21, 27, 'iban')]}), 
              ('what is the price of 90120,00', {'entities': [(21, 29, 'iban')]}), 
              ('what is the price of 50000,00', {'entities': [(21, 29, 'iban')]}), 
              ('what is the price of 1600,00', {'entities': [(21, 28, 'iban')]}), 
              ('what is the price of 1500,00', {'entities': [(21, 28, 'iban')]}), 
              ('what is the price of 70000,00', {'entities': [(21, 29, 'iban')]}), 
              ('what is the price of 30,00', {'entities': [(21, 26, 'iban')]}), 
              ('what is the price of 20,00', {'entities': [(21, 26, 'iban')]}), 
              ('what is the price of 11,50', {'entities': [(21, 26, 'iban')]})] 
            ]
     * </pre>
     * 
     **/

    /**
     * The setup method
     * 
     */
    @Before
    public void setup() {
        // Assumptions for integration tests
        org.junit.Assume.assumeTrue(integrationTest.connected());

        trainingService = new TrainingService();
      
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

        String[] items = { "org", "gpe", "money" };

        String text = "Apple is looking at buying U.K. startup for $1 billion";

        XMLTrainingData trainingData = new TrainingDataBuilder(text, doc, items).build();

        trainingService.printXML(trainingData);

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
     * 
     * 
     */
    @Test
    public void testCreateSingleXMLTrainingEntity() {
        items = new String[] { "iban","price" };
        String content = "what is the price of 11,00? with an IBAN of XX08 1101 0030 0000 2222 02.";
        ItemCollection doc = new ItemCollection();
        doc.setItemValue("price", "11,00");
        doc.setItemValue("iban", "XX08 1101 0030 0000 2222 02");

        // build a trainingData set
        XMLTrainingData trainingData = new TrainingDataBuilder(content, doc, items).build();
        Assert.assertEquals(2, trainingData.getEntities().size());
        Assert.assertEquals(21, trainingData.getEntities().get(1).getStart());
        Assert.assertEquals(26, trainingData.getEntities().get(1).getStop());

        trainingService.printXML(trainingData);
        trainingService.postTrainingData(trainingData,SERVICE_ENDPOINT);
    }

    /**
     * Full integration test using the following training data
     * 
     * * Training Data
     * 
     * <pre>
     *     [
              ('what is the price of 11,00', {'entities': [(21, 26, 'iban')]}), 
              ('what is the price of 12,50', {'entities': [(21, 26, 'iban')]}), 
              ('what is the price of 20000,00', {'entities': [(21, 29, 'iban')]}), 
              ('what is the price of 10120,00', {'entities': [(21, 29, 'iban')]}), 
              ('what is the price of 333,00', {'entities': [(21, 27, 'iban')]}), 
              ('what is the price of 0,50', {'entities': [(21, 25, 'iban')]}), 
              ('what is the price of 10,40', {'entities': [(21, 26, 'iban')]}), 
              ('what is the price of 0,49', {'entities': [(21, 25, 'iban')]}), 
              ('what is the price of 0,10', {'entities': [(21, 25, 'iban')]}), 
              ('what is the price of 9,99', {'entities': [(21, 25, 'iban')]}), 
              ('what is the price of 155,55', {'entities': [(21, 27, 'iban')]}), 
              ('what is the price of 200,00', {'entities': [(21, 27, 'iban')]}), 
              ('what is the price of 90120,00', {'entities': [(21, 29, 'iban')]}), 
              ('what is the price of 50000,00', {'entities': [(21, 29, 'iban')]}), 
              ('what is the price of 1600,00', {'entities': [(21, 28, 'iban')]}), 
              ('what is the price of 1500,00', {'entities': [(21, 28, 'iban')]}), 
              ('what is the price of 70000,00', {'entities': [(21, 29, 'iban')]}), 
              ('what is the price of 30,00', {'entities': [(21, 26, 'iban')]}), 
              ('what is the price of 20,00', {'entities': [(21, 26, 'iban')]}), 
              ('what is the price of 11,50', {'entities': [(21, 26, 'iban')]})] 
            ]
     * </pre>
     * 
     */
    @Test
    public void testFullTrainingDataSet() {
        items = new String[] { "iban","price" };
        String content = null;
        ItemCollection doc = null;
        XMLTrainingData trainingData = null;

        for (int i = 0; i < 10; i++) {
            content = "what is the price of 11,00? with an IBAN of XX08 1101 0030 0000 2222 02.";
            doc = new ItemCollection();
            doc.setItemValue("price", "11,00");
            doc.setItemValue("iban", "XX08 1101 0030 0000 2222 02");
            trainingData = new TrainingDataBuilder(content, doc, items).build();
            trainingService.postTrainingData(trainingData, SERVICE_ENDPOINT);

            content = "what is the price of 12,50? with an IBAN of XX08 1101 0030 0000 3333 02.";
            doc = new ItemCollection();
            doc.setItemValue("price", "12,50");
            doc.setItemValue("iban", "XX08 1101 0030 0000 3333 02");
            trainingData = new TrainingDataBuilder(content, doc, items).build();
            trainingService.postTrainingData(trainingData, SERVICE_ENDPOINT);
            
            content = "what is the price of 20000,00? with an IBAN of XX08 1101 0030 1111 2222 02.";
            doc = new ItemCollection();
            doc.setItemValue("price", "20000,00");
            doc.setItemValue("iban", "XX08 1101 0030 1111 2222 02");
            trainingData = new TrainingDataBuilder(content, doc, items).build();
            trainingService.postTrainingData(trainingData, SERVICE_ENDPOINT);
            
            content = "what is the price of 10120,00? with an IBAN of XX08 1101 0030 0000 2222 02.";
            doc = new ItemCollection();
            doc.setItemValue("price", "10120,00");
            doc.setItemValue("iban", "XX08 1101 0030 0000 2222 02");
            trainingData = new TrainingDataBuilder(content, doc, items).build();
            trainingService.postTrainingData(trainingData, SERVICE_ENDPOINT);
            
            content = "what is the price of 333,00? with an IBAN of XX08 1101 0030 0000 2222 02.";
            doc = new ItemCollection();
            doc.setItemValue("price", "333,00");
            doc.setItemValue("iban", "XX08 1101 0030 0000 2222 02");
            trainingData = new TrainingDataBuilder(content, doc, items).build();
            trainingService.postTrainingData(trainingData, SERVICE_ENDPOINT);
            
            content = "what is the price of 0,50? with an IBAN of aa08 1101 0030 0000 2222 02.";
            doc = new ItemCollection();
            doc.setItemValue("price", "0,50");
            doc.setItemValue("iban", "aa08 1101 0030 0000 2222 02");
            trainingData = new TrainingDataBuilder(content, doc, items).build();
            trainingService.postTrainingData(trainingData, SERVICE_ENDPOINT);
            
            content = "what is the price of 10,40";
            doc = new ItemCollection();
            doc.setItemValue("price", "10,40");
            trainingData = new TrainingDataBuilder(content, doc, items).build();
            trainingService.postTrainingData(trainingData, SERVICE_ENDPOINT);
            
            
            content = "what is the price of 0,49";
            doc = new ItemCollection();
            doc.setItemValue("price", "0,49");
            trainingData = new TrainingDataBuilder(content, doc, items).build();
            trainingService.postTrainingData(trainingData, SERVICE_ENDPOINT);
          
            
            content = "what is the price of 0,10";
            doc = new ItemCollection();
            doc.setItemValue("price", "0,10");
            trainingData = new TrainingDataBuilder(content, doc, items).build();
            trainingService.postTrainingData(trainingData, SERVICE_ENDPOINT);
          
            content = "what is the price of 9,99";
            doc = new ItemCollection();
            doc.setItemValue("price", "9,99");
            trainingData = new TrainingDataBuilder(content, doc, items).build();
            trainingService.postTrainingData(trainingData, SERVICE_ENDPOINT);
          
          
            content = "what is the price of 155,00";
            doc = new ItemCollection();
            doc.setItemValue("price", "155,00");
            trainingData = new TrainingDataBuilder(content, doc, items).build();
            trainingService.postTrainingData(trainingData, SERVICE_ENDPOINT);
          
            
            content = "what is the price of 200,00";
            doc = new ItemCollection();
            doc.setItemValue("price", "200,00");
            trainingData = new TrainingDataBuilder(content, doc, items).build();
            trainingService.postTrainingData(trainingData, SERVICE_ENDPOINT);
          
            content = "what is the price of 90120,00";
            doc = new ItemCollection();
            doc.setItemValue("price", "90120,00");
            trainingData = new TrainingDataBuilder(content, doc, items).build();
            trainingService.postTrainingData(trainingData, SERVICE_ENDPOINT);
          
            content = "what is the price of 50000,00";
            doc = new ItemCollection();
            doc.setItemValue("price", "50000,00");
            trainingData = new TrainingDataBuilder(content, doc, items).build();
            trainingService.postTrainingData(trainingData, SERVICE_ENDPOINT);
          
            
            content = "what is the price of 1600,00";
            doc = new ItemCollection();
            doc.setItemValue("price", "1600,00");
            trainingData = new TrainingDataBuilder(content, doc, items).build();
            trainingService.postTrainingData(trainingData, SERVICE_ENDPOINT);
          
          
            content = "what is the price of 1500,00";
            doc = new ItemCollection();
            doc.setItemValue("price", "1500,00");
            trainingData = new TrainingDataBuilder(content, doc, items).build();
            trainingService.postTrainingData(trainingData, SERVICE_ENDPOINT);
          
          
            content = "what is the price of 70000,00";
            doc = new ItemCollection();
            doc.setItemValue("price", "70000,00");
            trainingData = new TrainingDataBuilder(content, doc, items).build();
            trainingService.postTrainingData(trainingData, SERVICE_ENDPOINT);
          
          
            content = "what is the price of 30,00";
            doc = new ItemCollection();
            doc.setItemValue("price", "30,00");
            trainingData = new TrainingDataBuilder(content, doc, items).build();
            trainingService.postTrainingData(trainingData, SERVICE_ENDPOINT);
          
            content = "what is the price of 20,00";
            doc = new ItemCollection();
            doc.setItemValue("price", "20,00");
            trainingData = new TrainingDataBuilder(content, doc, items).build();
            trainingService.postTrainingData(trainingData, SERVICE_ENDPOINT);
            
            
            content = "what is the price of 11,00";
            doc = new ItemCollection();
            doc.setItemValue("price", "11,00");
            trainingData = new TrainingDataBuilder(content, doc, items).build();
            trainingService.postTrainingData(trainingData, SERVICE_ENDPOINT);
            
            
           // break;
        }
    }
    
    
    
    
    
    /**
     * This test verifies the computes of start stop positions in a text with special characters
     * 
     * </pre>
     */
    @Test
    public void testBuilderSpecialCharacters() {

        ItemCollection doc = new ItemCollection();
        doc.replaceItemValue("org", "Kéin");

        String[] items = { "org","gpe","money" };

        String text = "Postfach 51 10 26 - 50946 Kéin  Krieger GmbH Möbelhaus";

        XMLTrainingData trainingData = new TrainingDataBuilder(text, doc, items).build();

       

        List<XMLTrainingEntity> trainingEntities = trainingData.getEntities();
        Assert.assertEquals(1, trainingEntities.size());

        Assert.assertEquals(26, trainingEntities.get(0).getStart());
        Assert.assertEquals(30, trainingEntities.get(0).getStop());

        trainingService.printXML(trainingData);
        trainingService.postTrainingData(trainingData, SERVICE_ENDPOINT);
    }

}
