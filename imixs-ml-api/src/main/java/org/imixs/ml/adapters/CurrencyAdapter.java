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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.logging.Logger;

import javax.enterprise.event.Observes;
import javax.inject.Named;

import org.imixs.ml.service.AnalyzeEntityEvent;

/**
 * The CurrencyAdapter creates text variants for a float value.
 * <p>
 * e.g. for the float value '1500'
 * <ul>
 * <li>1500.00
 * <li>1500,00
 * <li>1.500,00 UK
 * <li>1,500.00 GERMAN
 * <p>
 * see: https://docs.oracle.com/javase/tutorial/java/data/numberformat.html
 * <p>
 * 
 * @author rsoika
 *
 */
@Named
public class CurrencyAdapter {
    private static Logger logger = Logger.getLogger(CurrencyAdapter.class.getName());

    public void onEvent(@Observes AnalyzeEntityEvent event) {
        if (event.getValue() == null) {
            return;
        }
        // test if the value is float or double

        BigDecimal d = null;
        if ((event.getValue()) instanceof BigDecimal) {
            d = (BigDecimal) event.getValue();
        }
        if ((event.getValue()) instanceof Double) {
            d = new BigDecimal((Double) event.getValue());
        }
        if ((event.getValue()) instanceof Float) {
            d = new BigDecimal((Float) event.getValue());
        }

        if (d == null) {
            return;
        }
        try {

            if (event.getLocals() == null || event.getLocals().size() == 0) {
                logger.warning("missing locals - adapter can not be applied!");
                return;
            }

            for (Locale locale : event.getLocals()) {
                DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance(locale);
                formatter.applyPattern("#,###,##0.00");
                event.getEnityVariants().add(formatter.format(d));

                formatter = (DecimalFormat) DecimalFormat.getInstance(locale);
                formatter.applyPattern("0.00");
                event.getEnityVariants().add(formatter.format(d));
            }

        } catch (NumberFormatException nfe) {
            // not a number
        }
    }
}
