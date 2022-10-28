package de.cmadjulian.jdsl

import com.fasterxml.jackson.databind.node.ArrayNode
import de.cmadjulian.jdsl.JacksonObjectNodeBuilder.Companion.obj
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
        val node = obj(Transformer.String) {
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
              "float" : 1337.0,
              "nested-object" : {
                "fizz" : "buzz"
              },
              "array-of-numbers" : [ 1, 2, 3 ],
              "array-of-objects" : [ {
                "name" : "tony stark"
              }, {
                "name" : "steve rogers"
              } ],
              "empty-arr" : [ ]
            }
        """.trimIndent()

        obj(Transformer.String) {
            "foo" `=` "bar"
            "integer" `=` 1337
            "boolean" `=` true
            "nullable" `=` `null`
            "float" `=` 1337.0
            "nested-object" `=` obj {
                "fizz" `=` "buzz"
            }
            "array-of-numbers" `=` arr[1, 2, 3]
            "array-of-objects" `=` arr[
                obj { "name" `=` "tony stark" },
                obj { "name" `=` "steve rogers" }
            ]
            "empty-arr" `=` arr
        } shouldBe expected
    }
}
