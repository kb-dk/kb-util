# Changelog
All notable changes to kb-util will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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
