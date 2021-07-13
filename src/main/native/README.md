# Semux Native Library

libsemuxcrypto is a JNI library that aims to increase the performance Semux wallet by implementing cryptography functions in C++. This library relies on thrid-parties including [libsodium](https://github.com/jedisct1/libsodium) and [ed25519-donna](https://github.com/floodyberry/ed25519-donna).  

## Build on x86_64 Linux

Build on linux supports cross-compiling to the following platforms:

1. Linux-aarch64
2. Linux-x86_64
3. Windows-x86_64

Prerequisites:
- cmake
- automake
- autoconf
- libtool
- gcc-x86_64-linux-gnu
- gcc-aarch64-linux-gnu 
- gcc-mingw-w64
- binutils-x86_64-linux-gnu
- binutils-aarch64-linux-gnu
- binutils-mingw-w64

Steps to build on Debian/Ubuntu based distributions with a x86_64 machine:
```
sudo apt install \
cmake automake autoconf libtool \
gcc gcc-aarch64-linux-gnu gcc-mingw-w64 \
g++-aarch64-linux-gnu g++-mingw-w64-x86-64 \
binutils binutils-aarch64-linux-gnu binutils-mingw-w64

mkdir build && cd build
cmake -vvv -DCMAKE_TOOLCHAIN_FILE=../cmake/toolchain-Linux-x86_64.cmake ../
make -j$(nproc)

cd .. && rm -rf build && mkdir build && cd build 
cmake -vvv -DCMAKE_TOOLCHAIN_FILE=../cmake/toolchain-Linux-aarch64.cmake ../
make -j$(nproc)

cd .. && rm -rf build && mkdir build && cd build
cmake -vvv -DCMAKE_TOOLCHAIN_FILE=../cmake/toolchain-Windows-x86_64.cmake ../
make -j$(nproc)
```

## Build on macOS

Prerequisites:
1. clang
2. cmake
3. autoconf
4. automake

Build:
```
mkdir build && cd build
cmake -vvv -DCMAKE_TOOLCHAIN_FILE=../cmake/toolchain-Darwin-x86_64.cmake ../
make -j$(nproc)
```
