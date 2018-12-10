/*
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
package com.gstarcad.unrar.library.org.apache.tika.extractor;


import com.gstarcad.unrar.library.org.apache.tika.exception.TikaException;
import com.gstarcad.unrar.library.org.apache.tika.io.CloseShieldInputStream;
import com.gstarcad.unrar.library.org.apache.tika.metadata.Metadata;
import com.gstarcad.unrar.library.org.apache.tika.parser.DelegatingParser;
import com.gstarcad.unrar.library.org.apache.tika.parser.ParseContext;
import com.gstarcad.unrar.library.org.apache.tika.parser.Parser;
import com.gstarcad.unrar.library.org.apache.tika.sax.BodyContentHandler;
import com.gstarcad.unrar.library.org.apache.tika.sax.EmbeddedContentHandler;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import static com.gstarcad.unrar.library.org.apache.tika.sax.XHTMLContentHandler.XHTML;

/**
 * Helper class for parsers of package archives or other compound document
 * formats that support embedded or attached component documents.
 *
 * @since Apache Tika 0.8
 */
public class EmbeddedDocumentExtractor {

    private static final File ABSTRACT_PATH = new File("");

    private static final Parser DELEGATING_PARSER = new DelegatingParser();

    private final ParseContext context;

    public EmbeddedDocumentExtractor(ParseContext context) {
        this.context = context;
    }

    public boolean shouldParseEmbedded(Metadata metadata) {
        DocumentSelector selector = context.get(DocumentSelector.class);
        if (selector != null) {
            return selector.select(metadata);
        }

        FilenameFilter filter = context.get(FilenameFilter.class);
        if (filter != null) {
            String name = metadata.get(Metadata.RESOURCE_NAME_KEY);
            if (name != null) {
                return filter.accept(ABSTRACT_PATH, name);
            }
        }

        return true;
    }

    /**
     * Processes the supplied embedded resource, calling the delegating
     *  parser with the appropriate details.
     * @param stream The embedded resource
     * @param handler The handler to use
     * @param metadata The metadata for the embedded resource
     * @param outputHtml Should we output HTML for this resource, or has the parser already done so?
     * @throws SAXException
     * @throws IOException
     */
    public void parseEmbedded(
            InputStream stream, ContentHandler handler, Metadata metadata, boolean outputHtml)
            throws SAXException, IOException {
        if(outputHtml) {
           AttributesImpl attributes = new AttributesImpl();
           attributes.addAttribute("", "class", "class", "CDATA", "package-entry");
           handler.startElement(XHTML, "div", "div", attributes);
        }

        String name = metadata.get(Metadata.RESOURCE_NAME_KEY);
        if (name != null && name.length() > 0 && outputHtml) {
            handler.startElement(XHTML, "h1", "h1", new AttributesImpl());
            char[] chars = name.toCharArray();
            handler.characters(chars, 0, chars.length);
            handler.endElement(XHTML, "h1", "h1");
        }

        // Use the delegate parser to parse this entry
        try {
            DELEGATING_PARSER.parse(
                    new CloseShieldInputStream(stream),
                    new EmbeddedContentHandler(new BodyContentHandler(handler)),
                    metadata, context);
        } catch (TikaException e) {
            // Could not parse the entry, just skip the content
        }

        if(outputHtml) {
           handler.endElement(XHTML, "div", "div");
        }
    }

}
