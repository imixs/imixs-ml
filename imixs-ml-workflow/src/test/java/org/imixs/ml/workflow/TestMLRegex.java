package org.imixs.ml.workflow;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.imixs.ml.xml.XMLAnalyseEntity;
import org.junit.Assert;
import org.junit.Test;

import util.LocaleHelper;

/**
 * Test the ml regex
 * 
 * @author rsoika
 * 
 */
public class TestMLRegex {

    /**
     * Test locale parser
     * 
     * 
     */
    @Test
    public void testParseMLRegex() {

        String text = "IBAN: DE11 5021 0900 0000 4910 29 Swift: CIXXDEFF and a second one CIXXDEAA-";
        String patternString = "[A-Z0-9]{4}[A-Z]{2}[A-Z0-9]{2}(?:[A-Z0-9]{3})?";

        // no value found so far - so apply the regex pattern...
        Pattern p = Pattern.compile(patternString); // the pattern to search for
        Matcher m = p.matcher(text);
        // now try to find at least one match
        while (m.find()) {
            String value = m.group();
            Assert.assertNotNull(value);
            System.out.println("value=" + value);
        }
    }

}
