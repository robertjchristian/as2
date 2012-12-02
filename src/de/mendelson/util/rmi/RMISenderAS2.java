//$Header: /cvsroot-fuse/mec-as2/39/mendelson/util/rmi/RMISenderAS2.java,v 1.1 2012/04/18 14:10:45 heller Exp $
package de.mendelson.util.rmi;
import de.mendelson.comm.as2.server.AS2Server;
import java.rmi.*;


/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */


/**
 * Class to send an object via RMI, execute it on the
 * server side and return a return object, implementation for MBI
 * @author  S.Heller
 * @version $Revision: 1.1 $
 */
public class RMISenderAS2 extends RMISender{
    
    /**Create a new RMISender object
     * @param host name of the server
     * @param port Port of the RMI server to connect to
     * @param service name of the registred service to connect to
     */
    public RMISenderAS2( String host, int port, String service ){
        super( host, port, service );
    }
    
    /**RMI Registry lookup for the service*/
    private MecRemote lookupRemoteService() throws Exception {
        StringBuilder lookupStr = new StringBuilder();
        lookupStr.append("rmi://");
        lookupStr.append(this.host);
        lookupStr.append(":");
        lookupStr.append(String.valueOf(this.port));
        lookupStr.append("/");
        lookupStr.append(this.service);
        MecRemote request = (MecRemote) Naming.lookup(lookupStr.toString());
        return (request);
    }

    /**Checks if the server has registered local services. That means that the server and
     * the client are running in the same VM and we can use the direct instanciation instead of
     * the RMI which is much more time and memory consuming because of the serialization
     * @return
     * @throws Exception
     */
    private MecRemote lookupLocalService() throws Exception {
        MecRemote request = AS2Server.lookupLocalRMI(this.service);
        //this will mainly return null because the servlet instanciatino is an other than
        //the server instanciation
        if (request == null) {
            return (this.lookupRemoteService());
        }
        return (request);
    }


    /**Sends a request to the server
     * @param requestObject contents of the request
     */
    @Override
    public Object sendRequest( IRMISenderObject requestObject ){
        MecRemote request = null;
        try{
            if (this.host.equalsIgnoreCase("localhost") || this.host.equalsIgnoreCase("127.0.0.1")) {
                    request = this.lookupLocalService();
                } else {
                    request = this.lookupRemoteService();
                }
        } catch( Throwable e ){
            String errorText
                    = super.rb.getResourceString(
                    "no.server", new Object[]{ this.host, String.valueOf( this.port ) } );
            this.logger.warning( errorText );
            StackTraceElement[] stackTrace = e.getStackTrace();
            StringBuilder buffer = new StringBuilder();
            if( stackTrace != null ){
                for( int i = 0; i < stackTrace.length; i++ ){
                    buffer.append( stackTrace[i].toString() + "\n");
                }
                this.logger.warning( buffer.toString() );
            }
            requestObject.indicateErrorOnServer( new String[]{ errorText } );
            return( requestObject );
        }
        Object requestReturn = null;
        try{
            requestReturn = request.execute( requestObject );
        } catch( Throwable e ){
            e.printStackTrace();
            String[] errorText = new String[]{
                this.rb.getResourceString( "fatal.error.long" ),
                this.rb.getResourceString( "message.returned" ),
                e.getMessage()
            };
            for( int i = 0; i < errorText.length; i++ )
                this.logger.info( errorText[i] );
            //give back the passed object to prevent any nullpointer exceptions
            requestObject.indicateErrorOnServer( errorText );
            return( requestObject );
        }
        if( requestReturn != null )
            return( requestReturn );
        else return( null );
    }
    
}
