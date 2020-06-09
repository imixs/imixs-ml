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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

import javax.enterprise.event.Observes;

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

    public void onEvent(@Observes AnalyzeEntityEvent event) {
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
            event.getEnityVariants().add(dateFormat.format(date));

            dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            event.getEnityVariants().add(dateFormat.format(date));

            dateFormat = new SimpleDateFormat("d.M.yyyy");
            event.getEnityVariants().add(dateFormat.format(date));

            dateFormat = new SimpleDateFormat("dd.MM.yy");
            event.getEnityVariants().add(dateFormat.format(date));

            dateFormat = new SimpleDateFormat("d.M.yy");
            event.getEnityVariants().add(dateFormat.format(date));

            if (event.getLocals() == null || event.getLocals().size() == 0) {
                logger.warning("missing locals - adapter can not applied locale formats!");

            } else {
                for (Locale locale : event.getLocals()) {
                    dateFormat = new SimpleDateFormat("d MMMMM yyyy", locale);
                    event.getEnityVariants().add(dateFormat.format(date));
                    dateFormat = new SimpleDateFormat("d. MMMMM yyyy", locale);
                    event.getEnityVariants().add(dateFormat.format(date));
                }
            }

        } catch (NumberFormatException nfe) {
            // not a number
        }
    }
}
