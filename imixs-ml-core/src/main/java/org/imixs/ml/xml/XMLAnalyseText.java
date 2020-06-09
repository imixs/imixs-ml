package org.imixs.ml.xml;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "analysedata")
public class XMLAnalyseText {
    
        private String text;

        public XMLAnalyseText(String text) {
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
