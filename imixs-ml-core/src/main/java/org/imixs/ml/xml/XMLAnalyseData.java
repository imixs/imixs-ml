package org.imixs.ml.xml;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "analysedata")
public class XMLAnalyseData {
    
        private String text;

        /**
         * Default constructor 
         */
        public XMLAnalyseData() {
            super();
        }

        public XMLAnalyseData(String _text) {
            super();
            setText(_text);
        }

        public String getText() {
            return text;
        }

        public void setText(String _text) {
            if (_text != null) {
                this.text = XMLTrainingData.cleanTextdata(_text);
            } else {
                this.text = null;
            }
        }
    
}
