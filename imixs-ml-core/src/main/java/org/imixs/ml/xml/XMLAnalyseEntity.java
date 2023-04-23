package org.imixs.ml.xml;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

/**
 * The XMLAnalyseEntity defines the Data structure for an entity element
 * contained in a XMLAnalyseResult object.
 * 
 * <pre>
 * { "entity": "label", "text": "some value..." }
 * </pre>
 * 
 * 
 * @author rsoika
 *
 */
@XmlRootElement(name = "entity")
public class XMLAnalyseEntity {
    
    @XmlTransient
    private static final long serialVersionUID = 1L;

    private String label;
    private String text;

    public XMLAnalyseEntity() {
        super();
    }

    public XMLAnalyseEntity(String label, String text) {
        super();
        this.label = label;
        this.text = text;
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
