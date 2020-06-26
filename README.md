# KB-Util 

Shared utility library for IT developers at the Royal Danish Library.

Available as a dependency
```$xslt
<dependency>
    <groupId>dk.kb.util</groupId>
    <artifactId>kb-util</artifactId>
    <version>VERSION</version>
<dependency>
```

## Requirements

* Maven 3                                  
* Java 11 (tested with OpenJDK 11)

## Build

``` 
mvn package
```

## Adding to the library

There are no set rules for what `kb-util` contains and no special group or person
responsible for approving additions, changes or bug fixes. Standard procedure for
developing software at the Royal Danish Library does apply: Please get someone to
review your changes.
 
The content of `kb-util` is expected to be used across multiple projects and it is
expected to be a light weight dependency: Please don't add an utility for OCRing of
subtitles from video streams, requiring gigabytes of third party libraries.
 
## Release procedure

The `master`-branch of `kb-util` is expected to be ready for release at all times.
Releases can be done by any IT developer at the Royal Danish Library, whenever they
see the need.

The practical steps are

1. Review that the `version` in `pom.xml` is fitting. `kb-util` uses
[Semantic Versioning](https://semver.org/spec/v2.0.0.html): The typical release
will bump the `MINOR` version and set `PATCH` to 0. Keep the `-SNAPSHOT`-part as
the Maven release plugin handles that detail.   
1. Ensure that [CHANGELOG.md](CHANGELOG.md) is up to date. `git log` is your friend.
1. Ensure all local changes are committed and pushed.
1. Ensure that your local `.m2/settings.xml` has a current `sbforge-nexus`-setup
(contact Kim Christensen kb@ or another Maven-wrangler for help)
1. Follow the instructions on
[Guide to using the release plugin](https://maven.apache.org/guides/mini/guide-releasing.html)
which boils down to
   * Run `mvn release:prepare`
   * Check that everything went well, then run `mvn release:perform`
   * Run `git push`   
