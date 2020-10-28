package com.lebusishu.router.operators

import com.lebusishu.router.Promise
import com.lebusishu.router.Reject
import com.lebusishu.router.Resolve
import com.lebusishu.router.exceptions.RouterRemoteException
import com.lebusishu.router.interfaces.TypeCase
import com.lebusishu.router.utils.RouterReflectTool
import com.lebusishu.router.utils.RouterUtils
import com.lebusishu.router.utils.RouterValueParser
import java.util.concurrent.CountDownLatch

/**
 * Description : Proxy of {@link Promise}
 * Create by wxh on 2020/10/16
 * Phone ：15233620521
 * Email：wangxiaohui1118@gmail.com
 * Person in charge : lebusishu
 */
open class CPromise<T> {
    private lateinit var target: Promise

    constructor(target: Promise) : this() {
        this.target = target
    }

    constructor() {
    }

    /**
     * Start route.Empty callback
     */
    fun <R> call() {
        call(null as Resolve<R>, null)
    }

    /**
     * Start route.
     *
     * @param resolve Result callback
     * @param <R>     the output type
     */
    fun <R : Any> call(resolve: Resolve<R>) {
        call(resolve, null)
    }

    /**
     * Start route
     *
     * @param reject Exception callback
     */
    fun <R> call(reject: Reject) {
        call(null as Resolve<R>, reject)
    }

    /**
     * Start route
     *
     * @param resolve Result callback
     * @param reject  Exception callback
     * @param <R>     the output type
     */
    fun <R> call(resolve: Resolve<R>?, reject: Reject?) {
        target.call(resolve as Resolve<Any>, reject)
    }

    /**
     * This method support reactive process.
     *
     * @param func a function to apply to each item emitted by the Publisher
     * @param <W>  the result type of remote.
     * @param <R>  the output type
     * @return a CPromise that emits the items from the source, transformed by the specified.
     */
    fun <W, R> call(func: Func<W, R>): CPromise<R> {
        return PromiseCall(CPromise<W>(target), func)
    }

    /**
     * Await the result returned.It will be block thread.
     *
     * @return result
     * @see CPromise#getValue(TypeCase, Reject)
     */
    fun <R> getValue(): R? {
        return getValue(null, null)
    }

    /**
     * Await the result returned.It will be block thread.
     *
     * @return result
     * @see CPromise#getValue(TypeCase, Reject reject)
     */
    fun <R> getValue(reject: Reject): R? {
        return getValue(null, reject)
    }

    /**
     * Await the result returned.It will be block thread.
     *
     * @return result
     * @see CPromise#getValue(TypeCase, Reject reject)
     */
    fun <R> getValue(type: TypeCase<R>): R? {
        return getValue(type, null)
    }

    /**
     * Await the result returned.It will be block thread.
     *
     * @param type   {@link TypeCase}
     * @param reject {@link Reject}
     * @return result Note:Didn't support different types cast.
     */
    fun <R> getValue(type: TypeCase<R>?, reject: Reject?): R? {
        val array: Array<Any?> = Array(1) { Any() }
        val latch = CountDownLatch(1)
        target.call(object : Resolve<Any> {
            override fun call(result: Any?) {
                if (result != Void::class.java) {
                    array[0] = result
                }
                latch.countDown()
            }

        }, object : Reject {
            override fun call(e: Exception?) {
                array[0] = e
                latch.countDown()
            }
        })
        var result: R? = array[0] as R
        try {
            latch.await()
            if (result is Throwable) {
                throw result
            }
            if (type != null) {
                result = RouterValueParser.parse(result, RouterReflectTool.tryGetGeneric(type)) as R
            }
        } catch (e: Throwable) {
            result = null
            reject?.call(RouterRemoteException(RouterUtils.INSTANCE.getRealException(e)))
        }
        return result
    }

    /**
     * Call on main thread.
     *
     * @return {@link CPromise}
     */
    fun callOnMainThread(): CPromise<T> {
        target.setThreadFlag(Promise.FLAG_CALL_MAIN)
        return this
    }

    /**
     * Call on Sub-thread.
     *
     * @return {@link CPromise}
     */
    fun callOnSubThread(): CPromise<T> {
        target.setThreadFlag(Promise.FLAG_CALL_THREAD)
        return this
    }

    /**
     * Callback on main thread.
     *
     * @return {@link CPromise}
     */
    fun returnOnMainThread(): CPromise<T> {
        target.setThreadFlag(Promise.FLAG_RETURN_MIAN)
        return this
    }

    /**
     * Callback on Sub-thread.
     *
     * @return {@link CPromise}
     */
    fun returnOnSubThread(): CPromise<T> {
        target.setThreadFlag(Promise.FLAG_RETURN_THREAD)
        return this
    }

    fun <R> then(func: Func<T, R>): CPromise<R> {
        return PromiseMap<T, R>(this, func)
    }

    fun done() {
        done(null, null)
    }

    fun done(reject: Reject) {
        done(null, reject)
    }

    fun done(resolve: Resolve<T>) {
        done(resolve, null)
    }

    fun done(resolve: Resolve<T>?, reject: Reject?) {
        callActual(resolve, reject)
    }

    open fun callActual(resolve: Resolve<T>?, reject: Reject?) {

    }
}