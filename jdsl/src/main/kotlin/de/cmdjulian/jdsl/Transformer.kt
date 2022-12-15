package de.cmdjulian.jdsl

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.databind.JsonNode as JacksonJsonNode
import com.fasterxml.jackson.databind.ObjectMapper as JacksonObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode as JacksonArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode as JacksonObjectNode

val JsonMapper = jsonMapper {
    configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    addModule(kotlinModule())
    addModule(Jdk8Module())
    addModule(JavaTimeModule())
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

    open class ObjectMapper<T>(private val type: Class<T>, private val mapper: JacksonObjectMapper = JsonMapper) :
        Transformer<T, JacksonJsonNode> {

        companion object {
            inline operator fun <reified T> invoke(mapper: JacksonObjectMapper = JsonMapper): ObjectMapper<T> {
                return ObjectMapper(T::class.java, mapper)
            }
        }

        override fun process(element: JacksonNodeBuilder<out JacksonJsonNode>): T {
            return mapper.treeToValue(element.node, type)
        }
    }
}

interface Transformer<T, V : JacksonJsonNode> {
    fun process(element: JacksonNodeBuilder<out V>): T
}
