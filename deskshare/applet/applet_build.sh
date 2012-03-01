#!/bin/bash
APPLET_VERSION=`date +%s`
gradle build --exclude-task test 
gradle jar 
#ant sign-jar
# sign the applet the right way
jarsigner -keystore ~/.keystores/crunch_self build/libs/bbb-deskshare-applet-0.71.jar crunch_self
cp build/libs/bbb-deskshare-applet*.jar /home/roger/dev/source/bigbluebutton/bigbluebutton-client/bin/
#sudo cp ~/dev/source/bigbluebutton/bigbluebutton-client/resources/prod/bbb-deskshare-applet*.jar /var/www/bigbluebutton/client/
#cd /var/www/bigbluebutton/client
#sudo mv bbb-deskshare-applet-0.71.jar bbb-deskshare-applet-0.71.${APPLET_VERSION}.jar
#sudo cat BigBlueButton.html.tpl | sed "s#%VERSION%#$APPLET_VERSION#g" > BigBlueButton.html
#sudo cat DeskshareStandalone.html.tpl | sed "s#%VERSION%#$APPLET_VERSION#g" > DeskshareStandalone.html
sudo /etc/init.d/red5 restart

