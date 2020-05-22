package org.imixs.ml.service;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.imixs.ml.adapters.CurrencyAdapter;
import org.imixs.workflow.exceptions.PluginException;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

/**
 * Test class for the XMLTrainingService
 * 
 * @author rsoika
 */
public class TestCurrencyAdapter {
    private static Logger logger = Logger.getLogger(TestCurrencyAdapter.class.getName());

    protected CurrencyAdapter currencyAdapter = null;

    @Before
    public void setup() throws PluginException {
        currencyAdapter = new CurrencyAdapter();

    }

    /**
     * test variants
     */
    @Test
    public void testVariants() {

        Set<String> enityVariants = new HashSet<String>();
        
        float f=1700411;

        currencyAdapter.onEvent(new AnalyzeEntityEvent(f, enityVariants));

        for (String variant : enityVariants) {
            logger.info(variant);

        }
        
        Assert.assertEquals(4, enityVariants.size());

        Assert.assertTrue(enityVariants.contains("1.700.411,00"));
    }

}
