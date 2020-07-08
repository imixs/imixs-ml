package org.imixs.ml.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.enterprise.context.ConversationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.imixs.workflow.FileData;
import org.imixs.workflow.faces.data.WorkflowController;

/**
 * The MLController CDI Bean provides Front-End methods for the ml integration.
 * The method getJSON returns a sringified JSON object providing the mls status
 * and ml items.
 * <p>
 * The method search provides a suggest list searching a phrase within the
 * document content of the current workitem.
 * 
 * @author rsoika
 *
 */
@Named("mlController")
//@RequestScoped
@ConversationScoped
public class MLController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(MLController.class.getName());

    @Inject
    protected WorkflowController workflowController;
    
    @Inject
    protected MLService mlService;

    private List<String> searchResult = null;

    /**
     * This method returns true if the item value for a given item was computed by
     * the MLAdatper
     * 
     * @param name
     * @return true if the item name contains a ml value
     */
    @SuppressWarnings("unchecked")
    public boolean isMLItem(String name) {

        if (workflowController.getWorkitem() != null) {
            List<String> mlItems = workflowController.getWorkitem().getItemValue(MLAdapter.ITEM_ML_ITEMES);
            return mlItems.contains(name);
        }
        return false;
    }

    /**
     * Returns a JSON object containing the current ml status and the item names
     * collected by the MLAdatper.
     * <p>
     * 
     * <pre>
     * {@code
     *  {"status":"suggest",
     *   "items": [
     *              "_invoicenumber",
     *              "_invoicetotal"
     *            ]
     *  }
     * }
     * </pre>
     * <p>
     * The following stati are defined:
     * <ul>
     * <li>suggest - not yet confirmed by the user
     * <li>confirmed - confirmed by the user
     * <li>training - workitem is ready for a training
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public String getJSON() {
        String result = "{";

        if (workflowController.getWorkitem() != null) {
            List<String> mlItems = workflowController.getWorkitem().getItemValue(MLAdapter.ITEM_ML_ITEMES);

            // set status
            result = result + "\"status\":\""
                    + workflowController.getWorkitem().getItemValueString(MLAdapter.ITEM_ML_STATUS) + "\"";

            if (mlItems != null && mlItems.size() > 0) {
                result = result + ",\"items\":";
                // create a json value array out of the list of item names...
                result = result + mlItems.stream().map(n -> "\"" + n + "\"").collect(Collectors.joining(",", "[", "]"));
            }

        } else {
            // no workitem!
            result = result + "\"status\":\"none\"";
        }

        result = result + "}";
        return result;
    }

    /**
     * This method searches a text phrase within the document content of attached
     * documents. The textphrase is extracted from the RequestParamterMap.
     * <p>
     * JSF Integration:
     * 
     * {@code 
     * 
     * <h:commandScript name="imixsOfficeWorkflow.mlSearch" action=
     * "#{mlController.search()}" rendered="#{mlController!=null}" render=
     * "ml-results" /> }
     * 
     * <p>
     * JavaScript Example:
     * 
     * <pre>
     * {@code
     *  imixsOfficeWorkflow.mlSearch({ item: '_invoicenumber' })
     *  }
     * </pre>
     * 
     */
    public void search() {

        // get the param from faces context....
        FacesContext fc = FacesContext.getCurrentInstance();
        String phrase = fc.getExternalContext().getRequestParameterMap().get("phrase");

        logger.info("search prase '" + phrase + "'");

        // String input =workflowController.getWorkitem().getItemValueString(itemName);
        if (phrase == null || phrase.length() < 2) {
            return;
        }

        logger.finest(".......triger search...");
        logger.fine("search for=" + phrase);
        searchResult = new ArrayList<String>();
        
        String text=getAllDocumentText();
        if (text!=null) {
            text=text.toLowerCase();
            int startPos=0;
            while (true) {
                // find up to 5 variants....
                int found=text.indexOf(phrase,startPos);
                if (found>-1) {
                    startPos=found+1;
                    
                    // take until next space 
                    String hit=text.substring(found ,text.indexOf( " ",startPos+1) );
                    
                    // the hit may not contain new lines...
                    
                    
                    
                    if (!searchResult.contains(hit)) {
                        searchResult.add(hit);
                    }
                    
                } else {
                    // no more found!
                    break;
                }
                
                // max 5 results!
                if (searchResult.size()>=5) {
                    break;
                }
            }
         }

       

    }

    public List<String> getSearchResult() {
        return searchResult;
    }

    /**
     * This method reset the search and input state.
     */
    public void reset() {
        searchResult = new ArrayList<String>();
        logger.fine("reset");
    }

    /**
     * Retruns a string with all the document text
     * 
     * @return
     */
    @SuppressWarnings("rawtypes")
    private String getAllDocumentText() {
        if (workflowController.getWorkitem() == null) {
            return null;
        }

        String result = "";
        List<FileData> fileDataList = workflowController.getWorkitem().getFileData();

        for (FileData fileData : fileDataList) {
            List fileText = (List) fileData.getAttribute("text");
            if (fileText != null && fileText.size()>0) {
                result = result + fileText.get(0) + " ";
            }
        }
        // clean the text form unsupported characters
        return mlService.cleanTextdata(result);
    }

}
