/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package demo;

import jakarta.activation.ActivationDataFlavor;
import jakarta.activation.DataContentHandlerFactory;
import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;

public class ODCHTest {
    private FileDataSource fds = null;
    private DataHandler dh = null;
    private DataContentHandlerFactory dchf = null;
    private String str;

    /**
     * main function
     *
     * @param args command line arguments
     */
    public static void main(String args[]) {
        ODCHTest test = new ODCHTest();

        if (args.length != 0) {
            System.out.println("usage: demo.ODCHTest");
            System.exit(1);
        }

        // first let's get a DataSource


        test.doit();
    }

    private void doit() {
        ActivationDataFlavor xfer_flavors[];
        Object content = null;

        str = new String("This is a test");

        // now let's create a DataHandler
        dh = new DataHandler(str, "text/plain");
        System.out.println("demo.ODCHTest: DataHandler created with str & text/plain");

        // now lets set a DataContentHandlerFactory
        dchf = new SimpleDCF("text/plain:demo.PlainDCH\n");
        System.out.println("demo.ODCHTest: Simple dchf created");

        // now let's set the dchf in the dh
        DataHandler.setDataContentHandlerFactory(dchf);
        System.out.println("demo.ODCHTest: DataContentHandlerFactory set in DataHandler");

        // get the dataflavors
        xfer_flavors = dh.getTransferDataFlavors();
        System.out.println("demo.ODCHTest: dh.getTransferDF returned " +
                xfer_flavors.length + " data flavors.");

        // get the content:
        try {
            content = dh.getContent();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (content == null)
            System.out.println("demo.ODCHTest: no content to be had!!!");
        else {
            System.out.println("demo.ODCHTest: got content of the following type: " +
                    content.getClass().getName());
            if (content == str)
                System.out.println("get content works");

        }
    }

}
