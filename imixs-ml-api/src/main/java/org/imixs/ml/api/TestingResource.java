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

import org.imixs.melman.FormAuthenticator;
import org.imixs.melman.RestAPIException;
import org.imixs.melman.WorkflowClient;
import org.imixs.ml.service.TrainingService;
import org.imixs.ml.xml.XMLConfig;
import org.imixs.workflow.ItemCollection;

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
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response testingData(XMLConfig config) {

        // validate Input Data....
        logger.info("...starting testing....");
      
        // properties.get("target.url");
        logger.info("target.url=" + config.getTarget());
        try {
            WorkflowClient worklowClient = new WorkflowClient(config.getTarget());
            // register the authenticator
            FormAuthenticator formAuth = new FormAuthenticator(config.getTarget(), config.getUser(),
                    config.getPassword());
            worklowClient.registerClientRequestFilter(formAuth);

            String items = config.getEntities();
            if (items.contains("$file") || items.contains("$snapshotid")) {
                logger.severe("$file and $snapshot must not be included in the target.entities!");
                System.exit(0);
            }
            // now lets see if we find some of our intem values....
            String[] itemNames = items.split(",");

            // select result
            String encodedQuery = URLEncoder.encode(config.getQuery(), StandardCharsets.UTF_8.toString());

            String queryURL = "documents/search/" + encodedQuery + "?sortBy=$created&sortReverse=true&pageSize="
                    + config.getPagesize();

            queryURL = appendItenNames(queryURL, itemNames);

            logger.info("......select workitems: " + queryURL);

            List<ItemCollection> documents = worklowClient.getCustomResource(queryURL);

         
            logger.info("...... " + documents.size() + " documents found");
            // now iterate over all documents and start the training algorithm
            for (ItemCollection doc : documents) {
                trainingService.testWorkitemData(doc, itemNames, worklowClient);
            }

        } catch (RestAPIException | UnsupportedEncodingException e) {

            logger.warning("Failed to query documents: " + e.getMessage());
            e.printStackTrace();
        }


        logger.info("**************** FINISHED ***********************");
       

        // return response
        return Response.ok(MediaType.APPLICATION_XML).build();
    }

    /**
     * THis method appends the item query param to an url based on a config list of
     * itemnames.
     * <p>
     * The method tests for | character. If found only the first part is taken as
     * the item name
     * 
     * @param url
     * @param entities
     * @return
     */
    private String appendItenNames(String url, String[] itemNames) {

        String queryParam = "&items=$file,$snapshotid,$uniqueid";
        for (String itemName : itemNames) {
            itemName = itemName.toLowerCase().trim();
            // if the itemName contains a | character than we do a mapping here.....
            if (itemName.contains("|")) {
                itemName = itemName.substring(0, itemName.indexOf('|')).trim();

            }
            queryParam = queryParam + "," + itemName;

        }
        url = url + queryParam;
        return url;
    }

}