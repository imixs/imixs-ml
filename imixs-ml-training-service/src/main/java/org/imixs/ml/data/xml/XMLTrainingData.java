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

package org.imixs.ml.data.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * The XMLEntity defines the core Data class used for training data.
 * 
 * 
 * @author: ralph.soika@imixs.com
 * @version: 1.0
 */
@XmlRootElement(name = "trainingdata")
public class XMLTrainingData implements Serializable {

    @XmlTransient
    private static final long serialVersionUID = 1L;

    private String text;

    private List<XMLTrainingEntity> entities;
    
    public XMLTrainingData() {
        super();
        // init entities
        entities=new ArrayList<XMLTrainingEntity>();
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<XMLTrainingEntity> getEntities() {
        return entities;
    }

    public void setEntities(List<XMLTrainingEntity> entities) {
        this.entities = entities;
    }
    
    public void addTrainingEntity(XMLTrainingEntity trainingEntity) {
        entities.add(trainingEntity);
    }

   
}
