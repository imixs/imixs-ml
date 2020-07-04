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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

import javax.enterprise.event.Observes;

import org.imixs.ml.events.EntityObjectEvent;
import org.imixs.ml.events.EntityTextEvent;

/**
 * The DateAdapter creates text variants for a Date value.
 * <p>
 * <ul>
 * <li>30.04.20
 * <li>30.04.2020
 * <li>30.4.20
 * <li>30.4.2020
 * <li>30 April 2020
 * <p>
 * 
 * @author rsoika
 *
 */
public class DateAdapter {
    private static Logger logger = Logger.getLogger(DateAdapter.class.getName());

    public void onEvent(@Observes EntityObjectEvent event) {
        if (event.getValue() == null) {
            return;
        }
        // test if the value can be converted into date...
        Date date = null;
        if ((event.getValue()) instanceof Date) {
            date = (Date) event.getValue();
        }
        if ((event.getValue()) instanceof Calendar) {
            date = ((Calendar) event.getValue()).getTime();
        }
        if ((event.getValue()) instanceof LocalDate) {
            LocalDate ld = (LocalDate) event.getValue();
            date = Date.from(ld.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        }
        if ((event.getValue()) instanceof LocalDateTime) {
            LocalDateTime ldt = (LocalDateTime) event.getValue();
            date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        }

        if (date == null) {
            return;
        }
        try {

            // add all supported date formats...
            DateFormat dateFormat = null;

            dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            event.getEnityTextVariants().add(dateFormat.format(date));

            dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            event.getEnityTextVariants().add(dateFormat.format(date));

            dateFormat = new SimpleDateFormat("d.M.yyyy");
            event.getEnityTextVariants().add(dateFormat.format(date));

            dateFormat = new SimpleDateFormat("dd.MM.yy");
            event.getEnityTextVariants().add(dateFormat.format(date));

            dateFormat = new SimpleDateFormat("d.M.yy");
            event.getEnityTextVariants().add(dateFormat.format(date));

            if (event.getLocals() == null || event.getLocals().size() == 0) {
                logger.warning("missing locals - adapter can not applied locale formats!");

            } else {
                for (Locale locale : event.getLocals()) {
                    dateFormat = new SimpleDateFormat("d MMMMM yyyy", locale);
                    event.getEnityTextVariants().add(dateFormat.format(date));
                    dateFormat = new SimpleDateFormat("d. MMMMM yyyy", locale);
                    event.getEnityTextVariants().add(dateFormat.format(date));
                }
            }

        } catch (NumberFormatException nfe) {
            // not a number
        }
    }

    /**
     * Method to parse text variants for date objects. The event is only processed
     * if the event type is 'date'.
     * 
     * @param event
     */
    public void onTextEvent(@Observes EntityTextEvent event) {

        String[] simplePatternList = { "yyyy-MM-dd", "dd.MM.yyyy", "d.M.yyyy", "dd.MM.yy", "d.M.yy" };
        String[] localePatternList = { "d MMMMM yyyy", "d. MMMMM yyyy" };

        // if the event already has a object then we return
        if (event.getItemValue() != null) {
            return;
        }

        // did the event type match our adapter type?
        if (event.getItemType() != null && !event.getItemType().isEmpty()
                && !event.getItemType().equalsIgnoreCase("date")) {
            // no match!
            return;
        }

        List<String> variants = event.getTextVariants();
        Date result = null;

        for (String variant : variants) {

            for (String simplePattern : simplePatternList) {
                result = parseDateByPattern(variant, simplePattern);
                if (result != null) {
                    event.setItemValue(result);
                    return;
                }
            }

            // try locale patterns...
            if (event.getLocals() == null || event.getLocals().size() == 0) {
                for (String localePattern : localePatternList) {
                    result = parseDateByLocale(variant, localePattern, event.getLocals());
                    if (result != null) {
                        event.setItemValue(result);
                        return;
                    }
                }
            }

        }

    }

    /**
     * Helper method to parse a string for a date object by a given pattern.
     * 
     * @param pattern - date pattern
     * @return date object if parse able - otherwise null.
     */
    private Date parseDateByPattern(String text, String pattern) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
            Date result = dateFormat.parse(text);
            // success!
            return result;
        } catch (ParseException e) {
            // unable to parse ...
            return null;
        }

    }

    /**
     * Helper method to parse a string for a date object by a given pattern and all
     * available locales.
     * 
     * @param pattern - date pattern
     * @return date object if parse able - otherwise null.
     */
    private Date parseDateByLocale(String text, String pattern, Set<Locale> locales) {

        for (Locale locale : locales) {

            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, locale);
                Date result = dateFormat.parse(text);
                // success!
                return result;
            } catch (ParseException e) {
                // unable to parse ...
                return null;
            }
        }
        return null;
    }

}
