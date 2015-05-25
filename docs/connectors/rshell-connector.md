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

Also, you can execute any `bash` or `powershell` script and expose its result as attribute.

> RShell Connector doesn't support notifications

The magic of this connector is hidden in XML-based `Tool Profile` (or Command-line tool profile). Tool Profile (TP) is an XML file that describes how to parse output from process and prepare input from SNAMP. You can use the following instruments for text parsing:
* Regular expressions
* JavaScript
* [StringTemplate](http://www.stringtemplate.org/) as an template for tool STDIN

## Command-line tool profile
Tool Profile is an XML file that describes the following aspects of the textual stream parsing and formatting.

* **Attribute Reader definition** - describes how to execute command-line program when reading attribute value. If this section is omitted then attribute is write-only
  * _Input section_ - how to construct command-line. The template can be constructed using [StringTemplate](http://www.stringtemplate.org/) syntax
  * _Output section_ - how to parse STDOUT from program. The parses can be described in declarative DSL using XML tags from `http://snamp.itworks.com/schemas/command-line-tool-profile/v1.0` namespace. Additionally, you can mix declarative syntax with regular expressions and JavaScript code.
* **Attribute Writer definition** - describes how to execute command-line program when writing attribute value. If this section is omitted then attribute is read-only
  * _Input section_ - how to construct command-line. The template can be constructed using [StringTemplate](http://www.stringtemplate.org/) syntax
  * _Output section_ can be omitted

XSD schema of the Tool Profile:
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<xs:schema version="1.0" targetNamespace="http://snamp.itworks.com/schemas/command-line-tool-profile/v1.0" xmlns:tns="http://snamp.itworks.com/schemas/command-line-tool-profile/v1.0" xmlns:xs="http://www.w3.org/2001/XMLSchema">

  <xs:element name="column" type="TableColumnParsingRule"/>
  <xs:element name="const" type="tns:Constant"/>
  <xs:element name="entry" type="tns:DictionaryEntryParsingRule"/>
  <xs:element name="item" type="tns:ArrayItemParsingRule"/>
  <xs:element name="line-terminator" type="tns:LineTerminationParsingRule"/>
  <xs:element name="profile" type="tns:XmlCommandLineToolProfile"/>
  <xs:element name="skip" type="tns:SkipTokenParsingRule"/>
  <xs:element name="template" type="tns:XmlCommandLineTemplate"/>

  <xs:complexType name="TableColumnParsingRule" final="extension restriction">
    <xs:simpleContent>
      <xs:extension base="xs:string">
        <xs:attribute ref="ns1:name" use="required"/>
        <xs:attribute ref="ns1:type" use="required"/>
        <xs:attribute ref="ns1:indexed" use="required"/>
      </xs:extension>
    </xs:simpleContent>
  </xs:complexType>

  <xs:complexType name="XmlCommandLineToolProfile">
    <xs:sequence>
      <xs:element name="modifier" type="tns:XmlCommandLineTemplate" minOccurs="0"/>
      <xs:element name="reader" type="tns:XmlCommandLineTemplate" minOccurs="0"/>
    </xs:sequence>
  </xs:complexType>

  <xs:complexType name="XmlCommandLineTemplate">
    <xs:sequence>
      <xs:element name="output" type="tns:CommandLineToolOutputParser" form="qualified" minOccurs="0"/>
      <xs:element name="input" type="xs:string" form="qualified" minOccurs="0"/>
    </xs:sequence>
  </xs:complexType>

  <xs:complexType name="CommandLineToolOutputParser" mixed="true">
    <xs:sequence>
      <xs:choice minOccurs="0" maxOccurs="unbounded">
        <xs:element ref="tns:entry"/>
        <xs:element ref="tns:line-terminator"/>
        <xs:element ref="tns:column"/>
        <xs:element ref="tns:item"/>
        <xs:element ref="tns:skip"/>
        <xs:element ref="tns:const"/>
      </xs:choice>
    </xs:sequence>
    <xs:attribute ref="tns:blobFormat"/>
    <xs:attribute ref="tns:dateTimeFormat"/>
    <xs:attribute ref="tns:numberFormat"/>
    <xs:attribute ref="tns:language" use="required"/>
    <xs:attribute ref="tns:type" use="required"/>
  </xs:complexType>

  <xs:complexType name="DictionaryEntryParsingRule" final="extension restriction">
    <xs:simpleContent>
      <xs:extension base="xs:string">
        <xs:attribute ref="tns:key" use="required"/>
        <xs:attribute ref="tns:type" use="required"/>
      </xs:extension>
    </xs:simpleContent>
  </xs:complexType>

  <xs:simpleType name="LineTerminationParsingRule">
    <xs:restriction base="xs:string"/>
  </xs:simpleType>


  <xs:complexType name="ArrayItemParsingRule" final="extension restriction">
    <xs:simpleContent>
      <xs:extension base="xs:string">
        <xs:attribute ref="tns:elementType" use="required"/>
      </xs:extension>
    </xs:simpleContent>
  </xs:complexType>

  <xs:simpleType name="SkipTokenParsingRule">
    <xs:restriction base="xs:string"/>
  </xs:simpleType>

  <xs:simpleType name="Constant">
    <xs:restriction base="xs:string"/>
  </xs:simpleType>

  <xs:simpleType name="BlobFormat">
    <xs:restriction base="xs:string">
      <xs:enumeration value="hex"/>
      <xs:enumeration value="base32"/>
      <xs:enumeration value="base64"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="CommandLineToolReturnType">
    <xs:restriction base="xs:string">
      <xs:enumeration value="boolean"/>
      <xs:enumeration value="string"/>
      <xs:enumeration value="char"/>
      <xs:enumeration value="blob"/>
      <xs:enumeration value="8bit"/>
      <xs:enumeration value="16bit"/>
      <xs:enumeration value="32bit"/>
      <xs:enumeration value="64bit"/>
      <xs:enumeration value="integer"/>
      <xs:enumeration value="decimal"/>
      <xs:enumeration value="dictionary"/>
      <xs:enumeration value="array"/>
      <xs:enumeration value="table"/>
      <xs:enumeration value="date"/>
      <xs:enumeration value="float"/>
      <xs:enumeration value="double"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:attribute name="blobFormat" type="tns:BlobFormat"/>
  <xs:attribute name="dateTimeFormat" type="xs:string"/>
  <xs:attribute name="elementType" type="tns:CommandLineToolReturnType"/>
  <xs:attribute name="indexed" type="xs:boolean"/>
  <xs:attribute name="key" type="xs:string"/>
  <xs:attribute name="language" type="xs:string"/>
  <xs:attribute name="name" type="xs:string"/>
  <xs:attribute name="numberFormat" type="xs:string"/>
  <xs:attribute name="type" type="tns:CommandLineToolReturnType"/>
</xs:schema>
```

The following example demonstrates how to parse output from `free` Linux utility:
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<ns1:profile xmlns:ns1="http://snamp.itworks.com/schemas/command-line-tool-profile/v1.0">
    <reader>
        <ns1:output ns1:language="regexp" ns1:type="dictionary">
            <ns1:skip>[a-z]+</ns1:skip>
            <ns1:skip>[a-z]+</ns1:skip>
            <ns1:skip>[a-z]+</ns1:skip>
            <ns1:skip>[a-z]+</ns1:skip>
            <ns1:skip>[a-z]+</ns1:skip>
            <ns1:skip>[a-z]+</ns1:skip>
            <ns1:skip>[a-zA-Z]+\:</ns1:skip>
            <ns1:entry ns1:key="total" ns1:type="64bit">[0-9]+</ns1:entry>
            <ns1:entry ns1:key="used" ns1:type="64bit">[0-9]+</ns1:entry>
            <ns1:entry ns1:key="free" ns1:type="64bit">[0-9]+</ns1:entry>
        </ns1:output>
        <ns1:input>free {format}</ns1:input>
    </reader>
</ns1:profile>
```
The first `skip` strings indicating that the name of the columns in the `free` output should be omitted.
