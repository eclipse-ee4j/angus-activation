/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */


package org.eclipse.angus.activation;

import jakarta.activation.MimetypesFileTypeMap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MimetypesFileTypeMapTest {

    @Test
    public void jakartaAndLegacyResources() {
        MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
        // Same value in mime.types and jakarta.mime.types
        assertEquals("aaa/aaa", mimetypesFileTypeMap.getContentType("foo.aaa"));
        // Conflict, mime.types wins
        assertEquals("bbb/bbb", mimetypesFileTypeMap.getContentType("foo.bbb"));
        // Only exists in mime.types
        assertEquals("ccc/ccc", mimetypesFileTypeMap.getContentType("foo.ccc"));
        // Only exists in jakarta.mime.types
        assertEquals("ddd/ddd", mimetypesFileTypeMap.getContentType("foo.ddd"));
    }
}
