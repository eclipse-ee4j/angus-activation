/*
 * Copyright (c) 1997, 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.eclipse.angus.activation;

import jakarta.activation.MimeTypeEntry;
import jakarta.activation.MimeTypeRegistry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

public class MimeTypeFile implements MimeTypeRegistry {
    private String fname = null;
    private Hashtable<String, MimeTypeEntry> type_hash = new Hashtable<>();

    /**
     * The construtor that takes a filename as an argument.
     *
     * @param new_fname The file name of the mime types file.
     * @throws IOException    for I/O errors
     */
    public MimeTypeFile(String new_fname) throws IOException {
        File mime_file = null;
        FileReader fr = null;

        fname = new_fname; // remember the file name

        mime_file = new File(fname); // get a file object

        fr = new FileReader(mime_file);

        try {
            parse(new BufferedReader(fr));
        } finally {
            try {
                fr.close(); // close it
            } catch (IOException e) {
                // ignore it
            }
        }
    }

    public MimeTypeFile(InputStream is) throws IOException {
        parse(new BufferedReader(new InputStreamReader(is, StandardCharsets.ISO_8859_1)));
    }

    /**
     * Creates an empty DB.
     */
    public MimeTypeFile() {
    }

    /**
     * get the MimeTypeEntry based on the file extension
     *
     * @param    file_ext    the file extension
     * @return the MimeTypeEntry
     */
    public MimeTypeEntry getMimeTypeEntry(String file_ext) {
        return type_hash.get(file_ext);
    }

    /**
     * Get the MIME type string corresponding to the file extension.
     *
     * @param    file_ext    the file extension
     * @return the MIME type string
     */
    public String getMIMETypeString(String file_ext) {
        MimeTypeEntry entry = this.getMimeTypeEntry(file_ext);

        if (entry != null)
            return entry.getMIMEType();
        else
            return null;
    }

    /**
     * Appends string of entries to the types registry, must be valid
     * .mime.types format.
     * A mime.types entry is one of two forms:
     * <p>
     * type/subtype	ext1 ext2 ...
     * or
     * type=type/subtype desc="description of type" exts=ext1,ext2,...
     * <p>
     * Example:
     * # this is a test
     * audio/basic            au
     * text/plain             txt text
     * type=application/postscript exts=ps,eps
     *
     * @param mime_types the mime.types string
     */
    public void appendToRegistry(String mime_types) {
        try {
            parse(new BufferedReader(new StringReader(mime_types)));
        } catch (IOException ex) {
            // can't happen
        }
    }

    /**
     * Parse a stream of mime.types entries.
     */
    private void parse(BufferedReader buf_reader) throws IOException {
        String line = null, prev = null;

        while ((line = buf_reader.readLine()) != null) {
            if (prev == null)
                prev = line;
            else
                prev += line;
            int end = prev.length();
            if (prev.length() > 0 && prev.charAt(end - 1) == '\\') {
                prev = prev.substring(0, end - 1);
                continue;
            }
            this.parseEntry(prev);
            prev = null;
        }
        if (prev != null)
            this.parseEntry(prev);
    }

    /**
     * Parse single mime.types entry.
     */
    private void parseEntry(String line) {
        String mime_type = null;
        String file_ext = null;
        line = line.trim();

        if (line.length() == 0) // empty line...
            return; // BAIL!

        // check to see if this is a comment line?
        if (line.charAt(0) == '#')
            return; // then we are done!

        // is it a new format line or old format?
        if (line.indexOf('=') > 0) {
            // new format
            LineTokenizer lt = new LineTokenizer(line);
            while (lt.hasMoreTokens()) {
                String name = lt.nextToken();
                String value = null;
                if (lt.hasMoreTokens() && lt.nextToken().equals("=") &&
                        lt.hasMoreTokens())
                    value = lt.nextToken();
                if (value == null) {
                    LogSupport.log("Bad .mime.types entry: " + line);
                    return;
                }
                if (name.equals("type"))
                    mime_type = value;
                else if (name.equals("exts")) {
                    StringTokenizer st = new StringTokenizer(value, ",");
                    while (st.hasMoreTokens()) {
                        file_ext = st.nextToken();
                        MimeTypeEntry entry =
                                new MimeTypeEntry(mime_type, file_ext);
                        type_hash.put(file_ext, entry);
                        LogSupport.log("Added: " + entry);
                    }
                }
            }
        } else {
            // old format
            // count the tokens
            StringTokenizer strtok = new StringTokenizer(line);
            int num_tok = strtok.countTokens();

            if (num_tok == 0) // empty line
                return;

            mime_type = strtok.nextToken(); // get the MIME type

            while (strtok.hasMoreTokens()) {
                MimeTypeEntry entry = null;

                file_ext = strtok.nextToken();
                entry = new MimeTypeEntry(mime_type, file_ext);
                type_hash.put(file_ext, entry);
                LogSupport.log("Added: " + entry);
            }
        }
    }

    // for debugging
    /*
    public static void main(String[] argv) throws Exception {
	MimeTypeFile mf = new MimeTypeFile(argv[0]);
	System.out.println("ext " + argv[1] + " type " +
						mf.getMIMETypeString(argv[1]));
	System.exit(0);
    }
    */
}

