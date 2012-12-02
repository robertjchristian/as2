//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/partner/HTTPAuthentication.java,v 1.1 2012/04/18 14:10:32 heller Exp $
package de.mendelson.comm.as2.partner;

import java.io.Serializable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * Object that stores the information for a HTTP authentication used by a partner
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class HTTPAuthentication implements Serializable {

    private String user = "";
    private String password = "";
    /**Use it or dont use it?*/
    private boolean enabled = false;

    public HTTPAuthentication() {
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Use it or dont use it?
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Use it or dont use it?
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**Serializes this authentication to XML
     * @param level level in the XML hierarchie for the xml beatifying
     */
    public String toXML(int level, String type) {
        StringBuilder builder = new StringBuilder();
        String offset = "";
        for (int i = 0; i < level; i++) {
            offset += "\t";
        }
        builder.append(offset).append("<httpauthentication type=\"").append(type).append("\">\n");
        builder.append(offset).append("\t<enabled>").append(String.valueOf(this.enabled)).append("</enabled>\n");
        if (this.user != null && this.user.length() > 0) {
            builder.append(offset).append("\t<user>").append(this.toCDATA(this.user)).append("</user>\n");
        }
        if (this.password != null && this.password.length() > 0) {
            builder.append(offset).append("\t<password>").append(this.toCDATA(this.password)).append("</password>\n");
        }
        builder.append(offset).append("</httpauthentication>\n");
        return (builder.toString());
    }

    /**Adds a cdata indicator to xml data*/
    private String toCDATA(String data) {
        return ("<![CDATA[" + data + "]]>");
    }

    /**Deserializes a httpauthentication from an XML node*/
    public static HTTPAuthentication fromXML(Element element) {
        HTTPAuthentication authentication = new HTTPAuthentication();
        NodeList propertiesNodeList = element.getChildNodes();
        for (int i = 0; i < propertiesNodeList.getLength(); i++) {
            if (propertiesNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element property = (Element) propertiesNodeList.item(i);
                String key = property.getTagName();
                String value = property.getTextContent();
                if (key.equals("user")) {
                    authentication.setUser(value);
                } else if (key.equals("password")) {
                    authentication.setPassword(value);
                } else if (key.equals("enabled")) {
                    authentication.setEnabled(value.equalsIgnoreCase("true"));
                }
            }
        }
        return (authentication);
    }
}
