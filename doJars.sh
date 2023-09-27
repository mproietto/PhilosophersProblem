#!/bin/bash

# Construct the java files
cd src
javac -d ../bin/ *.java server/*.java client/*.java utils/*.java
cd ..

# Construct the jar files
cd bin
jar cfe Server.jar server.Server server/*.class utils/*.class
jar cfe Client.jar client.Client client/*.class utils/*.class
