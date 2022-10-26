package de.cmadjulian.jdsl

import com.fasterxml.jackson.databind.node.ArrayNode
import de.cmadjulian.jdsl.JacksonObjectNodeBuilder.Companion.obj
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import com.fasterxml.jackson.databind.node.JsonNodeFactory.instance as JsonNodeFactory

internal class JacksonDslTest {

    @Test
    fun `object node`() {
        val node = obj {
            "foo" `=` "bar"
            "fizz" `=` arr[1, 2]
        }

        node shouldBe JsonNodeFactory.objectNode().apply {
            put("foo", "bar")
            set<ArrayNode>(
                "fizz",
                JsonNodeFactory.arrayNode(2).apply {
                    add(1L)
                    add(2L)
                }
            )
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
}
