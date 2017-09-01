# Arduino-debugging-server
Серверная часть сервиса удаленной отладки AVR

Сборка
------------
Установка и сборка симулятора AVaRICE:

Важно: требуется Avarice версии 2.11. В более старших версиях, возможность загрузки прошивки из этой утилиты убрали (в пользу использования для этого отдельно утилиты Avrdude)

    git clone git://git.savannah.nongnu.org/simulavr.git
    cd simulavr
    sudo apt-get install build-essential g++ libtool-bin binutils-dev texinfo
    ./bootstrap
    ./configure --disable-doxygen-doc --enable-dependency-tracking
    make
    
