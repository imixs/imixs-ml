package org.imixs.ml.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.imixs.workflow.FileData;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.exceptions.PluginException;

/**
 * This class is a helper class to extract text content form attached documents.
 * The service loads the corresponding snaptshot for a workitem. It is used
 * only by the TrainingService.
 * 
 * 
 * org.imixs.archive.documents.TikaService
 * 
 * See also the project: https://github.com/imixs/imixs-archive
 * 
 * @version 1.1
 * @author rsoika
 */
@Stateless
public class TikaHelperService {

    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String PLUGIN_ERROR = "PLUGIN_ERROR";
    public static final String ENV_OCR_SERVICE_ENDPOINT = "ocr.service.endpoint";
    public static final String ENV_OCR_SERVICE_MODE = "ocr.service.mode";
    public static final String ENV_OCR_STRATEGY = "ocr.strategy"; // NO_OCR, OCR_ONLY, OCR_AND_TEXT_EXTRACTION (default)

    public static final String OCR_STRATEGY_NO_OCR = "NO_OCR";
    public static final String OCR_STRATEGY_OCR_AND_TEXT_EXTRACTION = "OCR_AND_TEXT_EXTRACTION";
    public static final String OCR_STRATEGY_OCR_ONLY = "OCR_ONLY";
    public static final String OCR_STRATEGY_AUTO = "AUTO"; // default

    private static Logger logger = Logger.getLogger(TikaHelperService.class.getName());

    @Inject
    @ConfigProperty(name = ENV_OCR_SERVICE_ENDPOINT)
    Optional<String> serviceEndpoint;

    @Inject
    @ConfigProperty(name = ENV_OCR_STRATEGY, defaultValue = OCR_STRATEGY_AUTO)
    String ocrStategy;

    /**
     * Extracts the textual information from document attachments.
     * <p>
     * The method extracts the textual content for each new file attachment of a
     * given workitem. The text information is stored in the $file attribute 'text'.
     * <p>
     * For PDF files with textual content the method calls the method
     * 'extractTextFromPDF' using the PDFBox api. In other cases, the method sends
     * the content via a Rest API to the tika server for OCR processing.
     * <p>
     * The method also extracts files already stored in a snapshot workitem. In this
     * case the method tests if the $file attribute 'text' already exists.
     * 
     * @param workitem - workitem with file attachments
     * @param pdf_mode - TEXT_ONLY, OCR_ONLY, TEXT_AND_OCR
     * @param options  - optional tika header params
     * @throws PluginException
     */
    public String extractText(ItemCollection snapshot, Pattern mlFilenamePattern, String _ocrStategy,
            List<String> options) throws PluginException {
        boolean debug = logger.isLoggable(Level.FINE);

        String result = "";

        if (options == null) {
            options = new ArrayList<String>();
        }

        // overwrite ocrmode?
        if (_ocrStategy != null) {
            this.ocrStategy = _ocrStategy;
        }

        // validate OCR MODE....
        if ("AUTO, NO_OCR, OCR_ONLY, OCR_AND_TEXT_EXTRACTION".indexOf(ocrStategy) == -1) {
            throw new PluginException(TikaHelperService.class.getSimpleName(), PLUGIN_ERROR,
                    "Invalid TIKA_OCR_MODE - expected one of the following options: NO_OCR | OCR_ONLY | OCR_AND_TEXT_EXTRACTION");
        }

        // if the options did not already include the X-Tika-PDFOcrStrategy than we add
        // it now...
        boolean hasPDFOcrStrategy = options.stream()
                .anyMatch(s -> s.toLowerCase().startsWith("X-Tika-PDFOcrStrategy=".toLowerCase()));
        if (!hasPDFOcrStrategy) {
            // we do need to set a OcrStrategy from the environment...
            options.add("X-Tika-PDFOcrStrategy=" + ocrStategy);
        }

        // print tika options...
        if (debug) {
            for (String opt : options) {
                logger.info("......  Tika Option = " + opt);
            }
        }

        long l = System.currentTimeMillis();
        // List<ItemCollection> currentDmsList = DMSHandler.getDmsList(workitem);
        List<FileData> files = snapshot.getFileData();

        for (FileData fileData : files) {

            // apply filenamePattern if provided..
            if (mlFilenamePattern == null || mlFilenamePattern.matcher(fileData.getName()).find()) {

                String textContent = null;
                // extract the text content...
                try {
                    if (debug) {
                        logger.fine("...text extraction '" + fileData.getName() + "'...");
                    }
                    textContent = doORCProcessing(fileData, options);

                    if (textContent == null || textContent.isEmpty()) {
                        logger.warning("Unable to extract text-content for '" + fileData.getName() + "'");
                        textContent = "";
                    } else {
                        result = result + textContent + " ";
                    }

                } catch (IOException e) {
                    throw new PluginException(TikaHelperService.class.getSimpleName(), PLUGIN_ERROR,
                            "Unable to scan attached document '" + fileData.getName() + "'", e);
                }
            }

        }
        if (debug) {
            logger.fine("...extracted textual information in " + (System.currentTimeMillis() - l) + "ms");
        }

        return result;
    }

