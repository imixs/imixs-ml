package org.imixs.ml;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.imixs.melman.FormAuthenticator;
import org.imixs.melman.RestAPIException;
import org.imixs.melman.WorkflowClient;
import org.imixs.workflow.FileData;
import org.imixs.workflow.ItemCollection;

/**
 * This Java App can be used to generate a new ML Model by analyzing existing
 * data in an instance of Imixs-Office-Workflow.
 * <p>
 * The analyzer extracts the text contained in a document attaged to a workitem
 * and searches for 'known' entities within the text. If a valuable training
 * data set can be build the training data is send to the Imixs-ML service.
 * 
 * 
 * <p>
 * The application expects a property file named training.properties. The
 * following properties are defined.:
 * <ul>
 * <li>target.url = URL to an instance of Imixs-Office-Workflow</li>
 * <li>target.user = userid to access the target instance</li>
 * <li>target.password = password to access the target instance</li>
 * <li>target.query = document selection</li>
 * <li>target.pagesize = max page size</li>
 * <li>target.entities = list of training entities
 * </ul>
 * 
 * 
 * @author rsoika
 * @version 1.0
 * 
 */
public class ModelGenerator {
	private static Map<String, String> properties;
	private static Logger logger = Logger.getLogger(ModelGenerator.class.getName());

	static WorkflowClient worklowClient = null;

	public static void main(String[] args) {

		loadProperties();
		// read params
		String url = properties.get("target.url");
		String user = properties.get("target.user");
		String password = properties.get("target.password");
		String query = properties.get("target.query");
		int pageSize = Integer.parseInt(properties.getOrDefault("target.pagesize", "100"));

		// properties.get("target.url");
		logger.info("target.url=" + url);

		worklowClient = new WorkflowClient(url);
		// register the authenticator
		FormAuthenticator formAuth = new FormAuthenticator(url, user, password);
		worklowClient.registerClientRequestFilter(formAuth);

		try {

			String items = properties.get("target.entities");
			if (items.contains("$file") || items.contains("$snapshotid")) {
				logger.severe("$file and $snapshot must not be included in the target.entities!");
				System.exit(0);
			}

			// select result
			String queryURL = "documents/search/" + query + "?pagesize=" + pageSize + "&items=" + items
					+ ",$file,$snapshotid";
			logger.info("...select test data: " + queryURL);

			List<ItemCollection> documents = worklowClient.searchDocuments(query);

			// now iterate over all documents and start the training algorithm
			for (ItemCollection doc : documents) {

			}

			logger.info("...found " + documents.size() + " documents");
		} catch (RestAPIException | UnsupportedEncodingException e) {

			logger.warning("Failed to query documents: " + e.getMessage());
			e.printStackTrace();
		}

	}

	/**
	 * This method is used to extract the text contained in a document and search
	 * for 'known' entities within the text. If a valuable traing data set can be
	 * build the training data is send to the Imixs-ML service.
	 * 
	 * 
	 * @param doc
	 */
	private void analyzeDoc(ItemCollection doc) {

		// first we load the attachments...
		List<FileData> files = doc.getFileData();
		for (FileData file : files) {
			// load the file from the archive service....
			try {
				byte[] fileContent = loadFileFromArchive(file, doc.getUniqueID());
				if (fileContent!=null) {
					// OCR ......
					
				}
			} catch (RestAPIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	/**
	 * This method loads the file content for a given md5 checksum directly from the
	 * cassandra archive using the resource
	 * <p>
	 * <code>/archive/md5/{md5}</code>
	 * <p>
	 * To activate this mechansim the environment variable ARCHIVE_SERVICE_ENDPOINT
	 * must be set to a valid endpoint.
	 * 
	 * @param fileData - fileData object providing the MD5 checksum
	 * @throws RestAPIException
	 */
	public byte[] loadFileFromArchive(FileData fileData, String uniqueId) throws RestAPIException {

		if (fileData == null) {
			return null;
		}

		// first we lookup the FileData object
		if (fileData != null) {
			String fileURI = "snapshot/" + uniqueId + "/file/" + fileData.getName();
			Client rsClient = worklowClient.newClient();
			Response reponse = rsClient.target(fileURI).request(MediaType.APPLICATION_OCTET_STREAM).get();
			byte[] fileContent = reponse.readEntity(byte[].class);
			if (fileContent != null && fileContent.length > 0) {
				logger.finest("......md5 data object found");
				return fileContent;
			}
		}

		return null;

	}

	/**
	 * This method is used to load a imixs.property file into the property
	 * Map<String,String>
	 * <p>
	 * The imixs.property file is loaded from the current threads classpath.
	 * 
	 */
	private static void loadProperties() {
		properties = new HashMap<String, String>();
		Properties fileProperties = new Properties();
		try {
			fileProperties.load(
					Thread.currentThread().getContextClassLoader().getResource("training.properties").openStream());

			// now we put the values into the property Map.....
			for (Object key : fileProperties.keySet()) {
				String value = fileProperties.getProperty(key.toString());
				if (value != null && !value.isEmpty()) {
					properties.put(key.toString(), value);
				}
			}

		} catch (Exception e) {
			logger.warning("unable to find imixs.properties in current classpath");
			if (logger.isLoggable(Level.FINE)) {
				e.printStackTrace();
			}
		}

	}

}
