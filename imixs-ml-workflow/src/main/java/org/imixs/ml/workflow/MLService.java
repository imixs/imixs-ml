package org.imixs.ml.workflow;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.Model;
import org.imixs.workflow.engine.ModelService;
import org.imixs.workflow.engine.ProcessingEvent;
import org.imixs.workflow.exceptions.ModelException;

/**
 * The MLService reacts on Processing events and updates the  ml.status item. If no ml.status item
 * exists, and ml.items is not empty, than the status is set to 'suggest'. If the
 * status is 'suggest' and the current event is 'public' than the status is set
 * to 'confirmed'
 * 
 * @author rsoika
 *
 */
@Stateless
@LocalBean
public class MLService implements Serializable {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(MLService.class.getName());

 
    @Inject
    protected ModelService modelService;

    /**
     * WorkflowEvent listener to update the ml.status property.
     * <p>
     * If no ml.status item exists, and ml.item is not empty, than the status is set
     * to 'suggest'
     * <p>
     * If the status is 'suggest' and the current event is 'public' than the status
     * is set to 'confirmed'
     * 
     * @param processingEvent
     */
    @SuppressWarnings("unchecked")
    public void onWorkflowEvent(@Observes ProcessingEvent processingEvent) {
        if (processingEvent == null)
            return;

        ItemCollection workitem = processingEvent.getDocument();

        // skip if not a workItem...
        if (workitem != null && !workitem.getItemValueString("type").startsWith("workitem"))
            return;

        int eventType = processingEvent.getEventType();

        // if it was an public event we set the ml.status to 'verified'
        List<String> mlItems = workitem.getItemValue(MLAdapter.ITEM_ML_ITEMES);

        // set initial status?
        if (ProcessingEvent.AFTER_PROCESS == eventType) {
            if (mlItems.size() > 0 && workitem.getItemValueString(MLAdapter.ITEM_ML_STATUS).isEmpty()) {
                workitem.setItemValue(MLAdapter.ITEM_ML_STATUS, MLAdapter.ML_STATUS_SUGGEST);
                return;
            }
        }

        // set confirmed status?
        if (ProcessingEvent.BEFORE_PROCESS == eventType) {
            if (mlItems.size() > 0
                    && MLAdapter.ML_STATUS_SUGGEST.equals(workitem.getItemValueString(MLAdapter.ITEM_ML_STATUS))) {
                // test if we have a public event
                Model model;
                try {
                    model = modelService.getModelByWorkitem(workitem);
                    ItemCollection event = model.getEvent(workitem.getTaskID(), workitem.getEventID());
                    // ad only activities with userControlled != No
                    if (!"0".equals(event.getItemValueString("keypublicresult"))) {
                        // update status
                        workitem.setItemValue(MLAdapter.ITEM_ML_STATUS, MLAdapter.ML_STATUS_CONFIRMED);
                        return;
                    }
                } catch (ModelException e) {
                    logger.warning("unable to parse current bpmn event: " + e.getMessage());
                }
            }

        }

    }

}
