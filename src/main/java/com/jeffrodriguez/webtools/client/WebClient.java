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

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import net.sf.jsog.JSOG;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Jeff
 */
public interface WebClient {

    void setTimeout(int timeout);

    void setAuthorization(String authorization);

    void setAuthorization(String username, String password);

    /**
     * Gets the content at a URL, writing it to an output stream.
     */
    void get(String url, OutputStream out) throws IOException;

    byte[] get(String url) throws IOException;

    byte[] post(String url, byte[] data) throws IOException;

    String getString(String url) throws IOException;

    String postString(String url, String data) throws IOException;

    Document getDocument(String url) throws IOException, ParserConfigurationException, SAXException;

    Document postDocument(String url, Document data) throws IOException, TransformerException, SAXException, ParserConfigurationException;

    JSOG getJsog(String url) throws IOException;

    JSOG postJsog(String url, JSOG data) throws IOException;

    Map<String, List<String>> getHeaders(String url) throws IOException;

}
