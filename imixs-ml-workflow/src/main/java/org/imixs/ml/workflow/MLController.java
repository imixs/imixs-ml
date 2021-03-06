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

import org.imixs.workflow.ItemCollection;
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
            // iterate over all mlDefinitions 
            List<ItemCollection> mlDefinitionList = mlService.getMLDefinitions(workflowController.getWorkitem());
            for (ItemCollection mlDefinition : mlDefinitionList) {
                List<String> mlItems = mlDefinition.getItemValue(MLService.ITEM_ML_ITEMS);
                return mlItems.contains(name);
            }
        }
        return false;
    }

    /**
     * Returns a JSON object containing the current ml result status and the item
     * names collected by the MLAdatper.
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
    public String getMLResult() {
        String result = "{";
        String status=MLService.ML_STATUS_CONFIRMED;
        if (workflowController.getWorkitem() != null) {
            List<String> mlItems =new ArrayList<String>();

            // iterate over all mlDefinitions.
            List<ItemCollection> mlDefinitionList = mlService.getMLDefinitions(workflowController.getWorkitem());
            for (ItemCollection mlDefinition : mlDefinitionList) {
                mlItems.addAll(mlDefinition.getItemValue(MLService.ITEM_ML_ITEMS));
                if (MLService.ML_STATUS_SUGGEST.equals(mlDefinition.getItemValueString(MLService.ITEM_ML_STATUS))) {
                    status=MLService.ML_STATUS_SUGGEST;
                }
            }
            
            // set status
            result = result + "\"status\":\""
                    + status + "\"";

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

        String text = mlService.getAllDocumentText(workflowController.getWorkitem());

        if (text != null) {
            searchResult = findMatches(phrase, text);
        }

    }

    /**
     * Returns a matching text sequence form a search phrase
     * <p>
     * e.g. 'cat' is found in 'Catalog'
     * <p>
     * The method also searches for computed phrases based on the first hit.
     * <p>
     * It seems that this kind of problem can not be solved with regex.
     * 
     * @param data
     * @return
     */
    public static List<String> findMatches(String phrase,final String _text) {

        List<String> result = new ArrayList<String>();

        String text=_text;
        // replace \n with space
        text=text.replace("\n", " ");
        
        // search text is lower case!
        String searchText = text.toLowerCase();
       
        
        String searchPhrase = phrase.toLowerCase();
        String originSearchPhrase = searchPhrase;

        // find start pos...
        int index = 0;
        while (true) {
            int found = searchText.indexOf(searchPhrase, index);
            if (found > -1) {
                String hit = null;

                // take the phrase up to the space
                int endPos = searchText.indexOf(" ", found + searchPhrase.length() + 1);
                // if we do not found a space, we search for a .
                if (endPos == -1) {
                    endPos = searchText.indexOf(".", found + searchPhrase.length() + 1);
                }
                if (endPos > -1) {
                    hit = text.substring(found, endPos).trim();
                } else {
                    // take it as is
                    hit = text.substring(found).trim();

                }

                // if the hit is longer than 32 chars - we cut it....
                if (hit.length() > 32) {
                    hit = hit.substring(0, 32).trim();
                }

                if (!result.contains(hit)) {
                    result.add(hit);

                    // lets see if it makes sense to search for variant with spaces
                    if (!searchPhrase.endsWith(" ")) {
                        searchPhrase = hit.toLowerCase() + " ";
                    } else {
                        // reset to origin search phrase
                        searchPhrase = originSearchPhrase;
                        index = found + hit.length();
                    }
                } else {
                    index = found + hit.length();
                }

            } else {
                // no more matches
                break;
            }

            // if max count of 7 matches is reached we break;
            if (result.size() >= 7) {
                break;
            }

        }
        return result;
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

}
