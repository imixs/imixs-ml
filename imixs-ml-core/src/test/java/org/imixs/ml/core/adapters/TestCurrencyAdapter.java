package org.imixs.ml.core.adapters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

import org.imixs.ml.adapters.CurrencyAdapter;
import org.imixs.ml.events.EntityObjectEvent;
import org.imixs.ml.events.EntityTextEvent;
import org.imixs.workflow.exceptions.PluginException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

        float f = 1700411;

        List<Locale> locals = new ArrayList<Locale>();
        locals.add(Locale.UK);
        locals.add(Locale.GERMANY);

        currencyAdapter.onObjectEvent(new EntityObjectEvent(f, enityVariants, locals));

        for (String variant : enityVariants) {
            logger.info(variant);

        }

        Assert.assertEquals(4, enityVariants.size());

        Assert.assertTrue(enityVariants.contains("1.700.411,00"));
    }

    /**
     * test US currency conversion from 663.52 to float
     */
    @Test
    public void testUSCurrency() {
        List<Locale> locals = new ArrayList<Locale>();
        locals.add(Locale.GERMANY);
        locals.add(Locale.UK);
        locals.add(Locale.US);
        
        List<String> itemValueList = new ArrayList<String>();
        itemValueList.add("663.52");
        CurrencyAdapter currencyAdapter = new CurrencyAdapter();

        EntityTextEvent entityTextEvent = new EntityTextEvent(itemValueList, locals, "currency",0);
        currencyAdapter.onTextEvent(entityTextEvent);
        Object o = entityTextEvent.getItemValue();

        Assert.assertTrue(o instanceof Float);

        Float f = (Float) o;
        Assert.assertEquals(new Float("663.52"), f);
    }
    
    
  
    /**
     * test German currency conversion from 663,52 to float
     */
    @Test
    public void testGermanCurrency() {
        List<Locale> locals = new ArrayList<Locale>();
        locals.add(Locale.UK); // we switch locales here to see if German locale is detected correctly
        locals.add(Locale.GERMANY);

        List<String> itemValueList = new ArrayList<String>();
        itemValueList.add("663,52");
        CurrencyAdapter currencyAdapter = new CurrencyAdapter();

        EntityTextEvent entityTextEvent = new EntityTextEvent(itemValueList, locals, "currency",0);
        currencyAdapter.onTextEvent(entityTextEvent);
        Object o = entityTextEvent.getItemValue();

        Assert.assertTrue(o instanceof Float);

        Float f = (Float) o;
        Assert.assertEquals(new Float("663.52"), f);
    }

}
