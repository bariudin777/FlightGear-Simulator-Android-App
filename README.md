# FlightGear-Simulator-Android-App
An Android application designed to fly a FlightGear aircraft using a custom joystick.

### Installing
Download and install the simulator on your computer- https://www.flightgear.org/download/
Add the generic_small.xml file to the /data/Protocol directory where you installed the simulator
Config the following settings in the 'Settings' tab in the simulator:
```
--telnet=socket,in,10,127.0.0.1,5402,tcp
--generic=socket,out,10,127.0.0.1,5400,tcp,generic_small
```
This will open two communication sockets - 'in' where you send commands to the simulator, and 'out' where you receive data from it.
