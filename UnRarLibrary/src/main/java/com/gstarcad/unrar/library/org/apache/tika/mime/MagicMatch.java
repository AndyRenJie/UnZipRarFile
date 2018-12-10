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
package com.gstarcad.unrar.library.org.apache.tika.mime;

import com.gstarcad.unrar.library.org.apache.tika.detect.MagicDetector;
import com.gstarcad.unrar.library.org.apache.tika.metadata.Metadata;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Defines a magic match.
 */
class MagicMatch implements Clause {

    /**
	 * 
	 */
	private static final long serialVersionUID = 3867645655466902524L;

	private final MagicDetector detector;

    private final int length;

    MagicMatch(MagicDetector detector, int length) throws MimeTypeException {
        this.detector = detector;
        this.length = length;
    }

    public boolean eval(byte[] data) {
        try {
            return detector.detect(
                    new ByteArrayInputStream(data), new Metadata())
                    != MediaType.OCTET_STREAM;
        } catch (IOException e) {
            // Should never happen with a ByteArrayInputStream
            return false;
        }
    }

    public int size() {
        return length;
    }

    public String toString() {
        return detector.toString();
    }

}
