/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.activation.nativeimage;

import com.sun.activation.registries.MailcapFile;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Feature to add reflection configuration to the native image.
 */
public class AngusActivationFeature implements Feature {

    private static final boolean ENABLED = getOption("angus.activation.native-image.enable", true);
    private static final boolean DEBUG = getOption("angus.activation.native-image.trace", false);

    private static final List<String> RESOURCES = new ArrayList<String>() {{ add("META-INF/mailcap"); add("META-INF/mailcap.default");}} ;

    /**
     * Default constructor
     */
    public AngusActivationFeature() {
    }

    @Override
    public boolean isInConfiguration(Feature.IsInConfigurationAccess access) {
        return ENABLED;
    }

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        Set<String> commands = new HashSet<>();
        for (String resource: RESOURCES) {
            log(() -> "looking for " + resource);
            try {
                Enumeration<URL> urls = access.getApplicationClassLoader().getResources(resource);
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    log(() -> "processing " + url);
                    try (InputStream is = url.openStream()) {
                        MailcapFile mf = new MailcapFile(is);
                        for (String mimeType: mf.getMimeTypes()) {
                            Map<String, List<String>> map = mf.getMailcapList(mimeType);
                            if (map != null) {
                                map.values().forEach(list -> list.forEach(commands::add));
                            }
                            map = mf.getMailcapFallbackList(mimeType);
                            if (map != null) {
                                map.values().forEach( list -> list.forEach(commands::add));
                            }
                        }
                    }
                }
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }

        commands.forEach(command -> {
            Class<?> cls = access.findClassByName(command);
            if (cls != null) {
                log(() -> "Registering " + cls);
                try {
                    RuntimeReflection.register(cls);
                    RuntimeReflection.register(cls.getConstructor());
                } catch (NoSuchMethodException e) {
                    log(() -> "\tno constructor for " + cls);
                }
            } else {
                log(() -> "Class for '" + command + "' not found");
            }
        });
    }

    private static boolean getOption(String name, boolean def) {
        String prop = System.getProperty(name);
        if (prop == null) {
            return def;
        }
        return Boolean.parseBoolean(name);
    }

    private static void log(Supplier<String> msg) {
        if (DEBUG) {
            System.out.println(msg.get());
        }
    }

}
