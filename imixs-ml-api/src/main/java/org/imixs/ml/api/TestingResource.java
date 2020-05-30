package org.imixs.ml.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.imixs.melman.RestAPIException;
import org.imixs.melman.WorkflowClient;
import org.imixs.ml.service.TrainingService;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.xml.XMLDocument;
import org.imixs.workflow.xml.XMLDocumentAdapter;

@Named
@Path("testing")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class TestingResource {

    @Inject
    TrainingService trainingService;

    private static Logger logger = Logger.getLogger(TestingResource.class.getName());

    /**
     * POST Request with a valid training configuration
     * <p>
     * A valid xml document structure is expected:
     * 
     * <pre>
     * {@code
       <?xml version="1.0" encoding="UTF-8"?>
    <XMLConfig>
    <serialVersionUID>0</serialVersionUID>
    <target>string</target>
    <user>string</user>
    <password>string</password>
    <query>string</query>
    <pagesize>0</pagesize>
    <entities>string</entities>
    </XMLConfig>
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
    public Response testingData(XMLDocument xmlConfig) {

        ItemCollection config = XMLDocumentAdapter.putDocument(xmlConfig);
        // validate Input Data....
        logger.info("...starting testing....");

        try {
            WorkflowClient worklowClient = TrainingApplication.buildWorkflowClient(config);

            List<String> itemNames = config.getItemValue(TrainingApplication.ITEM_ENTITIES);
            if (itemNames.contains("$file") || itemNames.contains("$snapshotid")) {
                logger.severe("$file and $snapshot must not be included in the target.entities!");
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
            for (ItemCollection doc : documents) {
                trainingService.testWorkitemData(config, doc, worklowClient);
            }

        } catch (RestAPIException | UnsupportedEncodingException e) {

            logger.warning("Failed to query documents: " + e.getMessage());
            e.printStackTrace();
        }

        logger.info("**************** FINISHED ***********************");

        // return response
        return Response.ok(MediaType.APPLICATION_XML).build();
    }

}