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

package org.imixs.ml.events;

import java.util.Locale;
import java.util.Set;

/**
 * The EntityObjectEvent provides a CDI event fired by the TrainingService EJB.
 * This even can be used in a observer pattern to provide alternative text
 * representation for a given object value.
 * <p>
 * The event provides a given object value and a set of textVariants already
 * resolved for the given object. If an Observer finds new text variants for
 * the object value it adds those variants to the textVariants.
 * 
 * @author Ralph Soika
 * @version 1.0
 * @see org.imixs.workflow.engine.WorkflowService
 */
public class EntityObjectEvent {

    private Object value;
    private Set<String> enityTextVariants;
    private Set<Locale> locals;

    public EntityObjectEvent(Object value, Set<String> enityTextVariants, Set<Locale> locals) {
        this.value = value;
        this.enityTextVariants = enityTextVariants;
        this.locals = locals;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Set<String> getEnityTextVariants() {
        return enityTextVariants;
    }

    public void setEnityTextVariants(Set<String> enityTextVariants) {
        this.enityTextVariants = enityTextVariants;
    }

    public Set<Locale> getLocals() {
        return locals;
    }

    public void setLocals(Set<Locale> locals) {
        this.locals = locals;
    }

}
