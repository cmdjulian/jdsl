package de.cmadjulian.jdsl

import com.fasterxml.jackson.databind.JsonNode as JacksonJsonNode
import com.fasterxml.jackson.databind.node.ArrayNode as JacksonArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode as JacksonObjectNode

interface Transformer<T, V : JacksonJsonNode> {

    fun process(element: JacksonNodeBuilder<out V>): T

    object String : Transformer<kotlin.String, JacksonJsonNode> {
        override fun process(element: JacksonNodeBuilder<out JacksonJsonNode>) = element.toString()
    }

    object Byte : Transformer<ByteArray, JacksonJsonNode> {
        override fun process(element: JacksonNodeBuilder<out JacksonJsonNode>) = String.process(element).toByteArray()
    }

    open class JsonNode<T : JacksonJsonNode> : Transformer<T, T> {
        override fun process(element: JacksonNodeBuilder<out T>): T = element.node
    }

    object ObjectNode : JsonNode<JacksonObjectNode>() {
        override fun process(element: JacksonNodeBuilder<out JacksonObjectNode>) = element.node
    }

    object ArrayNode : JsonNode<JacksonArrayNode>() {
        override fun process(element: JacksonNodeBuilder<out JacksonArrayNode>) = element.node
    }
}
