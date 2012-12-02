#! /bin/sh
###############################################################################
#
#  Copyright (c) 2000-2012, mendelson-e-commerce GmbH  All Rights Reserved.
#
###############################################################################

CLASSPATH=as2.jar:jetty/start.jar
export CLASSPATH

if [ -d jlib ]; then
    JARDIR=jlib
    export JARDIR
    for jar in `ls $JARDIR/*.jar $JARDIR/*.zip 2>/dev/null`
    do
        CLASSPATH=$CLASSPATH:$jar
    done
fi
if [ -d jlib/mina ]; then
    MINADIR=jlib/mina
    export MINADIR
    for jar in `ls $MINADIR/*.jar $MINADIR/*.zip 2>/dev/null`
    do
        CLASSPATH=$CLASSPATH:$jar
    done
fi
if [ -d jlib/vaadin ]; then
    VAADINDIR=jlib/vaadin
    export VAADINDIR
    for jar in `ls $VAADINDIR/*.jar $VAADINDIR/*.zip 2>/dev/null`
    do
        CLASSPATH=$CLASSPATH:$jar
    done
fi
java -Xmx192M -Xms92M -classpath $CLASSPATH de.mendelson.comm.as2.AS2Shutdown

