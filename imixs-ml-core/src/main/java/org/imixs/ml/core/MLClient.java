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
package org.imixs.ml.core;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status.Family;

import org.imixs.ml.xml.XMLAnalyseData;
import org.imixs.ml.xml.XMLAnalyseResult;
import org.imixs.ml.xml.XMLTrainingData;

/**
 * The MLClient provides a Jax-RS client to post training data and analyse data
 * objects.
 * <p>
 * The MLClient can be constructed by a given service endpoint.
 * 
 * 
 * @version 1.0
 * @author rsoika
 */
public class MLClient {
    private static Logger logger = Logger.getLogger(MLClient.class.getName());

    private String serviceEndpoint = null;

    public MLClient(String serviceEndpoint) {
        super();
        // the service endpoint must not end with a /
        if (serviceEndpoint.endsWith("/")) {
            serviceEndpoint = serviceEndpoint.substring(0, serviceEndpoint.length() - 1);
        }
        this.serviceEndpoint = serviceEndpoint;
    }

    /**
     * This method posts a Imixs-ML json training string to a ML service endpoint
     * 
     * @param trainingData    - the training data object
     * @param serviceEndpoint - the ml API endpoint
     * @param options - an optional query parameter
     */
    public String postTrainingData(XMLTrainingData trainingData, String model, String options) {

        logger.fine("......sending new training data object...");
        Client client = ClientBuilder.newClient();
        String uri=serviceEndpoint + "/training/" + model;
        if (options!=null && !options.isEmpty()) {
            if (uri.contains("?")) {
                uri=uri+"&";
            } else {
                uri=uri+"?";
            }
            uri=uri+options;
        }
        WebTarget webTarget = client.target(uri);

        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);

        // build an array with one training data object
        List<XMLTrainingData> dataset = new ArrayList<XMLTrainingData>();
        dataset.add(trainingData);

        GenericEntity<List<XMLTrainingData>> list = new GenericEntity<List<XMLTrainingData>>(dataset) {
        };

        Response response = invocationBuilder.post(Entity.entity(list, MediaType.APPLICATION_JSON));
        String output = response.readEntity(String.class);
        return output;
       
    }
    
    /**
     * This method posts a Imixs-ML json training string to a ML service endpoint 
     * for validation only. Not model update is performed. 
     * 
     * @param trainingData    - the training data object
     * @param serviceEndpoint - the ml API endpoint
     * @param options - an optional query parameter
     */
    public String postValidateData(XMLTrainingData trainingData, String model) {

        logger.fine("......sending new training data object...");
        Client client = ClientBuilder.newClient();
        String uri=serviceEndpoint + "/validate/" + model;
        WebTarget webTarget = client.target(uri);

        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);

        // build an array with one training data object
        List<XMLTrainingData> dataset = new ArrayList<XMLTrainingData>();
        dataset.add(trainingData);

        GenericEntity<List<XMLTrainingData>> list = new GenericEntity<List<XMLTrainingData>>(dataset) {
        };

        Response response = invocationBuilder.post(Entity.entity(list, MediaType.APPLICATION_JSON));
        String output = response.readEntity(String.class);
        return output;
       
    }    

    /**
     * This method posts a Imixs-ML json training string to the ML service endpoint.
     * <p>
     * The method returns a list of XMLAnalyseEntity extracted from the given text
     * or null if the request failed.
     * 
     * 
     * @param text            - text to be analyzed
     * @param serviceEndpoint - the ml API endpoint
     * @return list of XMLAnalyseEntity
     **/
    public XMLAnalyseResult postAnalyseData(String text, String model) {

        logger.fine("......sending analyse data object...");
        XMLAnalyseData atext = new XMLAnalyseData(text);
        Client client = ClientBuilder.newClient();
        // client.register(RedirectFilterWorkAround.class);

        WebTarget webTarget = client.target(serviceEndpoint + "/analyse/" + model);
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.post(Entity.entity(atext, MediaType.APPLICATION_JSON));

        // in case of successful response we extract the XMLAnalyseEntity objects
        if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
            logger.finest("......POST request successfull (" + response.getStatus() + ")");
            // Now extract the List of XMLAnalyseEntity from the response object....
            // List<XMLAnalyseEntity> entities = response.readEntity(new
            // GenericType<List<XMLAnalyseEntity>>() {
            //  });
            XMLAnalyseResult resultObj = response.readEntity(XMLAnalyseResult.class);

            return resultObj;
        } else {
            logger.warning("......POST request failed: " + response.getStatus());
            return null;
        }

    }

  
}
