///$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/servlet/ServerState.java,v 1.1 2012/04/18 14:10:39 heller Exp $
package de.mendelson.comm.as2.servlet;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */

/**
 * Servlet to display the server state
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
import de.mendelson.comm.as2.AS2ServerVersion;
import de.mendelson.comm.as2.client.rmi.GenericClient;
import de.mendelson.comm.as2.clientserver.ErrorObject;
import de.mendelson.comm.as2.clientserver.serialize.CommandObjectServerInfo;
import java.io.*;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Map;
import javax.servlet.*;
import javax.servlet.http.*;

public class ServerState extends HttpServlet {

    /**Format the date display*/
    private DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);


    public ServerState() {
    }

    /**A GET request should be rejected*/
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        Map map = req.getParameterMap();
        String output = "html";
        if( map.containsKey("output")){
            output = ((String[])map.get( "output"))[0];
            if( output == null ){
                output = "html";
            }
        }
        PrintWriter out = res.getWriter();
        if( output.equalsIgnoreCase("html")){
            res.setContentType("text/html");
            out.println( this.getOutputHTML());
        }else{
            res.setContentType("text/html");
            out.println( "<html>Unknown output type, please use one of 'html'</html>");
        }
    }

    private String getOutputHTML(){
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
        builder.append("<html>");
        builder.append("    <head>");
        builder.append("        <META NAME=\"description\" CONTENT=\"mendelson-e-commerce GmbH: Your EAI partner\">");
        builder.append("        <META NAME=\"copyright\" CONTENT=\"mendelson-e-commerce GmbH\">");
        builder.append("        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
        builder.append("        <title>" + AS2ServerVersion.getProductName() + "</title>");
        builder.append("        <link rel=\"shortcut icon\" href=\"images/mendelson_favicon.png\" type=\"image/x-icon\" />");
        builder.append("    </head>");
        builder.append("    <body>");
        boolean processingUnitUp = false;        
        GenericClient client = new GenericClient();
        CommandObjectServerInfo commandObject = new CommandObjectServerInfo();
        ErrorObject errorObject = client.send(commandObject);
        if (errorObject.noErrorsAndWarnings()) {
            commandObject = (CommandObjectServerInfo) client.getCommandObject();
            long startTime = new Long(commandObject.getProperties().getProperty(CommandObjectServerInfo.SERVER_START_TIME)).longValue();
            builder.append("The AS2 processing unit " + commandObject.getProperties().getProperty(CommandObjectServerInfo.SERVER_PRODUCT_NAME) + " " + commandObject.getProperties().getProperty(CommandObjectServerInfo.SERVER_VERSION) + " " + commandObject.getProperties().getProperty(CommandObjectServerInfo.SERVER_BUILD) + " is up and running since " + format.format(startTime) + ".");
            processingUnitUp = true;
        } else {
            builder.append("Error connecting to AS2 processing unit: ");
            ArrayList log = client.getLog();
            for (int i = 0; i < log.size(); i++) {
                if (log.get(i) != null) {
                    builder.append(log.get(i));
                }
            }
        }
        builder.append("<br><br>");
        if (processingUnitUp) {
            builder.append("System status is fine.");
        } else {
            builder.append("Errors encounted.");
        }
        builder.append("</html>");
        return( builder.toString());
    }


    /**POST by the HTTP client: receive the message and work on it*/
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }//end of doPost

    /** Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Display AS2 server state";
    }
}
