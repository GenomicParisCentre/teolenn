#!/bin/bash

#
# This script set the right classpath and start the application
#

BASEDIR=`dirname $0`
LIBDIR=$BASEDIR/lib
java -server -Xmx512m -cp $LIBDIR/alloligos.jar:$LIBDIR/commons-math.jar:$LIBDIR/dom4j.jar:$LIBDIR/commons-cli.jar fr.ens.transcriptome.oligo.Main $*
