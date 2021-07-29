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

import jakarta.activation.DataHandler;

import java.awt.*;
import java.io.*;

public class TextInternalizer extends Panel implements Externalizable {
    // UI Vars...
    private TextArea text_area = null;

    // File Vars
    private File text_file = null;
    private String text_buffer = null;

    private DataHandler _dh = null;
    private boolean DEBUG = false;

    /**
     * Constructor
     */
    public TextInternalizer() {
        System.out.println("demo.TextInternalizer!!!!!");

        setLayout(new GridLayout(1, 1));
        // create the text area
        text_area = new TextArea("", 24, 80,
                TextArea.SCROLLBARS_VERTICAL_ONLY);
        text_area.setEditable(false);

        add(text_area);
    }

    public void writeExternal(ObjectOutput out) throws IOException {

    }

    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {


        this.setObjectInput(in);
    }


    //--------------------------------------------------------------------
//     public void setCommandContext(String verb, DataHandler dh) throws IOException {
// 	_dh = dh;
// 	this.setInputStream( _dh.getInputStream() );
//     }
    //--------------------------------------------------------------------

    /**
     * set the data stream, component to assume it is ready to
     * be read.
     *
     * @param ins input
     * @throws IOException for errors
     */
    public void setObjectInput(ObjectInput ins) throws IOException {
        try {

            text_buffer = (String) ins.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // place in the text area
        text_area.setText(text_buffer);

    }

    //--------------------------------------------------------------------
    public void addNotify() {
        super.addNotify();
        invalidate();
    }

    //--------------------------------------------------------------------
    public Dimension getPreferredSize() {
        return text_area.getMinimumSize(24, 80);
    }

}






