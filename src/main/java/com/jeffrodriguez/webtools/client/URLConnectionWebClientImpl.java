/* This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org/>
 */
package com.jeffrodriguez.webtools.client;

import com.jeffrodriguez.xmlwrapper.XML;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import net.sf.jsog.JSOG;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * A URLConnection-based WebClient implementation.
 * @author Jeff
 */
public class URLConnectionWebClientImpl implements WebClient {
    private static final Logger logger = Logger.getLogger(URLConnectionWebClientImpl.class.toString());

    private static void checkForErrors(HttpURLConnection connection) throws IOException {

        // If this is a 200-range response code, just return
        if (connection.getResponseCode() >= 200 && connection.getResponseCode() < 300) {
            return;
        }

        // Build the IO Exception
        IOException e = new IOException(connection.getResponseCode() + ": " + connection.getResponseMessage());

        // Get the response as a string
        String response = IOUtils.toString(connection.getErrorStream(), "ISO-8859-1");

        // Log it
        logger.log(Level.SEVERE, response, e);

        // Throw it
        throw e;
    }

    private static <T> T disconnectAndReturn(HttpURLConnection connection, T result) throws IOException {
        connection.disconnect();
        return result;
    }

    /**
     * The timeout when establishing a connection and reading/writing data.
     */
    private int timeout = 0;

    @Override
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * The authorization header to send, if any.
     */
    private String authorization;

