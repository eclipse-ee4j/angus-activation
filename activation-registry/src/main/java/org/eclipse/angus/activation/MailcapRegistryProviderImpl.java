/*
 * Copyright (c) 2021, 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.eclipse.angus.activation;

import jakarta.activation.MailcapRegistry;
import jakarta.activation.spi.MailcapRegistryProvider;

import java.io.IOException;
import java.io.InputStream;

public class MailcapRegistryProviderImpl implements MailcapRegistryProvider {

    /**
     * Default constructor
     */
    public MailcapRegistryProviderImpl() {
    }

    @Override
    public MailcapRegistry getByFileName(String name) throws IOException {
        return new MailcapFile(name);
    }

    @Override
    public MailcapRegistry getByInputStream(InputStream inputStream) throws IOException {
        return new MailcapFile(inputStream);
    }

    @Override
    public MailcapRegistry getInMemory() {
        return new MailcapFile();
    }
}
