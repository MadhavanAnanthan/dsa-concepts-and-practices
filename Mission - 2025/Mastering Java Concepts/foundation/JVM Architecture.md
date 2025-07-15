Before diving into JVM architecture, lets understand how other programming languages are executed.
1. If you consider C and C++, at the time of compilation the application will be converted into machine code directly. So we can easily understand, if its directly converting to machine code, the machine code should be converted based on the current environment (windows, linux, mac). So once the application is compiled for windows, the compiled app will be run only on windows machine.
2. Java, Python and Javascript languages are considered as platform independent. The compiler/interpreter is platform dependent. But after compilation let's say in Java - the .jar file is platform independent, we can build in once env and run in any env. Also python/JS codes is platform independent.

JVM Introduction and things to know before start
	
1. If you see any JDK version, only JRE will be present inside. When we start running a program, JVM instance will be created and it will destroyed once its executed.
2. If we run multiple programs, multiple JVM instance will be created.
3. If we run the program in command line, when we enter "javac filename.java", OS will trigger java compiler. It will convert the file into .class file. If we run "java filename", then OS will understand and create the JVM instance.

For each JVM instance, there are 3 components exits.

1. Class Loader
2. Runtime Memory/Data Area
3. Execution Engine

![img.png](img.png)

JVM Internal Working
