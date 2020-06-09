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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.imixs.ml.xml.XMLAnalyseData;
import org.imixs.ml.xml.XMLTrainingData;

/**
 * The MLClient provides a Jax-RS client to post training data and analyse data objects. 
 * 
 * 
 * @version 1.0
 * @author rsoika
 */

public class MLClient {
    private static Logger logger = Logger.getLogger(MLClient.class.getName());

    /**
     * This method posts a spacy json training string to the spacy service endpoint
     * 
     * @param trainingData
     */
    public void postTrainingData(XMLTrainingData trainingData, String serviceEndpoint) {
        logger.fine("......sending new training data object...");
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(serviceEndpoint);
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);

        // build an array with one training data object
        List<XMLTrainingData> dataset = new ArrayList<XMLTrainingData>();
        dataset.add(trainingData);

        GenericEntity<List<XMLTrainingData>> list = new GenericEntity<List<XMLTrainingData>>(dataset) {
        };

        Response response = invocationBuilder.post(Entity.entity(list, MediaType.APPLICATION_JSON));

    }

    /**
     * This method posts a spacy json training string to the spacy service endpoint
     * 
     * @param trainingData
     */
    public void postAnalyseData(String text, String serviceEndpoint) {
        logger.fine("......sending analyse data object...");
        
        XMLAnalyseData atext= new XMLAnalyseData(text);
        
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(serviceEndpoint);
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);

       

        Response response = invocationBuilder.post(Entity.entity(atext, MediaType.APPLICATION_JSON));

    }

}
