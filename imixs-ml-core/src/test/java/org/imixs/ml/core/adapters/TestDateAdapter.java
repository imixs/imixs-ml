package org.imixs.ml.core.adapters;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

import org.imixs.ml.adapters.DateAdapter;
import org.imixs.ml.events.EntityObjectEvent;
import org.imixs.workflow.exceptions.PluginException;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

/**
 * Test class for the XMLTrainingService
 * 
 * @author rsoika
 */
public class TestDateAdapter {
    private static Logger logger = Logger.getLogger(TestDateAdapter.class.getName());

    protected DateAdapter dateAdapter = null;

    @Before
    public void setup() throws PluginException {
        dateAdapter = new DateAdapter();

    }

    /**
     * test variants of 30-05-2020
     */
    @Test
    public void testVariants() {
        
        
        Set<Locale> locals=new HashSet<Locale>();
        locals.add(Locale.UK);
        locals.add(Locale.GERMAN);

        Set<String> enityVariants = new HashSet<String>();
        
        Date date=null;
        Calendar cal=Calendar.getInstance();
        cal.set(Calendar.MONTH,4);
        cal.set(Calendar.DAY_OF_MONTH,3);
        cal.set(Calendar.YEAR,2020);
        date=cal.getTime();

         dateAdapter.onEvent(new EntityObjectEvent(date, enityVariants,locals));

        for (String variant : enityVariants) {
            logger.info(variant);

        }
        
        Assert.assertEquals(9, enityVariants.size());

        Assert.assertTrue(enityVariants.contains("3 May 2020"));
        Assert.assertTrue(enityVariants.contains("3. May 2020"));
        Assert.assertTrue(enityVariants.contains("3 Mai 2020"));
        Assert.assertTrue(enityVariants.contains("3. Mai 2020"));
        Assert.assertTrue(enityVariants.contains("03.05.2020"));
    }

}
