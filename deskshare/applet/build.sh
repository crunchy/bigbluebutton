#!/bin/bash
APPLET_VERSION=`date +%s`
gradle build --exclude-task test
echo "signing the jar"
jarsigner -keystore ~/code/ops/certificates/code_signing/salescrunch.jks build/libs/bbb-deskshare-applet-0.71.jar salescrunch
echo "verifying the signature"
jarsigner -verbose -verify -certs build/libs/bbb-deskshare-applet-0.71.jar
