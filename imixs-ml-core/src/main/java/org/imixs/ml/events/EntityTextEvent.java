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

import java.util.List;
import java.util.Locale;

/**
 * The EntityTextEvent provides a CDI event fired by the MLAdapter. This event
 * can be used in a observer pattern to find the best Object value
 * representation for a given set of text values.
 * <p>
 * If the observer finds a matching value than the object property of the event
 * is be set.
 * <p>
 * The event provides a List of textVariants instead of a Set because also the
 * duplication of a textVariant can be a hint for the relevance of a value.
 * 
 * @author Ralph Soika
 * @version 1.0
 * @see org.imixs.workflow.engine.WorkflowService
 */
public class EntityTextEvent {

    private List<String> textVariants;
    private Object itemValue;
    private String itemType;
    private List<Locale> locals;

    public EntityTextEvent(List<String> textVariants, List<Locale> locals, String itemType) {
        this.textVariants = textVariants;
        this.locals = locals;
        this.itemType = itemType;
    }

    public List<String> getTextVariants() {
        return textVariants;
    }

    public void setTextVariants(List<String> textVariants) {
        this.textVariants = textVariants;
    }

    public Object getItemValue() {
        return itemValue;
    }

    public void setItemValue(Object itemValue) {
        this.itemValue = itemValue;
    }

    public List<Locale> getLocals() {
        return locals;
    }

  

    public String getItemType() {
        return itemType;
    }

 

}
