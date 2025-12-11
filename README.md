[![](https://jitpack.io/v/cmdjulian/jdsl.svg)](https://jitpack.io/#cmdjulian/jdsl)

![](logo.png)

# jdsl

Kotlin native DSL for Jackson Object Mapper to describe JSON in code.

## Version compatibility matrix

This table summarizes supported Jackson versions and required JVM baselines per jdsl version, plus notes about breaking changes.

| jdsl version | Jackson version | Minimum JVM version | Notes                                                                                                                                                                                                 |
|--------------|-----------------|---------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| v2.x.x       | 3.x             | 17                  | Breaking change: Jackson 3 migrated packages and removed deprecated APIs; imports change (e.g., com.fasterxml.jackson.databind.json.JsonMapper). Update your imports and ensure you build on JDK 17+. |
| v1.x.x       | 2.x             | 8                   | Compatible with Jackson 2.x. Project baseline was Java 8.                                                                                                                                             |

## Adding the Dependency

jdsl v1.x requires Java 8. jdsl v2.x requires Java 17 (due to Jackson 3).  
The client can be pulled into gradle or maven by using [jitpack](https://jitpack.io/#cmdjulian/jdsl).

<details>
<summary>Gradle</summary>

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}


dependencies {
    implementation 'com.github.cmdjulian:jdsl:{VERSION}'
}
```

</details>

<details>
<summary>Gradle Kts</summary>

```kotlin
repositories {
    maven(url = "https://jitpack.io")
}


dependencies {
    implementation("com.github.cmdjulian:jdsl:{VERSION}")
}
```

</details>

<details>
<summary>Maven</summary>

```xml

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    ...

    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>

    ...

    <dependencies>
        <dependency>
            <groupId>com.github.cmdjulian</groupId>
            <artifactId>jdsl</artifactId>
            <version>{VERSION}</version>
        </dependency>
    </dependencies>
</project>
```

</details>

## DSL

### array

```kotlin
// empty array '[ ]'
arr
arr()
arr { }

// results in '[1, 2, 3]'
arr[1, 2, 3]
arr {
    add(1)
    add(2)
    add(3)
}
```

### object

```kotlin
// empty object '{ }'
obj { }

// object with keys '{ "foo": "bar" }'
obj {
    "foo" `=` "bar"
}
```

## literals

Literals are not required to be used in the dsl. However, if you need an instance of a `JsonNode`, this can be a quick
way to achieve that.  
You can also use it top convert any pojo to json.

```kotlin
json(5)             // <-- ValueNode
json { 5 }          // <-- JsonNode
json { Pair(1, 2) } // <-- JsonNode
```

### mixed example

```kotlin

import java.time.OffsetDateTime

obj {
    "foo" `=` "bar"
    "integer" `=` 1337
    "boolean" `=` true
    "nullable" `=` `null`
    "float" `=` 69.0
    "time" `=` OffsetDateTime.now()
    "enum" `=` Sort.ASC
    "nested-object" `=` obj { "fizz" `=` "buzz" }
    "array-of-numbers" `=` arr[1, 2, 3]
    "array-of-objects" `=` arr[
        obj { "name" `=` "tony stark" },
        obj { "name" `=` "steve rogers" }
    ]
    "empty-arr" `=` arr
    "pojo" `=` json { Pair("airbus", "boeing") }
}

```

this results in the following JSON:

```json
{
  "foo": "bar",
  "integer": 1337,
  "boolean": true,
  "nullable": null,
  "time": "2022-10-28T20:49:03.121449+02:00",
  "enum": "ASC",
  "float": 1337.0,
  "nested-object": {
    "fizz": "buzz"
  },
  "array-of-numbers": [
    1,
    2,
    3
  ],
  "array-of-objects": [
    {
      "name": "tony stark"
    },
    {
      "name": "steve rogers"
    }
  ],
  "empty-arr": [],
  "pojo": {
    "first": "airbus",
    "second": "boeing"
  }
}
```

### transformers

As default, the `obj` and `arr` functions return Jackson's `ObjectNode` and `ArrayNode`, however, if you want for
instance a pretty printed String or Byte, you can supply a processor.

The following code for instance returns string:

```kotlin
obj(Transformers.String) { "fizz" `=` "buzz" } // <-- return type is string
arr(Transformers.Byte) { add(1) }              // <-- return type is byte
json(Transformers.String) { 5 }                // <-- return type is string
```

The library includes transformers for `String` and `Byte`. However, if you for instance want to convert json to CSV or
some other format / object, you can provide your own transformer by implementing the `Transformer` interface.

As `arr[]` doesn't support transformer, you can wrap it inside a json block, which in turn does support transformer:

```kotlin
json(Transformers.Byte) { arr[1, 2, 3] } // <-- return type is Byte
```

You can even use the included `ObjectMapper` `Transformer` to get arbitrary objects from the json
via `Jackson ObjectMapper`:

```kotlin
data class FizzBuzz(val fizz: String)

val json: FizzBuzz = obj(transformer = Transformers.ObjectMapper()) {
    "fizz" `=` "buzz"
}
```

Under the hood, an `ObjectMapper` is used to convert the `JsonNode` into a class. As default, the modules `kotlinModule`
, `Jdk8Module` and `JavaTimeModule` are registered. You can also customize the used mapper:

```kotlin
val mapper = jsonMapper { }

obj(transformer = Transformers.ObjectMapper(mapper)) {
    "fizz" `=` "buzz"
}
```

You could also create named objects to simplify the mapping for a specific type:

```kotlin
data class FizzBuzz(val fizz: String)

object FizzBuzzTransformer : Transformers.ObjectMapper<FizzBuzz>(FizzBuzz::class.java)

val json = obj(transformer = FizzBuzzTransformer) {
    "fizz" `=` "buzz"
}
```
