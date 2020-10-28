package com.lebusishu.router.utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import java.lang.Exception
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

/**
 * Description : reflect tool method
 * Create by wxh on 2020/10/16
 * Phone ：15233620521
 * Email：wangxiaohui1118@gmail.com
 * Person in charge : lebusishu
 */
class RouterReflectTool {
    companion object {
        private lateinit var application: Context

        /**
         * Get Application by reflect
         *
         * @return Application
         */
        @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
        @JvmName("getApplication1")
        fun getApplication(): Context? {
            try {
                val clazz = Class.forName("android.app.ActivityThread")
                val method: Method = clazz.getDeclaredMethod("currentActivityThread")
                method.isAccessible = true
                val obj = method.invoke(null)
                val field = clazz.getDeclaredField("mInitialApplication")
                field.isAccessible = true
                application = field.get(obj) as Application
                return application
            } catch (e: Exception) {
                return null
            }
        }

        /**
         * Try to get generic of object.
         *
         * @param t   any object.
         * @param <T> input type
         * @return generic type
         */
        fun tryGetGeneric(any: Any): String? {
            var pt: ParameterizedType? = null
            val types = any.javaClass.genericInterfaces
            if (types.isNotEmpty() && types[0] is ParameterizedType) {
                pt = types[0] as ParameterizedType
            } else {
                val type = any.javaClass.genericSuperclass
                if (type is ParameterizedType) {
                    pt = type
                }
            }
            if (pt == null) {
                return null
            }
            val actual = pt.actualTypeArguments
            val unsafe = actual[0].toString()
            val genericString: String
            if (unsafe.contains(",")) {
                genericString = unsafe
            } else {
                val elms = unsafe.split(" ")
                genericString = if (elms.size <= 1) unsafe else elms[1]
            }
            return if (genericString.length < 4) null else genericString
        }

        fun getFieldTypeWithGeneric(field: Field): String? {
            val fieldType = field.type
            val type = fieldType.canonicalName
            if (fieldType.isAssignableFrom(List::class.java)) {
                val pt = field.genericType as ParameterizedType
                return pt.toString()
            }
            return type
        }

    }
}