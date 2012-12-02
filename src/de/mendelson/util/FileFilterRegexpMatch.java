 //$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/FileFilterRegexpMatch.java,v 1.1 2012/04/18 14:10:41 heller Exp $
package de.mendelson.util;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * File filter that stores lists of wildcard pattern to match file lists. There
 * are positive and negative patterns possible (matching/nonmatching).
 * @author S.Heller
 * @version $Revision: 1.1 $
 */


public class FileFilterRegexpMatch implements FileFilter{
    
    /**List of matching conditions, the file will go through if it matches any
     *of these conditions*/
    private List<Pattern> matchingList = new ArrayList<Pattern>();
    
    /**List of nonmatching conditions, the file will not go through if it matches any
     *of these conditions*/
    private List<Pattern> nonmatchingList = new ArrayList<Pattern>();
    
    
    /** Creates a new instance of FileMatcher */
    public FileFilterRegexpMatch() {        
    }
    
    /**Adds a pattern the file will NOT go through if it matches. The pattern
     * could be any wildcard like "*.tmp", "jetty*.*", "*66.txt", "??xx.txt"
     */
    public void addNonMatchingPattern( String pattern ){
        this.addPattern( this.nonmatchingList, pattern );
    }
    
    /**Adds a pattern the file will go through if it matches. The pattern
     * could be any wildcard like "*.tmp", "jetty*.*", "*66.txt", "??xx.txt"
     */
    public void addMatchingPattern( String pattern ){
        this.addPattern( this.matchingList, pattern );
    }
    
    /**Adds a passed pattern to a passed list*/
    private void addPattern( List<Pattern> patternList, String pattern ){
        pattern = this.replace( pattern, ".", "\\." );
        pattern = this.replace( pattern, "*", ".*" );
        pattern = this.replace( pattern, "?", "." );
        Pattern compiledPattern = Pattern.compile( pattern );
        patternList.add( compiledPattern );
    }
    
    /**Returns if this file filer accepts the passed file
     */
    public boolean accept( String filename){
        boolean accepted = this.matchingList.isEmpty();
        //check for matching patterns
        for( int i = 0; i < this.matchingList.size(); i++ ){
            Matcher matcher = this.matchingList.get(i).matcher( filename );
            accepted = accepted | matcher.matches();
        }
        //check for nonmatching patterns
        for( int i = 0; i < this.nonmatchingList.size(); i++ ){
            Matcher matcher = this.nonmatchingList.get(i).matcher( filename );
            if( matcher.matches() ){
                return( false );
            }
        }
        return( accepted );
    }

    /**Returns if this file filer accepts the passed file
     */
    @Override
    public boolean accept( File file ){
        return( this.accept(file.getName()));
    }
    
    /**Descibr the filter*/
    public String getDescription(){
        return( "RegExp pattern matching/nonmatching" );
    }
    
    /** Replaces the string tag by the string replacement in the sourceString
     * @param source Source string
     * @param tag	String that will be replaced
     * @param replacement String that will replace the tag
     * @return String that contains the replaced values
     */
    private String replace( String source, String tag, String replacement ){
        if (source == null) return null;
        StringBuilder buffer = new StringBuilder();
        while( true ){
            int index= source.indexOf(tag);
            if( index == -1 ){
                buffer.append( source );
                return( buffer.toString() );
            }
            buffer.append( source.substring(0, index) );
            buffer.append( replacement );
            source = source.substring(index + tag.length());
        }
    }
        
    
//    public static final void main( String[] args ){
//        FileFilterRegexpMatch fileFilter = new FileFilterRegexpMatch();
//        fileFilter.addNonMatchingPattern( "*.tmp" );
//        File dir = new File( "c:/temp/testdir" );
//        File[] files = dir.listFiles( fileFilter );
//        for( int i = 0; i < files.length; i++ ){
//            System.out.println(files[i].getName());
//        }
//    }

    
    
    
    
}
