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

package org.imixs.ml.service;

import java.util.Set;

/**
 * The AnalyzeEntityEvent provides a CDI event fired by the TrainingService EJB.
 * This even can be used in a observer pattern to provide alternative text representation for a given item value.
 * 
 * @author Ralph Soika
 * @version 1.0
 * @see org.imixs.workflow.engine.WorkflowService
 */
public class AnalyzeEntityEvent {

    private Object value;
    private Set<String> enityVariants;
    
    public AnalyzeEntityEvent(Object value, Set<String> enityVariants) {
        this.value=value;
        this.enityVariants=enityVariants;
    }


    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Set<String> getEnityVariants() {
        return enityVariants;
    }

    public void setEnityVariants(Set<String> enityVariants) {
        this.enityVariants = enityVariants;
    }

   

}
