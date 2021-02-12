package org.imixs.ml.xml;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * The XMLAnalyseCategory defines the Data structure for an category element
 * contained in a XMLAnalyseResult object.
 * 
 * <pre>
 * { "category": "some cat...", "score": "0.7920878529548645" }
 * </pre>
 * 
 * @author rsoika
 *
 */
@XmlRootElement(name = "category")
public class XMLAnalyseCategory {

    @XmlTransient
    private static final long serialVersionUID = 1L;

    private String label;
    private double score;

    public XMLAnalyseCategory() {
        super();
    }

    public XMLAnalyseCategory(String label, double score) {
        super();
        this.label = label;
        this.score = score;
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
     * Returns the score value of the category
     * 
     * @return - score value
     */
    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

}
