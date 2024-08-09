@file:Suppress("unused")

package de.cmdjulian.jdsl

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.ValueNode
import java.time.Duration
import java.time.temporal.Temporal
import java.util.*
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

internal class GeneralJacksonNodeBuilder(node: JsonNode) : JacksonNodeBuilder<JsonNode>(node)

@Suppress("FunctionName", "kotlin:S100", "MemberVisibilityCanBePrivate", "TooManyFunctions")
class JacksonObjectNodeBuilder private constructor(node: JacksonObjectNode = JsonNodeFactory.objectNode()) :
    JacksonNodeBuilder<JacksonObjectNode>(node) {

    companion object {
        fun obj(init: JacksonObjectNodeBuilder.() -> Unit): JacksonObjectNode = obj(Transformers.ObjectNode, init)
        fun <T> obj(transformer: Transformer<T, in ObjectNode>, init: JacksonObjectNodeBuilder.() -> Unit): T =
            transformer.process(JacksonObjectNodeBuilder().apply(init))
    }

    infix fun String.`=`(value: arr?) = put(this, value?.invoke())
    infix fun String.`=`(value: Boolean?) = put(this, value)
    infix fun String.`=`(value: Enum<*>?) = put(this, value)
    infix fun String.`=`(value: JsonNode?) = put(this, value)
    infix fun String.`=`(value: Number?) = put(this, value)
    infix fun String.`=`(value: String?) = put(this, value)
    infix fun String.`=`(value: Temporal?) = put(this, value)
    infix fun String.`=`(value: UUID?) = put(this, value)
    infix fun String.`=`(value: Duration?) = put(this, value)

    fun put(key: String, value: arr?) = put(key, value?.invoke())
    fun put(key: String, value: Boolean?) = if (value != null) put(key, json(value)) else put(key, `null`)
    fun put(key: String, value: Enum<*>?) = if (value != null) put(key, json(value)) else put(key, `null`)
    fun put(key: String, value: Number?) = if (value != null) put(key, json(value)) else put(key, `null`)
    fun put(key: String, value: String?) = if (value != null) put(key, json(value)) else put(key, `null`)
    fun put(key: String, value: Temporal?) = if (value != null) put(key, json(value)) else put(key, `null`)
    fun put(key: String, value: UUID?) = if (value != null) put(key, json(value)) else put(key, `null`)
    fun put(key: String, value: Duration?) = if (value != null) put(key, json(value)) else put(key, `null`)
    fun put(key: String, value: JsonNode?) {
        node.replace(key, value)
    }
}

@Suppress("MemberVisibilityCanBePrivate")
class JacksonArrayNodeBuilder private constructor(array: ArrayNode = JsonNodeFactory.arrayNode()) :
    JacksonNodeBuilder<ArrayNode>(array) {

    companion object {
        fun arr(init: JacksonArrayNodeBuilder.() -> Unit): ArrayNode = arr(Transformers.ArrayNode, init)
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
    operator fun invoke(): ArrayNode = JacksonArrayNodeBuilder.arr { }
}

// null
@JsonMarker
@Suppress("ClassName", "JavaIoSerializableObjectMustHaveReadResolve")
object `null` : NullNode()

// value
fun <T> json(generator: () -> T): JsonNode = json(Transformers.JsonNode(), generator)
fun <T, U> json(transformer: Transformer<T, in JsonNode>, generator: () -> U): T {
    val node = when (val value = generator()) {
        `null` -> `null`
        is JsonNode -> value
        is Boolean -> json(value)
        is Enum<*> -> json(value)
        is String -> json(value)
        is Temporal -> json(value)
        is Number -> json(value)
        else -> JsonNodeFactory.pojoNode(value)
    }

    return transformer.process(GeneralJacksonNodeBuilder(node))
}

fun json(value: `null`): ValueNode = value
fun json(value: Boolean): ValueNode = JsonNodeFactory.booleanNode(value)
fun json(value: Enum<*>): ValueNode = JsonNodeFactory.textNode(value.name)
fun json(value: String): ValueNode = JsonNodeFactory.textNode(value)
fun json(value: Temporal): ValueNode = JsonNodeFactory.textNode(value.toString())
fun json(value: Duration): ValueNode = JsonNodeFactory.textNode(value.toString())
fun json(value: UUID): ValueNode = JsonNodeFactory.textNode(value.toString())
fun json(value: Number): ValueNode = when (value) {
    is Double -> JsonNodeFactory.numberNode(value.toDouble())
    is Float -> JsonNodeFactory.numberNode(value.toFloat())
    is Long -> JsonNodeFactory.numberNode(value.toLong())
    is Int -> JsonNodeFactory.numberNode(value.toInt())
    is Short -> JsonNodeFactory.numberNode(value.toShort())
    else -> JsonNodeFactory.numberNode(value.toByte())
}
