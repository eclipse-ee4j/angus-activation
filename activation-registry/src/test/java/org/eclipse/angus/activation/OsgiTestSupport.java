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

import java.io.File;
import java.util.UUID;

import javax.inject.Inject;

import org.ops4j.pax.exam.options.ModifiableCompositeOption;
import org.ops4j.pax.exam.options.SystemPropertyOption;
import org.ops4j.pax.exam.options.UrlProvisionOption;
import org.ops4j.pax.exam.util.PathUtils;
import org.osgi.framework.BundleContext;

import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.keepCaches;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.workingDirectory;

public class OsgiTestSupport {

    private final String workingDirectory = String.format("%s/target/paxexam/%s/%s", PathUtils.getBaseDir(), getClass().getSimpleName(), UUID.randomUUID());

    @Inject
    protected BundleContext bundleContext;

    protected static SystemPropertyOption failOnUnresolvedBundles() {
        return systemProperty("pax.exam.osgi.unresolved.fail").value("true");
    }

    protected static UrlProvisionOption testBundle() {
        final String pathname = System.getProperty("bundle.filename");
        final File file = new File(pathname);
        return bundle(file.toURI().toString());
    }

    protected ModifiableCompositeOption baseConfiguration() {
        return composite(
            testBundle(),
            mavenBundle().groupId("jakarta.activation").artifactId("jakarta.activation-api").versionAsInProject(),
            mavenBundle().groupId("org.apache.aries.spifly").artifactId("org.apache.aries.spifly.dynamic.framework.extension").versionAsInProject(),
            junitBundles(),
            failOnUnresolvedBundles(),
            keepCaches(),
            workingDirectory(workingDirectory)
        );
    }

}
