/*
 * Copyright (c) 2025 Oliver Lietz. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package org.eclipse.angus.activation;

import javax.inject.Inject;

import jakarta.activation.spi.MailcapRegistryProvider;
import jakarta.activation.spi.MimeTypeRegistryProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.options;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class SpiIT extends OsgiTestSupport {

    @Inject
    private MailcapRegistryProvider mailcapRegistryProvider;

    @Inject
    private MimeTypeRegistryProvider mimeTypeRegistryProvider;

    @Configuration
    public Option[] configuration() {
        return options(
            super.baseConfiguration()
        );
    }

    @Test
    public void testMailcapRegistryProvider() {
        assertThat(mailcapRegistryProvider, is(notNullValue()));
        assertThat(mailcapRegistryProvider.getInMemory(), is(notNullValue()));
    }

    @Test
    public void testMimeTypeRegistryProvider() {
        assertThat(mimeTypeRegistryProvider, is(notNullValue()));
        assertThat(mimeTypeRegistryProvider.getInMemory(), is(notNullValue()));
    }

}
