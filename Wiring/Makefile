### AVR + Arduino Wiring small Makefile


# Arduino core sources:
ARDDIR = /home/constantin/arduino-1.6.4

# Target file name (without extension).
TARGET = Test

#Target chip
MCU = atmega128

# Optimization level, can be [0, 1, 2, 3, s]. 0 turns off optimization.
# (Note: 3 is not always the best optimization level. See avr-libc FAQ.)
OPT=0

# Variable that only pertains to this project
OWN_LIBRARY_DIR =

# "EXTRAINCDIRS" is a variable used by the base makefile. The base makefile creates a -I compiler flag for every item in the "EXTRAINCDIRS" list.
EXTRAINCDIRS = $(MY_OWN_LIBRARY_DIR)

# *.c source files pertaining to project
SRC = $(wildcard *.c)

# *.cpp source files pertaining to project
PSRC = $(wildcard *.cpp)

# specify additional (non-core) Arduino libraries to include
ARDLIBS =

# include base Makefile
include /home/constantin/Test/AVR-makefile-base.mk

