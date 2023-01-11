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

import jakarta.activation.MailcapRegistry;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MailcapFile implements MailcapRegistry {

    /**
     * A Map indexed by MIME type (string) that references
     * a Map of commands for each type.  The comand Map
     * is indexed by the command name and references a List of
     * class names (strings) for each command.
     */
    private Map<String, Map<String, List<String>>> type_hash = new HashMap<>();

    /**
     * Another Map like above, but for fallback entries.
     */
    private Map<String, Map<String, List<String>>> fallback_hash = new HashMap<>();

    /**
     * A Map indexed by MIME type (string) that references
     * a List of native commands (string) corresponding to the type.
     */
    private Map<String, List<String>> native_commands = new HashMap<>();

    private static boolean addReverse = false;

    static {
        try {
            addReverse = Boolean.getBoolean("angus.activation.addreverse");
            // deprecated in 1.1.0:
            String prop = System.getProperty("jakarta.activation.addreverse");
            if (prop != null) {
                LogSupport.log("'jakarta.activation.addreverse' property is deprecated, use 'angus.activation.addreverse' instead");
                addReverse = Boolean.parseBoolean(prop);
            }
            prop = System.getProperty("javax.activation.addreverse");
            if (prop != null) {
                LogSupport.log("'javax.activation.addreverse' property is deprecated, use 'angus.activation.addreverse' instead");
                addReverse = Boolean.parseBoolean(prop);
            }
        } catch (Throwable t) {
            // ignore any errors
        }
    }

    /**
     * The constructor that takes a filename as an argument.
     *
     * @param new_fname The file name of the mailcap file.
     * @throws IOException    for I/O errors
     */
    public MailcapFile(String new_fname) throws IOException {
        LogSupport.log("new MailcapFile: file " + new_fname);
        try (BufferedReader reader = new BufferedReader(new FileReader(new_fname))) {
            parse(reader);
        }
    }

    /**
     * The constructor that takes an input stream as an argument.
     *
     * @param is the input stream
     * @throws IOException    for I/O errors
     */
    public MailcapFile(InputStream is) throws IOException {
        LogSupport.log("new MailcapFile: InputStream");
        parse(new BufferedReader(new InputStreamReader(is, StandardCharsets.ISO_8859_1)));
    }

    /**
     * Mailcap file default constructor.
     */
    public MailcapFile() {
        LogSupport.log("new MailcapFile: default");
    }

    /**
     * Get the Map of MailcapEntries based on the MIME type.
     *
     * <p>
     * <strong>Semantics:</strong> First check for the literal mime type,
     * if that fails looks for wildcard &lt;type&gt;/\* and return that.
     * Return the list of all that hit.
     *
     * @param    mime_type    the MIME type
     * @return the map of MailcapEntries
     */
    public Map<String, List<String>> getMailcapList(String mime_type) {
        return getMailcapList(mime_type, type_hash);
    }

    /**
     * Get the Map of fallback MailcapEntries based on the MIME type.
     *
     * <p>
     * <strong>Semantics:</strong> First check for the literal mime type,
     * if that fails looks for wildcard &lt;type&gt;/\* and return that.
     * Return the list of all that hit.
     *
     * @param    mime_type    the MIME type
     * @return the map of fallback MailcapEntries
     */
    public Map<String, List<String>> getMailcapFallbackList(String mime_type) {
        return getMailcapList(mime_type, fallback_hash);
    }

    /**
     * Get the Map of MailcapEntries from given db based on the MIME type.
     *
     * <p>
     * <strong>Semantics:</strong> First check for the literal mime type,
     * if that fails looks for wildcard &lt;type&gt;/\* and return that.
     * Return the list of all that hit.
     *
     * @param    mime_type    the MIME type
     * @param    db           the db to search in
     * @return the map of fallback MailcapEntries
     */
    private Map<String, List<String>> getMailcapList(String mime_type, Map<String, Map<String, List<String>>> db) {
        Map<String, List<String>> search_result = null;
        Map<String, List<String>> wildcard_result = null;

        // first try the literal
        search_result = db.get(mime_type);

        // ok, now try the wildcard
        int separator = mime_type.indexOf('/');
        String subtype = mime_type.substring(separator + 1);
        if (!subtype.equals("*")) {
            String type = mime_type.substring(0, separator + 1) + "*";
            wildcard_result = db.get(type);

            if (wildcard_result != null) { // damn, we have to merge!!!
                if (search_result != null)
                    search_result =
                            mergeResults(search_result, wildcard_result);
                else
                    search_result = wildcard_result;
            }
        }
        return search_result;
    }

    /**
     * Return all the MIME types known to this mailcap file.
     *
     * @return a String array of the MIME types
     */
    public String[] getMimeTypes() {
        Set<String> types = new HashSet<>(type_hash.keySet());
        types.addAll(fallback_hash.keySet());
        types.addAll(native_commands.keySet());
        String[] mts = new String[types.size()];
        mts = types.toArray(mts);
        return mts;
    }

    /**
     * Return all the native comands for the given MIME type.
     *
     * @param    mime_type    the MIME type
     * @return a String array of the commands
     */
    public String[] getNativeCommands(String mime_type) {
        String[] cmds = null;
        List<String> v = native_commands.get(mime_type.toLowerCase(Locale.ENGLISH));
        if (v != null) {
            cmds = new String[v.size()];
            cmds = v.toArray(cmds);
        }
        return cmds;
    }

    /**
     * Merge the first hash into the second.
     * This merge will only effect the hashtable that is
     * returned, we don't want to touch the one passed in since
     * its integrity must be maintained.
     */
    private Map<String, List<String>> mergeResults(Map<String, List<String>> first, Map<String, List<String>> second) {
        Iterator<String> verb_enum = second.keySet().iterator();
        Map<String, List<String>> clonedHash = new HashMap<>(first);

        // iterate through the verbs in the second map
        while (verb_enum.hasNext()) {
            String verb = verb_enum.next();
            List<String> cmdVector = clonedHash.get(verb);
            if (cmdVector == null) {
                clonedHash.put(verb, second.get(verb));
            } else {
                // merge the two
                List<String> oldV = second.get(verb);
                cmdVector = new ArrayList<>(cmdVector);
                cmdVector.addAll(oldV);
                clonedHash.put(verb, cmdVector);
            }
        }
        return clonedHash;
    }

    /**
     * appendToMailcap: Append to this Mailcap DB, use the mailcap
     * format:
     * Comment == "# <i>comment string</i>"
     * Entry == "mimetype;        javabeanclass"
     * <p>
     * Example:
     * # this is a comment
     * image/gif       jaf.viewers.ImageViewer
     *
     * @param    mail_cap    the mailcap string
     */
    public void appendToMailcap(String mail_cap) {
        LogSupport.log("appendToMailcap: " + mail_cap);
        try {
            parse(new BufferedReader(new StringReader(mail_cap)));
        } catch (IOException ex) {
            // can't happen
        }
    }

    /**
     * parse file into a hash table of MC Type Entry Obj
     */
    private void parse(BufferedReader reader) throws IOException {
        String line = null;
        String continued = null;

        while ((line = reader.readLine()) != null) {
            //    LogSupport.log("parsing line: " + line);

            line = line.trim();

            try {
                if (line.charAt(0) == '#')
                    continue;
                if (line.charAt(line.length() - 1) == '\\') {
                    if (continued != null)
                        continued += line.substring(0, line.length() - 1);
                    else
                        continued = line.substring(0, line.length() - 1);
                } else if (continued != null) {
                    // handle the two strings
                    continued = continued + line;
                    //	LogSupport.log("parse: " + continued);
                    try {
                        parseLine(continued);
                    } catch (MailcapParseException e) {
                        //e.printStackTrace();
                    }
                    continued = null;
                } else {
                    //	LogSupport.log("parse: " + line);
                    try {
                        parseLine(line);
                        // LogSupport.log("hash.size = " + type_hash.size());
                    } catch (MailcapParseException e) {
                        //e.printStackTrace();
                    }
                }
            } catch (StringIndexOutOfBoundsException e) {
            }
        }
    }

    /**
     * A routine to parse individual entries in a Mailcap file.
     * <p>
     * Note that this routine does not handle line continuations.
     * They should have been handled prior to calling this routine.
     *
     * @param    mailcapEntry    the mailcap entry
     * @throws MailcapParseException    for parse errors
     * @throws IOException    for I/O errors
     */
    protected void parseLine(String mailcapEntry)
            throws MailcapParseException, IOException {
        MailcapTokenizer tokenizer = new MailcapTokenizer(mailcapEntry);
        tokenizer.setIsAutoquoting(false);

        LogSupport.log("parse: " + mailcapEntry);
        //	parse the primary type
        int currentToken = tokenizer.nextToken();
        if (currentToken != MailcapTokenizer.STRING_TOKEN) {
            reportParseError(MailcapTokenizer.STRING_TOKEN, currentToken,
                    tokenizer.getCurrentTokenValue());
        }
        String primaryType =
                tokenizer.getCurrentTokenValue().toLowerCase(Locale.ENGLISH);
        String subType = "*";

        //	parse the '/' between primary and sub
        //	if it's not present that's ok, we just don't have a subtype
        currentToken = tokenizer.nextToken();
        if ((currentToken != MailcapTokenizer.SLASH_TOKEN) &&
                (currentToken != MailcapTokenizer.SEMICOLON_TOKEN)) {
            reportParseError(MailcapTokenizer.SLASH_TOKEN,
                    MailcapTokenizer.SEMICOLON_TOKEN, currentToken,
                    tokenizer.getCurrentTokenValue());
        }

        //	only need to look for a sub type if we got a '/'
        if (currentToken == MailcapTokenizer.SLASH_TOKEN) {
            //	parse the sub type
            currentToken = tokenizer.nextToken();
            if (currentToken != MailcapTokenizer.STRING_TOKEN) {
                reportParseError(MailcapTokenizer.STRING_TOKEN,
                        currentToken, tokenizer.getCurrentTokenValue());
            }
            subType =
                    tokenizer.getCurrentTokenValue().toLowerCase(Locale.ENGLISH);

            //	get the next token to simplify the next step
            currentToken = tokenizer.nextToken();
        }

        String mimeType = primaryType + "/" + subType;

        LogSupport.log("  Type: " + mimeType);

        //	now setup the commands hashtable
        Map<String, List<String>> commands = new LinkedHashMap<>();    // keep commands in order found

        //	parse the ';' that separates the type from the parameters
        if (currentToken != MailcapTokenizer.SEMICOLON_TOKEN) {
            reportParseError(MailcapTokenizer.SEMICOLON_TOKEN,
                    currentToken, tokenizer.getCurrentTokenValue());
        }
        //	eat it

        //	parse the required view command
        tokenizer.setIsAutoquoting(true);
        currentToken = tokenizer.nextToken();
        tokenizer.setIsAutoquoting(false);
        if ((currentToken != MailcapTokenizer.STRING_TOKEN) &&
                (currentToken != MailcapTokenizer.SEMICOLON_TOKEN)) {
            reportParseError(MailcapTokenizer.STRING_TOKEN,
                    MailcapTokenizer.SEMICOLON_TOKEN, currentToken,
                    tokenizer.getCurrentTokenValue());
        }

        if (currentToken == MailcapTokenizer.STRING_TOKEN) {
            // have a native comand, save the entire mailcap entry
            //String nativeCommand = tokenizer.getCurrentTokenValue();
            List<String> v = native_commands.get(mimeType);
            if (v == null) {
                v = new ArrayList<>();
                v.add(mailcapEntry);
                native_commands.put(mimeType, v);
            } else {
                // XXX - check for duplicates?
                v.add(mailcapEntry);
            }
        }

        //	only have to get the next token if the current one isn't a ';'
        if (currentToken != MailcapTokenizer.SEMICOLON_TOKEN) {
            currentToken = tokenizer.nextToken();
        }

        // look for a ';' which will indicate whether
        // a parameter list is present or not
        if (currentToken == MailcapTokenizer.SEMICOLON_TOKEN) {
            boolean isFallback = false;
            do {
                //	eat the ';'

                //	parse the parameter name
                currentToken = tokenizer.nextToken();
                if (currentToken != MailcapTokenizer.STRING_TOKEN) {
                    reportParseError(MailcapTokenizer.STRING_TOKEN,
                            currentToken, tokenizer.getCurrentTokenValue());
                }
                String paramName = tokenizer.getCurrentTokenValue().
                        toLowerCase(Locale.ENGLISH);

                //	parse the '=' which separates the name from the value
                currentToken = tokenizer.nextToken();
                if ((currentToken != MailcapTokenizer.EQUALS_TOKEN) &&
                        (currentToken != MailcapTokenizer.SEMICOLON_TOKEN) &&
                        (currentToken != MailcapTokenizer.EOI_TOKEN)) {
                    reportParseError(MailcapTokenizer.EQUALS_TOKEN,
                            MailcapTokenizer.SEMICOLON_TOKEN,
                            MailcapTokenizer.EOI_TOKEN,
                            currentToken, tokenizer.getCurrentTokenValue());
                }

                //	we only have a useful command if it is named
                if (currentToken == MailcapTokenizer.EQUALS_TOKEN) {
                    //	eat it

                    //	parse the parameter value (which is autoquoted)
                    tokenizer.setIsAutoquoting(true);
                    currentToken = tokenizer.nextToken();
                    tokenizer.setIsAutoquoting(false);
                    if (currentToken != MailcapTokenizer.STRING_TOKEN) {
                        reportParseError(MailcapTokenizer.STRING_TOKEN,
                                currentToken, tokenizer.getCurrentTokenValue());
                    }
                    String paramValue =
                            tokenizer.getCurrentTokenValue();

                    // add the class to the list iff it is one we care about
                    if (paramName.startsWith("x-java-")) {
                        String commandName = paramName.substring(7);
                        //	7 == "x-java-".length

                        if (commandName.equals("fallback-entry") &&
                                paramValue.equalsIgnoreCase("true")) {
                            isFallback = true;
                        } else {

                            //	setup the class entry list
                            LogSupport.log("    Command: " + commandName +
                                        ", Class: " + paramValue);
                            List<String> classes = commands.computeIfAbsent(commandName, k -> new ArrayList<>());
                            if (addReverse)
                                classes.add(0, paramValue);
                            else
                                classes.add(paramValue);
                        }
                    }

                    //	set up the next iteration
                    currentToken = tokenizer.nextToken();
                }
            } while (currentToken == MailcapTokenizer.SEMICOLON_TOKEN);

            Map<String, Map<String, List<String>>> masterHash = isFallback ? fallback_hash : type_hash;
            Map<String, List<String>> curcommands = masterHash.get(mimeType);
            if (curcommands == null) {
                masterHash.put(mimeType, commands);
            } else {
                LogSupport.log("Merging commands for type " + mimeType);
                // have to merge current and new commands
                // first, merge list of classes for commands already known
                Iterator<String> cn = curcommands.keySet().iterator();
                while (cn.hasNext()) {
                    String cmdName = cn.next();
                    List<String> ccv = curcommands.get(cmdName);
                    List<String> cv = commands.get(cmdName);
                    if (cv == null)
                        continue;
                    // add everything in cv to ccv, if it's not already there
                    Iterator<String> cvn = cv.iterator();
                    while (cvn.hasNext()) {
                        String clazz = cvn.next();
                        if (!ccv.contains(clazz))
                            if (addReverse)
                                ccv.add(0, clazz);
                            else
                                ccv.add(clazz);
                    }
                }
                // now, add commands not previously known
                cn = commands.keySet().iterator();
                while (cn.hasNext()) {
                    String cmdName = cn.next();
                    if (curcommands.containsKey(cmdName))
                        continue;
                    List<String> cv = commands.get(cmdName);
                    curcommands.put(cmdName, cv);
                }
            }
        } else if (currentToken != MailcapTokenizer.EOI_TOKEN) {
            reportParseError(MailcapTokenizer.EOI_TOKEN,
                    MailcapTokenizer.SEMICOLON_TOKEN,
                    currentToken, tokenizer.getCurrentTokenValue());
        }
    }

    protected static void reportParseError(int expectedToken, int actualToken,
                                           String actualTokenValue) throws MailcapParseException {
        throw new MailcapParseException("Encountered a " +
                MailcapTokenizer.nameForToken(actualToken) + " token (" +
                actualTokenValue + ") while expecting a " +
                MailcapTokenizer.nameForToken(expectedToken) + " token.");
    }

    protected static void reportParseError(int expectedToken,
                                           int otherExpectedToken, int actualToken, String actualTokenValue)
            throws MailcapParseException {
        throw new MailcapParseException("Encountered a " +
                MailcapTokenizer.nameForToken(actualToken) + " token (" +
                actualTokenValue + ") while expecting a " +
                MailcapTokenizer.nameForToken(expectedToken) + " or a " +
                MailcapTokenizer.nameForToken(otherExpectedToken) + " token.");
    }

    protected static void reportParseError(int expectedToken,
                                           int otherExpectedToken, int anotherExpectedToken, int actualToken,
                                           String actualTokenValue) throws MailcapParseException {
        throw new MailcapParseException("Encountered a " +
                MailcapTokenizer.nameForToken(actualToken) + " token (" +
                actualTokenValue + ") while expecting a " +
                MailcapTokenizer.nameForToken(expectedToken) + ", a " +
                MailcapTokenizer.nameForToken(otherExpectedToken) + ", or a " +
                MailcapTokenizer.nameForToken(anotherExpectedToken) + " token.");
    }

     /* for debugging
     public static void	main(String[] args) throws Exception {
     	Map masterHash = new HashMap();
     	for (int i = 0; i < args.length; ++i) {
	    System.out.println("Entry " + i + ": " + args[i]);
	    parseLine(args[i], masterHash);
     	}

     	Enumeration types = masterHash.keys();
     	while (types.hasMoreElements()) {
	    String key = (String)types.nextElement();
	    System.out.println("MIME Type: " + key);

	    Map commandHash = (Map)masterHash.get(key);
	    Enumeration commands = commandHash.keys();
	    while (commands.hasMoreElements()) {
		String command = (String)commands.nextElement();
		System.out.println("    Command: " + command);

		Vector classes = (Vector)commandHash.get(command);
		for (int i = 0; i < classes.size(); ++i) {
			System.out.println("        Class: " +
					    (String)classes.elementAt(i));
		}
	    }

	    System.out.println("");
	}
    }
    */
}
