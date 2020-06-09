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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
