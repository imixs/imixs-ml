package org.imixs.ml.core;

import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.imixs.workflow.FileData;
import org.imixs.workflow.ItemCollection;

/**
 * This builder class builds the text content from a workitem to be used for
 * training and analyses
 * 
 * @author rsoika
 *
 */
public class MLContentBuilder {
    private static Logger logger = Logger.getLogger(MLContentBuilder.class.getName());

    private List<String> itemNames = null;
    private boolean ignoreFiles = false;
    private Pattern filenamePattern = null;
    private ItemCollection workitem = null;

    /**
     * Construct a new Bulder instance to buidl a ML content
     * 
     * @param itemNames
     * @param ignoreFiles
     */
    public MLContentBuilder(ItemCollection workitem, List<String> itemNames, boolean ignoreFiles,
            Pattern mlFilenamePattern) {
        super();
        this.workitem = workitem;
        this.itemNames = itemNames;
        this.ignoreFiles = ignoreFiles;
        this.filenamePattern = mlFilenamePattern;
    }

    /**
     * This method builds a new text content based on a given workiem. The method
     * build the content form the ml-content items and the file attachments.
     * <p>
     * File attachments can be ignored setting the flag 'ignorefiles'.
     * 
     * @return - text content
     */
    public String build() {
        String mlContent = "";

        // first build content form provided items...
        if (itemNames != null && itemNames.size() > 0) {
            for (String itemName : itemNames) {
                mlContent = mlContent + itemName + ": \n";
                List<String> values = workitem.getItemValueList(itemName, String.class);
                for (String value : values) {
                    mlContent = mlContent + value + "\n";
                }
            }
            mlContent = mlContent + "\n\n";
        }

        // now we add the filedata ...
        if (!ignoreFiles) {
            List<FileData> files = workitem.getFileData();
            if (files != null && files.size() > 0) {

                // aggregate all text attributes form attached files
                // apply an optional regex for filenames
                for (FileData file : files) {
                    // test if the filename matches the pattern or the pattern is null
                    if (filenamePattern == null || filenamePattern.matcher(file.getName()).find()) {
                        logger.info("...analyzing content of '" + file.getName() + "'.....");
                        ItemCollection metadata = new ItemCollection(file.getAttributes());
                        String _text = metadata.getItemValueString("text");
                        if (!_text.isEmpty()) {
                            mlContent = mlContent + _text + " \n\n";
                        }
                    }
                 
                }
            }
        }

        return mlContent;

    }
}
