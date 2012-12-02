//$Header: /cvsroot-fuse/mec-as2/39/mendelson/comm/as2/server/AS2Agent.java,v 1.1 2012/04/18 14:10:38 heller Exp $
package de.mendelson.comm.as2.server;
import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Agent to control the AS2 server via jmx
 * @author S.Heller
 * @version $Revision: 1.1 $
 */
public class AS2Agent {

    private MBeanServer mbeanServer = null;

    public AS2Agent(AS2Server as2MBean) {

        mbeanServer = ManagementFactory.getPlatformMBeanServer();

        ObjectName beanName = null;

        try {
            String objectName = this.getClass().getPackage().getName() + ":"
                    + "type=" + AS2Server.class.getName();
            beanName = new ObjectName(objectName);
            mbeanServer.registerMBean(as2MBean, beanName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}