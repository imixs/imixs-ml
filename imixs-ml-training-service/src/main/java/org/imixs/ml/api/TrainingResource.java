package org.imixs.ml.api;

import java.io.StringWriter;
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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.imixs.melman.FormAuthenticator;
import org.imixs.melman.RestAPIException;
import org.imixs.melman.WorkflowClient;
import org.imixs.ml.data.xml.XMLConfig;
import org.imixs.ml.service.TrainingService;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.xml.XMLDataCollectionAdapter;
import org.imixs.workflow.xml.XMLDocument;
import org.imixs.workflow.xml.XMLDocumentAdapter;

@Named
@Path("training")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class TrainingResource {

    @Inject
    TrainingService documentExtractorService;

    private static Logger logger = Logger.getLogger(TrainingResource.class.getName());

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
    public Response trainData(XMLConfig config) {

        // validate Input Data....
        logger.info("...starting training....");
        ItemCollection stats = new ItemCollection();

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

            stats.setItemValue("doc.count", documents.size());

            logger.info("...... " + documents.size() + " documents found");
            // now iterate over all documents and start the training algorithm
            for (ItemCollection doc : documents) {
                documentExtractorService.trainWorkitemData(doc, itemNames, stats, worklowClient);
            }

        } catch (RestAPIException | UnsupportedEncodingException e) {

            logger.warning("Failed to query documents: " + e.getMessage());
            e.printStackTrace();
        }

        // compute item.rate statistik
        int count = stats.getItemValueInteger("doc.count");
        if (count > 0) {
            List<String> names = stats.getItemNames();
            for (String item : names) {
                if (item.startsWith("item.count.")) {
                    float rate = stats.getItemValueFloat(item) / count;
                    logger.finest("......" + item + " count=" + stats.getItemValueInteger(item) + " rate=" + rate);
                    stats.replaceItemValue("item.rate." + item.substring(11), rate);
                }
            }
        }

        logger.info("**************** FINISHED ***********************");
        // log the stats XMLDocument object....
        try {
            JAXBContext context;
            context = JAXBContext.newInstance(XMLDocument.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringWriter out = new StringWriter();
            marshaller.marshal(XMLDocumentAdapter.getDocument(stats), out);
            String xml = out.toString();
            logger.info(xml);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
        logger.info("**************** FINISHED ***********************");

        // return response
        return Response.ok(XMLDataCollectionAdapter.getDataCollection(stats), MediaType.APPLICATION_XML).build();
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