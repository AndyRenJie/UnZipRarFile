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
package com.unrar.andy.library.org.apache.tika.detect;

import com.unrar.andy.library.org.apache.tika.metadata.Metadata;
import com.unrar.andy.library.org.apache.tika.mime.MediaType;
import com.unrar.andy.library.org.apache.tika.mime.MediaTypeRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;


/**
 * Content type detector that combines multiple different detection mechanisms.
 */
public class CompositeDetector implements Detector {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 5980683158436430252L;

    private final MediaTypeRegistry registry;

    private final List<Detector> detectors;

    public CompositeDetector(
            MediaTypeRegistry registry, List<Detector> detectors) {
        this.registry = registry;
        this.detectors = detectors;
    }

    public CompositeDetector(List<Detector> detectors) {
        this(new MediaTypeRegistry(), detectors);
    }

    public CompositeDetector(Detector... detectors) {
        this(Arrays.asList(detectors));
    }

    @Override
    public MediaType detect(InputStream input, Metadata metadata)
            throws IOException { 
        MediaType type = MediaType.OCTET_STREAM;
        for (Detector detector : detectors) {
            MediaType detected = detector.detect(input, metadata);
            if (registry.isSpecializationOf(detected, type)) {
                type = detected;
            }
        }
        return type;
    }

}
