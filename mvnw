#!/bin/bash
# ------------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------
# Maven Start Up Batch script
#
# Required ENV vars:
# ------------------
#   JAVA_HOME - location of a JDK home dir
# ------------------------------------------------------------------------------

if [ -z "$JAVA_HOME" ] ; then
  echo "Error: JAVA_HOME not found in your environment."
  echo "Please set the JAVA_HOME variable in your environment to match the"
  echo "location of your Java installation."
  exit 1
fi

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false;
darwin=false;
mingw=false
case "`uname`" in
  CYGWIN*) cygwin=true ;;
  MINGW*) mingw=true;;
  Darwin*) darwin=true
    # Use /usr/libexec/java_home if available, otherwise fall back to /Library/Java/Home
    # See https://developer.apple.com/library/mac/qa/qa1170/_index.html
    if [ -z "$JAVA_HOME" ]; then
        if [ -x "/usr/libexec/java_home" ]; then
            export JAVA_HOME="`/usr/libexec/java_home`"
        else
            export JAVA_HOME="/Library/Java/Home"
        fi
    fi
    ;;
esac

if [ -z "$JAVA_HOME" ] ; then
  echo "Error: JAVA_HOME not found in your environment."
  echo "Please set the JAVA_HOME variable in your environment to match the"
  echo "location of your Java installation."
  exit 1
fi

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
fi

# For Mingw, ensure paths are in UNIX format before anything is touched
if $mingw ; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME="`(cd "$JAVA_HOME"; pwd)`"
fi

if [ -z "$JAVA_BASE" ] ; then
  JAVA_BASE="$JAVA_HOME"
fi

exec "$JAVA_BASE/bin/java" $JAVA_OPTS \
  -classpath "$MAVEN_HOME/boot/plexus-classworlds-2.6.0.jar" \
  "-Dclassworlds.conf=$MAVEN_HOME/bin/m2.conf" \
  "-Dmaven.home=$MAVEN_HOME" \
  "-Dlibrary.jansi.path=$MAVEN_HOME/lib/jansi-native" \
  "-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR" \
  org.codehaus.plexus.classworlds.launcher.Launcher \
  "$@"
