package de.mendelson.comm.as2.database;
import java.sql.*;
/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Interface that has to be included for all update classes in the
 * sql update directory
 * @author S.Heller
 * @version $Revision: 1.1 $
 * @since build 128
 */
public interface IUpdater{

    /**Return if the update was successfully*/
    public boolean updateWasSuccessfully();
    
    /**Starts the update process*/
    public void startUpdate( Connection connection ) throws Exception;
    
}

