#!/bin/sh

#
# This script set the right classpath and start the application
#

# Get the path to this script
BASEDIR=`dirname $0`

# Set the Teolenn libraries path
LIBDIR=$BASEDIR/lib

# Set the memory in MiB needed by Teolenn (only Java part, not external tools)
# By Default 1024
MEMORY=2048

# Add here your plugins and dependencies
PLUGINS=

# Set the path to java
JAVA_CMD=java

# Launch Teolenn
$JAVA_CMD -server \
          -Xmx${MEMORY}m \
          -cp $LIBDIR/teolenn.jar:$LIBDIR/commons-math.jar:$LIBDIR/dom4j.jar:$LIBDIR/commons-cli.jar:$PLUGINS \
          fr.ens.transcriptome.teolenn.Main $*
