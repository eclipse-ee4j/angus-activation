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

import jakarta.activation.*;

public class DCHTest2 {
    private FileDataSource fds = null;
    private MailcapCommandMap cmdMap = null;
    private DataHandler dh = null;
    private DataContentHandlerFactory dchf = null;

    /**
     * main function
     *
     * @param args command line arguments
     */
    public static void main(String args[]) {
        DCHTest2 test = new DCHTest2();

        if (args.length < 2) {
            System.out.println("usage: demo.DCHTest2 file.txt mailcap");
            System.exit(1);
        }

        // first let's get a DataSource

        test.setFile(args[0]);

        test.setMailcap(args[1]);
        test.doit();
    }

    private void doit() {
        ActivationDataFlavor xfer_flavors[];
        Object content = null;

        // now let's create a DataHandler
        dh = new DataHandler(fds);
        dh.setCommandMap(cmdMap);
        System.out.println("demo.DCHTest2: DataHandler created");

        // get the dataflavors
        xfer_flavors = dh.getTransferDataFlavors();
        System.out.println("demo.DCHTest2: dh.getTransferDF returned " +
                xfer_flavors.length + " data flavors.");

        // get the content:
        try {
            content = dh.getContent();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (content == null)
            System.out.println("demo.DCHTest2: no content to be had!!!");
        else
            System.out.println("demo.DCHTest2: got content of the following type: " +
                    content.getClass().getName());

    }

    /**
     * set the file
     *
     * @param filename file
     */
    public void setFile(String filename) {
        fds = new FileDataSource(filename);
        System.out.println("demo.DCHTest2: FileDataSource created");
        if (!fds.getContentType().equals("text/plain")) {
            System.out.println("Type must be text/plain");
            System.exit(1);
        }
    }

    /**
     * set the mailcap file in the CMD Map
     *
     * @param mailcap mailcap file
     */
    public void setMailcap(String mailcap) {

        try {
            cmdMap = new MailcapCommandMap(mailcap);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


}


