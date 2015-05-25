RShell Resource Connector
====
RShell Resource Connector is a multiprotocol connector that allows to monitor resources using the following protocols:
* [Remote Process Execution](http://en.wikipedia.org/wiki/Remote_Process_Execution) - allows to execute process on remote machine using _rexec_ protocol
* [Remote Shell](http://en.wikipedia.org/wiki/Remote_Shell), or _rsh_ - equivalent of _rexec_ protocol for BSD Unix systems
* Local Process Execution - executes any process on the same OS where SNAMP installed
* [Secure Shell](http://en.wikipedia.org/wiki/Secure_Shell) - allows to execute process on remote machine using _SSH_ protocol

This connector uses one of the supported protocols to execute a process (local or remote) and convert information from its STDOUT into management information. It is known that many command-line utilities provide very useful information about OS and hardware state, such as:
* GNU Core Utilities:
  * `df` - shows disk free space on file systems
  * `du` - shows disk usage on file systems
  * `nice` - modifies scheduling priority
  * `stat` - returns data about an inode
  * `uptime` - tells how long the system has been running
* Linux Commands:
  * `free` - provides information about unused and used memory and swap space
  * `who` - display who is on the system

Also, you can execute any `bash` or `powershell` script and expose its return information into notification or attribute value.

The magic of this connector is hidden in XML-based `Tool Profile`. Tool Profile describes how to parse output from process and prepare input from SNAMP. You can use the following instruments for text parsing:
* Regular expressions
