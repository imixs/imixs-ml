package org.imixs.ml.workflow;

import java.util.List;
import java.util.logging.Logger;

import org.junit.Test;

import junit.framework.Assert;

/**
 * Test the SnapshotService EJB. The WorkflowArchiveMockEnvironment provides a
 * workflowService and Database mock.
 * 
 * @author rsoika
 * 
 */
public class TestMLController {
    
    /**
     * Test the findText method
     * 
     * 
     */
    @Test
    public void testFindText() {

        String text = "The Catalog is big.";

        List<String> result = MLController.findMatches("Cat", text);
        Assert.assertEquals("Catalog", result.get(0));

        result = MLController.findMatches("cat", text);
        Assert.assertEquals("Catalog", result.get(0));
        
        // 3rd example
        text="Kraxi GmbH Kraxi GmbH ";
        result = MLController.findMatches("kra", text);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("Kraxi", result.get(0));
        Assert.assertEquals("Kraxi GmbH", result.get(1));
      
        
        // second example
        text="12345 Papierfeld · Deutschland Flugzeugallee 17 12345 Papierfeld Deutschland Tel. (0123) Papierflieger-Vertriebs-GmbH ";
        result = MLController.findMatches("pap", text);
        Assert.assertEquals(3, result.size());
        Assert.assertEquals("Papierfeld", result.get(0));
        Assert.assertEquals("Papierflieger-Vertriebs-GmbH", result.get(2));
        
        
      
    }

}