class LineTokenizer {
    private int currentPosition;
    private int maxPosition;
    private String str;
    private Vector<String> stack = new Vector<>();
    private static final String singles = "=";    // single character tokens

    /**
     * Constructs a tokenizer for the specified string.
     * <p>
     *
     * @param str a string to be parsed.
     */
    public LineTokenizer(String str) {
        currentPosition = 0;
        this.str = str;
        maxPosition = str.length();
    }

    /**
     * Skips white space.
     */
    private void skipWhiteSpace() {
        while ((currentPosition < maxPosition) &&
                Character.isWhitespace(str.charAt(currentPosition))) {
            currentPosition++;
        }
    }

    /**
     * Tests if there are more tokens available from this tokenizer's string.
     *
     * @return <code>true</code> if there are more tokens available from this
     * tokenizer's string; <code>false</code> otherwise.
     */
    public boolean hasMoreTokens() {
        if (stack.size() > 0)
            return true;
        skipWhiteSpace();
        return (currentPosition < maxPosition);
    }

    /**
     * Returns the next token from this tokenizer.
     *
     * @return the next token from this tokenizer.
     * @throws NoSuchElementException if there are no more tokens in this
     *                                tokenizer's string.
     */
    public String nextToken() {
        int size = stack.size();
        if (size > 0) {
            String t = stack.elementAt(size - 1);
            stack.removeElementAt(size - 1);
            return t;
        }
        skipWhiteSpace();

        if (currentPosition >= maxPosition) {
            throw new NoSuchElementException();
        }

        int start = currentPosition;
        char c = str.charAt(start);
        if (c == '"') {
            currentPosition++;
            boolean filter = false;
            while (currentPosition < maxPosition) {
                c = str.charAt(currentPosition++);
                if (c == '\\') {
                    currentPosition++;
                    filter = true;
                } else if (c == '"') {
                    String s;

                    if (filter) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = start + 1; i < currentPosition - 1; i++) {
                            c = str.charAt(i);
                            if (c != '\\')
                                sb.append(c);
                        }
                        s = sb.toString();
                    } else
                        s = str.substring(start + 1, currentPosition - 1);
                    return s;
                }
            }
        } else if (singles.indexOf(c) >= 0) {
            currentPosition++;
        } else {
            while ((currentPosition < maxPosition) &&
                    singles.indexOf(str.charAt(currentPosition)) < 0 &&
                    !Character.isWhitespace(str.charAt(currentPosition))) {
                currentPosition++;
            }
        }
        return str.substring(start, currentPosition);
    }

    public void pushToken(String token) {
        stack.addElement(token);
    }
}
