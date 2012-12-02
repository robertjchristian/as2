//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/security/DNUtil.java,v 1.1 2012/04/18 14:10:45 heller Exp $
package de.mendelson.util.security;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import javax.security.auth.x500.X500Principal;

/**
 * Utility class to display DN information. 
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class DNUtil {

    public static final int ISSUER = 1;
    public static final int SUBJECT = 2;
    private static final String CN = "CN";
    private static final String OU = "OU";
    private static final String O = "O";
    private static final String L = "L";
    private static final String ST = "ST";
    private static final String C = "C";
    private static final String E = "E";

    public static String getCommonName(X509Certificate cert, int type) {
        return getDNPart(cert, CN, type);
    }

    public static String getOrgUnit(X509Certificate cert, int type) {
        return getDNPart(cert, OU, type);
    }

    public static String getOrganization(X509Certificate cert, int type) {
        return getDNPart(cert, O, type);
    }

    public static String getLocality(X509Certificate cert, int type) {
        return getDNPart(cert, L, type);
    }

    public static String getState(X509Certificate cert, int type) {
        return getDNPart(cert, ST, type);
    }

    public static String getCountryCode(X509Certificate cert, int type) {
        return getDNPart(cert, C, type);
    }

    public static String getEmail(X509Certificate cert, int type) {
        return getDNPart(cert, E, type);
    }

    /**
     * Get the string value of a X509 DN.
     * @param name the X509Name to extract a value from
     * @param target the OID of the target value
     * @param type one of the types defined in this class
     * @return a string holding the value, or <code>null</code> if the
     * specified target OID is not available in <code>name</code>.
     */
    public static String getDNPart(X509Certificate cert, String target, int type) {
        X500Principal principal;
        if (type == SUBJECT) {
            //principal = cert.getSubjectDN();
            principal = cert.getSubjectX500Principal();
        } else if (type == ISSUER) {
            principal = cert.getIssuerX500Principal();
        } else {
            throw new IllegalArgumentException("DNUtil: Unsupported principal type " + type + ".");
        }
        String name = principal.getName(X500Principal.RFC1779);
        HashMap<String, String> map = parseDN(name);
        if (map.containsKey(target)) {
            return (map.get(target));
        }
        return (null);
    }

    private static HashMap<String, String> parseDN(String dn) {
        HashMap<String, String> map = new HashMap<String, String>();
        StringBuilder buffer = new StringBuilder();
        boolean inString = false;
        for (int i = 0; i < dn.length(); i++) {
            char testChar = dn.charAt(i);
            if (inString) {
                if (testChar == '"') {
                    inString = false;
                } else {
                    buffer.append(testChar);
                }
            } else if (testChar == '"') {
                inString = true;
            } else if (testChar == ',') {
                String foundString = buffer.toString().trim();
                int index = foundString.indexOf("=");
                if (index > 0) {
                    String key = foundString.substring(0, index);
                    String value = foundString.substring(index + 1);
                    if (map.containsKey(key)) {
                        value = map.get(key) + value;
                    }
                    map.put(key, value);
                }
                buffer = new StringBuilder();
            } else {
                buffer.append(testChar);
            }
        }
        return (map);
    }
}
