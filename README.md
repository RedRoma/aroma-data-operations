Aroma Service Data Operations
==============================================

[<img src="https://raw.githubusercontent.com/RedRoma/aroma/develop/Graphics/Logo.png" width="300">](http://aroma.redroma.tech/)

[![Build Status](http://jenkins.redroma.tech/job/Aroma%20Data%20Operations/badge/icon)](http://jenkins.redroma.tech/job/Aroma%20Data%20Operations/)

Defines the Data Interfaces and Operations used by the various Aroma Services.

These Data Operations are used by Business Logic living within the various Aroma Services.

For example, the Application Service needs the MessageRepository to store messages, and the Aroma
Service needs it to retrieve Messages.


# Download

To use, simply add the following maven dependency.

## Release
```xml
<dependency>
	<groupId>tech.aroma</groupId>
	<artifactId>aroma-data-operations</artifactId>
	<version>2.1.2</version>
</dependency>
```

## Snapshot

>First add the Snapshot Repository
```xml
<repository>
	<id>ossrh</id>
    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
</repository>
```

```xml
<dependency>
	<groupId>tech.aroma</groupId>
	<artifactId>aroma-data-operations</artifactId>
	<version>2.2-SNAPSHOT</version>
</dependency>
```

# [Javadocs](http://www.javadoc.io/doc/tech.aroma/aroma-data-operations/)
