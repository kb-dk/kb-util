# Changelog
All notable changes to kb-util will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added

## [1.4.14](https://github.com/kb-dk/kb-util/tree/kb-util-1.4.14)
### Bugfix
- Path-substitution still failed when using `YAML.getSubMap(...)` and requesting a key with a substitution using value.

## [1.4.13](https://github.com/kb-dk/kb-util/tree/kb-util-1.4.13)
### Added
- Path-substitution in YAML, allowing for YAML entries like `dburl: ${path:databases.primary.url}`
- Conditionals in path in YAML, allowing for Java calls like `myYAML.get("databases.[default=true].url");`
- Combining path substitution and conditional paths, allowing for YAML entries like `dburl: ${path:databases.[default=true].url}` 

## [1.4.12](https://github.com/kb-dk/kb-util/tree/kb-util-1.4.12)
### Added

- Support for positioning an XMLStreamReader at a given XPath, so that the path-matching sub-XML can be processed or extracted
- Exception throwing on unfulfillable expansion in YAML configs

## [1.4.11](https://github.com/kb-dk/kb-util/tree/kb-util-1.4.11)
### Added

- Large port from sb-util: The full replacer-suite and XML-utilities plus profiling tools

## [1.4.10](https://github.com/kb-dk/kb-util/tree/kb-util-1.4.10)
### Added

- ExtractionUtils focused on sampling and extracting minimum or maximum values from Streams and Collections.
- Splitting of an incoming Stream to multiple partitions 

## [1.4.9](https://github.com/kb-dk/kb-util/tree/kb-util-1.4.9)
### Added

- Auto updating YAML based config
- Streaming export, used by webservices
- HTTP Exceptions used by webservices
- OpenAPI implementation super class used by OpenAPI applications
- build.properties programmatic access (application-ID, version, build-time)
- kbutil.build.properties file in the kb-util JAR

## [1.4.8](https://github.com/kb-dk/kb-util/tree/kb-util-1.4.8)
### Added

- Support for quoting keys in `YAML` paths to allow for keys with dots (.)
- Expansion of system properties in YAML
- Fully flattening (single line) XML.domToString method

## [1.4.7](https://github.com/kb-dk/kb-util/tree/kb-util-1.4.7)
### Changed

- Fix resource leak in `Resolver#walkMatches`

## [1.4.6](https://github.com/kb-dk/kb-util/tree/kb-util-1.4.6)
### Added

- `CallbackReplacer` and `XMLEscapeSanitiser`: Regexp-based replacements with callbacks

## [1.4.5](https://github.com/kb-dk/kb-util/tree/kb-util-1.4.5)
### Added

- Added `StringListUtils.toModifiableList(list)` utility to check, and, if nessesary, wrap your list as a modifiable list

### Changed

- Fixed JSON.java to correctly handle java 8 datetime objects

## [1.4.4](https://github.com/kb-dk/kb-util/tree/kb-util-1.4.4)

### Added

Changed jaxb to use 
```xml
<!--Nessesary for jaxb-xml with java 11-->
<!-- https://mvnrepository.com/artifact/jakarta.xml.bind/jakarta.xml.bind-api -->
<dependency>
    <groupId>jakarta.xml.bind</groupId>
    <artifactId>jakarta.xml.bind-api</artifactId>
    <version>2.3.2</version>
</dependency>
<!-- https://mvnrepository.com/artifact/jakarta.activation/jakarta.activation-api -->
<dependency>
    <groupId>jakarta.activation</groupId>
    <artifactId>jakarta.activation-api</artifactId>
    <version>1.2.1</version>
</dependency>
<!-- https://mvnrepository.com/artifact/org.glassfish.jaxb/jaxb-runtime -->
<dependency>
    <groupId>org.glassfish.jaxb</groupId>
    <artifactId>jaxb-runtime</artifactId>
    <version>2.3.2</version>
    <scope>runtime</scope>
</dependency>
<!--XML end-->
```
as this prevents the nasty 
```
java.lang.module.FindException: Two versions of module jakarta.activation found in /home/abr/Projects/java-xcorrsound/java-xcorrsound-cli/target/java-xcorrsound-cli-0.1-SNAPSHOT/bin/../lib (jakarta.activation-api-1.2.2.jar and jakarta.activation-1.2.2.jar)
```
when used in a module-based projekt

