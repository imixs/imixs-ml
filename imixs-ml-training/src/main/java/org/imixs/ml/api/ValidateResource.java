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
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response testingData(XMLDocument xmlConfig) {

        ItemCollection config = XMLDocumentAdapter.putDocument(xmlConfig);
        // validate Input Data....
        logger.info("...starting testing....");

        try {
            WorkflowClient worklowClient = TrainingApplication.buildWorkflowClient(config);

            // select result
            String encodedQuery = URLEncoder.encode(config.getItemValueString(TrainingApplication.ITEM_WORKFLOW_QUERY),
                    StandardCharsets.UTF_8.toString());

            String queryURL = "documents/search/" + encodedQuery + "?sortBy=$created&sortReverse=true";
            queryURL = queryURL + "&pageSize=" + config.getItemValueInteger(TrainingApplication.ITEM_WORKFLOW_PAGESIZE)
                    + "&pageIndex=" + config.getItemValueInteger(TrainingApplication.ITEM_WORKFLOW_PAGEINDEX);
            queryURL = TrainingApplication.appendItenNames(queryURL, null);

            logger.info("......select workitems: " + queryURL);
            List<ItemCollection> documents = worklowClient.getCustomResource(queryURL);
            logger.info("...... " + documents.size() + " documents found");

            // now iterate over all documents and start the training algorithm
            for (ItemCollection doc : documents) {
                trainingService.validateWorkitemData(config, doc, worklowClient);
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