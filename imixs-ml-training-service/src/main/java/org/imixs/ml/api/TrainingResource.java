package org.imixs.ml.api;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.imixs.melman.FormAuthenticator;
import org.imixs.melman.RestAPIException;
import org.imixs.melman.WorkflowClient;
import org.imixs.ml.service.AnalyzService;
import org.imixs.ml.xml.XMLConfig;
import org.imixs.workflow.ItemCollection;

@Path("training")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class TrainingResource {

	@Inject
	AnalyzService analyzService;

	private static Logger logger = Logger.getLogger(TrainingResource.class.getName());

	/**
	 * POST Request with a valid training configuration
	 * <p>
	 * A valid xml document structure is expected:
	 * 
	 * <pre>
	 * {@code
	   <?xml version="1.0" encoding="UTF-8"?>
		<document xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			xmlns:xs="http://www.w3.org/2001/XMLSchema">
		  <item name="modeltype">
			  <value xsi:type="xs:string">M5</value>
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
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getOptionList(XMLConfig config) {

		// validate Input Data....
		logger.info("...starting training....");

		// properties.get("target.url");
		logger.info("target.url=" + config.getTarget());

		WorkflowClient worklowClient = new WorkflowClient(config.getTarget());
		// register the authenticator
		FormAuthenticator formAuth = new FormAuthenticator(config.getTarget(), config.getUser(), config.getPassword());
		worklowClient.registerClientRequestFilter(formAuth);

		try {

			String items = config.getEntities();
			if (items.contains("$file") || items.contains("$snapshotid")) {
				logger.severe("$file and $snapshot must not be included in the target.entities!");
				System.exit(0);
			}

			// select result
			String queryURL = "documents/search/" + config.getQuery() + "?pagesize=" + config.getPagesize() + "&items="
					+ config.getEntities() + ",$file,$snapshotid";
			logger.info("...select test data: " + queryURL);

			List<ItemCollection> documents = worklowClient.getCustomResource(queryURL);
					//.searchDocuments(config.getQuery());

			logger.info("...found " + documents.size() + " documents");
			// now iterate over all documents and start the training algorithm
			for (ItemCollection doc : documents) {

			}

			
		} catch (RestAPIException  e) {

			logger.warning("Failed to query documents: " + e.getMessage());
			e.printStackTrace();
		}

		// return response
		return Response.ok().build();
	}

}