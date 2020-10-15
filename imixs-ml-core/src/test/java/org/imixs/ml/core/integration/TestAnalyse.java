package org.imixs.ml.core.integration;

import java.util.List;
import java.util.logging.Logger;

import org.imixs.ml.core.MLClient;
import org.imixs.ml.xml.XMLAnalyseEntity;
import org.imixs.workflow.exceptions.PluginException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration Test class for the MLClient. The test assumes that a
 * imixs-ml-spacy instance is running on port 8000
 * 
 * @author rsoika
 */
public class TestAnalyse {
    private static Logger logger = Logger.getLogger(TestAnalyse.class.getName());

    static String SPACY_ENDPOINT = "http://localhost:8000/analyse";
    private IntegrationTest integrationTest = new IntegrationTest(SPACY_ENDPOINT);

    MLClient mlClient = null;

    @Before
    public void setup() throws PluginException {
        // Assumptions for integration tests
        org.junit.Assume.assumeTrue(integrationTest.connected());

        mlClient = new MLClient();
    }

    /**
     * test a invoice test against the invoice model
     */
    @Test
    public void testAnlyse() {
        // some test text of a demo invoice
        String text = "Author: DocType: Rechnung Page 1 von 1 . . . extends the way people work together Imixs Software Solutions GmbH - Agnes-Pockels-Bogen 1 - 80992 München Test GmbH Teststraße 610 60311 Frankfurt am Main Sehr geehrter Herr Mustermann, für die Leistung „Imixs-Office-Workflow – Service Subscription“ erlauben wir uns, die unten aufgeführte Rechnung zu stellen. Lieferdatum: 31.12.2019 Leistungszeitraum: Oktober bis Dezember 2019 Vertragsnummer: 100234 Bitte überweisen Sie den Rechnungsbetrag auf unten stehendes Konto. Die Firma Imixs Software Solutions GmbH bedankt sich bei Ihnen für Ihr Vertrauen und die gute Zusammenarbeit. Imixs Software Solutions GmbH Gerichtsstand Geschäftsführer Bankverbindung Agnes-Pockels-Bogen 1 München Tel.:++49(0)89-45 21 36 - 0  Postbank München Web: www.imixs.com Mail: info@imixs.com IBAN: DE11100100444076555000 BIC: PBNKXXYY Rechnungs-Nr: 2049-704 28.02.2020 Position Leistungsbeschreibung €/Stunde Stunden Euro 1 125,00 € 6,5 812,50 € Zwischensumme 9 1.875,00 € 19% Mehrwertsteuer 356,25 € Summe 2.231,25 € ";

        List<XMLAnalyseEntity> result = mlClient.postAnalyseData(text, SPACY_ENDPOINT);

        Assert.assertTrue(result.size() > 0);
        
        // print result list...
        for (XMLAnalyseEntity entity: result) {
            logger.info(entity.getLabel() + "=" + entity.getText());
        }

    }

}
