/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package user.code.test;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.MailcapCommandMap;
import jakarta.activation.MimeType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.*;

public class ActivationBundleTest {

    @Test
    void testDataHandlerCreation() throws Exception {
        String content = "Hello, Angus Activation!";
        DataSource ds = new DataSource() {
            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(content.getBytes());
            }

            @Override
            public OutputStream getOutputStream() {
                throw new UnsupportedOperationException("write not supported");
            }

            @Override
            public String getContentType() {
                return "text/plain";
            }

            @Override
            public String getName() {
                return "test.txt";
            }
        };

        DataHandler handler = new DataHandler(ds);
        assertNotNull(handler);
        assertEquals("text/plain", handler.getContentType());

        // Test MIME type parsing
        MimeType mt = new MimeType("text/plain");
        assertEquals("text", mt.getPrimaryType());
        assertEquals("plain", mt.getSubType());

        // Test MailcapCommandMap default handling
        MailcapCommandMap map = (MailcapCommandMap) MailcapCommandMap.getDefaultCommandMap();
        assertNotNull(map);
    }
}
