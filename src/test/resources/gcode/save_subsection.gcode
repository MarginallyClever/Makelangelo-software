;FLAVOR:Marlin-polargraph
;MINX:-4.000
;MINY:-8.000
;MAXX:10.000
;MAXY:15.000
;Start of user gcode

;End of user gcode
G28 X Y
M280 P0 S90 T50
M0 Ready 0x0044ff and click
G0 X-4.000 Y-8.000 F3000.0
M280 P0 S25 T50
G1 X10.000 Y15.000 F3000.0
M280 P0 S90 T50
;Start of user gcode

;End of user gcode
;End of Gcode
