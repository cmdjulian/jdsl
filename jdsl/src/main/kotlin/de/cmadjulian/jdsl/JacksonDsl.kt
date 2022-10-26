@file:Suppress("unused")

package de.cmadjulian.jdsl

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.ValueNode
import java.time.temporal.Temporal
import com.fasterxml.jackson.databind.node.JsonNodeFactory.instance as JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode as JacksonObjectNode

@DslMarker
annotation class JsonMarker

@JsonMarker
sealed class JacksonNodeBuilder<T : JsonNode>(internal val node: T) {
    override fun toString(): String = node.toPrettyString()
    override fun hashCode(): Int = node.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JacksonNodeBuilder<*>) return false
        return node == other.node
    }
}

@Suppress("FunctionName", "kotlin:S100", "MemberVisibilityCanBePrivate")
class JacksonObjectNodeBuilder private constructor(node: JacksonObjectNode = JsonNodeFactory.objectNode()) :
    JacksonNodeBuilder<JacksonObjectNode>(node) {

    companion object {
        fun obj(init: JacksonObjectNodeBuilder.() -> Unit): JacksonObjectNode = obj(Transformer.ObjectNode, init)
        fun <T> obj(transformer: Transformer<T, in ObjectNode>, init: JacksonObjectNodeBuilder.() -> Unit): T =
            transformer.process(JacksonObjectNodeBuilder().apply(init))
    }

    infix fun String.`=`(value: Boolean) = put(this, value)
    infix fun String.`=`(value: Enum<*>) = put(this, value)
    infix fun String.`=`(value: JsonNode) = put(this, value)
    infix fun String.`=`(value: Number) = put(this, value)
    infix fun String.`=`(value: String) = put(this, value)
    infix fun String.`=`(value: Temporal) = put(this, value)

    fun put(key: String, value: Boolean) = put(key, json(value))
    fun put(key: String, value: Enum<*>) = put(key, json(value))
    fun put(key: String, value: Number) = put(key, json(value))
    fun put(key: String, value: String) = put(key, json(value))
    fun put(key: String, value: Temporal) = put(key, json(value))
    fun put(key: String, value: JsonNode) {
        node.replace(key, value)
    }
}

@Suppress("MemberVisibilityCanBePrivate")
class JacksonArrayNodeBuilder private constructor(array: ArrayNode = JsonNodeFactory.arrayNode()) :
    JacksonNodeBuilder<ArrayNode>(array) {

    companion object {
        fun arr(init: JacksonArrayNodeBuilder.() -> Unit): ArrayNode = arr(Transformer.ArrayNode, init)
        fun <T> arr(transformer: Transformer<T, in ArrayNode>, init: JacksonArrayNodeBuilder.() -> Unit): T =
            transformer.process(JacksonArrayNodeBuilder().apply(init))
    }

    fun add(value: Boolean) = add(json(value))
    fun add(value: Enum<*>) = add(json(value))
    fun add(value: Number) = add(json(value))
    fun add(value: String) = add(json(value))
    fun add(value: Temporal) = add(json(value))
    fun add(value: JsonNode) {
        node.add(value)
    }
}

// array
@JsonMarker
@Suppress("ClassName")
object arr {
    operator fun get(vararg elements: Boolean): ArrayNode = JacksonArrayNodeBuilder.arr { elements.forEach(::add) }
    operator fun get(vararg elements: Enum<*>) = JacksonArrayNodeBuilder.arr { elements.forEach(::add) }
    operator fun get(vararg elements: JsonNode): ArrayNode = JacksonArrayNodeBuilder.arr { elements.forEach(::add) }
    operator fun get(vararg elements: Number): ArrayNode = JacksonArrayNodeBuilder.arr { elements.forEach(::add) }
    operator fun get(vararg elements: String): ArrayNode = JacksonArrayNodeBuilder.arr { elements.forEach(::add) }
    operator fun get(vararg elements: Temporal): ArrayNode = JacksonArrayNodeBuilder.arr { elements.forEach(::add) }
}

// null
@JsonMarker
@Suppress("ClassName")
object `null` : NullNode()

// value
fun json(value: Boolean): ValueNode = JsonNodeFactory.booleanNode(value)
fun json(value: Enum<*>): ValueNode = JsonNodeFactory.textNode(value.name)
fun json(value: String): ValueNode = JsonNodeFactory.textNode(value)
fun json(value: Temporal): ValueNode = JsonNodeFactory.textNode(value.toString())
fun json(value: Number): ValueNode = when (value) {
    is Double -> JsonNodeFactory.numberNode(value.toDouble())
    is Float -> JsonNodeFactory.numberNode(value.toFloat())
    is Long -> JsonNodeFactory.numberNode(value.toLong())
    is Int -> JsonNodeFactory.numberNode(value.toInt())
    is Short -> JsonNodeFactory.numberNode(value.toShort())
    else -> JsonNodeFactory.numberNode(value.toByte())
}
