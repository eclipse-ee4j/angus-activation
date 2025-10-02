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

import jakarta.activation.MailcapCommandMap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MailcapCommandMapTest {

    @Test
    public void jakartaAndLegacyResources() {
        MailcapCommandMap mailcapCommandMap = new MailcapCommandMap();
        // Same class in mailcap and jakarta.mailcap
        assertEquals("com.sun.activation.viewers.ImageViewer", mailcapCommandMap.getCommand("image/gif", "view").getCommandClass());
        // Conflict, mailcap wins
        assertEquals("com.sun.activation.viewers.ImageViewer", mailcapCommandMap.getCommand("image/jpeg", "view").getCommandClass());
        // Only exists in mailcap
        assertEquals("com.sun.activation.viewers.TextEditor", mailcapCommandMap.getCommand("text/plain", "edit").getCommandClass());
        // Only exists in jakarta.mailcap
        assertEquals("com.sun.activation.viewers.TextViewer", mailcapCommandMap.getCommand("text/json", "view").getCommandClass());
    }
}
