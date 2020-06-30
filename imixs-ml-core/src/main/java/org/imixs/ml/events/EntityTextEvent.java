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
 * The EntityTextEvent provides a CDI event fired by the MLAdapter.
 * This even can be used in a observer pattern to find the best Object value
 * representation for a given text value.
 * <p>
 * The event provides a given text value and a set of...?
 * 
 * 
 * 
 * @author Ralph Soika
 * @version 1.0 
 * @see org.imixs.workflow.engine.WorkflowService
 */
public class EntityTextEvent {

    private String text;
    private Object itemValue;
    private Set<Locale> locals;

    public EntityTextEvent(String text, Set<Locale> locals) {
        this.text = text;
        this.locals = locals;
    }

  
    public String getText() {
        return text;
    }


    public void setText(String text) {
        this.text = text;
    }



    public Object getItemValue() {
        return itemValue;
    }


    public void setItemValue(Object itemValue) {
        this.itemValue = itemValue;
    }


    public Set<Locale> getLocals() {
        return locals;
    }

    public void setLocals(Set<Locale> locals) {
        this.locals = locals;
    }

}
