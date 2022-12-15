package de.cmdjulian.jdsl

import com.fasterxml.jackson.databind.node.ArrayNode
import de.cmdjulian.jdsl.JacksonObjectNodeBuilder.Companion.obj
import io.kotest.matchers.shouldBe
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import com.fasterxml.jackson.databind.node.JsonNodeFactory.instance as JsonNodeFactory

internal class JacksonDslTest {

    @Test
    fun `object node`() {
        val node = obj {
            "foo" `=` "bar"
            "fizz" `=` arr[1, 2]
            "boo" `=` arr()
        }

        node shouldBe JsonNodeFactory.objectNode().apply {
            put("foo", "bar")
            set<ArrayNode>(
                "fizz",
                JsonNodeFactory.arrayNode(2).apply {
                    add(1)
                    add(2)
                }
            )
            set<ArrayNode>("boo", JsonNodeFactory.arrayNode(0))
        }
    }

    @Test
    fun `pretty print json string`() {
        val node = obj(Transformers.String) {
            "foo" `=` "bar"
            "fizz" `=` arr[1, 2]
            "test" `=` `null`
        }

        //language=JSON
        node shouldBe """
            {
              "foo" : "bar",
              "fizz" : [ 1, 2 ],
              "test" : null
            }
        """.trimIndent()
    }

    @Test
    fun example() {
        @Language("JSON")
        val expected = """
            {
              "foo" : "bar",
              "integer" : 1337,
              "boolean" : true,
              "nullable" : null,
              "float" : 69.69,
              "nested-object" : {
                "fizz" : "buzz"
              },
              "array-of-numbers" : [ 1, 2, 3 ],
              "array-of-objects" : [ {
                "name" : "tony stark"
              }, {
                "name" : "steve rogers"
              } ],
              "empty-arr" : [ ],
              "pojo" : {
                "first" : "airbus",
                "second" : "boeing"
              }
            }
        """.trimIndent()

        obj(Transformers.String) {
            "foo" `=` "bar"
            "integer" `=` 1337
            "boolean" `=` true
            "nullable" `=` `null`
            "float" `=` 69.69
            "nested-object" `=` obj {
                "fizz" `=` "buzz"
            }
            "array-of-numbers" `=` arr[1, 2, 3]
            "array-of-objects" `=` arr[
                obj { "name" `=` "tony stark" },
                obj { "name" `=` "steve rogers" }
            ]
            "empty-arr" `=` arr
            "pojo" `=` json { Pair("airbus", "boeing") }
        } shouldBe expected
    }

    @Test
    fun json() {
        json(transformer = Transformers.String) { 5 } shouldBe "5"
        json { 5 } shouldBe JsonNodeFactory.numberNode(5)
    }

    @Test
    fun jsonDsl() {
        @Language("JSON")
        val expected = """
            {
              "fizz" : "buzz"
            }
        """.trimIndent()

        json(transformer = Transformers.String) {
            obj {
                "fizz" `=` "buzz"
            }
        } shouldBe expected
    }

    @Test
    fun `object mapping`() {
        data class FizzBuzz(val fizz: String)

        val json: FizzBuzz = obj(transformer = Transformers.ObjectMapper()) {
            "fizz" `=` "buzz"
        }

        json shouldBe FizzBuzz("buzz")
    }

    @Test
    fun `static object mapping`() {
        data class FizzBuzz(val fizz: String)

        val fizzBuzzTransformer = object : Transformers.ObjectMapper<FizzBuzz>(FizzBuzz::class.java) {}

        val json = obj(transformer = fizzBuzzTransformer) {
            "fizz" `=` "buzz"
        }

        json shouldBe FizzBuzz("buzz")
    }
}
