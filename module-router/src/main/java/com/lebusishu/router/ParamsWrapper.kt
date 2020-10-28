package com.lebusishu.router

import com.lebusishu.router.exceptions.RouterParseException
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Description : Params wrapper.The middle data map between from and to.
 * Create by wxh on 2020/10/15
 * Phone ：15233620521
 * Email：wangxiaohui1118@gmail.com
 * Person in charge : lebusishu
 */
class ParamsWrapper {
    companion object {
        val _PARAMS_ = "_params_"
    }

    var result = HashMap<String, Any?>()

    constructor(params: Any?) {
        result = parseParamsToMap(params)
    }

    @Throws(JSONException::class)
    @Suppress("UNCHECKED_CAST")
    private fun parseParamsToMap(params: Any?): HashMap<String, Any?> {
        val map = HashMap<String, Any?>()
        if (params is Map<*, *>) {
            return params as HashMap<String, Any?>
        } else if (params is List<*>) {
            map[_PARAMS_] = params
        } else if (params is String) {
            val json = params.toString()
            if (json[0] == '[' && json[json.length - 1] == ']') {
                map[_PARAMS_] = JSONArray(json)
                return map
            }
            val obj = JSONObject(json)
            val iterable = obj.keys()
            while (iterable.hasNext()) {
                val key = iterable.next()
                var value = obj.get(key)
                if (value is Double || value is Float) {
                    value = value.toString()
                }
                map[key] = value
            }
        }
        return map
    }

    fun getValue(key: Any): Any? {
        if (key is String && _PARAMS_ == key) {
            return result
        }
        return result[key]
    }

    fun putValue(key: String, value: Any?) {
        result[key] = value
    }

    @Throws(JSONException::class)
    fun append(params: Any?) {
        result.putAll(parseParamsToMap(params))
    }
}