    @Override
    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    @Override
    public void setAuthorization(String username, String password) {
        String userPass = username + ":" + password;
        try {
            authorization = "Basic " + Base64.encodeBase64String(userPass.getBytes("ISO-8859-1"));
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    /**
     * Builds a document from an input stream.
     *
     * Override this method if you need a special Document building facility.
     * @param in the input stream.
     * @return the document.
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    protected Document buildDocument(InputStream in) throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(in);
    }

    private HttpURLConnection buildConnection(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
        connection.setUseCaches(false);

        // Set the authorization header if one has been specified
        if (authorization != null) {
            connection.setRequestProperty("Authorization", authorization);
        }

        return connection;
    }

    @Override
    public void get(String url, OutputStream out) throws IOException {
        logger.log(Level.INFO, "Getting from {0}", url);

        // Build and connect
        HttpURLConnection connection = buildConnection(url);
        connection.connect();

        // Check for errors
        checkForErrors(connection);

        // Get the result
        disconnectAndReturn(connection, IOUtils.copy(connection.getInputStream(), out));
    }

    @Override
    public byte[] get(String url) throws IOException {
        logger.log(Level.INFO, "Getting byte[] from {0}", url);

        // Build and connect
        HttpURLConnection connection = buildConnection(url);
        connection.connect();

        // Check for errors
        checkForErrors(connection);

        // Get the result
        return disconnectAndReturn(connection, IOUtils.toByteArray(connection.getInputStream()));
    }

    @Override
    public byte[] post(String url, byte[] data) throws IOException {

        // Build and connect
        HttpURLConnection connection = buildConnection(url);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.connect();

        // Send the request
        connection.getOutputStream().write(data);
        connection.getOutputStream().flush();
        connection.getOutputStream().close();

        // Check for errors
        checkForErrors(connection);

        // Get the result
        return disconnectAndReturn(connection, IOUtils.toByteArray(connection.getInputStream()));
    }

    @Override
    public String getString(String url) throws IOException {
        logger.log(Level.INFO, "Getting String from {0}", url);

        // Build and connect
        HttpURLConnection connection = buildConnection(url);
        connection.connect();

        // Check for errors
        checkForErrors(connection);

        // Get the result
        return disconnectAndReturn(connection,
                IOUtils.toString(connection.getInputStream(), "ISO-8859-1"));
    }

    @Override
    public String postString(String url, String data) throws IOException {

        // Build and connect
        HttpURLConnection connection = buildConnection(url);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.connect();

        // Send the request
        connection.getOutputStream().write(data.getBytes("ISO-8859-1"));
        connection.getOutputStream().flush();
        connection.getOutputStream().close();

        // Check for errors
        checkForErrors(connection);

        // Get the result
        return disconnectAndReturn(connection,
                IOUtils.toString(connection.getInputStream(), "ISO-8859-1"));
    }

    @Override
    public Document getDocument(String url) throws IOException, ParserConfigurationException, SAXException {
        logger.log(Level.INFO, "Getting Document from {0}", url);

        // Build and connect
        HttpURLConnection connection = buildConnection(url);
        connection.connect();

        // Check for errors
        checkForErrors(connection);

        // Get the result
        return disconnectAndReturn(connection,
                buildDocument(connection.getInputStream()));
    }

    @Override
    public Document postDocument(String url, Document data) throws IOException, TransformerException, SAXException, ParserConfigurationException {
        logger.log(Level.INFO, "Posting document to {0}", url);

        // Build and connect
        HttpURLConnection connection = buildConnection(url);
        connection.setDoOutput(true);
        OutputStream out = connection.getOutputStream();
        connection.connect();

        // Write the document
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(data), new StreamResult(out));
        out.flush();
        out.close();

        // Check for errors
        checkForErrors(connection);

        // Get the result
        return disconnectAndReturn(connection,
                buildDocument(connection.getInputStream()));
    }

    @Override
    public XML getXML(String url) throws IOException, ParserConfigurationException, SAXException {
        logger.log(Level.INFO, "Getting Document from {0}", url);

        // Build and connect
        HttpURLConnection connection = buildConnection(url);
        connection.connect();

        // Check for errors
        checkForErrors(connection);

        // Get the result
        Document document = buildDocument(connection.getInputStream());
        return disconnectAndReturn(connection, new XML(document));
    }

    @Override
    public XML postXML(String url, XML data) throws IOException, TransformerException, SAXException, ParserConfigurationException {
        logger.log(Level.INFO, "Posting document to {0}", url);

        // Build and connect
        HttpURLConnection connection = buildConnection(url);
        connection.setDoOutput(true);
        OutputStream out = connection.getOutputStream();
        connection.connect();

        // Write the document
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(data.getDocument()), new StreamResult(out));
        out.flush();
        out.close();

        // Check for errors
        checkForErrors(connection);

        // Get the result
        Document document = buildDocument(connection.getInputStream());
        return disconnectAndReturn(connection, new XML(document));
    }

    @Override
    public JSOG getJsog(String url) throws IOException {
        logger.log(Level.INFO, "Getting JSOG from {0}", url);

        // Build and connect
        HttpURLConnection connection = buildConnection(url);
        connection.connect();

        // Check for errors
        checkForErrors(connection);

        // Get the result
        return disconnectAndReturn(connection,
                JSOG.parse(
                    IOUtils.toString(connection.getInputStream(), "ISO-8859-1")));
    }

    @Override
    public JSOG postJsog(String url, JSOG data) throws IOException {

        // Build and connect
        HttpURLConnection connection = buildConnection(url);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Content-Type", "application/json; charset=ISO-8859-1");
        connection.connect();

        // Send the request
        connection.getOutputStream().write(data.toString().getBytes("ISO-8859-1"));
        connection.getOutputStream().flush();
        connection.getOutputStream().close();

        // Check for errors
        checkForErrors(connection);

        // Get the result
        return disconnectAndReturn(connection,
                JSOG.parse(
                    IOUtils.toString(connection.getInputStream(), "ISO-8859-1")));
    }

    @Override
    public Map<String, List<String>> getHeaders(String url) throws IOException {

        logger.log(Level.INFO, "Getting HEAD from {0}", url);

        // Build and connect
        HttpURLConnection connection = buildConnection(url);
        connection.setDoInput(true);
        connection.setRequestMethod("HEAD");
        connection.connect();

        // Get the headers
        return disconnectAndReturn(connection, connection.getHeaderFields());
    }

}
