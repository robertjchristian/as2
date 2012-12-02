///$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/servlet/HttpReceiver.java,v 1.1 2012/04/18 14:10:39 heller Exp $
package de.mendelson.comm.as2.servlet;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * Servlet to receive rosettanet messages via HTTP
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
import de.mendelson.Copyright;
import de.mendelson.comm.as2.AS2ServerVersion;
import de.mendelson.comm.as2.client.rmi.GenericClient;
import de.mendelson.comm.as2.clientserver.ErrorObject;
import de.mendelson.comm.as2.clientserver.serialize.CommandObjectIncomingMessage;
import de.mendelson.comm.as2.preferences.PreferencesAS2;
import de.mendelson.comm.as2.server.AS2Server;
import de.mendelson.util.AS2Tools;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.logging.Logger;
import javax.servlet.*;
import javax.servlet.http.*;

public class HttpReceiver extends HttpServlet {

    private Logger logger = Logger.getLogger(AS2Server.SERVER_LOGGER_NAME);
    private PreferencesAS2 preferences = new PreferencesAS2();

    public HttpReceiver() {
    }

    /**A GET request should be rejected*/
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        PrintWriter out = res.getWriter();
        res.setContentType("text/html");
        out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
        out.println("<html>");
        out.println("    <head>");
        out.println("        <META NAME=\"description\" CONTENT=\"mendelson-e-commerce GmbH: Your EAI partner\">");
        out.println("        <META NAME=\"copyright\" CONTENT=\"mendelson-e-commerce GmbH\">");
        out.println("        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
        out.println("        <title>" + AS2ServerVersion.getProductName() + "</title>");
        out.println("        <link rel=\"shortcut icon\" href=\"images/mendelson_favicon.png\" type=\"image/x-icon\" />");
        out.println("    </head>");
        out.println("    <body>");
        out.println("<H2>" + AS2ServerVersion.getProductName() + " " + AS2ServerVersion.getVersion() + " " + AS2ServerVersion.getBuild() + "</H2>");
        out.println("<BR> " + Copyright.getCopyrightMessage());
        out.println("<BR><br>You have performed an HTTP GET on this URL. <BR>");
        out.println("To submit an AS2 message, you must POST the message to this URL <BR>");
        out.println("    </body>");
        out.println("</html>");
    }

