![Kealth](docs/logo.png)

> Kealth is a health check library for external dependencies in Kotlin

[![CircleCI](https://circleci.com/gh/marioalvial/kealth.svg?style=svg)](https://circleci.com/gh/marioalvial/kealth)
[![Known Vulnerabilities](https://snyk.io/test/github/marioalvial/kealth/badge.svg?targetFile=build.gradle)](https://snyk.io/test/github/marioalvial/kealth?targetFile=build.gradle)
[![codecov](https://codecov.io/gh/marioalvial/kealth/branch/master/graph/badge.svg)](https://codecov.io/gh/marioalvial/kealth)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.marioalvial/kealth-jdbc.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:"io.github.marioalvial")
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/f51e7103bcc34855b506e947990b2395)](https://www.codacy.com/app/marioalvial/kealth?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=marioalvial/kealth&amp;utm_campaign=Badge_Grade)
[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Installation

![Maven](docs/maven.png) 

```xml
<repositories>
    <repository>
        <id>central</id>
        <name>Central Repository</name>
        <url>http://repo.maven.apache.org/maven2</url>
    </repository>
</repositories>

<dependency>
    <groupId>io.github.marioalvial</groupId>
    <artifactId>kealth-core</artifactId>
    <version>${kealth-version}</version>
</dependency>
```

![Gradle](docs/gradle.png)

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "io.github.marioalvial:kealth-core:$kealth_version"
}    
```

## Getting Started

1. Create your component:

```kotlin
class HealthComponentA : HealthComponent() {

    override val name = "component A"
    override val criticalLevel = CriticalLevel.HIGH

    override fun doHealthCheck(): HealthStatus {
        val result = doHealthCheckCallToComponentAService()
        return if(result) HealthStatus.HEALTHY else HealthStatus.UNHEALTHY
    }
    
    override fun handleFailure(throwable: Throwable) {
        sendAlert()
    }
}
```


You can also override other methods that handle failures during `health()`:

```kotlin
override fun handleUnhealthyStatus(){
    val parameters = parameters()
        
    println("Error while executing doHealthCheck. Parameters for debug: $parameters")
}
    
override fun handleCoroutineException(coroutineContext: CoroutineContext, exception: Throwable) {
    println("Coroutine throws exception with context $coroutineContext and error ${exception.printStackTrace()}")
}
```

2. Instantiate `HealthAggregator`:

```kotlin
val aggregator = HealthAggregator(listOf(HealthComponentA()))
```

3. Execute `aggregate()`:

```kotlin
val componentMap: Map<String, HealthInfo> = aggregator.aggregate() 
```

If you prefer you can also use `aggregateWithFilter()`. This method will only execute the `health()` function of components that matched the given predicate:

```kotlin
val componentMap: Map<String, HealthInfo> = aggregator.aggregateWithFilter{ name, criticalLevel -> name == "Component A" && criticalLevel == "HIGH" } 
```

**Example of `componentMap` serialized to json:**

```json
{
	"component-A": {
		"status": "HEALTHY",
		"criticalLevel": "HIGH",
		"duration": 1500
	},
	"component-B": {
		"status": "UNHEALTHY",
		"criticalLevel": "HIGH",
		"duration": 400
	}
}
```
## Handle Failure

`handleFailure()` will be trigger only if `doHealthCheck()` call throws exception.

## Handle Unhealthy Status

`handleUnhealthyStatus()` will be trigger only if `doHealthCheck()` call return HealthStatus.UNHEALTHY.
This method has a default implementation that can be override anytime.

## Handle Coroutine Exception

`handleCoroutineException()` will be trigger only if `handleException()` throws an exception.

## Share context

You can share some context from thread to any scope that is running your coroutine. Just override context val.

```kotlin
private val threadLocal = ThreadLocal<String>().apply { set("Thread Local $name") }
override var context: CoroutineContext = threadLocal.asContextElement()
```

## How it works

When `aggregator.aggregate()` is called it will execute `health()` of each component in parallel and create a map with the component's name as key and health info as value.

If the `doHealthCheck()` throws exception the component will trigger the `handleFailure()` method asynchronous with the exception that was thrown.

## Modules

| Module                                                                                   | Description                              | Artifacts                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| ---------------------------------------------------------------------------------------- | ---------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [kealth-core](kealth-core)                                                               | Core module                              | [![jar](https://img.shields.io/badge/jar-v2.0.1-green.svg)](https://search.maven.org/artifact/io.github.marioalvial/kealth-core/2.0.1/jar) [![javadoc](https://img.shields.io/badge/javadoc-v2.0.1-blue.svg)](https://search.maven.org/artifact/io.github.marioalvial/kealth-core/2.0.1/javadoc) [![sources](https://img.shields.io/badge/sources-v2.0.1-yellow.svg)](https://search.maven.org/artifact/io.github.marioalvial/kealth-core/2.0.1/sources)                                 |
| [kealth-jdbc](kealth-jdbc)                                                               | Health Component for JDBC                | [![jar](https://img.shields.io/badge/jar-v2.0.1-green.svg)](https://search.maven.org/artifact/io.github.marioalvial/kealth-jdbc/2.0.1/jar) [![javadoc](https://img.shields.io/badge/javadoc-v2.0.1-blue.svg)](https://search.maven.org/artifact/io.github.marioalvial/kealth-jdbc/2.0.1/javadoc) [![sources](https://img.shields.io/badge/sources-v2.0.1-yellow.svg)](https://search.maven.org/artifact/io.github.marioalvial/kealth-jdbc/2.0.1/sources)                                 |
| [kealth-http](kealth-http)                                                               | Health Component for HTTP Request        | [![jar](https://img.shields.io/badge/jar-v2.0.1-green.svg)](https://search.maven.org/artifact/io.github.marioalvial/kealth-http/2.0.1/jar) [![javadoc](https://img.shields.io/badge/javadoc-v2.0.1-blue.svg)](https://search.maven.org/artifact/io.github.marioalvial/kealth-http/2.0.1/javadoc) [![sources](https://img.shields.io/badge/sources-v2.0.1-yellow.svg)](https://search.maven.org/artifact/io.github.marioalvial/kealth-http/2.0.1/sources)                                 |

## Continuous Integration and Test Coverage

Test Coverage configured on CodeCov. Checkout the [test coverage here](https://codecov.io/gh/marioalvial/kealth).

Continuous Integration is configured on CircleCI. Checkout the [continuous integration here](https://circleci.com/gh/marioalvial/kealth)

## Testing

```shell
./gradlew test
```

## Built With

- [Kotlin](https://kotlinlang.org/) - Programming language
- [IntelliJ](https://www.jetbrains.com/idea/) - IDE
- [Gradle](https://gradle.org/) - Dependency Management

## Changelog

For latest updates see [CHANGELOG.md](CHANGELOG.md) file.

## Contributing 

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for more details, and the process for submitting pull requests to us.

## Authors

* **[Mário Alvial](https://github.com/marioalvial)**

## License

This project is licensed under the Apache License, Version 2.0 - see the [LICENSE](LICENSE) file for details.
