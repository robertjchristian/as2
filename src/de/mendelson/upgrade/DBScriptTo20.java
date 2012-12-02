//$Header: /cvsroot-fuse/mec-as2/39/mendelson/upgrade/DBScriptTo20.java,v 1.1 2012/04/18 14:10:41 heller Exp $
package de.mendelson.upgrade;

import de.mendelson.comm.as2.database.DBDriverManager;
import de.mendelson.util.ConsoleProgressBar;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import org.hsqldb.Server;
import org.hsqldb.persist.HsqlProperties;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * Update as2, must be applied for versions < 2012
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class DBScriptTo20 {

    private Connection createConnection(String hostName, String dbName) {
        if (hostName.toLowerCase().equals("localhost")) {
            hostName = "127.0.0.1";
        }
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("jdbc:hsqldb:hsql://").
                    append(hostName).append(":").
                    append("3336").
                    append("/").
                    append(dbName);
            Connection connection = DriverManager.getConnection(builder.toString(), "sa", "");
            return connection;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public void startUpgrade(String dbName) throws Throwable {
        //skip this upgrade if it is not required
        if (!this.updateIsRequired(dbName)) {
            return;
        }
        //skip this step if the database does not exist
        File dbFile = new File(dbName + ".script");
        if (!dbFile.exists()) {
            return;
        }
        Class.forName("org.hsqldb.jdbcDriver");
        HsqlProperties hsqlProperties = new HsqlProperties();
        hsqlProperties.setProperty("server.port", 3336);
        hsqlProperties.setProperty("server.database.0", "file:" + dbName);
        hsqlProperties.setProperty("server.dbname.0", dbName);
        hsqlProperties.setProperty("server.silent", true);
        hsqlProperties.setProperty("hsqldb.cache_file_scale", 128);
        hsqlProperties.setProperty("hsqldb.write_delay", false);
        hsqlProperties.setProperty("hsqldb.write_delay_millis", 0);
        Server server = new Server();
        server.setProperties(hsqlProperties);
        server.setLogWriter(null);
        server.start();
        Connection connection = createConnection("localhost", dbName);
        if (connection != null) {
            this.readObjects(connection, dbName);
            Statement statement = connection.createStatement();
            statement.execute("ALTER USER SA SET PASSWORD 'as2dbadmin'");
            ConsoleProgressBar.print(95);
            statement.execute("SHUTDOWN COMPACT");
            ConsoleProgressBar.print(100);
            System.out.println();
        }
        connection.close();
        server.shutdown();
    }

    public boolean updateIsRequired() throws Exception {
        return (this.updateIsRequired(DBDriverManager.getDBName(DBDriverManager.DB_CONFIG))
                || this.updateIsRequired(DBDriverManager.getDBName(DBDriverManager.DB_RUNTIME)));
    }

    private boolean updateIsRequired(String dbName) throws Exception {
        File propertiesFile = new File(dbName + ".properties");
        String version = "";
        if (propertiesFile.exists()) {
            Properties dbProperties = new Properties();
            FileInputStream inStream = null;
            try {
                inStream = new FileInputStream(propertiesFile.getAbsolutePath());
                dbProperties.load(inStream);
            } finally {
                if (inStream != null) {
                    inStream.close();
                }
            }
            version = dbProperties.getProperty("version");
        }
        if (version.startsWith("1")) {
            return (true);
        } else {
            return (false);
        }
    }

    private void readObjects(Connection connection, String dbName) throws Exception {
        File objectsFile = new File(dbName + ".objects");
        ConsoleProgressBar.print(1);
        BufferedReader countReader = null;
        int lineCount = 0;
        try {
            countReader = new BufferedReader(new FileReader(objectsFile));
            String countLine = "";
            while (countLine != null) {
                countLine = countReader.readLine();
                lineCount++;
            }
        } finally {
            if (countReader != null) {
                countReader.close();
            }
        }
        ConsoleProgressBar.print(10);
        int actualLine = 0;
        float lastPercent = 0f;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(objectsFile));
            String line = "";
            while (line != null) {
                actualLine++;
                float newPerc = ((float) actualLine / (float) lineCount) * 100f;
                if (newPerc > lastPercent) {
                    lastPercent = newPerc;
                    ConsoleProgressBar.print(newPerc, 20, 90);
                }
                line = reader.readLine();
                if (line != null && line.trim().length() > 0) {
                    String[] token = line.split(":");
                    String query = "UPDATE " + token[0] + " SET " + token[3] + "=? WHERE " + token[1] + "=?";
                    PreparedStatement statement = connection.prepareStatement(query);
                    byte[] data = Base64.decode(token[4]);
                    try {
                        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
                        Object dataObj = in.readObject();
                        in.close();
                        statement.setObject(1, dataObj);
                    } catch (StreamCorruptedException e) {
                        //it is no serialized object, its a byte array
                        statement.setBytes(1, data);
                    }
                    statement.setString(2, token[2]);
                    statement.executeUpdate();
                    statement.close();
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
            objectsFile.delete();
        }
    }

    public static final class Base64 {

        static private final int BASELENGTH = 255;
        static private final int LOOKUPLENGTH = 64;
        static private final int TWENTYFOURBITGROUP = 24;
        static private final int EIGHTBIT = 8;
        static private final int SIXTEENBIT = 16;
        static private final int SIXBIT = 6;
        static private final int FOURBYTE = 4;
        static private final int SIGN = -128;
        static private final char PAD = '=';
        static private final boolean fDebug = false;
        static final private byte[] base64Alphabet = new byte[BASELENGTH];
        static final private char[] lookUpBase64Alphabet = new char[LOOKUPLENGTH];

        static {

            for (int i = 0; i < BASELENGTH; i++) {
                base64Alphabet[i] = -1;
            }
            for (int i = 'Z'; i >= 'A'; i--) {
                base64Alphabet[i] = (byte) (i - 'A');
            }
            for (int i = 'z'; i >= 'a'; i--) {
                base64Alphabet[i] = (byte) (i - 'a' + 26);
            }

            for (int i = '9'; i >= '0'; i--) {
                base64Alphabet[i] = (byte) (i - '0' + 52);
            }

            base64Alphabet['+'] = 62;
            base64Alphabet['/'] = 63;

            for (int i = 0; i <= 25; i++) {
                lookUpBase64Alphabet[i] = (char) ('A' + i);
            }
            for (int i = 26, j = 0; i <= 51; i++, j++) {
                lookUpBase64Alphabet[i] = (char) ('a' + j);
            }
            for (int i = 52, j = 0; i <= 61; i++, j++) {
                lookUpBase64Alphabet[i] = (char) ('0' + j);
            }
            lookUpBase64Alphabet[62] = (char) '+';
            lookUpBase64Alphabet[63] = (char) '/';

        }

        protected static boolean isWhiteSpace(char octect) {
            return (octect == 0x20 || octect == 0xd || octect == 0xa || octect == 0x9);
        }

        protected static boolean isPad(char octect) {
            return (octect == PAD);
        }

        protected static boolean isData(char octect) {
            return (base64Alphabet[octect] != -1);
        }

        protected static boolean isBase64(char octect) {
            return (isWhiteSpace(octect) || isPad(octect) || isData(octect));
        }

        /**
         * Encodes hex octects into Base64
         *
         * @param binaryData Array containing binaryData
         * @return Encoded Base64 array
         */
        public static String encode(byte[] binaryData) {

            if (binaryData == null) {
                return null;
            }
            int lengthDataBits = binaryData.length * EIGHTBIT;
            if (lengthDataBits == 0) {
                return "";
            }

            int fewerThan24bits = lengthDataBits % TWENTYFOURBITGROUP;
            int numberTriplets = lengthDataBits / TWENTYFOURBITGROUP;
            int numberQuartet = fewerThan24bits != 0 ? numberTriplets + 1 : numberTriplets;
            int numberLines = (numberQuartet - 1) / 19 + 1;
            char encodedData[] = null;

            encodedData = new char[numberQuartet * 4 + numberLines];

            byte k = 0, l = 0, b1 = 0, b2 = 0, b3 = 0;

            int encodedIndex = 0;
            int dataIndex = 0;
            int i = 0;
            if (fDebug) {
                System.out.println("number of triplets = " + numberTriplets);
            }

            for (int line = 0; line < numberLines - 1; line++) {
                for (int quartet = 0; quartet < 19; quartet++) {
                    b1 = binaryData[dataIndex++];
                    b2 = binaryData[dataIndex++];
                    b3 = binaryData[dataIndex++];

                    if (fDebug) {
                        System.out.println("b1= " + b1 + ", b2= " + b2 + ", b3= " + b3);
                    }

                    l = (byte) (b2 & 0x0f);
                    k = (byte) (b1 & 0x03);

                    byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2) : (byte) ((b1) >> 2 ^ 0xc0);

                    byte val2 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 4) : (byte) ((b2) >> 4 ^ 0xf0);
                    byte val3 = ((b3 & SIGN) == 0) ? (byte) (b3 >> 6) : (byte) ((b3) >> 6 ^ 0xfc);

                    if (fDebug) {
                        System.out.println("val2 = " + val2);
                        System.out.println("k4   = " + (k << 4));
                        System.out.println("vak  = " + (val2 | (k << 4)));
                    }

                    encodedData[encodedIndex++] = lookUpBase64Alphabet[val1];
                    encodedData[encodedIndex++] = lookUpBase64Alphabet[val2 | (k << 4)];
                    encodedData[encodedIndex++] = lookUpBase64Alphabet[(l << 2) | val3];
                    encodedData[encodedIndex++] = lookUpBase64Alphabet[b3 & 0x3f];

                    i++;
                }
                encodedData[encodedIndex++] = 0xa;
            }

            for (; i < numberTriplets; i++) {
                b1 = binaryData[dataIndex++];
                b2 = binaryData[dataIndex++];
                b3 = binaryData[dataIndex++];

                if (fDebug) {
                    System.out.println("b1= " + b1 + ", b2= " + b2 + ", b3= " + b3);
                }

                l = (byte) (b2 & 0x0f);
                k = (byte) (b1 & 0x03);

                byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2) : (byte) ((b1) >> 2 ^ 0xc0);

                byte val2 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 4) : (byte) ((b2) >> 4 ^ 0xf0);
                byte val3 = ((b3 & SIGN) == 0) ? (byte) (b3 >> 6) : (byte) ((b3) >> 6 ^ 0xfc);

                if (fDebug) {
                    System.out.println("val2 = " + val2);
                    System.out.println("k4   = " + (k << 4));
                    System.out.println("vak  = " + (val2 | (k << 4)));
                }

                encodedData[encodedIndex++] = lookUpBase64Alphabet[val1];
                encodedData[encodedIndex++] = lookUpBase64Alphabet[val2 | (k << 4)];
                encodedData[encodedIndex++] = lookUpBase64Alphabet[(l << 2) | val3];
                encodedData[encodedIndex++] = lookUpBase64Alphabet[b3 & 0x3f];
            }

            // form integral number of 6-bit groups
            if (fewerThan24bits == EIGHTBIT) {
                b1 = binaryData[dataIndex];
                k = (byte) (b1 & 0x03);
                if (fDebug) {
                    System.out.println("b1=" + b1);
                    System.out.println("b1<<2 = " + (b1 >> 2));
                }
                byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2) : (byte) ((b1) >> 2 ^ 0xc0);
                encodedData[encodedIndex++] = lookUpBase64Alphabet[val1];
                encodedData[encodedIndex++] = lookUpBase64Alphabet[k << 4];
                encodedData[encodedIndex++] = PAD;
                encodedData[encodedIndex++] = PAD;
            } else if (fewerThan24bits == SIXTEENBIT) {
                b1 = binaryData[dataIndex];
                b2 = binaryData[dataIndex + 1];
                l = (byte) (b2 & 0x0f);
                k = (byte) (b1 & 0x03);

                byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2) : (byte) ((b1) >> 2 ^ 0xc0);
                byte val2 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 4) : (byte) ((b2) >> 4 ^ 0xf0);

                encodedData[encodedIndex++] = lookUpBase64Alphabet[val1];
                encodedData[encodedIndex++] = lookUpBase64Alphabet[val2 | (k << 4)];
                encodedData[encodedIndex++] = lookUpBase64Alphabet[l << 2];
                encodedData[encodedIndex++] = PAD;
            }

            encodedData[encodedIndex] = 0xa;

            return new String(encodedData);
        }

        /**
         * Decodes Base64 data into octects
         *
         * @param binaryData Byte array containing Base64 data
         * @return Array containind decoded data.
         */
        public static byte[] decode(String encoded) {

            if (encoded == null) {
                return null;
            }
            char[] base64Data = encoded.toCharArray();
            // remove white spaces
            int len = removeWhiteSpace(base64Data);

            if (len % FOURBYTE != 0) {
                return null;//should be divisible by four
            }

            int numberQuadruple = (len / FOURBYTE);

            if (numberQuadruple == 0) {
                return new byte[0];
            }
            byte decodedData[] = null;
            byte b1 = 0, b2 = 0, b3 = 0, b4 = 0, marker0 = 0, marker1 = 0;
            char d1 = 0, d2 = 0, d3 = 0, d4 = 0;

            int i = 0;
            int encodedIndex = 0;
            int dataIndex = 0;
            decodedData = new byte[(numberQuadruple) * 3];

            for (; i < numberQuadruple - 1; i++) {

                if (!isData((d1 = base64Data[dataIndex++]))
                        || !isData((d2 = base64Data[dataIndex++]))
                        || !isData((d3 = base64Data[dataIndex++]))
                        || !isData((d4 = base64Data[dataIndex++]))) {
                    return null;//if found "no data" just return null
                }
                b1 = base64Alphabet[d1];
                b2 = base64Alphabet[d2];
                b3 = base64Alphabet[d3];
                b4 = base64Alphabet[d4];

                decodedData[encodedIndex++] = (byte) (b1 << 2 | b2 >> 4);
                decodedData[encodedIndex++] = (byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
                decodedData[encodedIndex++] = (byte) (b3 << 6 | b4);
            }

            if (!isData((d1 = base64Data[dataIndex++]))
                    || !isData((d2 = base64Data[dataIndex++]))) {
                return null;//if found "no data" just return null
            }

            b1 = base64Alphabet[d1];
            b2 = base64Alphabet[d2];

            d3 = base64Data[dataIndex++];
            d4 = base64Data[dataIndex++];
            if (!isData((d3))
                    || !isData((d4))) {//Check if they are PAD characters
                if (isPad(d3) && isPad(d4)) {               //Two PAD e.g. 3c[Pad][Pad]
                    if ((b2 & 0xf) != 0)//last 4 bits should be zero
                    {
                        return null;
                    }
                    byte[] tmp = new byte[i * 3 + 1];
                    System.arraycopy(decodedData, 0, tmp, 0, i * 3);
                    tmp[encodedIndex] = (byte) (b1 << 2 | b2 >> 4);
                    return tmp;
                } else if (!isPad(d3) && isPad(d4)) {               //One PAD  e.g. 3cQ[Pad]
                    b3 = base64Alphabet[d3];
                    if ((b3 & 0x3) != 0)//last 2 bits should be zero
                    {
                        return null;
                    }
                    byte[] tmp = new byte[i * 3 + 2];
                    System.arraycopy(decodedData, 0, tmp, 0, i * 3);
                    tmp[encodedIndex++] = (byte) (b1 << 2 | b2 >> 4);
                    tmp[encodedIndex] = (byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
                    return tmp;
                } else {
                    return null;//an error  like "3c[Pad]r", "3cdX", "3cXd", "3cXX" where X is non data
                }
            } else { //No PAD e.g 3cQl
                b3 = base64Alphabet[d3];
                b4 = base64Alphabet[d4];
                decodedData[encodedIndex++] = (byte) (b1 << 2 | b2 >> 4);
                decodedData[encodedIndex++] = (byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
                decodedData[encodedIndex++] = (byte) (b3 << 6 | b4);

            }

            return decodedData;
        }

        /**
         * remove WhiteSpace from MIME containing encoded Base64 data.
         *
         * @param data  the byte array of base64 data (with WS)
         * @return      the new length
         */
        protected static int removeWhiteSpace(char[] data) {
            if (data == null) {
                return 0;        // count characters that's not whitespace
            }
            int newSize = 0;
            int len = data.length;
            for (int i = 0; i < len; i++) {
                if (!isWhiteSpace(data[i])) {
                    data[newSize++] = data[i];
                }
            }
            return newSize;
        }
    }

    public static final void main(String args[]) {
        DBScriptTo20 updater = new DBScriptTo20();
        try {
            boolean updateRequired = updater.updateIsRequired();
            if (updateRequired) {
                updater.startUpgrade(DBDriverManager.getDBName(DBDriverManager.DB_CONFIG));
                updater.startUpgrade(DBDriverManager.getDBName(DBDriverManager.DB_RUNTIME));
                System.out.println("Upgrade complete. Please start the AS2 now.");
            } else {
                System.out.println("No upgrade required.");
            }
        } catch (Throwable e) {
            e.printStackTrace();
            System.out.println((new StringBuilder()).append("Unable to upgrade the system: ").append(e.getMessage()).toString());
        }
    }
}
