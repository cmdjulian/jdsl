[![](https://jitpack.io/v/cmdjulian/jdsl.svg)](https://jitpack.io/#cmdjulian/jdsl)

# jdsl

Kotlin native DSL for Jackson Object Mapper to describe JSON in code.

## Adding the Dependency

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

## literals

Literals are not required to be used in the dsl. However, if you need an instance of a `JsonNode`, this can be a quick
way to achieve that.

```kotlin
json(5) // <-- ValueNode 
```

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
  ]
}
```

### transformers

As default, the `obj` and `arr` functions return Jackson's `ObjectNode` and `ArrayNode`, however, if you want for
instance a pretty printed String or Byte, you can supply a processor.

The following code for instance returns string:

```kotlin
obj(Transformer.String) { "fizz" `=` "buzz" } // <-- return type is string
arr(Transformer.String) { add(1) } // <-- return type is string
```

The library includes transformers for `String` and `Byte`. However, if you for instance want to convert json to CSV or
some other format / object, you can provide your own transformer by implementing the `Transformer` interface.