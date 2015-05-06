SNAMP Installation
====
This page provides installation instructions and system requirements

## System requirements
Environment:
* Java SE Runtime Environment 7. Recommended JVMs
    * OpenJDK
    * Oracle JRE
* The `JAVA_HOME` environment variable must be set to the directory where the Java runtime is installed

Supported operating systems:
* Windows
    * Windows Vista SP2
    * Windows 7
    * Windows 8
    * Windows 10
    * Windows Server 2008 R2 SP1 (64-bit)
    * Windows Server 2012 (64-bit)
* Linux
    * Red Hat Enterprise Linux 5.5+, 6.x (32-bit), 6.x (64-bit)
    * Ubuntu Linux 10.04 and above (only LTS versions)
    * Suse Linux Enterprise Server 10 SP2, 11.x

Hardware:
* Processor architectures:
    * x86
    * x64
    * ARMv6/ARMv7 (in paid subscription only)
* 2 Cores (minimum)
* 150 MB of free disk space
> Disk space requirement ignores growing of log files
* 2 GB RAM (minimum)

It is possible to run SNAMP on ARM-based hardware (such as RaspberryPi). Contact us for more information.

## Installation
1. Download latest SNAMP distribution package. You may choose package format: `zip` or `tar.gz`
1. Extract `snamp-X.Y.Z.tar.gz` or `snamp-X.Y.Z.zip` into your installation folder
> There is no limitations for installation destination

SNAMP may be launched in the following modes:
* the `regular` mode starts SNAMP in foreground, including the shell console
* the `server` mode starts SNAMP in foreground, without the shell console
* the `background` mode starts Apache Karaf in background.

See [start, stop, restart Apache Karaf](https://karaf.apache.org/manual/latest/users-guide/start-stop.html) for more information about Apache Karaf lifecycle management.

### Regular mode
The regular mode uses the `<snamp>/bin/karaf` Unix script (`<snamp>\bin\karaf.bat` on Windows). It's the default start process.

It starts SNAMP as a foreground process, and displays the shell console.

On Unix:
```bash
cd <snamp>
sh bin/karaf
```

On Windows:
```
cd <snamp>
bin\karaf.bat
```

Note that closing the console or shell window will cause SNAMP to terminate.

### Server mode
The server mode starts SNAMP as a foreground process, but it doesn't start the shell console.

To use this mode, you use the server argument to the `<snamp>/bin/karaf` Unix script (`<snamp>\bin\karaf.bat` on Windows).

On Unix:
```bash
cd <snamp>
sh bin/karaf server
```

On Windows:
```
cd <snamp>
bin\karaf.bat server
```

Note that closing the console or shell window will cause Apache Karaf to terminate.

You can connect to the shell console using SSH:
* On Unix: `<snamp>/bin/client`
* On Windows: `<snamp>\bin\client.bat`

By default, client tries to connect on localhost, on port 8101. You can use `--help` to get details about the options

### Background mode
The background mode starts SNAMP as a background process.

To start in background mode, you have to use `<snamp>/bin/start` Unix script (`<snamp>\bin\start.bat` on Windows).

You can connect to the shell console using SSH.

## Root privileges
SNAMP doesn't require `root` privileges for running. But if you want to use standard ports in the configured resource adapters (161 for `SNMP` protocol and 80, 8080, 443 for `HTTP` protocol) then you should have `root` privileges.

## Integration in the operating system
SNAMP may be integrated as an OS System Service:
* like a native Windows Service
* like a Unix daemon process

Because of SNAMP is developed on top of Apache Karaf therefore you can use exising [Apache Karaf Integration Guide](https://karaf.apache.org/manual/latest/users-guide/wrapper.html).