    /**POST by the HTTP client: receive the message and work on it*/
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //stores if the commit already occured. Do not send an additional error in this case
        boolean committed = false;
        File dataFile = null;
        try {
            InputStream inStream = request.getInputStream();
            //store the data in a file to process it later. This may be useful
            //for a huge data request that may lead to a out of memory fairly easy.
            dataFile = AS2Tools.createTempFile("as2", "request");
            dataFile.deleteOnExit();
            FileOutputStream fileStream = new FileOutputStream(dataFile);
            this.copyStreams(inStream, fileStream);
            fileStream.flush();
            fileStream.close();
            //extract header
            LinkedHashMap<String, String> headerMap = new LinkedHashMap<String, String>();
            Enumeration enumeration = request.getHeaderNames();
            while (enumeration.hasMoreElements()) {
                String key = (String) enumeration.nextElement();
                headerMap.put(key.toLowerCase(), request.getHeader(key));
            }
            //check if this is a AS2 message that requests async MDN. In this case return the ok code
            //before processing the message, there is no need to keep the connection alive.
            boolean isAS2MessageRequestingAsyncMDN = headerMap.containsKey("receipt-delivery-option") && headerMap.get("receipt-delivery-option") != null && headerMap.get("receipt-delivery-option").trim().length() > 0;
            if (isAS2MessageRequestingAsyncMDN) {
                response.setStatus(HttpServletResponse.SC_OK);
                //close the connection
                response.getWriter().flush();
                response.getWriter().close();
                committed = true;
                this.informAS2ServerIncomingMessage(dataFile, headerMap, request, null);
            } else {
                this.informAS2ServerIncomingMessage(dataFile, headerMap, request, response);                
            }
        } catch (Throwable e) {
            if (!committed) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } finally {
            if (dataFile != null) {
                dataFile.delete();
            }
        }
    }//end of doPost

    /**Informs the AS2 server that a new message arrived and returns the HTTP returncode that has been set
     * by the processing server
     */
    private void informAS2ServerIncomingMessage(File dataFile,
            LinkedHashMap<String, String> headerMap, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        GenericClient client = new GenericClient();
        CommandObjectIncomingMessage commandObject = new CommandObjectIncomingMessage();
        commandObject.setMessageDataFilename(dataFile.getAbsolutePath());
        commandObject.setContentType(request.getContentType());
        String remoteHost = request.getRemoteHost();
        if (remoteHost == null) {
            remoteHost = request.getRemoteAddr();
        }
        commandObject.setRemoteHost(remoteHost);
        Iterator<String> headerIterator = headerMap.keySet().iterator();
        while (headerIterator.hasNext()) {
            String key = headerIterator.next();
            commandObject.addHeader(key, headerMap.get(key));
        }
        ErrorObject errorObject = client.send(commandObject);
        if (errorObject.getErrors() > 0) {
            StringBuilder exceptionBuffer = new StringBuilder();
            ArrayList log = commandObject.getLog();
            for (int i = 0; i < log.size(); i++) {
                if (log.get(i) != null) {
                    this.logger.severe(log.get(i).toString());
                    exceptionBuffer.append(log.get(i).toString() + "\n");
                }
            }
            throw new Exception(exceptionBuffer.toString());
        }
        //build up response, this is the sync MDN
        if (response != null) {
            CommandObjectIncomingMessage responseCommand = (CommandObjectIncomingMessage) client.getCommandObject();
            if( responseCommand.getHttpReturnCode() != HttpServletResponse.SC_OK){
                response.setStatus(responseCommand.getHttpReturnCode());
            }
            //add MDN data
            if (responseCommand.getMDNData() != null) {
                Properties header = responseCommand.getHeader();
                Iterator iterator = header.keySet().iterator();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    response.setHeader(key, header.getProperty(key));
                }
                ByteArrayInputStream inStream = new ByteArrayInputStream(responseCommand.getMDNData());
                ServletOutputStream outStream = response.getOutputStream();
                this.copyStreams(inStream, outStream);
                inStream.close();
                outStream.flush();
            }
        }
    }

    /**Checks if the mbi server runs local or not
     */
    public boolean serverRunsLocal() {
        try {
            String server = this.preferences.get(PreferencesAS2.SERVER_HOST);
            String serverAddress = InetAddress.getByName(server).getHostAddress();
            String localname = InetAddress.getLocalHost().getCanonicalHostName();
            InetAddress[] localhost = InetAddress.getAllByName("127.0.0.1");
            for (int i = 0; i < localhost.length; i++) {
                if (localhost[i].getHostAddress().equals(serverAddress)) {
                    return (true);
                }
            }
            localhost = InetAddress.getAllByName(localname);
            for (int i = 0; i < localhost.length; i++) {
                if (localhost[i].getHostAddress().equals(serverAddress)) {
                    return (true);
                }
            }
        } catch (UnknownHostException ignore) {
        }
        return (false);
    }

    /**Copies all data from one stream to another*/
    private void copyStreams(InputStream in, OutputStream out)
            throws IOException {
        BufferedInputStream inStream = new BufferedInputStream(in);
        BufferedOutputStream outStream = new BufferedOutputStream(out);
        //copy the contents to an output stream
        byte[] buffer = new byte[1024];
        int read = 1024;
        //a read of 0 must be allowed, sometimes it takes time to
        //extract data from the input
        while (read != -1) {
            read = inStream.read(buffer);
            if (read > 0) {
                outStream.write(buffer, 0, read);
            }
        }
        outStream.flush();
    }

    /** Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Receive AS2 messages via HTTP/S";
    }
}
