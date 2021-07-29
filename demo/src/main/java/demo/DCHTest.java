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

public class DCHTest {
    private FileDataSource fds = null;
    private DataHandler dh = null;
    private DataContentHandlerFactory dchf = null;

    /**
     * main function
     *
     * @param args command line arguments
     */
    public static void main(String args[]) {
        DCHTest test = new DCHTest();

        if (args.length == 0) {
            System.out.println("usage: demo.DCHTest file.txt");
            System.exit(1);
        }

        // first let's get a DataSource

        test.setFile(args[0]);

        test.doit();
    }

    private void doit() {
        ActivationDataFlavor xfer_flavors[];
        Object content = null;

        // now let's create a DataHandler
        dh = new DataHandler(fds);
        System.out.println("demo.DCHTest: DataHandler created");

        // now lets set a DataContentHandlerFactory
        dchf = new SimpleDCF("text/plain:demo.PlainDCH\n");
        System.out.println("demo.DCHTest: Simple dchf created");

        // now let's set the dchf in the dh
        DataHandler.setDataContentHandlerFactory(dchf);
        System.out.println("demo.DCHTest: DataContentHandlerFactory set in DataHandler");

        // get the dataflavors
        xfer_flavors = dh.getTransferDataFlavors();
        System.out.println("demo.DCHTest: dh.getTransferDF returned " +
                xfer_flavors.length + " data flavors.");

        // get the content:
        try {
            content = dh.getContent();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (content == null)
            System.out.println("demo.DCHTest: no content to be had!!!");
        else
            System.out.println("demo.DCHTest: got content of the following type: " +
                    content.getClass().getName());

    }

    /**
     * set the file
     *
     * @param filename file
     */
    public void setFile(String filename) {
        fds = new FileDataSource(filename);
        System.out.println("demo.DCHTest: FileDataSource created");
        if (!fds.getContentType().equals("text/plain")) {
            System.out.println("Type must be text/plain");
            System.exit(1);
        }
    }

}


