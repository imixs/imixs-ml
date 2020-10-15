package org.imixs.ml.core.adapters;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

import org.imixs.ml.adapters.DateAdapter;
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

        List<Locale> locals = new ArrayList<Locale>();
        locals.add(Locale.UK);
        locals.add(Locale.GERMAN);

        Set<String> enityVariants = new HashSet<String>();

        Date date = null;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, 4);
        cal.set(Calendar.DAY_OF_MONTH, 3);
        cal.set(Calendar.YEAR, 2020);
        date = cal.getTime();

        dateAdapter.onEvent(new EntityObjectEvent(date, enityVariants, locals));

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

    /**
     * Test conversion of German long date "26. Januar 2017" to Date object
     * 
     * 
     */
    @Test
    public void testGermanLongFomatToDate() {
        List<Locale> locals = new ArrayList<Locale>();
        locals.add(Locale.UK);
        locals.add(Locale.GERMAN);

        List<String> itemValueList = new ArrayList<String>();
        itemValueList.add("26. Januar 2017");
        DateAdapter dateAdapter = new DateAdapter();

        EntityTextEvent entityTextEvent = new EntityTextEvent(itemValueList, locals, "date");

        dateAdapter.onTextEvent(entityTextEvent);
        Object o = entityTextEvent.getItemValue();

        Assert.assertTrue(o instanceof Date);

        Calendar cal = Calendar.getInstance();
        cal.setTime((Date) o);

        Assert.assertEquals(2017, cal.get(Calendar.YEAR));
        Assert.assertEquals(0, cal.get(Calendar.MONTH));
        Assert.assertEquals(26, cal.get(Calendar.DAY_OF_MONTH));

    }

}
