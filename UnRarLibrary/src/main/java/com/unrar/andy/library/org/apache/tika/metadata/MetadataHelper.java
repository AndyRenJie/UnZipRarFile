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
package com.unrar.andy.library.org.apache.tika.metadata;

import com.unrar.andy.library.org.apache.tika.io.TikaInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Collection of static helper methods for handling metadata.
 *
 * @deprecated Use {@link TikaInputStream} instead
 * @since Apache Tika 0.7
 */
public class MetadataHelper {

    /**
     * Private constructor to prevent instantiation.
     */
    private MetadataHelper() {
    }

    /**
     * Returns the content at the given URL, and sets any related
     * metadata entries.
     *
     * @param url the URL of the resource to be read
     * @param metadata where the resource metadata is stored
     * @return resource content
     * @throws IOException if the URL can not be accessed
     */
    public static InputStream getInputStream(URL url, Metadata metadata)
            throws IOException {
        return TikaInputStream.get(url, metadata);
    }

}