    /**
     * This method sends the content of a document to the Tika Rest API for OCR
     * processing.
     * <p>
     * In case the contentType is PDF then the following tika specific header is
     * added:
     * <p>
     * <code>X-Tika-PDFOcrStrategy: ocr_only</code>
     * <p>
     * 
     * @param fileData - file content and metadata
     * @return text content
     * @throws IOException
     */
    public String doORCProcessing(FileData fileData, List<String> options) throws IOException {
        @SuppressWarnings("unused")
        boolean debug = logger.isLoggable(Level.FINE);

        // read the Tika Service Enpoint
        if (!serviceEndpoint.isPresent() || serviceEndpoint.get().isEmpty()) {
            logger.severe(
                    "OCR_SERVICE_ENDPOINT is missing - OCR processing not supported without a valid tika server endpoint!");
            return null;
        }

        logger.fine("...ocr scanning....");
        // adapt ContentType
        String contentType = adaptContentType(fileData);

        // validate content type
        if (!acceptContentType(contentType)) {
            logger.fine("contentType '" + contentType + " is not supported by Tika Server");
            return null;
        }

        PrintWriter printWriter = null;
        HttpURLConnection urlConnection = null;
        PrintWriter writer = null;
        try {
            urlConnection = (HttpURLConnection) new URL(serviceEndpoint.get()).openConnection();
            urlConnection.setRequestMethod("PUT");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setAllowUserInteraction(false);

            /** * HEADER ** */
            urlConnection.setRequestProperty("Content-Type", contentType + "; charset=" + DEFAULT_ENCODING);
            urlConnection.setRequestProperty("Accept", "text/plain");

            /** do we have header options? **/
            if (options != null && options.size() > 0) {
                for (String option : options) {
                    int i = option.indexOf("=");

                    if (i > -1) {
                        String key = option.substring(0, i);
                        String value = option.substring(i + 1);
                        if (key.startsWith("X-Tika")) {
                            // urlConnection.setRequestProperty("X-Tika-PDFOcrStrategy", "ocr_only");
                            urlConnection.setRequestProperty(key, value);
                        } else {
                            logger.warning("Invalid tika option : '" + option + "'  key must start with 'X-Tika'");
                        }
                    } else {
                        logger.warning("Invalid tika option : '" + option + "'  character '=' expeced!");
                    }
                }
            }

            // compute length
            urlConnection.setRequestProperty("Content-Length", "" + Integer.valueOf(fileData.getContent().length));
            OutputStream output = urlConnection.getOutputStream();
            writer = new PrintWriter(new OutputStreamWriter(output, DEFAULT_ENCODING), true);
            output.write(fileData.getContent());
            writer.flush();

            int resposeCode = urlConnection.getResponseCode();

            if (resposeCode >= 200 && resposeCode <= 299) {
                return readResponse(urlConnection, DEFAULT_ENCODING);
            }

            // no data!
            return null;

        } finally {
            // Release current connection
            if (printWriter != null)
                printWriter.close();
        }
    }

    /**
     * Reads the response from a http request.
     * 
     * @param urlConnection
     * @throws IOException
     */
    private String readResponse(URLConnection urlConnection, String encoding) throws IOException {
        boolean debug = logger.isLoggable(Level.FINE);

        // get content of result
        if (debug) {
            logger.finest("......readResponse....");
        }
        StringWriter writer = new StringWriter();
        BufferedReader in = null;
        try {
            // test if content encoding is provided
            String sContentEncoding = urlConnection.getContentEncoding();
            if (sContentEncoding == null || sContentEncoding.isEmpty()) {
                // no so lets see if the client has defined an encoding..
                if (encoding != null && !encoding.isEmpty())
                    sContentEncoding = encoding;
            }

            // if an encoding is provided read stream with encoding.....
            if (sContentEncoding != null && !sContentEncoding.isEmpty())
                in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), sContentEncoding));
            else
                in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (debug) {
                    logger.finest("......" + inputLine);
                }
                // append text plus new line!
                writer.write(inputLine + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null)
                in.close();
        }

        return writer.toString();

    }

    /**
     * Tika does not support any content type. So we filter some of them (e.g.
     * application/octet-stream).
     * 
     * @param contentType
     * @return
     */
    private boolean acceptContentType(String contentType) {

        if (contentType == null || contentType.isEmpty()) {
            return false;
        }
        if ("application/octet-stream".equalsIgnoreCase(contentType)) {
            return false;
        }

        return true;
    }

    /**
     * This method verifies the content Type stored in a FileData object.
     * <p>
     * In case no contenttype is provided or is '*' the adapts the content type
     * based on the file extension
     * <p>
     * If no contentType can be computed, the method returns the default contentType
     * application/xml
     * 
     * 
     * @param fileData
     * @return
     */
    private String adaptContentType(FileData fileData) {

        String contentType = fileData.getContentType();

        // verify */*
        if (contentType == null || contentType.isEmpty() || "*/*".equals(contentType)) {
            // compute contentType based on file extension...
            if (fileData.getName().toLowerCase().endsWith(".pdf")) {
                contentType = "application/pdf";
            } else {
                // set default type
                contentType = "application/xml";
            }
        }

        return contentType;
    }

}