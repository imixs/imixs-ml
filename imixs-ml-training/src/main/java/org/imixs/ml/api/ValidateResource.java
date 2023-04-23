package org.imixs.ml.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.List;
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
import org.imixs.workflow.xml.XMLDocument;
import org.imixs.workflow.xml.XMLDocumentAdapter;

/**
 * This Resource Endpoint can be used to validate a model against a training data set without updating the model.
 * 
 * @author rsoika
 *
 */
@Named
@Path("validate")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class ValidateResource {

    @Inject
    TrainingService trainingService;

    private static Logger logger = Logger.getLogger(ValidateResource.class.getName());

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
    
        <item name="ml.analyse.endpoint"><value xsi:type=
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
    public Response validateData(XMLDocument xmlConfig) {
        int countTotal = 0;
        int countQualityGood = 0;
        int countQualityLow = 0;
        int countQualityBad = 0;
        double nerFactor = -1;
        double allNerFactors = 0.0;
        String log="";
        
        ItemCollection config = XMLDocumentAdapter.putDocument(xmlConfig);
        // validate Input Data....
        logger.info("...starting testing....");

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

            String queryURL = "documents/search/" + encodedQuery + "?sortBy=$created&sortReverse=true";
            queryURL = queryURL + "&pageSize=" + config.getItemValueInteger(TrainingApplication.ITEM_WORKFLOW_PAGESIZE)
                    + "&pageIndex=" + config.getItemValueInteger(TrainingApplication.ITEM_WORKFLOW_PAGEINDEX);
            queryURL = TrainingApplication.appendItenNames(queryURL, itemNames);

            logger.info("......select workitems: " + queryURL);
            List<ItemCollection> documents = worklowClient.getCustomResource(queryURL);
            logger.info("...... " + documents.size() + " documents found");

            // now iterate over all documents and start the training algorithm
            int currentCount = 0;
            for (ItemCollection doc : documents) {
                MLTrainingResult trainingResult = trainingService.validateWorkitemData(config, doc, worklowClient);
                currentCount++;
                countTotal++;
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

        } catch (RestAPIException | UnsupportedEncodingException e) {

            logger.warning("Failed to query documents: " + e.getMessage());
            e.printStackTrace();
        }

        logger.info("**************** FINISHED ***********************");

        DecimalFormat df = new DecimalFormat("###.##");

        log = log + "  ......   quality level GOOD = "
                + df.format(((double) countQualityGood / (double) countTotal) * 100) + "%  (" + countQualityGood + ")"
                + "\n";

        log = log + "  ......      quality level LOW = "
                + df.format(((double) countQualityLow / (double) countTotal) * 100) + "%  (" + countQualityLow + ")"
                + "\n";
        log = log + "  ......      quality level BAD = "
                + df.format(((double) countQualityBad / (double) countTotal) * 100) + "%  (" + countQualityBad + ")";
        log = log + "\n  ......            average NER = " + nerFactor;
        log = log + "\n";
       
        logger.info(log);
        
        // return response
        return Response.ok(MediaType.APPLICATION_XML).build();
    }

}