/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.unrar.andy.library.org.apache.tika.parser;

import com.unrar.andy.library.org.apache.tika.exception.TikaException;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Parse context. Used to pass context information to Tika parsers.
 *
 * @since Apache Tika 0.5
 * @see <a href="https://issues.apache.org/jira/browse/TIKA-275">TIKA-275</a>
 */
public class ParseContext implements Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -5921436862145826534L;

    private final Map<Class<?>, Object> context =
        new HashMap<Class<?>, Object>();
 
    public <T> void set(Class<T> key, T value) {
        context.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> key) {
        return (T) context.get(key);
    }

    public <T> T get(Class<T> key, T defaultValue) {
        T value = get(key);
        if (value != null) {
            return value;
        } else {
            return defaultValue;
        }
    }

    /**
     * Returns the SAX parser specified in this parsing context. If a parser
     * is not explicitly specified, then one is created using the specified
     * or the default SAX parser factory.
     *
     * @see #getSAXParserFactory()
     * @since Apache Tika 0.8
     * @return SAX parser
     * @throws TikaException if a SAX parser could not be created
     */
    public SAXParser getSAXParser() throws TikaException {
        SAXParser parser = get(SAXParser.class);
        if (parser != null) {
            return parser;
        } else {
            try {
                return getSAXParserFactory().newSAXParser();
            } catch (ParserConfigurationException e) {
                throw new TikaException("Unable to configure a SAX parser", e);
            } catch (SAXException e) {
                throw new TikaException("Unable to create a SAX parser", e);
            }
        }
    }

    /**
     * Returns the SAX parser factory specified in this parsing context.
     * If a factory is not explicitly specified, then a default factory
     * instance is created and returned. The default factory instance is
     * configured to be namespace-aware and to use
     * {@link XMLConstants#FEATURE_SECURE_PROCESSING secure XML processing}.
     *
     * @since Apache Tika 0.8
     * @return SAX parser factory
     */
    public SAXParserFactory getSAXParserFactory() {
        SAXParserFactory factory = get(SAXParserFactory.class);
        if (factory == null) {
            factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            try {
                factory.setFeature(
                        XMLConstants.FEATURE_SECURE_PROCESSING, true);
            } catch (ParserConfigurationException e) {
            } catch (SAXNotSupportedException e) {
            } catch (SAXNotRecognizedException e) {
                // TIKA-271: Some XML parsers do not support the
                // secure-processing feature, even though it's required by
                // JAXP in Java 5. Ignoring the exception is fine here, as
                // deployments without this feature are inherently vulnerable
                // to XML denial-of-service attacks.
            }
        }
        return factory;
    }

}
