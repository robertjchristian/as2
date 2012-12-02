//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/statistic/StatisticExport.java,v 1.1 2012/04/18 14:10:39 heller Exp $
package de.mendelson.comm.as2.statistic;

import de.mendelson.comm.as2.partner.Partner;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Exports the statistic data to a passed export file, format is XML
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class StatisticExport {

    public StatisticExport(Connection configConnection, Connection runtimeConnection) {        
    }

    /**Exports the statistic data to a passed export file*/
    public void export(OutputStream streamout, long startDate, long endDate, long timestep, Partner localStation, Partner partner)
            throws SQLException, IOException {           
    }

    
}
