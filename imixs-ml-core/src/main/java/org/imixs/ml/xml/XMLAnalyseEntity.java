package org.imixs.ml.xml;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * { "label": "_invoicedate", "text": "0002" },
 * 
 * @author rsoika
 *
 */
@XmlRootElement(name = "analyseresponse")
public class XMLAnalyseEntity {

    private String label;

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

    private String text;

    public XMLAnalyseEntity() {
        super();

    }

    /**
     * Returns the text value of the entity
     * @return - text value
     */
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
