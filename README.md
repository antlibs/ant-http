# ant-http library
This is a clone of missing-link project on Google Code (https://code.google.com/p/missing-link/)

## Overview

The missing link Ant HTTP task was created due to the lack of a full featured, usable and liberally licensed Ant HTTP task. This Ant task is coded from scratch utilizing only core Java classes; as such it does not require any third party dependencies at runtime.

This Ant task also simplifies common aspects of HTTP communication, which other libraries seem to needlessly complicate, such as: authentication, TLS/SSL and HTTP methods other then GET and POST.


## License

The missing link Ant HTTP task is licensed under the Apache 2.0 license, a copy of the license can be found with the missing link Ant HTTP task distribution or at http://www.apache.org/licenses/LICENSE-2.0.html.


## Features

The missing link Ant HTTP task was created with the following features in mind:

- No third party library dependencies.
- TLS/SSL support on a configuration, per-connection basis, not JVM-wide.
- Support for HTTP methods GET, POST, PUT, OPTIONS, HEAD and TRACE.
- Support for BASIC authentication.
- Support for multiple URL/URI building options.
- Support for specifying arbitrary HTTP request headers.
- Support for specifying a request entity in-line or through a file.
- Options on what information to print to the output.
- Options to govern what response status codes are expected, and what should cause a build-failing exception.


## Ant task XML elements

### http

#### supported parameters:

| Name | Description | Required | Default | Example |
|------|-------------|----------|---------|---------|
| **`url`** | HTTP URL | Yes |   | `http://www.google.com` |
| **`method`** | HTTP method | No | `GET` | `GET`, `PUT`, `POST`, etc. |
| **`printRequest`** | Print request entity | No | `false` | `true` or `false` |
| **`printResponse`** | Print response entity | No | `false` | `true` or `false` |
| **`printRequestHeaders`** | Print request headers | No | `true` | `true` or `false` |
| **`printResponseHeaders`** | Print response headers | No | `true` | `true` or `false` | `expected` | Expected HTTP status | No | `200` | `200`, `201`, `404`, etc. |
| **`failOnUnexpected`** | Fail on unexpected status | No | `true` | `true` or `false` | `outfile` | Write response to file | No |   | Any filename |
| **`followRedirects`** | Follow redirections | No | `true` | `true` or `false` |
| **`setContentLengthHeader`** | Set Content-Length header | No |`false`| `true` or `false` |
| **`statusProperty`** | Property to save status to | No |   | `http.status` |
| **`update`** | Update/overwrite outfile | No | `true` | `true` or `false` |
| **`entityProperty`** | Write response entity to prop | No |   | `response.entity` |

#### example:

```xml
<http
  url="http://google.com"
  method="GET"
  printRequest="false"
  printResponse="true"
  printRequestHeaders="true"
  printResponseHeaders="true"
  expected="200"
  failOnUnexpected="false"
/>
```

### http/credentials

#### supported parameters:

| Name | Description | Required | Default | Example |
|------|-------------|----------|---------|---------|
| **`username`** | HTTP Basic username | Yes |   | `john.doe` |
| **`password`** | HTTP Basic password | No |   | `p@55w0rd` |
| **`show`** | Print the credentials | No | `false` | `true` or `false` |

#### example:

```xml
<http url="http://google.com">
  <credentials username="john.doe" password="p@55w0rd"/>
</http>
```


### http/keystore

#### supported parameters:

| Name | Description | Required | Default | Example |
|------|-------------|----------|---------|---------|
| **`file`** | KeyStore file | Yes |   | `/path/to/keystore.jks` |
| **`password`** | KeyStore password | No |   | `p@55w0rd` |

#### example:

```xml
<http url="http://google.com">
  <keystore file="/path/to/keystore.jks" password="p@55w0rd"/>
</http>
```


### http/headers/header

#### supported parameters:

| Name | Description | Required | Default | Example |
|------|-------------|----------|---------|---------|
| **`name`** | Header name | Yes |   | `Accept` |
| **`value`** | Header value | No |   | `application/xml,*/*` |

#### example:

```xml
<http url="http://google.com">
  <headers>
    <header name="Accept" value="application/xml,*/*"/>
    <header name="Content-Type" value="application/xml"/>
  </headers>
</http>
```


### http/query/parameter

#### supported parameters:

| Name | Description | Required | Default | Example |
|------|-------------|----------|---------|---------|
| **`name`** | Query parameter name | Yes |   | `qp` |
| **`value`** | Query parameter value | No |   | `value123` |

#### example:

```xml
<http url="http://google.com">
  <query>
    <parameter name="qp1" value="value1"/>
    <parameter name="qp2" value="value2"/>
  </query>
</http>
```


### http/entity

#### supported parameters:

| Name | Description | Required | Default | Example |
|------|-------------|----------|---------|---------|
| **`file`** | File to read entity from | No |   | `request.xml` |
| **`binary`** | Treat entity as binary | False |   | `true` or `false` |
| **`value`** | Value to use as entity | No |   | `${my.prop}` |

#### example:

```xml
<http url="http://google.com">
  <entity file="request.zip" binary="true"/>
</http>
```

```xml
<http url="http://google.com">
  <entity><![CDATA[Request Entity]]></entity>
</http>
```


## Ant configuration

The following is a basic example of how to import and use the missing link Ant HTTP task:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project name="ml-ant-http" basedir="." default="http-get">

  <property name="ml-ant-http.jar" value="ml-ant-http-1.0.jar"/>
  
  <fileset id="runtime.libs" dir=".">
    <include name="${ml-ant-http.jar}"/>
  </fileset>
  
  <path id="runtime.classpa<th">
    <fileset refid="runtime.libs"/>
  </path>
  
  <taskdef name="http" classname="org.missinglink.ant.task.http.HttpClientTask">
    <classpath refid="runtime.classpath"/>
  </taskdef>
  
  <target name="http-get">
    <http url="http://www.google.com"/>
  </target>
  
</project>
```
