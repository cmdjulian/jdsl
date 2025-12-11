package de.cmdjulian.jdsl

import tools.jackson.module.kotlin.jsonMapper
import tools.jackson.module.kotlin.kotlinModule
import tools.jackson.databind.JsonNode as JacksonJsonNode
import tools.jackson.databind.ObjectMapper as JacksonObjectMapper
import tools.jackson.databind.node.ArrayNode as JacksonArrayNode
import tools.jackson.databind.node.ObjectNode as JacksonObjectNode

val JsonMapper =
    jsonMapper {
        addModule(kotlinModule())
    }

object Transformers {
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

    open class ObjectMapper<T>(
        private val type: Class<T>,
        private val mapper: JacksonObjectMapper = JsonMapper,
    ) : Transformer<T, JacksonJsonNode> {
        companion object {
            inline operator fun <reified T> invoke(mapper: JacksonObjectMapper = JsonMapper): ObjectMapper<T> =
                ObjectMapper(T::class.java, mapper)
        }

        override fun process(element: JacksonNodeBuilder<out JacksonJsonNode>): T =
            mapper.treeToValue(element.node, type)
    }
}

interface Transformer<T, V : JacksonJsonNode> {
    fun process(element: JacksonNodeBuilder<out V>): T
}
