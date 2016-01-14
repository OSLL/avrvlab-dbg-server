### AVR + Arduino Wiring Makefile

# Arduino core sources:
ifndef ARDDIR
ARDDIR = /home/user/arduino-1.6.4
endif

# Need to declare in small Makefile for project. Small Makefile should be in the same
# directory as TARGET file.
#
# Example:
#
# TARGET = foo 							# the name of main file, with no extension
# SRC = foo.c bar.c  					# the C source files
# PSRC = baz.cpp qux.cpp 				# the C++ source files
# ARDLIBS = SoftwareSerial				# extra Arduino libraries
# include /avr/Makefile.base 			# this line includes the Makefile.base

# Variables that are set to defaults, but you then can be overrided
# in small Makefile:
# F_CPU (default is 16000000)
# MCU (default is atmega128)
#
# To include Arduino libraries, define
# ARDLIBS = SoftwareSerial Servo...
#
# This Makefile requires that you have no spaces in file paths, including
# Arduino directory (where core, variants, and libs are stored).

# This Makefile is modified from
# WinAVR Sample makefile written by Eric B. Weddington, Jörg Wunsch, et al.

# Tasks are defined in this Makefile:
# make all = Make software and program
# make clean = Clean out built project files.

# Supply the following variables:
# SRC = source files ending with .c
# PSRC = source files ending with .cpp

### These macros pertain to compiler flags
# Target file name (without extension).
ifndef TARGET
TARGET = main
endif
# List any extra directories to look for include files here.
ifndef EXTRAINCDIRS
EXTRAINCDIRS = 
endif
#Linker flags
ifndef LDFLAGS
LDFLAGS = 
endif
#Compiler flags
ifndef CFLAGS
CFLAGS = 
endif


### These macros pertain to hardware settings
#Target chip
ifndef MCU
MCU = atmega128
endif
#Frequency
ifndef F_CPU
F_CPU = 16000000
endif
CFLAGS += -D F_CPU=$(F_CPU)

# Optimization level, can be [0, 1, 2, 3, s]. 0 turns off optimization.
# (Note: 3 is not always the best optimization level. See avr-libc FAQ.)
ifndef OPT
OPT = s
endif

### These macros pertain to supporting Arduino libs
### It can be swithed off, if this variables defined in small Makefile
ifndef NO_ARDUINO
	LDFLAGS += -lm # -lm = math library
	ARDLIBDIR 		= $(ARDDIR)/libraries
	ARDCOREDIR 		= $(ARDDIR)/hardware/arduino/avr/cores/arduino
	ifeq ($(MCU),atmega328p)
	EXTRAINCDIRS += $(ARDDIR)/hardware/arduino/avr/variants/standard
	endif
	ifeq ($(MCU),atmega2560)
	EXTRAINCDIRS += $(ARDDIR)/hardware/arduino/avr/variants/mega
	endif
	ifeq ($(MCU),atmega128)
	EXTRAINCDIRS += $(ARDDIR)/hardware/arduino/avr/variants/atmega128
	endif
	# add Arduino sources and include directories to PSRC and EXTRAINCDIRS
	PSRC += $(wildcard $(ARDCOREDIR)/*.cpp)
	SRC += $(wildcard $(ARDCOREDIR)/*.c)
	EXTRAINCDIRS += $(ARDCOREDIR)
	PSRC += $(foreach lib,$(ARDLIBS),$(ARDLIBDIR)/$(lib)/$(lib).cpp)
	EXTRAINCDIRS += $(foreach lib,$(ARDLIBS),$(ARDLIBDIR)/$(lib))
endif


############# Main part  

# Output format. (can be srec, ihex, binary)
FORMAT = ihex


# Optional compiler flags.
#  -g:        generate debugging information (for GDB, or for COFF conversion)
#  -O*:       optimization level
#  -Wall...:  warning level
CFLAGS += -g -O$(OPT) -Wall \
$(patsubst %,-I%,$(EXTRAINCDIRS))

# Optional linker flags.
#============================================================== -g ?????????
LDFLAGS += -g -mmcu=$(MCU)

# Additional libraries

# Minimalistic printf version
#LDFLAGS += -Wl,-u,vfprintf -lprintf_min

# Floating point printf version (requires -lm below)
#LDFLAGS += -Wl,-u,vfprintf -lprintf_flt


# ---------------------------------------------------------------------------

# Define programs and commands.

CC = avr-gcc

OBJCOPY = avr-objcopy

# Define Messages
# English
MSG_ERRORS_NONE = Errors: none
MSG_BEGIN = -------- begin --------
MSG_END = --------  end  --------
MSG_FLASH = Creating load file for Flash:
MSG_LINKING = Linking:
MSG_COMPILING = Compiling:
MSG_CLEANING = Cleaning project:

# Define all object files.
OBJ = $(SRC:.c=.o) $(PSRC:.cpp=.o)

# Combine all necessary flags and optional flags.
# Add target processor to flags.
ALL_CFLAGS 		= -mmcu=$(MCU) -I. $(CFLAGS)
ALL_CXXFLAGS 	= -mmcu=$(MCU) -I. $(CFLAGS)


# Default target: make program!
all: begin \
	$(TARGET).elf $(TARGET).hex \
	finished end

# Status messages
begin:
	@echo
	@echo $(MSG_BEGIN)

finished:
	@echo $(MSG_ERRORS_NONE)

end:
	@echo $(MSG_END)
	@echo

# Create final output file (.hex) from ELF output file.
%.hex: %.elf
	@echo
	@echo $(MSG_FLASH) $@
	$(OBJCOPY) -O $(FORMAT) -R .eeprom $< $@

# Link: create ELF output file from object files.
.SECONDARY: $(TARGET).elf
.PRECIOUS: $(OBJ)
%.elf: $(OBJ)
	@echo
	@echo $(MSG_LINKING) $@
	$(CC) $(ALL_CFLAGS) $(OBJ) --output $@ $(LDFLAGS)


# Compile: create object files from C source files.
%.o: %.c
	@echo
	@echo $(MSG_COMPILING) $<
	$(CC) -c $(ALL_CFLAGS) $< -o $@


# Compile: create object files from C++ source files
%.o: %.cpp
	@echo
	@echo $(MSG_COMPILING) $<
	$(CC) -c $(ALL_CXXFLAGS) $< -o $@

# Target: clean project.
clean: begin clean_list finished end

clean_list :
	@echo
	@echo $(MSG_CLEANING)
	rm -f $(TARGET).hex
	rm -f $(TARGET).elf
	rm -f $(OBJ)
	rm -f *~


# Listing of phony targets.
.PHONY : all begin finish end \
	clean clean_list
