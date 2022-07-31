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
        locals.add(Locale.GERMANY);

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

        Assert.assertTrue(enityVariants.size()>16);

        Assert.assertTrue(enityVariants.contains("3 May 2020"));
        Assert.assertTrue(enityVariants.contains("3. May 2020"));
        Assert.assertTrue(enityVariants.contains("3 Mai 2020"));
        Assert.assertTrue(enityVariants.contains("3. Mai 2020"));
        Assert.assertTrue(enityVariants.contains("03.05.2020"));
        // special formats
        Assert.assertTrue(enityVariants.contains("MAY. 03, 2020"));
        Assert.assertTrue(enityVariants.contains("03/MAY/2020"));
        Assert.assertTrue(enityVariants.contains("03/05/2020"));
        Assert.assertTrue(enityVariants.contains("03 MAY 2020"));
        Assert.assertTrue(enityVariants.contains("03-MAY-2020"));
        Assert.assertTrue(enityVariants.contains("03-05-2020"));
        
        Assert.assertTrue(enityVariants.contains("MAI 03, 2020"));

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
        locals.add(Locale.GERMANY);

        List<String> itemValueList = new ArrayList<String>();
        itemValueList.add("26. Januar 2017");
        DateAdapter dateAdapter = new DateAdapter();
        EntityTextEvent entityTextEvent = new EntityTextEvent(itemValueList, locals, "date", 0);
        dateAdapter.onTextEvent(entityTextEvent);
        Object o = entityTextEvent.getItemValue();
        Assert.assertTrue(o instanceof Date);
        Calendar cal = Calendar.getInstance();
        cal.setTime((Date) o);
        Assert.assertEquals(2017, cal.get(Calendar.YEAR));
        Assert.assertEquals(0, cal.get(Calendar.MONTH));
        Assert.assertEquals(26, cal.get(Calendar.DAY_OF_MONTH));

    }

    /**
     * Test conversion of UK Date long date "APR. 14, 2021" to Date object
     * 
     * 
     * Wed Apr 14 00:00:00 CEST 2021
     * 
     * 
     */
    @Test
    public void testUKSpecialFomatToDate() {
        List<Locale> locals = new ArrayList<Locale>();
        locals.add(Locale.UK);
        locals.add(Locale.US);
        locals.add(Locale.GERMANY);

        List<String> itemValueList = new ArrayList<String>();
        itemValueList.add("APR. 14, 2021");
        DateAdapter dateAdapter = new DateAdapter();
        EntityTextEvent entityTextEvent = new EntityTextEvent(itemValueList, locals, "date", 0);
        dateAdapter.onTextEvent(entityTextEvent);
        Object o = entityTextEvent.getItemValue();
        Assert.assertTrue(o instanceof Date);
        Calendar cal = Calendar.getInstance();
        cal.setTime((Date) o);
        Assert.assertEquals(2021, cal.get(Calendar.YEAR));
        Assert.assertEquals(3, cal.get(Calendar.MONTH));
        Assert.assertEquals(14, cal.get(Calendar.DAY_OF_MONTH));

        // 14/APR/2021
        itemValueList = new ArrayList<String>();
        itemValueList.add("14/APR/2021");
        dateAdapter = new DateAdapter();
        entityTextEvent = new EntityTextEvent(itemValueList, locals, "date", 0);
        dateAdapter.onTextEvent(entityTextEvent);
        o = entityTextEvent.getItemValue();
        Assert.assertTrue(o instanceof Date);
        cal = Calendar.getInstance();
        cal.setTime((Date) o);
        Assert.assertEquals(2021, cal.get(Calendar.YEAR));
        Assert.assertEquals(3, cal.get(Calendar.MONTH));
        Assert.assertEquals(14, cal.get(Calendar.DAY_OF_MONTH));

        // reverse order of year/month/day
        // 2021-04-14
        itemValueList = new ArrayList<String>();
        itemValueList.add("2021-04-14");
        dateAdapter = new DateAdapter();
        entityTextEvent = new EntityTextEvent(itemValueList, locals, "date", 0);
        dateAdapter.onTextEvent(entityTextEvent);
        o = entityTextEvent.getItemValue();
        Assert.assertTrue(o instanceof Date);
        cal = Calendar.getInstance();
        cal.setTime((Date) o);
        Assert.assertEquals(2021, cal.get(Calendar.YEAR));
        Assert.assertEquals(3, cal.get(Calendar.MONTH));
        Assert.assertEquals(14, cal.get(Calendar.DAY_OF_MONTH));


        // 14-04-2021
        itemValueList = new ArrayList<String>();
        itemValueList.add("14-04-2021");
        dateAdapter = new DateAdapter();
        entityTextEvent = new EntityTextEvent(itemValueList, locals, "date", 0);
        dateAdapter.onTextEvent(entityTextEvent);
        o = entityTextEvent.getItemValue();
        Assert.assertTrue(o instanceof Date);
        cal = Calendar.getInstance();
        cal.setTime((Date) o);
        Assert.assertEquals(2021, cal.get(Calendar.YEAR));
        Assert.assertEquals(3, cal.get(Calendar.MONTH));
        Assert.assertEquals(14, cal.get(Calendar.DAY_OF_MONTH));

    }




    /**
     * Test YYYY at start or end position
     * Compare  beween
     * 
     * yyyy-MM-dd  and dd-MM-yyyy
     * 
     */
    @Test
    public void testYYYYPostion() {
        List<Locale> locals = new ArrayList<Locale>();
        locals.add(Locale.UK);
        locals.add(Locale.US);
        locals.add(Locale.GERMANY);

      
        // reverse order of year/month/day
        // 2021-04-14
        ArrayList<String> itemValueList = new ArrayList<String>();
        itemValueList.add("2021-04-14");
        dateAdapter = new DateAdapter();
        EntityTextEvent entityTextEvent = new EntityTextEvent(itemValueList, locals, "date", 0);
        dateAdapter.onTextEvent(entityTextEvent);
        Object o = entityTextEvent.getItemValue();
        Assert.assertTrue(o instanceof Date);
        Calendar cal = Calendar.getInstance();
        cal.setTime((Date) o);
        Assert.assertEquals(2021, cal.get(Calendar.YEAR));
        Assert.assertEquals(3, cal.get(Calendar.MONTH));
        Assert.assertEquals(14, cal.get(Calendar.DAY_OF_MONTH));


        // 14-04-2021
        itemValueList = new ArrayList<String>();
        itemValueList.add("14-04-2021");
        dateAdapter = new DateAdapter();
        entityTextEvent = new EntityTextEvent(itemValueList, locals, "date", 0);
        dateAdapter.onTextEvent(entityTextEvent);
        o = entityTextEvent.getItemValue();
        Assert.assertTrue(o instanceof Date);
        cal = Calendar.getInstance();
        cal.setTime((Date) o);
        Assert.assertEquals(2021, cal.get(Calendar.YEAR));
        Assert.assertEquals(3, cal.get(Calendar.MONTH));
        Assert.assertEquals(14, cal.get(Calendar.DAY_OF_MONTH));

    }

    /**
     * Test invalid date string
     * 
     * 
     */
    @Test
    public void testInvalidDateString() {
        List<Locale> locals = new ArrayList<Locale>();
        locals.add(Locale.UK);
        locals.add(Locale.GERMANY);

        List<String> itemValueList = new ArrayList<String>();
        itemValueList.add("1005.55");
        DateAdapter dateAdapter = new DateAdapter();

        EntityTextEvent entityTextEvent = new EntityTextEvent(itemValueList, locals, "date", 0);

        dateAdapter.onTextEvent(entityTextEvent);
        Object o = entityTextEvent.getItemValue();

        Assert.assertNull(o);

    }
    

    /**
     * Test conversion of German long date "MAI 18, 2021" to Date object
     * 
     * 
     */
    @Test
    public void testGerman18Mai2021() {
        List<Locale> locals = new ArrayList<Locale>();
        locals.add(Locale.GERMANY);

        List<String> itemValueList = new ArrayList<String>();
        itemValueList.add("MAI 18, 2021");
        DateAdapter dateAdapter = new DateAdapter();
        EntityTextEvent entityTextEvent = new EntityTextEvent(itemValueList, locals, "date", 0);
        dateAdapter.onTextEvent(entityTextEvent);
        Object o = entityTextEvent.getItemValue();
        Assert.assertTrue(o instanceof Date);
        Calendar cal = Calendar.getInstance();
        cal.setTime((Date) o);
        Assert.assertEquals(2021, cal.get(Calendar.YEAR));
        Assert.assertEquals(4, cal.get(Calendar.MONTH));
        Assert.assertEquals(18, cal.get(Calendar.DAY_OF_MONTH));

        
        // test text variants...
        Set<String> enityVariants = new HashSet<String>();
        dateAdapter.onEvent(new EntityObjectEvent(cal.getTime(), enityVariants, locals));

        for (String variant : enityVariants) {
            logger.info(variant);
        }

        Assert.assertTrue(enityVariants.size()>16);

    }


}
