package org.imixs.ml.xml;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "analysedata")
public class XMLAnalyseData {
    
        private String text;

        public XMLAnalyseData(String text) {
            super();
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    
}
