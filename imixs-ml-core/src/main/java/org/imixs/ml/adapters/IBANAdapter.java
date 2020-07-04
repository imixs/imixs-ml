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

package org.imixs.ml.adapters;

import java.util.List;
import java.util.regex.Pattern;

import javax.enterprise.event.Observes;

import org.imixs.ml.events.EntityObjectEvent;
import org.imixs.ml.events.EntityTextEvent;

/**
 * The IBANAdapter creates text variants for an IBAN value
 * <p>
 * e.g. 'DE69 5095 1469 0005 0111 11'
 * <ul>
 * <li>DE69 5095 1469 0005 0111 11
 * <li>DE69509514690005011111
 * <p>
 * see: https://docs.oracle.com/javase/tutorial/java/data/numberformat.html
 * <p>
 * 
 * @author rsoika
 *
 */
public class IBANAdapter {

    public static final String IBAN_PATTERN = "^$|(^[A-Z]{2}(?:[ ]?[A-Z0-9]){13,32}$)";

    public void onEvent(@Observes EntityObjectEvent event) {

        if (event.getValue() == null) {
            return;
        }
        // test if the value is an IBAN
        String iban = event.getValue().toString();
        if (Pattern.matches(IBAN_PATTERN, iban)) {

            // first take the string as is...
            event.getEnityTextVariants().add(iban.trim());

            // next strip ' '
            iban = iban.replace(" ", "");
            event.getEnityTextVariants().add(iban.trim());

            // finally add standard spaces...
            String iban_formated = "";
            // int pos = 0;
            while (true) {
                if (iban.length() >= 4) {
                    iban_formated = iban_formated + iban.substring(0, 4) + " ";
                    iban = iban.substring(4);
                } else {
                    iban_formated = iban_formated + iban;
                    break;
                }

            }
            event.getEnityTextVariants().add(iban_formated.trim());
        }

    }

    /**
     * Method to parse text variants for IBAN objects. The event is only processed
     * if the event type is 'iban'.
     * 
     * @param event
     */
    public void onTextEvent(@Observes EntityTextEvent event) {

        // if the event already has a object then we return
        if (event.getItemValue() != null) {
            return;
        }

        // did the event type match our adapter type?
        if (event.getItemType() != null && !event.getItemType().isEmpty()
                && !event.getItemType().equalsIgnoreCase("iban")) {
            // no match!
            return;
        }

        List<String> variants = event.getTextVariants();
        for (String variant : variants) {
            if (Pattern.matches(IBAN_PATTERN, variant)) {
                event.setItemValue(variant);
                return;
            }

        }

    }
}
