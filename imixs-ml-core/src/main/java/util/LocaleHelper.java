package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * The LocaleHelper provides methods to parse Locale objects from a string
 * 
 * @author rsoika
 */
public class LocaleHelper {

    private static Logger logger = Logger.getLogger(LocaleHelper.class.getName());

    /**
     * This method parses a string with locales and returns the corresponding Locale
     * objects. A locale string should contain the language and the country
     * component separated by '_' . Multiple locale definitions can be separated by
     * ','
     * <p>
     * e.g.:
     * 
     * <pre>
     *   de_DE,en_GB
     * </pre>
     * 
     * This example defines the locales for GERMANY and UK.
     * 
     * @param - string with a comma separated list of locale definitions
     * @return
     */
    public static List<Locale> parseLocales(String locales) {
        List<Locale> locals = new ArrayList<Locale>();
        // split locale strings..
        String[] sLocales = locales.split(",");
        // validate locale definitions
        for (String _locale : sLocales) {

            // split language and country component
            String[] localeDefinition = _locale.split("_");
            if (localeDefinition.length != 2) {
                logger.warning("incrrect locale format for: '" + _locale
                        + "' . Expected format LANGUAGE_COUNTRY - e.g. de_DE");
                Locale aLocale = new Locale(_locale);
                locals.add(aLocale);
            } else {
                // create Locale form language and country component
                Locale aLocale = new Locale(localeDefinition[0], localeDefinition[1]);
                locals.add(aLocale);
            }

        }
        return locals;
    }

}
