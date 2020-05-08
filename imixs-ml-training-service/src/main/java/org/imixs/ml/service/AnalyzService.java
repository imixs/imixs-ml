/*******************************************************************************
 *  Imixs Workflow Technology
 *  Copyright (C) 2001, 2008 Imixs Software Solutions GmbH,  
 *  http://www.imixs.com
 *  
 *  This program is free software; you can redistribute it and/or 
 *  modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation; either version 2 
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 *  General Public License for more details.
 *  
 *  You can receive a copy of the GNU General Public
 *  License at http://www.gnu.org/licenses/gpl.html
 *  
 *  Contributors:  
 *  	Imixs Software Solutions GmbH - initial API and implementation
 *  	Ralph Soika
 *******************************************************************************/
package org.imixs.ml.service;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.imixs.melman.RestAPIException;
import org.imixs.melman.WorkflowClient;
import org.imixs.workflow.FileData;
import org.imixs.workflow.ItemCollection;

/**
 * The AnalyzService is be used to generate a new ML Model by analyzing existing
 * data in an instance of Imixs-Office-Workflow.
 * <p>
 * The analyzer extracts the text contained in a document attached to a workitem
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
 * @version 1.0
 * @author rsoika
 */

@Stateless
public class AnalyzService {
 private static Logger logger = Logger.getLogger(AnalyzService.class.getName());

  

	/**
	 * This method is used to extract the text contained in a document and search
	 * for 'known' entities within the text. If a valuable traing data set can be
	 * build the training data is send to the Imixs-ML service.
	 * 
	 * 
	 * @param doc
	 */
	public void analyzeDoc(ItemCollection doc,WorkflowClient workflowClient) {

		// first we load the attachments...
		List<FileData> files = doc.getFileData();
		for (FileData file : files) {
			// load the file from the archive service....
			try {
				byte[] fileContent = loadFileFromArchive(file, doc.getUniqueID(),workflowClient);
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
	public byte[] loadFileFromArchive(FileData fileData, String uniqueId, WorkflowClient workflowClient) throws RestAPIException {

		if (fileData == null) {
			return null;
		}

		// first we lookup the FileData object
		if (fileData != null) {
			String fileURI = "snapshot/" + uniqueId + "/file/" + fileData.getName();
			Client rsClient = workflowClient.newClient();
			Response reponse = rsClient.target(fileURI).request(MediaType.APPLICATION_OCTET_STREAM).get();
			byte[] fileContent = reponse.readEntity(byte[].class);
			if (fileContent != null && fileContent.length > 0) {
				logger.finest("......md5 data object found");
				return fileContent;
			}
		}

		return null;

	}

}