## [1.4.3](https://github.com/kb-dk/kb-util/tree/kb-util-1.4.3)
### Added

- XML.unmarshall checks the presense of `@XmlRootElement` annotation on the supplied type and selects the appropriate strategy from 
    * [Unmarshal Global Root Element](https://jakarta.ee/specifications/xml-binding/2.3/apidocs/javax/xml/bind/unmarshaller#unmarshalGlobal)
    * [Unmarshal by Declared Type](https://jakarta.ee/specifications/xml-binding/2.3/apidocs/javax/xml/bind/unmarshaller#unmarshalByDeclaredType)  

## [1.4.2](https://github.com/kb-dk/kb-util/tree/kb-util-1.4.2)
### Added

- Better exceptions when creating a YAML from non-existing resources
- Choice between loading multiple YAML-files as a single reference-supporting stream or as multiple overwriting streams 

## [1.4.1](https://github.com/kb-dk/kb-util/tree/kb-util-1.4.1)
### Bugfix

- Globbing now supports . and .. (foo/bar/../bar/./boom.txt)

## [1.4.0](https://github.com/kb-dk/kb-util/tree/kb-util-1.4.0)
### Added
- `YAML.parse` methods that takes a Path or File as input, rather than a classpath Name or InputStream. Just for more flexibility in how to read your YAML file.
- Globbing (resolving of files with wildcards) to the Resolver
- Default globbing when resolving YAML configs

## [1.3.0](https://github.com/kb-dk/kb-util/tree/kb-util-1.3.0)
### Added

- Shadow-class `dk.kb.util.YAML` introduced to compensate for breaking backwards
compatibility in 1.2.0.

- Dependency for jaxb runtime (org.glassfish.jaxb:jaxb-runtime) to make xml classes 
not throw runtime exceptions due to missing implementation

### Updated

- Dependencies updated:
    - snakeyaml: 1.23 -> 1.26
    - slf4j-api: 1.7.25 -> 1.7.30
    - jackson-databind: 2.9.10 -> 2.11.2
    - jakarta.ws.rs-api: 2.1.5 -> 2.1.6

## [1.2.0](https://github.com/kb-dk/kb-util/tree/kb-util-1.2.0)

### Added

- YAML.java supports indexed lists in getter methods

- Generic utils from alma-client ported to kb-utils
    - JSON 
        - fromJson(String/File) parses json to java object
        - toJson(Object) serialize java object to json
    - AutochainingIterator: Class to automatically chain generated iterators
    - NamedThread: Autoclosable thread namer
    - StringListUtils: Utility String and Stream methods
    - Resolver
        - GetPathFromClasspath -> Path
        - ReadFileFromClasspath -> String
        - OpenFileFromClasspath -> InputStream
    - XML (ported and adapted from [sbutil](https://github.com/statsbiblioteket/sbutil))
        - String/Stream to/from Document
        - Java Object to/from Xml String
        - XPath support
        
- Apache Commons Utilities that we should use
    - `commons-io:2.7`
    - `commons-collections4:4.4`
    - `commons-codec:1.14`
    - `commons-lang3:3.11`
    - `commons-text:1.9`

- Xml libraries for java 11+
    - `jakarta.xml.bind-api 2.3.3`
    - `com.sun.xml.bind:jaxb-impl:2.3.3`

- JSON libraries
    - `com.fasterxml.jackson.core:jackson-databind:2.11.0`
    - `jakarta.ws.rs:jakarta.ws.rs-api:2.1.5`

- Other libraries
    - `org.hamcrest:hamcrest-core:2.1` for `assertThat` in tests
    - `jakarta.validation:jakarta.validation-api:2.0.2` for `@NotNull` annotations in 
        method contracts


## [1.1.0](https://github.com/kb-dk/kb-util/tree/kb-util-1.1.0)
### Added

- Changelog
- Release procedure in the [README.md](README.md)
- Fail-early for missing values when using YAML `get`-methods without default value
- Key-value based recursive extraction of YAML leaf entries
- `YAMLUtils` with `toProperties` method for conversion to flat Java `Properties`

## [1.0.0](https://github.com/kb-dk/kb-util/tree/kb-util-1.0.0)
### Added

- The `kb-util`-project itself
- YAML: Parse from file or URL, path-based getters of elements
