package org.imixs.ml.workflow;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertEquals(4, result.size());
        Assert.assertEquals("Kraxi", result.get(0));
        Assert.assertEquals("Kraxi GmbH", result.get(1));
      
        
        // second example
        text="12345 Papierfeld · Deutschland Flugzeugallee 17 12345 Papierfeld Deutschland Tel. (0123) Papierflieger-Vertriebs-GmbH ";
        result = MLController.findMatches("pap", text);
        Assert.assertEquals(9, result.size());
        Assert.assertEquals("Papierfeld", result.get(0));
        Assert.assertEquals("Papierflieger-Vertriebs-GmbH", result.get(8));
        
        
      
    }
    
    
    
    /**
     * Test the findText method
     * 
     * 
     */
    @Test
    public void testFindTextWithMultiplSpaces() {
        List<String>  result = null;
        String  text="12345 Papierfeld · Deutschland Flugzeugallee 17 12345 Papierfeld Deutschland - IBAN XY44 1020 4426 0044 1002 0399 4444\n - Tel.(0123) Papierflieger-Vertriebs-GmbH ";
        
        
        result=MLController.findMatches("XY", text);
        Assert.assertEquals(7, result.size());
        Assert.assertEquals("XY44", result.get(0));
        Assert.assertEquals("XY44 1020", result.get(1));
        Assert.assertEquals("XY44 1020 4426", result.get(2));
        Assert.assertEquals("XY44 1020 4426 0044 1002 0399 4444", result.get(6));
        
        
        text="12345 Papierfeld · IBAN: XY44 1020 4426 0044 1002 0399 4444\n"
                + "BANK: PKO BP S.A.";
        
         result = MLController.findMatches("XY", text);
        Assert.assertEquals(7, result.size());
        Assert.assertEquals("XY44", result.get(0));
        Assert.assertEquals("XY44 1020", result.get(1));
        Assert.assertEquals("XY44 1020 4426", result.get(2));
        Assert.assertEquals("XY44 1020 4426 0044 1002 0399 4444", result.get(6));
      
    }

}
