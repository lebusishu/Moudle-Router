package com.lebusishu.router

import android.util.Log
import com.lebusishu.router.exceptions.PathNotFoundException
import com.lebusishu.router.utils.RouterValueParser
import java.lang.Exception
import java.lang.reflect.Method
import kotlin.jvm.Throws

/**
 * Description : Invoked by mirror impl
 * Create by wxh on 2020/10/15
 * Phone ：15233620521
 * Email：wangxiaohui1118@gmail.com
 * Person in charge : lebusishu
 * Leader：肖辉
 */
@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class ModuleDelegater {
    companion object {
        const val _METHOD = "_METHOD"
        const val _ARGS = "_ARGS"
        const val _TYPES = "_TYPES"

        @Throws(Exception::class)
        fun invoke(
            path: String,
            params: ParamsWrapper,
            target: Any,
            mapping: HashMap<String, Any>
        ) {
            val method = mapping[path + _METHOD] as Method?
                ?: throw PathNotFoundException("path:$path not found")
            val args = mapping[path + _ARGS] as String
            val types = mapping[path + _TYPES] as String
            if (args.isEmpty()) {
                autoReturn(
                    params,
                    method,
                    method.invoke(target)
                )
                return
            }
            if (!args.contains(",")) {
                val arr = arrayOfNulls<Any?>(1)
                arr[0] = RouterValueParser.parse(params.getValue(args), types)
                autoReturn(
                    params,
                    method,
                    method.invoke(target, *arr)
                )
                return
            }
            val argNames = args.split(",")
            val typeNames = types.split(",")
            val arr = arrayOfNulls<Any?>(argNames.size)
            argNames.forEachIndexed { index, s ->
                arr[index] =
                    RouterValueParser.parse(params.getValue(s), typeNames[index])
            }
            autoReturn(params, method, method.invoke(target, *arr))
        }

        private fun autoReturn(params: ParamsWrapper, method: Method, result: Any?) {
            val returnType = method.returnType.name
            if (returnType == "void") {
                (params.getValue("promise") as VPromise).resolve(Unit)
            } else {
                (params.getValue("promise") as VPromise).resolve(result)
            }
        }
    }
}