/*  
 *  Imixs-Workflow 
 *  
 *  Copyright (C) 2001-2020 Imixs Software Solutions GmbH,  
 *  http://www.imixs.com
 *  
 *  This program is free software; you can redistribute it and/or 
 *  modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation; either version 2 
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 *  General Public License for more details.
 *  
 *  You can receive a copy of the GNU General Public
 *  License at http://www.gnu.org/licenses/gpl.html
 *  
 *  Project: 
 *      https://www.imixs.org
 *      https://github.com/imixs/imixs-workflow
 *  
 *  Contributors:  
 *      Imixs Software Solutions GmbH - Project Management
 *      Ralph Soika - Software Developer
 */

package org.imixs.ml.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

/**
 * The XMLEntity defines the core Data class used for training data.
 * <p>
 * The setter methods prettify the text and entities provided to the
 * XMLTrainingData. This is important to ensure the JSON result string is
 * parseable by SpaCy!.
 * 
 * 
 * @author: ralph.soika@imixs.com
 * @version: 1.0
 */
@XmlRootElement(name = "trainingdata")
public class XMLTrainingData implements Serializable {

    @XmlTransient
    public static final byte TRAININGDATA_QUALITY_GOOD = 10;
    @XmlTransient
    public static final byte TRAININGDATA_QUALITY_LOW = 4;
    @XmlTransient
    public static final byte TRAININGDATA_QUALITY_BAD = 0;

    @XmlTransient
    private static final long serialVersionUID = 1L;

    @XmlTransient
    private int quality = 0;

    private String text;
    private List<XMLTrainingEntity> entities;
    private List<XMLTrainingCategory> categories;

    public XMLTrainingData() {
        super();
        // init entities
        entities = new ArrayList<XMLTrainingEntity>();
        // init categories
        categories = new ArrayList<XMLTrainingCategory>();
    }

    public String getText() {
        return text;
    }

    public void setText(String _text) {
        if (_text != null) {
            this.text = cleanTextdata(_text);
        } else {
            this.text = null;
        }
    }

    public List<XMLTrainingEntity> getEntities() {
        return entities;
    }

    public void setEntities(List<XMLTrainingEntity> entities) {
        this.entities = entities;
    }

    public List<XMLTrainingCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<XMLTrainingCategory> categories) {
        this.categories = categories;
    }

    public void addTrainingEntity(XMLTrainingEntity trainingEntity) {
        entities.add(trainingEntity);
    }

    /**
     * Returns the quality level of the trainingData set. The quality is computed
     * during the build() method.
     * 
     * @return
     */
    public int getQuality() {
        return quality;
    }

    /**
     * Set the trainingData quality leval
     * 
     * @param quality
     */
    @XmlTransient
    public void setQuality(int quality) {
        this.quality = quality;
    }

    /**
     * This method returns true if no text is defined, or no entities and no
     * categories are contained
     * 
     * @return
     */
    @XmlTransient
    public boolean isEmpty() {
        if (text == null || text.isEmpty()) {
            return true;
        }

        if ((entities == null || entities.size() == 0) && (categories == null || categories.size() == 0)) {
            return true;
        }

        return false;
    }

    /**
     * This method is used to cleanup the text data send for training or analysis.
     * <p>
     * The method replaces JSON structure characters with spaces. Control codes
     * which are not allowed in a text are removed.
     * <p>
     * This method is called by the setText() method of the XMLTraingData class and
     * the XMLAnalyseText class. The method ensures that the text is clean for any
     * ML training or analyse procedure.
     * 
     * @param text
     * @return
     */
    public static String cleanTextdata(String text) {
        String result = text;
        // strip control characters but prevent newline and tab
        result = stripChars(result, c -> (c > '\u001F' && c != '\u007F') || (c == '\n') || (c == '\t'));
        // replace JSON structure characters with spaces
        result = result.replaceAll("[{}\"]", " ");
        return result;
    }

    /**
     * Helper method to strip Chars form a string.
     * 
     * @param s
     * @param include
     * @return
     */
    private static String stripChars(String s, IntPredicate include) {
        return s.codePoints().filter(include::test)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }
}
