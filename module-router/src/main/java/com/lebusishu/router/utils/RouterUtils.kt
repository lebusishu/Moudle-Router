package com.lebusishu.router.utils

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.lebusishu.router.VPromise
import com.lebusishu.router.interfaces.IMirror
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Description : project utils
 * Create by wxh on 2020/10/16
 * Phone ：15233620521
 * Email：wangxiaohui1118@gmail.com
 * Person in charge : lebusishu
 */
class RouterUtils() {
    companion object {
        val HANDLER = Handler(Looper.getMainLooper())
        val EXECUTOR =
            ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS, LinkedBlockingDeque<Runnable>())
        private val cacheCool = RouterCacheCool()
        val INSTANCE by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { RouterUtils() }
    }

    init {
        EXECUTOR.allowCoreThreadTimeOut(true)
    }

    fun popPromiseByTag(tag: String): VPromise? {
        return cacheCool.popPromise(tag)
    }

    fun removePromiseByTag(tag: String) {
        cacheCool.removePromiseByTag(tag)
    }

    fun addPromiseToPool(tag: String, promise: VPromise) {
        cacheCool.addPromiseToCool(tag, promise)
    }

    fun getMirrorByKey(key: String): IMirror? {
        return cacheCool.getMirrorByKey(key)
    }

    fun addMirrorToPool(key: String, mirror: IMirror) {
        cacheCool.addMirrorToCool(key, mirror)
    }

    fun buildPromiseTag(): String {
        val promisePoolSize = cacheCool.getPromisePoolSize()
        return "$promisePoolSize" + "_" + SystemClock.currentThreadTimeMillis()
    }

    fun getRealException(throwable: Throwable): Throwable {
        if (throwable is InvocationTargetException) {
            return getRealException(throwable.targetException)
        }
        return throwable
    }
}