package org.imixs.ml.xml;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * { "label": "invoice.total", "text": "42.00" },
 * 
 * @author rsoika
 *
 */
@XmlRootElement(name = "analyseresponse")
public class XMLAnalyseEntity {

    private String label;
    private String text;

    public XMLAnalyseEntity() {
        super();
    }

    /**
     * Returns the name of the entity.
     * 
     * @return - name of this entity
     */
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Returns the text value of the entity
     * 
     * @return - text value
     */
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
