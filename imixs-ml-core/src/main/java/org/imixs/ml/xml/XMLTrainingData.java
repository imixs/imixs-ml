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

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

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
    public static final int TRAININGDATA_QUALITY_LEVEL_FULL = 1;
    @XmlTransient
    public static final int TRAININGDATA_QUALITY_LEVEL_PARTIAL = 2;
    @XmlTransient
    public static final int TRAININGDATA_QUALITY_LEVEL_BAD = 0;

    @XmlTransient
    private static final long serialVersionUID = 1L;

    @XmlTransient
    private int quality = 0;

    private String text;
    private List<XMLTrainingEntity> entities;

    public XMLTrainingData() {
        super();
        // init entities
        entities = new ArrayList<XMLTrainingEntity>();
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
    public void setQuality(int quality) {
        this.quality = quality;
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
     * @param s
     * @param include
     * @return
     */
    private static String stripChars(String s, IntPredicate include) {
        return s.codePoints().filter(include::test)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }
}
