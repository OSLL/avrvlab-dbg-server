#!/bin/sh

PROGRAM=Test.elf
DEVNAME=/dev/ttyUSB0
TARGET=atmega128
PORT=4242

sudo avarice --erase --program --file $PROGRAM --part $TARGET --jtag $DEVNAME :$PORT
