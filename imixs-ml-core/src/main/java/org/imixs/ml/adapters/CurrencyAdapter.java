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
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

import javax.enterprise.event.Observes;

import org.imixs.ml.events.EntityObjectEvent;
import org.imixs.ml.events.EntityTextEvent;

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
public class CurrencyAdapter {
    private static Logger logger = Logger.getLogger(CurrencyAdapter.class.getName());

    public void onObjectEvent(@Observes EntityObjectEvent event) {
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
                event.getEnityTextVariants().add(formatter.format(d));

                formatter = (DecimalFormat) DecimalFormat.getInstance(locale);
                formatter.applyPattern("0.00");
                event.getEnityTextVariants().add(formatter.format(d));
            }

        } catch (NumberFormatException nfe) {
            // not a number
        }
    }

    public void onTextEvent(@Observes EntityTextEvent event) {

        // if the event already has a object then we return
        if (event.getItemValue() != null) {
            return;
        }
        
        // did the event type match our adapter type?
        if (event.getItemType()!=null && !event.getItemType().isEmpty() 
                && !event.getItemType().equalsIgnoreCase("currency")) {
            // no match!
            return;
        }
        

        List<String> variants = event.getTextVariants();
        BigDecimal result = null;

        for (String variant : variants) {

            BigDecimal b=textToCurrency(variant, event.getLocals());
            if (b!=null ) {
                if (result!=null) {
                    // take highest value...
                    if (b.compareTo(result)==1) {
                        result=b;
                    }
                } else {
                    result=b;
                }
            }
        }
        
        if (result!=null) {
            // convert to float
            event.setItemValue(result.floatValue());
        }

    }

    /**
     * This helper method translates a text into a BigDecimal by applying different
     * currency formats and different locals
     * 
     * @param text   - text representing a number
     * @param locals - list of locales
     * @return BigDecimal or null if no number was found.
     */
    private BigDecimal textToCurrency(String text, Set<Locale> locals) {
        for (Locale locale : locals) {

            BigDecimal b = null;

            b = formatTextToNumber(text, "#,###,##0.00", locale);
            if (b == null) {
                // try second pattern
                b = formatTextToNumber(text, "0.00", locale);
            }
            if (b != null) {
                return b;
            }
        }
        // no match!
        return null;
    }

    /**
     * This helper method formats a text by a given number pattern and a locale into
     * number.
     * 
     * @param text
     * @param pattern
     * @param locale
     * @return
     */
    private BigDecimal formatTextToNumber(String text, String pattern, Locale locale) {
        DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance(locale);
        formatter.applyPattern(pattern);

        try {
            Number n = formatter.parse(text);
            Double d = n.doubleValue();
            return BigDecimal.valueOf(d);

        } catch (java.text.ParseException e) {
            // no number!
        }
        return null;
    }

}
