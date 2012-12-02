//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/ExecArgumentParser.java,v 1.1 2012/04/18 14:10:41 heller Exp $
package de.mendelson.util;

import java.util.*;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * Parses the arguments of an exec call, allows to quote arguments by double quote
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class ExecArgumentParser {

    private final String DOUBLE_QUOTE = "\"";
    //the parser flips between these two sets of delimiters
    private final String WHITESPACE_AND_QUOTES = " \t\r\n\"";
    private final String QUOTES_ONLY = "\"";

    public ExecArgumentParser() {
    }

    /**
     * Parses the command line and returns the command itself and the arguments
     *
     * @return Set of Strings, one for each word in fSearchText; here "word"
     * is defined as either a lone word surrounded by whitespace, or as a series
     * of words surrounded by double quotes, "like this"
     */
    public String[] parse(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Text to parse cannot be null.");
        }
        Set<String> result = new LinkedHashSet<String>();

        boolean returnTokens = true;
        String currentDelims = WHITESPACE_AND_QUOTES;
        StringTokenizer parser = new StringTokenizer(
                text,
                currentDelims,
                returnTokens);

        String token = null;
        while (parser.hasMoreTokens()) {
            token = parser.nextToken(currentDelims);
            if (!isDoubleQuote(token)) {
                addTokenToResult(token, result);
            } else {
                currentDelims = flipDelimiters(currentDelims);
            }
        }
        String[] resultArray = new String[result.size()];
        result.toArray(resultArray);
        return( resultArray );
    }


    private boolean textHasContent(String aText) {
        return (aText != null) && (!aText.trim().equals(""));
    }

    private void addTokenToResult(String aToken, Set<String> aResult) {
        if (textHasContent(aToken)) {
            aResult.add(aToken.trim());
        }
    }

    private boolean isDoubleQuote(String aToken) {
        return aToken.equals(DOUBLE_QUOTE);
    }

    private String flipDelimiters(String aCurrentDelims) {
        String result = null;
        if (aCurrentDelims.equals(WHITESPACE_AND_QUOTES)) {
            result = QUOTES_ONLY;
        } else {
            result = WHITESPACE_AND_QUOTES;
        }
        return result;
    }

//    public static void main(String[] aArguments) {
//        ExecArgumentParser parser = new ExecArgumentParser();
//        String[] result = parser.parse("myexec.bat -a \"ABC DEF\" ABC 123 \"A A A \" XXX");
//        for( String token:result){
//            System.out.println("#" + token + "#");
//        }
//    }
}

