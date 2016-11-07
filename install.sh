#!/bin/sh

SERVER_APP_NAME=Server.jar
SERVER_APP_PATH=/opt/avr-debug

#Download required tools:
sudo apt-get install git ant build-essential g++ libtool-bin binutils-dev texinfo

#Compile Server application into .jar file
ant jar

#Loading and compiling SimulAVR
git clone git://git.savannah.nongnu.org/simulavr.git
cd simulavr
./bootstrap
./configure --disable-doxygen-doc --enable-dependency-tracking
make
cd ..

#Copy Server required files to /opt/avr-debug folder
sudo mkdir -p -m 777 /opt/avr-debug
sudo cp -p ./simulavr/src/simulavr $SERVER_APP_PATH/simulavr
sudo cp -p ./$SERVER_APP_NAME $SERVER_APP_PATH/$SERVER_APP_NAME
sudo cp -p ./avarice_supported_devices.txt $SERVER_APP_PATH/avarice_supported_devices.txt
sudo cp -p -R ./icons $SERVER_APP_PATH/

#Creating Server run-sript
echo "java -jar $SERVER_APP_NAME" > $SERVER_APP_PATH/run.sh
chmod +x $SERVER_APP_PATH/run.sh
