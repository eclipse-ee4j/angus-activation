<br/>

The Angus Activation is a compatible implementation of
the [Jakarta Activation Specification 2.1+](https://jakarta.ee/specifications/activation/)
which lets you take advantage of standard services to:
determine the type of an arbitrary piece of data; encapsulate access to
it; discover the operations available on it; and instantiate the
appropriate bean to perform the operation(s).

This project is part of [Eclipse Angus project](https://projects.eclipse.org/projects/ee4j.angus).


# Table of Contents
* [Latest News](#Latest_News)
* [Download Angus Activation Release](#Download_Angus_Activation_Release)
* [API Documentation](#API_Documentation)
* [Help](#Help)
* [Bugs](#Bugs)
* [Development Releases](#Development_Releases)

# <a name="Latest_News"></a>Latest News

## TBD - Angus Activation 2.0.0 Final Release

Changes module name from `com.sun.activation.registries`
to `org.eclipse.angus.activation` and package name from
`com.sun.activation.registries` to `org.eclipse.angus.activation`.

## December 14, 2021 - Angus Activation 1.0.0 Final Release

Initial release of the Eclipse Angus - Angus Activation project.
Provides implementation of the [Jakarta Activation 2.1 Specification](https://jakarta.ee/specifications/activation/2.1/).


## July 27, 2021 - Angus Activation inception

As part of breaking tight integration between Jakarta Activation API
and the implementation, implementation sources were moved to this new project -
Eclipse Angus - and further development continues here.
Eclipse Angus is direct accessor of JavaActivation/JakartaActivation.

<br/>

# <a name="Download_Angus_Activation_Release"></a>Download Angus Activation Release

The Angus Activation jar file, which is all most applications will need,
is published to the Maven repository and can be included in a project using
this Maven dependency:

```
        <dependencies>
            <dependency>
                <groupId>jakarta.activation</groupId>
                <artifactId>jakarta.activation-api</artifactId>
                <version>${activation-api.version}</version>
            </dependency>
            <dependency>
                <groupId>org.eclipse.angus</groupId>
                <artifactId>angus-activation</artifactId>
                <version>${angus-activation.version}</version>
                <scope>runtime</scope>
            </dependency>
        </dependencies>
```

You can find all of the Angus Mail jar files in
[Maven Central](https://search.maven.org/search?q=g:org.eclipse.angus).

<br/>

# <a name="API_Documentation"></a>API Documentation

The Angus Activation API documentation is available
[here](https://eclipse-ee4j.github.io/angus-activation/api/).

<br/>

# <a name="Help"></a>Help

You can post questions to the
[angus-dev mailing list](https://accounts.eclipse.org/mailing-list/angus-dev).

<br/>

# <a name="Bugs"></a>Bugs

Angus Activation bugs are tracked in the
[GitHub Angus Activation project issue tracker](https://github.com/eclipse-ee4j/angus-activation/issues).

<br/>

# <a name="Development_Releases"></a>Development Releases

Snapshot builds of the next version of the Angus Activation
under development are published to the
[Jakarta Sonatype OSS repository](https://jakarta.oss.sonatype.org).
These snapshot builds have received only minimal testing, but may
provide previews of bug fixes or new features under development.

For example, you can download the angus-activation.jar file from the Eclipse Angus
2.0.0-SNAPSHOT release
[here](https://jakarta.oss.sonatype.org/content/repositories/snapshots/com/sun/activation/jakarta.activation/2.0.0-SNAPSHOT/).
Be sure to scroll to the bottom and choose the jar file with the most
recent time stamp.

You'll need to add the following configuration to your Maven ~/.m2/settings.xml
to be able to use these with Maven:

```
    <profiles>
        <!-- to allow loading Jakarta snapshot artifacts -->
        <profile>
            <id>jakarta-snapshots</id>
            <pluginRepositories>
                <pluginRepository>
                    <id>jakarta-snapshots</id>
                    <name>Jakarta Snapshots</name>
                    <snapshots>
                        <enabled>true</enabled>
                        <updatePolicy>always</updatePolicy>
                        <checksumPolicy>fail</checksumPolicy>
                    </snapshots>
                    <url>https://jakarta.oss.sonatype.org/content/repositories/snapshots/</url>
                    <layout>default</layout>
                </pluginRepository>
            </pluginRepositories>
        </profile>
    </profiles>
```

And then when you build use `mvn -Pjakarta-snapshots ...`.

If you want the plugin repository to be enabled all the time so you don't need the -P, add:

```
    <activeProfiles>
        <activeProfile>jakarta-snapshots</activeProfile>
    </activeProfiles>
```

<br/>

By contributing to this project, you agree to these additional terms of
use, described in [CONTRIBUTING](CONTRIBUTING.md).
