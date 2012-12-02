//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/notification/NotificationMail.java,v 1.1 2012/04/18 14:10:31 heller Exp $
package de.mendelson.comm.as2.notification;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Properties;

/**
 * Storage class for a notification mail, could also read the mail template format
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class NotificationMail {

    private String body = "--";
    private String subject = "--";

    /**Reads the notification mail template file*/
    public void read(String templateFile, Properties replacement) throws Exception {
        StringBuilder bodyBuffer = new StringBuilder();
        boolean inSubject = false;
        boolean inBody = false;

        FileReader templateIn = new FileReader(templateFile);
        BufferedReader reader = new BufferedReader(templateIn);
        String line = "";
        while (line != null) {
            line = reader.readLine();
            if (line != null) {
                if (line.equals("[SUBJECT]")) {
                    inSubject = true;
                    inBody = false;
                    continue;
                } else if (line.equals("[BODY]")) {
                    inSubject = false;
                    inBody = true;
                    continue;
                }
                if (inSubject) {
                    this.setSubject(this.replaceAllVars(line, replacement));
                    inSubject = false;
                } else if (inBody) {
                    if (bodyBuffer.length() > 0) {
                        bodyBuffer.append("\n");
                    }
                    bodyBuffer.append(line);
                }
            }
        }
        this.setBody(this.replaceAllVars(bodyBuffer.toString(), replacement));
    }

    /**Replaces all used variables in the passed source and returns them
     * 
     * @param source Source string to replace the var occurences in
     * @param replacement container that contains the key-value pairs of replacements
     * @return The replaced string
     */
    private String replaceAllVars(String source, Properties replacement) {
        Iterator iterator = replacement.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            String value = replacement.getProperty(key);
            source = this.replace(source, key, value);
        }
        return (source);
    }

    /** Replaces the string tag by the string replacement in the sourceString
     * @param source Source string
     * @param tag	String that will be replaced
     * @param replacement String that will replace the tag
     * @return String that contains the replaced values
     */
    private String replace(String source, String tag, String replacement) {
        if (source == null) {
            return null;
        }
        StringBuilder buffer = new StringBuilder();
        while (true) {
            int index = source.indexOf(tag);
            if (index == -1) {
                buffer.append(source);
                return (buffer.toString());
            }
            buffer.append(source.substring(0, index));
            buffer.append(replacement);
            source = source.substring(index + tag.length());
        }
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
