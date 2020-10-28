package com.lebusishu.router

import android.net.Uri
import com.lebusishu.router.exceptions.RouterException
import com.lebusishu.router.exceptions.RouterNotFoundException
import com.lebusishu.router.exceptions.RouterRemoteException
import com.lebusishu.router.interfaces.IMirror
import com.lebusishu.router.utils.RouterReflectTool
import com.lebusishu.router.utils.RouterUtils
import org.json.JSONException
import java.lang.reflect.InvocationTargetException
import java.net.URLDecoder
import java.util.*

/**
 * Description : Parse url and invoke
 * Create by wxh on 2020/10/15
 * Phone ：15233620521
 * Email：wangxiaohui1118@gmail.com
 * Person in charge : lebusishu
 */
class Asker {
    companion object {
        val MIRROR_PREFIX = "com.lebusishu.router."+BuildConfig.AUTO_PREFIX
        val URL_PARAMS = "?params="
    }

    private lateinit var promise: Promise
    private var scheme: String = ""
    private var host: String = ""
    private var path: String? = null
    private var params: Any? = null
    private var appendParams: Any? = null
    private var _e: Exception? = null

    constructor(urlWithParams: String) {
        parseUrl(urlWithParams)
    }

    constructor(url: String, appendParams: Any) {
        parseUrl(url)
        this.appendParams = appendParams
    }

    constructor(scheme: String?, host: String?, path: String?, params: Any?) {
        if (scheme == null || scheme.isEmpty() || host == null || host.isEmpty()) {
            reject(RouterException("scheme or host is empty"))
            return
        }
        this.scheme = scheme
        this.host = host.toLowerCase(Locale.ROOT)
        this.path = if (path == null || path.isEmpty()) "" else path
        this.params = params
    }

    private fun parseUrl(url: String?) {
        try {
            if (url == null || url.isEmpty()) {
                reject(RouterNotFoundException(" url is empty"))
                return
            }
            val decodeUrl = URLDecoder.decode(url, "utf-8")
            val uri = Uri.parse(decodeUrl)
            scheme = uri.scheme.toString()
            host = uri.host.toString().toLowerCase(Locale.ROOT)
            path = if (uri.path == null) "" else uri.path.toString().toLowerCase(Locale.ROOT)
            val s = uri.toString()
            val index = s.indexOf(URL_PARAMS)
            if (index != -1) {
                params = s.substring(index + URL_PARAMS.length, s.length)
            }
        } catch (e: Exception) {
            reject(RouterNotFoundException("invaild router url :$url", e))
        }
    }

    fun request() {
        if (_e == null) {
            searchAndInvoke()
            return
        }
        reject(_e!!)
    }

    private fun searchAndInvoke() {
        val mirror = MIRROR_PREFIX + scheme + "_" + host
        try {
            val clazz = Class.forName(mirror)
            val method = clazz.getMethod("invoke", String::class.java, ParamsWrapper::class.java)
            val target =
                RouterUtils.INSTANCE.getMirrorByKey(mirror) ?: clazz.newInstance() as IMirror
            method.invoke(target, path, createParamsWrapper(params))
        } catch (e: ClassNotFoundException) {
            reject(RouterNotFoundException("invalid router url:" + getUrl()))
        } catch (e: InvocationTargetException) {
            reject(RouterRemoteException(RouterUtils.INSTANCE.getRealException(e)))
        } catch (e: Exception) {
            reject(e)
        }
    }

    @Throws(JSONException::class)
    private fun createParamsWrapper(params: Any?): ParamsWrapper {
        val wrapper = ParamsWrapper(params)
        wrapper.putValue("scheme", scheme)
        wrapper.putValue("promise", promise.getVPromise())
        if (wrapper.getValue("context") == null) {
            wrapper.putValue("context", RouterReflectTool.getApplication())
        }
        if (wrapper.getValue("application") == null) {
            wrapper.putValue("application", RouterReflectTool.getApplication())
        }
        if (appendParams != null) {
            wrapper.append(appendParams)
        }
        return wrapper
    }

    private fun reject(e: Exception) {
        promise.reject(e)
    }

    fun setPromise(promise: Promise) {
        this.promise = promise
    }

    private fun getUrl(): String {
        val param = if (params == null) "" else URL_PARAMS + params.toString()
        return "$scheme://$host$path$param"
    }
}