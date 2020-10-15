package org.imixs.ml.core.adapters;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.imixs.ml.adapters.IBANAdapter;
import org.imixs.ml.events.EntityObjectEvent;
import org.imixs.workflow.exceptions.PluginException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for the XMLTrainingService
 * 
 * @author rsoika
 */
public class TestIBANAdapter {
    private static Logger logger = Logger.getLogger(TestIBANAdapter.class.getName());

    protected IBANAdapter ibanAdapter = null;

    @Before
    public void setup() throws PluginException {
        ibanAdapter = new IBANAdapter();
    }

    /**
     * test variants
     */
    @Test
    public void testVariants() {

        Set<String> enityVariants = new HashSet<String>();
        
       String iban="DE69 5095 1469 0005 0111 11";

       ibanAdapter.onEvent(new EntityObjectEvent(iban, enityVariants,null));

        for (String variant : enityVariants) {
            logger.info(variant);

        }
        
        Assert.assertEquals(2, enityVariants.size());

        Assert.assertTrue(enityVariants.contains("DE69 5095 1469 0005 0111 11"));
        Assert.assertTrue(enityVariants.contains("DE69509514690005011111"));
    }

}
