package org.imixs.ml.adapters;

/**
 * The OCRTextAdapter is a helper class optimizing text results from a ORC scan.
 * If the scan quality was bad, it can happen hat a number is recognized as a
 * letter. E.g. the number '0' is wrong detected as the letter 'O', or the
 * number '1' is wrong detected as a the letter 'I'. Also spaces can make it
 * impossible to detect a date or currency correctly.
 * <p>
 * The OCRTextAdapter is used by the DateAdatper and CurrencyAdapter to fix
 * these kind of issues.
 * 
 * @author rsoika
 *
 */
public class OCRTextAdapter {

    /**
     * Converts letters to numbers. e.g. I->1 or O->0
     * 
     * @param text
     * @return
     */
    public static String letterToNumber(String text) {
        String result = text;

        result = result.replace("O", "0");
        result = result.replace("o", "0");
        result = result.replace("I", "1");
        result = result.replace("l", "1");

        return result;
    }

    /**
     * Remove spaces
     * 
     * @param text
     * @return
     */
    public static String stripSpaces(String text) {
        String result = text;
        result = result.replaceAll(" ", "");
        return result;
    }

}
