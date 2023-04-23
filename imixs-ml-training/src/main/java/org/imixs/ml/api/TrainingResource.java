package org.imixs.ml.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.imixs.melman.RestAPIException;
import org.imixs.melman.WorkflowClient;
import org.imixs.ml.core.MLTrainingResult;
import org.imixs.ml.service.TrainingService;
import org.imixs.ml.xml.XMLTrainingData;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.util.JSONParser;
import org.imixs.workflow.xml.XMLDataCollectionAdapter;
import org.imixs.workflow.xml.XMLDocument;
import org.imixs.workflow.xml.XMLDocumentAdapter;

@Named
@Path("training")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class TrainingResource {

    @Inject
    TrainingService trainingService;

    private static Logger logger = Logger.getLogger(TrainingResource.class.getName());

    /**
     * POST Request with a valid training configuration
     * <p>
     * A valid xml document structure is expected:
     * 
     * <pre>
     * {@code
    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <document xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:xs="http://www.w3.org/2001/XMLSchema">
        <item name="workflow.url"><value xsi:type=
    "xs:string">http://localhost:8080/api/</value></item>
        <item name="workflow.userid"><value xsi:type=
    "xs:string">admin</value></item>
        <item name="workflow.password"><value xsi:type=
    "xs:string">...</value></item>
        <item name="workflow.query"><value xsi:type=
    "xs:string">($workflowgroup:"Invoice") AND ($taskid:5900)</value></item>
        <item name="workflow.pagesize"><value xsi:type="xs:int">100</value></item>
        <item name="workflow.pageindex"><value xsi:type="xs:int">0</value></item>
        
        <item name="workflow.entities">
            <value xsi:type="xs:string">_iban</value>
            <value xsi:type="xs:string">_bic</value>
            <value xsi:type="xs:string">_invoicetotal</value>
            <value xsi:type="xs:string">_invoicenumber</value>
        </item>
        
        <item name="tika.options">
            <value xsi:type="xs:string">4711-1</value>
            <value xsi:type="xs:string">4711-2</value>
        </item>
        
        <item name="ml.training.endpoint"><value xsi:type=
    "xs:string">http://imixs-ml-spacy:8000/</value></item>
    </document>
     * }
     * </pre>
     * 
     * 
     * </p>
     * The method returns a XMLDocument with an option list included.
     * 
     * 
     * @param requestXML - workitem data
     * @return - XMLDocument with option list
     */
    @SuppressWarnings("unchecked")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response trainData(XMLDocument xmlConfig) {
        String mainLog="";
        Map<String, ItemCollection> trainingDataSet = null;

        ItemCollection config = XMLDocumentAdapter.putDocument(xmlConfig);
        // validate Input Data....
        logger.info("...starting training....");
        logger.fine("......model=" + config.getItemValueString(TrainingApplication.ITEM_ML_TRAINING_MODEL));
        logger.info("......required qualityLevel=" + config.getItemValueString(TrainingApplication.ITEM_ML_TRAINING_QUALITYLEVEL));
        logger.info("......ocrMode=" + config.getItemValueString(TrainingApplication.ITEM_TIKA_OCR_MODE));
        
        try {
            WorkflowClient worklowClient = TrainingApplication.buildWorkflowClient(config);

            List<String> itemNames = config.getItemValue(TrainingApplication.ITEM_ENTITIES);
            itemNames.add(TrainingService.ITEM_ML_DEFINITIONS);
            if (itemNames.contains("$file") || itemNames.contains("$snapshotid")) {
                logger.severe("$file and $snapshot must not be included in the workflow.entities!");
                System.exit(0);
            }

            // select result
            String encodedQuery = URLEncoder.encode(config.getItemValueString(TrainingApplication.ITEM_WORKFLOW_QUERY),
                    StandardCharsets.UTF_8.toString());
            String queryURL = "documents/search/" + encodedQuery + "?sortBy=$modified&sortReverse=true";
            queryURL = queryURL + "&pageSize=" + config.getItemValueInteger(TrainingApplication.ITEM_WORKFLOW_PAGESIZE)
                    + "&pageIndex=" + config.getItemValueInteger(TrainingApplication.ITEM_WORKFLOW_PAGEINDEX);
            queryURL = TrainingApplication.appendItenNames(queryURL, itemNames);

            logger.info("......select workitems: " + queryURL);
            List<ItemCollection> documents = worklowClient.getCustomResource(queryURL);
           
            // now iterate over all documents and start the training algorithm
            logger.info("...... " + documents.size() + " documents found");

            // now we store all Documents in a HashMap.....
            trainingDataSet = new HashMap<String, ItemCollection>();
            for (ItemCollection doc : documents) {
                trainingDataSet.put(doc.getUniqueID(), doc);
            }

            int trainingInterations = config.getItemValueInteger(TrainingApplication.ITEM_ML_TRAINING_ITERATIONS);
            double trainigDropoutRate = config.getItemValueDouble(TrainingApplication.ITEM_ML_TRAINING_DROP_OUT_RATE);
            if (trainigDropoutRate < 0 || trainigDropoutRate >= 1) {
                logger.warning("WRONG ml.training.dropoutrate : " + trainigDropoutRate + " should be between 0 and 1");
                trainigDropoutRate = 0.25;
            }
            logger.info("..... iterations=" + trainingInterations);
            logger.info("..... dropoutrate=" + trainigDropoutRate);
            // we iterate over the whole trainingg data several times.
            for (int iteration = 0; iteration < trainingInterations; iteration++) {
                logger.info("\n\n\n---- Starting " + iteration + ". Training Iteration ----\n\n");
                // Start the Training
               String log= startSingleTrainingIteration(trainingDataSet, trainigDropoutRate, worklowClient, config);
               mainLog=mainLog+log;
               logger.info(log);
               logger.info("\\n\\n\\n---- Training Iteration Completed -----");

            }


        } catch (RestAPIException | UnsupportedEncodingException e) {
            logger.warning("Failed to query documents: " + e.getMessage());
            e.printStackTrace();
        }

        logger.info(" ");
        logger.info(" ");
        logger.info("****************Trainng Iteration Completed ***********************");
        logger.info("SUMMMARY:");
        logger.info(mainLog);
        // log the stats XMLDocument object....
        ItemCollection stats = new ItemCollection();


        // return response
        return Response.ok(XMLDataCollectionAdapter.getDataCollection(stats), MediaType.APPLICATION_XML).build();
    }

    /**
     * Helper method to train a trainingDataSet. The training data is shuffled an
     * the size of the training data set is reduzed by the trainigDropoutRate. This
     * is to avoid memory effects.
     * 
     * @param trainingDataSet
     * @param trainigDropoutRate
     */
    private String startSingleTrainingIteration(Map<String, ItemCollection> trainingDataSet, double trainigDropoutRate,
            WorkflowClient worklowClient, ItemCollection config) {
        int countTotal = 0;
        int countQualityGood = 0;
        int countQualityLow = 0;
        int countQualityBad = 0;
        double nerFactor = -1;
        double allNerFactors = 0.0;

        List<String> iterraionUniqueIDs = new ArrayList<String>(trainingDataSet.keySet());
        // shuffle the uniqueids
        Collections.shuffle(iterraionUniqueIDs);
        // drop
        int size = iterraionUniqueIDs.size();
        int targetSize = (int) (size - (size * trainigDropoutRate));
        while (iterraionUniqueIDs.size() > targetSize) {
            iterraionUniqueIDs.remove(0);
        }

        // now we start the training with the shuffled reduced traing set....

        int currentCount = 0;
        for (String uniqueid : iterraionUniqueIDs) {
            ItemCollection doc = trainingDataSet.get(uniqueid);
            currentCount++;
            countTotal++;
            logger.fine("...... train " + uniqueid + "...");
            MLTrainingResult trainingResult = trainingService.trainWorkitemData(config, doc, worklowClient);

            if (trainingResult != null) {
                // compute quality statistic
                switch (trainingResult.getQualityLevel()) {
                case XMLTrainingData.TRAININGDATA_QUALITY_GOOD:
                    countQualityGood++;
                    break;
                case XMLTrainingData.TRAININGDATA_QUALITY_LOW:
                    countQualityLow++;
                    break;
                default:
                    countQualityBad++;
                }

                // extract ner factor
                String resultData = trainingResult.getData();
                if (resultData != null && !resultData.isEmpty()) {
                    // parse currentNerFactor....
                    try {
                        String nerString = JSONParser.getKey("ner", resultData);
                        double newNerFactor = Double.parseDouble(nerString);
                        allNerFactors = allNerFactors + newNerFactor;
                        nerFactor = allNerFactors / currentCount;
                    } catch (Exception e) {
                        logger.severe("failed to parse training result (ner)");
                    }
                }

            } else {
                countQualityBad++;
            }

        }
        DecimalFormat df = new DecimalFormat("###.##");
        String log = "\n......documents trained in total = " + countTotal + "\n";

        log = log + "  ......     quality level GOOD = "
                + df.format(((double) countQualityGood / (double) countTotal) * 100) + "%  (" + countQualityGood + ")"
                + "\n";

        log = log + "  ......      quality level LOW = "
                + df.format(((double) countQualityLow / (double) countTotal) * 100) + "%  (" + countQualityLow + ")"
                + "\n";
        log = log + "  ......      quality level BAD = "
                + df.format(((double) countQualityBad / (double) countTotal) * 100) + "%  (" + countQualityBad + ")";
        log = log + "\n  ......            average NER = " + nerFactor;
        log = log + "\n";
        return log;
       
    }
}