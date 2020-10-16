package org.imixs.ml.workflow;

import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

import util.LocaleHelper;

/**
 * Test the ml service
 * 
 * @author rsoika
 * 
 */
public class TestMLService {
    
    /**
     * Test locale parser
     * 
     * 
     */
    @Test
    public void testParseMLLocales() {

        String sLocales = "de_DE,en_GB";
        
        List<Locale> locales = LocaleHelper.parseLocales(sLocales);

        Assert.assertEquals(2, locales.size());

        Assert.assertEquals(Locale.GERMANY, locales.get(0));
        Assert.assertEquals(Locale.UK, locales.get(1));

        
      
    }

}
