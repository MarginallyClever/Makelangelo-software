;FLAVOR:Marlin-polargraph
;MINX:-15.000
;MINY:-7.000
;MAXX:7.000
;MAXY:8.000
;Start of user gcode
M300
M200
;End of user gcode
G28 X Y
M280 P0 S90 T50
M0 Ready black and click
G0 X-15.000 Y-7.000 F3000.0
M280 P0 S25 T50
G1 X3.000 Y4.000 F3000.0
G1 X7.000 Y8.000 F3000.0
M280 P0 S90 T50
;Start of user gcode
M400
M200
;End of user gcode
;End of Gcode