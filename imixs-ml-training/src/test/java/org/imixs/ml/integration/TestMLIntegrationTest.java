package org.imixs.ml.integration;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.imixs.ml.core.MLClient;
import org.imixs.ml.service.TrainingService;
import org.imixs.ml.training.TrainingDataBuilder;
import org.imixs.ml.xml.XMLTrainingData;
import org.imixs.ml.xml.XMLTrainingEntity;
import org.imixs.workflow.ItemCollection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

    // static String SERVICE_ENDPOINT="http://localhost:8000/trainingdatasingle/";
    static String SERVICE_ENDPOINT = "http://localhost:8000/trainingdata/";

    private IntegrationTest integrationTest = new IntegrationTest(ML_SERVICE_API);
    private  List<Locale> locals=null;
    private TrainingService trainingService = null;
    List<String> items = null;
    MLClient mlClient = null;

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
        logger.info("setup...");
        // Assumptions for integration tests
        org.junit.Assume.assumeTrue(integrationTest.connected());

        trainingService = new TrainingService();

        mlClient = new MLClient();
        locals=new ArrayList<Locale>();
        locals.add(Locale.UK);
        locals.add(Locale.GERMAN);
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
        items = Arrays.asList( new String[] { "project", "city" });
        ItemCollection doc = new ItemCollection();
        doc.replaceItemValue("project", "Imixs Workflow");
        doc.replaceItemValue("city", "Munich");

        String text = "Imixs Workflow in an open source project organized from Munich.";

        XMLTrainingData trainingData = new TrainingDataBuilder(text, doc, items,locals).build();

        trainingService.printXML(trainingData);

        List<XMLTrainingEntity> trainingEntities = trainingData.getEntities();
        Assert.assertEquals(2, trainingEntities.size());

        Assert.assertEquals(0, trainingEntities.get(0).getStart());
        Assert.assertEquals(14, trainingEntities.get(0).getStop());

        Assert.assertEquals(56, trainingEntities.get(1).getStart());
        Assert.assertEquals(62, trainingEntities.get(1).getStop());

    }

    /**
     * 
     * 
     */
    @Test
    public void testCreateSingleXMLTrainingEntity() {
        items = Arrays.asList( new String[]  { "iban", "price" });
        String content = "what is the price of 11,00? with an IBAN of XX08 1101 0030 0000 2222 02.";
        ItemCollection doc = new ItemCollection();
        doc.setItemValue("price", "11,00");
        doc.setItemValue("iban", "XX08 1101 0030 0000 2222 02");

        // build a trainingData set
        XMLTrainingData trainingData = new TrainingDataBuilder(content, doc, items,locals).build();
        Assert.assertEquals(2, trainingData.getEntities().size());
        Assert.assertEquals(21, trainingData.getEntities().get(1).getStart());
        Assert.assertEquals(26, trainingData.getEntities().get(1).getStop());

        trainingService.printXML(trainingData);
        mlClient.postTrainingData(trainingData, SERVICE_ENDPOINT);

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

        items = Arrays.asList( new String[]  { "iban", "price" });
        String content = null;
        ItemCollection doc = null;
        XMLTrainingData trainingData = null;

        content = "what is the price of 11,00? with an IBAN of XX08 1101 0030 0000 2222 02.";
        doc = new ItemCollection();
        doc.setItemValue("price", "11,00");
        doc.setItemValue("iban", "XX08 1101 0030 0000 2222 02");
        trainingData = new TrainingDataBuilder(content, doc, items,locals).build();
        mlClient.postTrainingData(trainingData, SERVICE_ENDPOINT);

        content = "what is the price of 12,50? with an IBAN of XX08 1101 0030 0000 3333 02.";
        doc = new ItemCollection();
        doc.setItemValue("price", "12,50");
        doc.setItemValue("iban", "XX08 1101 0030 0000 3333 02");
        trainingData = new TrainingDataBuilder(content, doc, items,locals).build();
        mlClient.postTrainingData(trainingData, SERVICE_ENDPOINT);

        content = "what is the price of 20000,00? with an IBAN of XX08 1101 0030 1111 2222 02.";
        doc = new ItemCollection();
        doc.setItemValue("price", "20000,00");
        doc.setItemValue("iban", "XX08 1101 0030 1111 2222 02");
        trainingData = new TrainingDataBuilder(content, doc, items,locals).build();
        mlClient.postTrainingData(trainingData, SERVICE_ENDPOINT);

        content = "what is the price of 10120,00? with an IBAN of XX08 1101 0030 0000 2222 02.";
        doc = new ItemCollection();
        doc.setItemValue("price", "10120,00");
        doc.setItemValue("iban", "XX08 1101 0030 0000 2222 02");
        trainingData = new TrainingDataBuilder(content, doc, items,locals).build();
        mlClient.postTrainingData(trainingData, SERVICE_ENDPOINT);

        content = "what is the price of 333,00? with an IBAN of XX08 1101 0030 0000 2222 02.";
        doc = new ItemCollection();
        doc.setItemValue("price", "333,00");
        doc.setItemValue("iban", "XX08 1101 0030 0000 2222 02");
        trainingData = new TrainingDataBuilder(content, doc, items,locals).build();
        mlClient.postTrainingData(trainingData, SERVICE_ENDPOINT);

        content = "what is the price of 0,50? with an IBAN of aa08 1101 0030 0000 2222 02.";
        doc = new ItemCollection();
        doc.setItemValue("price", "0,50");
        doc.setItemValue("iban", "aa08 1101 0030 0000 2222 02");
        trainingData = new TrainingDataBuilder(content, doc, items,locals).build();
        mlClient.postTrainingData(trainingData, SERVICE_ENDPOINT);

        content = "what is the price of 10,40";
        doc = new ItemCollection();
        doc.setItemValue("price", "10,40");
        trainingData = new TrainingDataBuilder(content, doc, items,locals).build();
        mlClient.postTrainingData(trainingData, SERVICE_ENDPOINT);

        content = "what is the price of 0,49";
        doc = new ItemCollection();
        doc.setItemValue("price", "0,49");
        trainingData = new TrainingDataBuilder(content, doc, items,locals).build();
        mlClient.postTrainingData(trainingData, SERVICE_ENDPOINT);

        content = "what is the price of 0,10";
        doc = new ItemCollection();
        doc.setItemValue("price", "0,10");
        trainingData = new TrainingDataBuilder(content, doc, items,locals).build();
        mlClient.postTrainingData(trainingData, SERVICE_ENDPOINT);

        content = "what is the price of 9,99";
        doc = new ItemCollection();
        doc.setItemValue("price", "9,99");
        trainingData = new TrainingDataBuilder(content, doc, items,locals).build();
        mlClient.postTrainingData(trainingData, SERVICE_ENDPOINT);

        content = "what is the price of 155,00";
        doc = new ItemCollection();
        doc.setItemValue("price", "155,00");
        trainingData = new TrainingDataBuilder(content, doc, items,locals).build();
        mlClient.postTrainingData(trainingData, SERVICE_ENDPOINT);

        content = "what is the price of 200,00";
        doc = new ItemCollection();
        doc.setItemValue("price", "200,00");
        trainingData = new TrainingDataBuilder(content, doc, items,locals).build();
        mlClient.postTrainingData(trainingData, SERVICE_ENDPOINT);

        content = "what is the price of 90120,00";
        doc = new ItemCollection();
        doc.setItemValue("price", "90120,00");
        trainingData = new TrainingDataBuilder(content, doc, items,locals).build();
        mlClient.postTrainingData(trainingData, SERVICE_ENDPOINT);

        content = "what is the price of 50000,00";
        doc = new ItemCollection();
        doc.setItemValue("price", "50000,00");
        trainingData = new TrainingDataBuilder(content, doc, items,locals).build();
        mlClient.postTrainingData(trainingData, SERVICE_ENDPOINT);

        content = "what is the price of 1600,00";
        doc = new ItemCollection();
        doc.setItemValue("price", "1600,00");
        trainingData = new TrainingDataBuilder(content, doc, items,locals).build();
        mlClient.postTrainingData(trainingData, SERVICE_ENDPOINT);

        content = "what is the price of 1500,00";
        doc = new ItemCollection();
        doc.setItemValue("price", "1500,00");
        trainingData = new TrainingDataBuilder(content, doc, items,locals).build();
        mlClient.postTrainingData(trainingData, SERVICE_ENDPOINT);

        content = "what is the price of 70000,00";
        doc = new ItemCollection();
        doc.setItemValue("price", "70000,00");
        trainingData = new TrainingDataBuilder(content, doc, items,locals).build();
        mlClient.postTrainingData(trainingData, SERVICE_ENDPOINT);

        content = "what is the price of 30,00";
        doc = new ItemCollection();
        doc.setItemValue("price", "30,00");
        trainingData = new TrainingDataBuilder(content, doc, items,locals).build();
        mlClient.postTrainingData(trainingData, SERVICE_ENDPOINT);

        content = "what is the price of 20,00";
        doc = new ItemCollection();
        doc.setItemValue("price", "20,00");
        trainingData = new TrainingDataBuilder(content, doc, items,locals).build();
        mlClient.postTrainingData(trainingData, SERVICE_ENDPOINT);

        content = "what is the price of 11,00";
        doc = new ItemCollection();
        doc.setItemValue("price", "11,00");
        trainingData = new TrainingDataBuilder(content, doc, items,locals).build();
        mlClient.postTrainingData(trainingData, SERVICE_ENDPOINT);

    }

    /**
     * This test verifies the computes of start stop positions in a text with
     * special characters
     * 
     * </pre>
     */
    @Test
    public void testBuilderSpecialCharacters() {

        ItemCollection doc = new ItemCollection();
        doc.replaceItemValue("org", "Kéin");
  
        items = Arrays.asList( new String[]  { "org", "gpe", "money" });

        String text = "Postfach 51 10 26 - 50946 Kéin  Krouser GmbH Möbelhaus";

        XMLTrainingData trainingData = new TrainingDataBuilder(text, doc, items,locals).build();

        List<XMLTrainingEntity> trainingEntities = trainingData.getEntities();
        Assert.assertEquals(1, trainingEntities.size());

        Assert.assertEquals(26, trainingEntities.get(0).getStart());
        Assert.assertEquals(30, trainingEntities.get(0).getStop());

        trainingService.printXML(trainingData);
        // trainingService.postTrainingData(trainingData, SERVICE_ENDPOINT);
        mlClient.postTrainingData(trainingData, SERVICE_ENDPOINT);

    }
    
    @Test
    public void testRound() {
        int countTotal=10;
        int countQualityFull=1;
        DecimalFormat df = new DecimalFormat("###.##");
        logger.info("......workitems read in total = "+countTotal );
        logger.info("......           full quality = "+ df.format(((double)countQualityFull/(double)countTotal)*100)  + "%  (" + countQualityFull + ")");
     
    }

}
