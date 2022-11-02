# ECM GUI

Very simple GUI to encode/decode ECM using [libecm](https://github.com/aefermiano/libecm).

Made just to play around with Swing and JNI.

Native code tested only with GCC on GNU/Linux amd64.

## Building

### Java code

```
mvn clean package
```

Build will also generate the C header into "native" folder.

### C code

Make sure libecm submodule is cloned:

```
git submodule init
git submodule update
```

First, figure out where your JAVA\_HOME is. This is necessary because JNI requires some headers that come with your JDK.

Then:

```
cd native
cmake -DJAVA_HOME=/usr/lib/jvm/java-18-openjdk-amd64
make
```

## Using

This script will already set the necessary variable to load the native library:

```
./run.sh
```

You can also launch the JAR with "--test-mode-success" or "--test-mode-failure" to use a mocked library instead of the real one. I used this to test the GUI events.
