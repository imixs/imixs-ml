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

package org.imixs.ml.xml;

import java.io.Serializable;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

/**
 * The XMLEntity defines the core Data class used for training data.
 * 
 * 
 * @author: ralph.soika@imixs.com
 * @version: 1.0
 */
@XmlRootElement(name = "entity")
public class XMLTrainingEntity implements Serializable {

    @XmlTransient
    private static final long serialVersionUID = 1L;

   
    private String value;

    private String label;
    
    private int start;
    
    private int stop;
    
    
    @XmlElement
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
    @XmlElement
    public int getStart() {
        return start;
    }
    public void setStart(int start) {
        this.start = start;
    }
    @XmlElement
    public int getStop() {
        return stop;
    }
    public void setStop(int stop) {
        this.stop = stop;
    }
    
    @XmlTransient
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((label == null) ? 0 : label.hashCode());
        result = prime * result + start;
        result = prime * result + stop;
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        XMLTrainingEntity other = (XMLTrainingEntity) obj;
        if (label == null) {
            if (other.label != null)
                return false;
        } else if (!label.equals(other.label))
            return false;
        if (start != other.start)
            return false;
        if (stop != other.stop)
            return false;
        return true;
    }
    
 

}
