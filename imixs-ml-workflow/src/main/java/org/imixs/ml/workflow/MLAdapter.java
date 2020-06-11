/*  
 *  Imixs-Workflow 
 *  
 *  Copyright (C) 2001-2020 Imixs Software Solutions GmbH,  
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
 *  Project: 
 *      https://www.imixs.org
 *      https://github.com/imixs/imixs-workflow
 *  
 *  Contributors:  
 *      Imixs Software Solutions GmbH - Project Management
 *      Ralph Soika - Software Developer
 */

package org.imixs.ml.workflow;

import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.imixs.ml.core.MLClient;
import org.imixs.ml.xml.XMLAnalyseEntity;
import org.imixs.ml.xml.XMLTrainingData;
import org.imixs.workflow.FileData;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.SignalAdapter;
import org.imixs.workflow.exceptions.AdapterException;

/**
 * This adapter class is used as a generic API Adapter for the PWGY. The adapter
 * connects to an API endpoint to exchange data. The Adapter supports the REST
 * request methods GET and POST. The result of a request is added to the current
 * workitem.
 * <p>
 * A result of an API request can be a single workitem or a list of workitems.
 * <p>
 * The Adapter is configured through the model by defining a workflow result
 * item named 'pgwy_api'.
 * <p>
 * Example:
 * 
 * <pre>
 * {@code
		<api>
			<method>POST</method>
			<resource>https://localhost:8111/api/resource/</resource>
			<mediatype>application/json</mediatype>
			<input>customer.firstname,customer.lastname</input>
			<output>id|order.id,desc|order.description</output>
		</api>
 * }
 * </pre>
 * 
 * This example definition will initiate an api call against the endpoint
 * 'https://localhost:8111/api/resource/' with a POST method. The call will send
 * the items customer.firstname,customer.lastname in the payload and adds the
 * items order.id,order.description from the result into the current workitem.
 * <p>
 * The list of output items can be mapped to custom item names by using the |
 * character to separate the origin item name (from the api call) to a target
 * item name.
 * <p>
 * For example 'id|order.id' will map the item 'id' into the current workitem
 * with the item named 'order.id'.
 * <p>
 * By using the optional tag 'output.target' it is possible to map a resultset
 * of multiple documents into an embedded list of ItemCollections. This allows
 * to store multiple results into a single workitem by using child-item
 * structures.
 * <p>
 * Examlple:
 * 
 * {@code<output.target>offers</output.target>}
 * 
 * <p>
 * The adapter catches any type of exception and continues with event=90 to
 * avoid an exception loop
 * 
 * @author Ralph Soika
 * @version 1.0
 *
 */

public class MLAdapter implements SignalAdapter {

	public static final String ERROR_API_COMMUNICATION = "ERROR_API_COMMUNICATION";
	public static final String ERROR_API_DEFINITION = "ERROR_API_DEFINITION";
	public static final String ML_API_ENDPOINT = "ML_API_ENDPOINT";

	public static final int API_EVENT_SUCCESS = 110;
	public static final int API_EVENT_FAILURE = 90;

	
	private static Logger logger = Logger.getLogger(MLAdapter.class.getName());

	  @Inject
	  @ConfigProperty(name = ML_API_ENDPOINT, defaultValue = "")
	  private String mlAPIEndpoint;
	  
	  
	/**
	 * This method posts a text from an attachment to the Imixs-ML Analyse service endpoint
	 */
	public ItemCollection execute(ItemCollection document, ItemCollection event) throws AdapterException {

		logger.info("...running api adapter...");
		// get the data object...
		
		
	    
        List<FileData> files = document.getFileData();
        if (files != null && files.size() > 0) {
            for (FileData file : files) {

                logger.info("...analyzing content of '" + file.getName() + "'.....");
                ItemCollection metadata = new ItemCollection(file.getAttributes());
                String content = metadata.getItemValueString("content");

                // clean content string....
                content = XMLTrainingData.cleanTextdata(content);
                
              
                MLClient mlClient = new MLClient();
                List<XMLAnalyseEntity> result =  mlClient.postAnalyseData(content, mlAPIEndpoint);

                if (result!=null && result.size()>0) {
                    // extract entities....
                }
                

            }

        } else {
            logger.finest("......no files found for " + document.getUniqueID());
        }

		

		return document;
	}

}
