/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

module jakarta.activation {

    requires java.logging;
    requires static org.graalvm.sdk;

    exports jakarta.activation;
    exports jakarta.activation.spi;
    exports org.eclipse.angus.activation;

    uses jakarta.activation.spi.MailcapRegistryProvider;
    uses jakarta.activation.spi.MimeTypeRegistryProvider;

    provides jakarta.activation.spi.MailcapRegistryProvider
        with org.eclipse.angus.activation.MailcapRegistryProviderImpl;

    provides jakarta.activation.spi.MimeTypeRegistryProvider
        with org.eclipse.angus.activation.MimeTypeRegistryProviderImpl;
}
