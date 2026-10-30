# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## 7.81.0
### Added
- added "remove hops" tool that cuts travel under your chosen threshold.
- added more german translations
- added "Mask Line By Image" tool
- added "reverse turtle" tool that can reverse entire drawing or just the order travel between islands (pen down areas)
### Changed
- improved much faster Pulse converters
- improved "fit image to paper" and "fill paper with image", which didn't work when the aspect ratios were just so.
- improved simulation speed a lot for time estimates, etc.
- improved Hexxy for lower travel time and more pen diamter respect
- improved respect for feed rate and acceleration.  Please play with the numbers and report your results!
- changed "Line Weight By Image" is now a tool, not a generator
- changed some bits to support BTT mini e3 hardware.


### Added
- Fully customizable shortcuts in the main menu, including the ability to add new shortcuts for any menu item.
### Changed
- Improved support for BTT mini E3 v3 by tweaking serial settings.  Tested with RUMBA and Mini.
### Fixed
- Editor context (machine/paper/pen) settings were not being passed to the live drawing controls.
- `Save to` path remembered across all dialogs.
- MickeyMoe1992 now properly obeys the margin limits, same as the other converters.

## 7.78.6

### Added
- Added an EditorContext to better track drawing updates to refresh the view
- Real time updates when using the Rotate, Scale, and Translate tools.

### Fixed
- First travel move was appearing in white instead of the travel color.
- Render of the drawing was fixed to the world center, not the Turtle center.

## 7.78.5

### Added
- Drag + drop a file now adds that file to the "Recent Files" list.

### Fixed

- Infill of closed loops should now be properly filled.  Was occasionally failing the in/out test and creating a mess.
- MickeyMoe1992 now properly obeys the margin limits, same as the other converters.
- Render hints are now forced to enable antialiasing, etc.

## 7.78.3

### Added
- Added Generator "Registration Marks"
- Added "Zoom to fit machine"

### Removed 
- Hid the Donatello panel at startup for a less confusing new user experience.

## 7.78.0

### Added
- This changelog.
- Open source button to README.md.
- GCode headers now include M201, M203, M204, and M205 for speed tuning.
- English for "Jerk" in plotter settings tab.
- Added Peano and other L system fractals, all consolidated under `Fractals > L Systems`.
### Changed
- Size of "upload firmware" dialog should now be large enough to see all text.
- Bumped Jogamp to 2.6.0.
### Deprecated
### Removed
- use of OpenGL and all hardware accelerated graphics
### Fixed
- English label for "Acceleration" in plotter settings tab.
- Calculation for meters of timing belt needed on each side of Makelangelo 5 and HUGE.
### Security

## [7.76.1] - 2025-08-29

Last release before changelog was introduced.