# Changelog
All notable changes to kb-util will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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
