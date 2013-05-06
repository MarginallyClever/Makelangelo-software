Makelangelo instructions for OSX
=================================

Installation
-----------
1.  Make sure the Arduino software is installed.  
    If it is not, you can get it from the [arduino website](http://arduino.cc/en/Main/Software).
2. Copy the AFMotorDrawbot library to your Arduino library directory.
   The AFMotorDrawbot library is in the 'arduino' sub-directory.
   The Ardunio library is typically located in Documents/library/.
3. If you choose to place the DrawbotGUI in your Applications directory, 
   you must copy the entire directory (not just DrawbotGUI.jar) because it contains dependencies.


Assembly
---------
If your Makelangelo is not already assembled, 
see the instructions [on the wiki](https://github.com/MarginallyClever/Makelangelo/wiki/Assembly).


Initial Setup -- Ardunio
----------------------
Your Makelangelo must have the drawbot program installed on it before you can use it.
This often only needs to be done once (though if you use your Ardunio for other projects,
  it will need to be done each time you setup your Makelangelo).

1. Connect your drawbot to your computer.
2. Open the Arduino application.
3. Load the drawbot program (./arduino/drawbot.ino).
4. Compile and upload.


DrawbotGUI
---------------------------
To execute the DrawbotGUI, double click on DrawbotGUI.jar.
If that doesn't work, try double clikcing on start.command.
For those inclined to using the terminal, "java -jar DrawbotGUI.jar" should work.

The DrawbotGUI also needs configuration the first time you run it, 
  when you change its location or the paper size.
These configuration options are set under the 'settings' menu.
To indicate what port to find the drawbot (Ardunio) on, go to Settings->Port.
The correct port is often the first on the list.  
It is usually of the form /dev/tty.usbmodemYYY (where YYY is some number).
Page setup is also found in the 'settings' menu.
These settings are remembered between executions, so they usually only need
to be set once.



Troubleshooting
----------------
_With your Makelangelo connected, when you start DrawbotGUI,
  the title bar should says "Makelangelo not connected."_
Is the USB plugged in securely on both ends?
Have you configured the port to look at (see DrawbotGUI)
Did you upload the drawbot software to the ardunio (see Initial Setup--Ardunio)?

There is more extensive description of how to configure and use the software 
on the [wiki pages](https://github.com/MarginallyClever/Makelangelo/wiki).

If the software is still not working or you need any further help,
please join us in our [support forums](http://www.marginallyclever.com/forum/).
