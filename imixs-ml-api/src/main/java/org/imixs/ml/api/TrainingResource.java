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

import org.imixs.melman.RestAPIException;
import org.imixs.melman.WorkflowClient;
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
    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <document xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:xs="http://www.w3.org/2001/XMLSchema">
        <item name="target.url"><value xsi:type=
    "xs:string">http://localhost:8080/api/</value></item>
        <item name="target.userid"><value xsi:type="xs:string">admin</value></item>
        <item name="target.password"><value xsi:type="xs:string">...</value></item>
        <item name="target.query"><value xsi:type=
    "xs:string">($workflowgroup:"Invoice") AND ($taskid:5900)</value></item>
        <item name="target.pagesize"><value xsi:type="xs:int">100</value></item>
        <item name="target.pageindex"><value xsi:type="xs:int">0</value></item>
        
        <item name="entities">
            <value xsi:type="xs:string">_iban</value>
            <value xsi:type="xs:string">_bic</value>
            <value xsi:type="xs:string">_invoicetotal</value>
            <value xsi:type="xs:string">_invoicenumber</value>
        </item>
        
        <item name="tika.options">
            <value xsi:type="xs:string">4711-1</value>
            <value xsi:type="xs:string">4711-2</value>
        </item>
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

        ItemCollection config = XMLDocumentAdapter.putDocument(xmlConfig);
        // validate Input Data....
        logger.info("...starting testing....");

        // validate Input Data....
        logger.info("...starting training....");
        ItemCollection stats = new ItemCollection();

        try {
            WorkflowClient worklowClient = TrainingApplication.buildWorkflowClient(config);

            List<String> itemNames = config.getItemValue(TrainingApplication.ITEM_ENTITIES);
            if (itemNames.contains("$file") || itemNames.contains("$snapshotid")) {
                logger.severe("$file and $snapshot must not be included in the target.entities!");
                System.exit(0);
            }

            // select result
            String encodedQuery = URLEncoder.encode(config.getItemValueString(TrainingApplication.ITEM_TRAGET_QUERY),
                    StandardCharsets.UTF_8.toString());

            String queryURL = "documents/search/" + encodedQuery + "?sortBy=$created&sortReverse=true";

            queryURL = queryURL + "&pageSize=" + config.getItemValueInteger(TrainingApplication.ITEM_TRAGET_PAGESIZE)
                    + "&pageIndex=" + config.getItemValueInteger(TrainingApplication.ITEM_TRAGET_PAGEINDEX);

            queryURL = TrainingApplication.appendItenNames(queryURL, itemNames);

            logger.info("......select workitems: " + queryURL);

            List<ItemCollection> documents = worklowClient.getCustomResource(queryURL);

            stats.setItemValue("doc.count", documents.size());

            logger.info("...... " + documents.size() + " documents found");
            List<String> tikaOptions=config.getItemValue(TrainingApplication.ITEM_TIKA_OPTIONS);
            String ocrMode=config.getItemValueString(TrainingApplication.ITEM_TIKA_OCR_MODE);
            // now iterate over all documents and start the training algorithm
            for (ItemCollection doc : documents) {
                documentExtractorService.trainWorkitemData(doc, itemNames, stats, worklowClient,ocrMode,tikaOptions);
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

   

}