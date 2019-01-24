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
package com.unrar.andy.library.org.apache.tika.mime;

import com.unrar.andy.library.org.apache.tika.config.TikaConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Registry of known Internet media types.
 */
public class MediaTypeRegistry {

    /**
     * Returns the built-in media type registry included in Tika.
     *
     * @since Apache Tika 0.8
     * @return default media type registry
     */
    public static MediaTypeRegistry getDefaultRegistry() {
        return TikaConfig.getDefaultConfig().getMediaTypeRegistry();
    }

    /**
     * Registry of known media types, including type aliases. A canonical
     * media type is handled as an identity mapping, while an alias is stored
     * as a mapping from the alias to the corresponding canonical type.
     */
    private final Map<MediaType, MediaType> registry =
        new HashMap<MediaType, MediaType>();

    /**
     * Known type inheritance relationships. The mapping is from a media type
     * to the closest supertype.
     */
    private final Map<MediaType, MediaType> inheritance =
        new HashMap<MediaType, MediaType>();

    /**
     * Returns the set of all known canonical media types. Type aliases are
     * not included in the returned set.
     *
     * @since Apache Tika 0.8
     * @return canonical media types
     */
    public SortedSet<MediaType> getTypes() {
        return new TreeSet<MediaType>(registry.values());
    }

    /**
     * Returns the set of known aliases of the given canonical media type.
     *
     * @since Apache Tika 0.8
     * @param type canonical media type
     * @return known aliases
     */
    public SortedSet<MediaType> getAliases(MediaType type) {
        SortedSet<MediaType> aliases = new TreeSet<MediaType>();
        for (Map.Entry<MediaType, MediaType> entry : registry.entrySet()) {
            if (entry.getValue().equals(type) && !entry.getKey().equals(type)) {
                aliases.add(entry.getKey());
            }
        }
        return aliases;
    }

    public void addType(MediaType type) {
        registry.put(type, type);
    }

    public void addAlias(MediaType type, MediaType alias) {
        registry.put(alias, type);
    }

    public void addSuperType(MediaType type, MediaType supertype) {
        inheritance.put(type, supertype);
    }

    public MediaType normalize(MediaType type) {
        MediaType canonical = registry.get(type.getBaseType());
        if (canonical == null) {
            return type;
        } else if (type.hasParameters()) {
            return new MediaType(canonical, type.getParameters());
        } else {
            return canonical;
        }
    }

    /**
     * Checks whether the given media type a is a specialization of a more
     * generic type b.
     *
     * @since Apache Tika 0.8
     * @param a media type
     * @param b suspected supertype
     * @return <code>true</code> if b is a supertype of a,
     *         <code>false</code> otherwise
     */
    public boolean isSpecializationOf(MediaType a, MediaType b) {
        MediaType x = getSupertype(a);
        return x != null && (x.equals(b) || isSpecializationOf(x, b));
    }

    /**
     * Returns the supertype of the given type. If the given type has any
     * parameters, then the respective base type is returned. Otherwise
     * built-in heuristics like text/... -&gt; text/plain and
     * .../...+xml -&gt; application/xml are used in addition to explicit
     * type inheritance rules read from the media type database. Finally
     * application/octet-stream is returned for all types for which no other
     * supertype is known, and the return value for application/octet-stream
     * is <code>null</code>.
     *
     * @since Apache Tika 0.8
     * @param type media type
     * @return supertype, or <code>null</code> for application/octet-stream
     */
    public MediaType getSupertype(MediaType type) {
        if (type.hasParameters()) {
            return type.getBaseType();
        } else if (inheritance.containsKey(type)) {
            return inheritance.get(type);
        } else if (type.getSubtype().endsWith("+xml")) {
            return MediaType.APPLICATION_XML;
        } else if (type.getSubtype().endsWith("+zip")) {
            return MediaType.APPLICATION_ZIP;
        } else if ("text".equals(type.getType())
                && !MediaType.TEXT_PLAIN.equals(type)) {
            return MediaType.TEXT_PLAIN;
        } else if (!MediaType.OCTET_STREAM.equals(type)) {
            return MediaType.OCTET_STREAM;
        } else {
            return null;
        }
    }

}
