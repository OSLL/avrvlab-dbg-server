# Arduino-debugging-server
Серверная часть сервиса удаленной отладки AVR

Сборка
------------
Установка и сборка симулятора AVaRICE:

    git clone git://git.savannah.nongnu.org/simulavr.git
    cd simulavr
    sudo apt-get install build-essential g++ libtool-bin binutils-dev texinfo
    ./bootstrap
    ./configure --disable-doxygen-doc --enable-dependency-tracking
    make
    
