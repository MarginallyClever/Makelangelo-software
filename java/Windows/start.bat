@echo off
 
Set RegQry=HKLM\Hardware\Description\System\CentralProcessor\0
 
REG.exe Query %RegQry% > checkOS.txt
 
Find /i "x86" < CheckOS.txt > StringCheck.txt
 
If %ERRORLEVEL% == 0 (
    java -classpath RXTXcomm.jar -Djava.library.path=classes\rxtxnative\Windows\i3680mingw32 -jar DrawbotGUI.jar
) ELSE (
    java -classpath RXTXcomm.jar -Djava.library.path=classes\rxtxnative\Windows\windows-x64 -jar DrawbotGUI.jar
)

