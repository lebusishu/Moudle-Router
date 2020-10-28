package com.lebusishu.router.utils

import com.lebusishu.router.ParamsWrapper
import com.lebusishu.router.exceptions.RouterParseException
import com.lebusishu.router.interfaces.IRouter
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Array

/**
 * Description : Parse tool parameter, from type to expected type.
 * Create by wxh on 2020/10/15
 * Phone ：15233620521
 * Email：wangxiaohui1118@gmail.com
 * Person in charge : lebusishu
 */
@Suppress("UNCHECKED_CAST")
class RouterValueParser {
    companion object {
        @Throws(RouterParseException::class)
        fun parse(from: Any?, expectedType: String?): Any? {
            if (expectedType == null) {
                return from
            }
            val result = from ?: Any()
            if (expectedType == "int" || expectedType == "java.lang.Integer") {
                return toInteger(result)
            }
            if ("boolean" == expectedType || "java.lang.Boolean" == expectedType) {
                return toBoolean(result)
            }
            if (expectedType == "long" || expectedType == "java.lang.Long") {
                return toLong(result)
            }
            if (expectedType == "double" || expectedType == "java.lang.Double") {
                return toDouble(result)
            }
            if (expectedType == "float" || expectedType == "java.lang.Float") {
                return toFloat(result)
            }
            if (expectedType == "[") {
                return toArray(result, expectedType)
            }
            if (expectedType.contains("java.util.List") || expectedType.contains("java.util.ArrayList")) {
                return toList(result, expectedType)
            }
            if (expectedType.contains("java.util.Map") || expectedType.contains("java.util.HashMap")) {
                return toMap(result, expectedType)
            }
            if (expectedType != "java.lang.Object") {
                return toTargetObj(result, expectedType)
            }
            return result
        }

        private fun toInteger(any: Any?, defVal: Int = 0): Int {
            if (any is Int) {
                return any
            } else if (any is Number) {
                return any.toInt()
            } else if (any is String) {
                try {
                    return any.toDouble().toInt()
                } catch (e: NumberFormatException) {

                }
            }
            return defVal
        }

        private fun toBoolean(any: Any?, defVal: Boolean = false): Boolean {
            if (any is Boolean) {
                return any
            } else if (any is String) {
                return "true".equals(any, true)
            }
            return defVal
        }

        private fun toLong(any: Any?, defVal: Long = 0): Long {
            if (any is Long) {
                return any
            } else if (any is Number) {
                return any.toLong()
            } else if (any is String) {
                try {
                    return any.toDouble().toLong()
                } catch (e: NumberFormatException) {

                }
            }
            return defVal
        }

        private fun toDouble(any: Any?, defVal: Double = 0.0): Double {
            if (any is Double) {
                return any
            } else if (any is Number) {
                return any.toDouble()
            } else if (any is String) {
                try {
                    return any.toDouble()
                } catch (e: NumberFormatException) {

                }
            }
            return defVal
        }

        private fun toFloat(any: Any?, defVal: Float = 0f): Float {
            if (any is Float) {
                return any
            } else if (any is Number) {
                return any.toFloat()
            } else if (any is String) {
                try {
                    return any.toDouble().toFloat()
                } catch (e: NumberFormatException) {

                }
            }
            return defVal
        }

        @Throws(RouterParseException::class)
        private fun toArray(any: Any?, expectedType: String): Any? {
            try {

                val jarray: JSONArray
                if (any is String) {
                    if (isJson(any)) {
                        jarray = JSONArray(any)
                    } else {
                        throw RouterParseException("Expected $expectedType ,But The input string isn't json.")
                    }
                    val type = getExpectedArrayType(expectedType)
                    val size = jarray.length()
                    val target = newArray(type, size)
                    for (i in 0 until size) {
                        setArray(target, type, i, jarray.get(i))
                    }
                    return target
                } else if (any != null && any::class.java.isArray && any::class.java.canonicalName != expectedType) {
                    val origin: kotlin.Array<Any> = any as kotlin.Array<Any>
                    val type = getExpectedArrayType(expectedType)
                    val first = if (origin.isEmpty()) null else origin[0]
                    if (first != null && first::class.java.canonicalName != type) {
                        val target = newArray(type, origin.size)
                        for (i in origin.indices) {
                            setArray(target, type, i, origin[i])
                        }
                        return target
                    }
                }

            } catch (e: Exception) {
                throw RouterParseException("parse to $expectedType type fail.")
            }
            return any
        }

        @Throws(RouterParseException::class)
        private fun toList(any: Any?, type: String): Any? {
            try {
                if (any is String || any is JSONArray) {
                    val jarray: JSONArray = if (any is String) JSONArray(any) else any as JSONArray
                    val generic = getListGeneric(type)
                    val size = jarray.length()
                    val list = ArrayList<Any?>(size)
                    for (i in 0 until size) {
                        val a = jarray[i]
                        list.add(parse(a, generic))
                    }
                    return list
                } else if (any is List<*>) {
                    val origin: List<*> = any
                    val generic = getListGeneric(type)
                    if (origin.isNotEmpty() && origin[0]!!::class.java.canonicalName.equals(
                            generic,
                            true
                        )
                    ) {
                        val target = ArrayList<Any?>(origin.size)
                        for (item in origin) {
                            target.add(parse(item, generic))
                        }
                        return target
                    }
                } else if (any is Map<*, *>) {
                    val params = any[ParamsWrapper._PARAMS_]
                    return parse(params, type)
                }
            } catch (e: Exception) {
                throw RouterParseException("parse to $type type fail.", e)
            }
            return any
        }

        @Throws(RouterParseException::class)
        private fun toMap(any: Any?, type: String): Any? {
            try {
                if (any is String || any is JSONObject) {
                    if (any !is JSONObject) {
                        if (!isJson(any.toString())) {
                            throw RouterParseException("Expected $type,But The input string isn't json.")
                        }
                    }
                    val map = HashMap<String, String>()
                    val iterable = (any as JSONObject).keys()
                    while (iterable.hasNext()) {
                        val key = iterable.next()
                        map[key] = any[key].toString()
                    }
                    return map
                }
            } catch (e: Exception) {
                throw RouterParseException("parse to $type type fail.", e)
            }
            return any
        }

        @Throws(RouterParseException::class)
        private fun toTargetObj(any: Any?, type: String): Any? {
            if (any == null) {
                return any
            }
            if (any is String || any is JSONObject) {
                parseJsonToTarget(any, type)
            } else if (any is Map<*, *>) {
                parseMapToTarget(any, type)
            } else if (canToParse(any, type) && type.equals(
                    any::class.java.canonicalName,
                    true
                )
            ) {
                parseObjToTarget(any, type)
            }
            return any
        }

        private fun canToParse(any: Any?, type: String): Boolean {
            try {
                if (any !is IRouter) {
                    return false
                }
                val interfaces = Class.forName(type).interfaces
                if (interfaces.isEmpty()) {
                    return false
                }
                for (item in interfaces) {
                    if (item.canonicalName == IRouter::class.java.canonicalName) {
                        return true
                    }
                }
                return false
            } catch (e: Exception) {
                return false
            }
        }

        @Throws(RouterParseException::class)
        private fun parseJsonToTarget(any: Any?, expectType: String): Any? {
            try {
                if (!isJson(any.toString())) {
                    return any
                }
                val obj = if (any is String) JSONObject(any) else any as JSONObject
                var clazz = Class.forName(getNoGenericTypeName(expectType))
                val target = clazz.newInstance()
                do {
                    val fields = target::class.java.declaredFields
                    for (field in fields) {
                        val name = field.name
                        val type = RouterReflectTool.getFieldTypeWithGeneric(field)
                        field.isAccessible = true
                        field.set(target, parse(obj.get(name), type))
                    }
                    clazz = clazz.superclass
                } while (clazz != Object::class.java)
            } catch (e: Exception) {
                throw RouterParseException("parse to $expectType type fail.", e)
            }
            return any
        }

        @Throws(RouterParseException::class)
        private fun parseMapToTarget(any: Any?, expectType: String): Any? {
            try {
                var clazz = Class.forName(getNoGenericTypeName(expectType))
                val target = clazz.newInstance()
                val map = any as Map<*, *>
                do {
                    val fields = target::class.java.declaredFields
                    for (field in fields) {
                        val name = field.name
                        val type = RouterReflectTool.getFieldTypeWithGeneric(field)
                        val value = map[name]
                        field.isAccessible = true
                        field.set(target, parse(value, type))
                    }
                    clazz = clazz.superclass
                } while (clazz != Object::class.java)
            } catch (e: Exception) {
                throw RouterParseException("parse to $expectType type fail.", e)
            }
            return any
        }

        @Throws(RouterParseException::class)
        private fun parseObjToTarget(any: Any?, expectType: String): Any? {
            try {
                var clazz = Class.forName(getNoGenericTypeName(expectType))
                val target = clazz.newInstance()
                val map = extractKeyValue(any)
                do {
                    val fields = target::class.java.declaredFields
                    for (field in fields) {
                        field.isAccessible = true
                        val name = field.name
                        val value = map[name]
                        val type = RouterReflectTool.getFieldTypeWithGeneric(field)
                        field.set(target, parse(value, type))
                    }
                    clazz = clazz.superclass
                } while (clazz != Object::class.java)
            } catch (e: Exception) {
                throw RouterParseException("parse to $expectType type fail.", e)
            }
            return any
        }

        @Throws(IllegalArgumentException::class)
        private fun extractKeyValue(any: Any?): Map<String, Any?> {
            val keys = HashMap<String, Any?>()
            if (any == null) {
                return keys
            }
            var clazz = any::class.java
            do {
                val fields = clazz.declaredFields
                for (filed in fields) {
                    filed.isAccessible = true
                    keys.put(filed.name, filed.get(any))
                }
                clazz = clazz.superclass as Class<out Any>
            } while (clazz != Object::class.java)
            return keys
        }

        private fun isJson(string: String): Boolean {
            return (string.contains("{") && string.endsWith("}"))
                    || (string.contains("[") && string.endsWith(
                "]"
            ))
        }

        private fun getExpectedArrayType(type: String): String {
            if (type.contains("[]")) {
                return type.replace("[]", "")
            }
            return type
        }

        private fun getNoGenericTypeName(className: String): String {
            val index = className.indexOf("<")
            if (index != -1) {
                return className.substring(0, index)
            }
            return className
        }

        private fun getListGeneric(type: String): String {
            return if (type.contains("<")) type.substring(
                type.indexOf("<") + 1,
                type.indexOf(">")
            ) else ""
        }

        @Throws(ClassNotFoundException::class)
        private fun newArray(type: String, length: Int): Any {
            val arr: Any
            if ("int" == type) {
                arr = IntArray(length)
            } else if ("long" == type) {
                arr = LongArray(length)
            } else if ("boolean" == type) {
                arr = BooleanArray(length)
            } else if ("double" == type) {
                arr = DoubleArray(length)
            } else if ("float" == type) {
                arr = FloatArray(length)
            } else if ("java.lang.String" == type) {
                arr = arrayOfNulls<String>(length)
            } else {
                arr = Array.newInstance(Class.forName(type), length)
            }
            return arr
        }

        @Throws(RouterParseException::class)
        private fun setArray(any: Any, type: String, index: Int, value: Any) {
            if ("int" == type) {
                Array.setInt(any, index, parse(value, type) as Int)
            } else if ("boolean" == type) {
                Array.setBoolean(any, index, parse(value, type) as Boolean)
            } else if ("long" == type) {
                Array.setLong(any, index, parse(value, type) as Long)
            } else if ("double" == type) {
                Array.setDouble(any, index, parse(value, type) as Double)
            } else if ("float" == type) {
                Array.setFloat(any, index, parse(value, type) as Float)
            } else {
                Array.set(any, index, parse(value, type))
            }
        }
    }
}