package com.lebusishu.router

import android.os.Looper
import com.lebusishu.router.exceptions.RouterParseException
import com.lebusishu.router.utils.RouterReflectTool
import com.lebusishu.router.utils.RouterUtils
import com.lebusishu.router.utils.RouterValueParser
import kotlin.Exception
import kotlin.properties.Delegates

/**
 * Description : Manage router send and receive.
 * Create by wxh on 2020/10/15
 * Phone ：15233620521
 * Email：wangxiaohui1118@gmail.com
 * Person in charge : lebusishu
 */
class Promise {
    companion object {
        /**
         * Call on main thread.{@link Promise#call(Resolve, Reject)}
         */
        val FLAG_CALL_MAIN = 1 shl 1

        /**
         * Call on thread.{@link Promise#call(Resolve, Reject)}
         */
        val FLAG_CALL_THREAD = 1 shl 2

        /**
         * return on main thread.
         *
         * {@link Promise#resolve(Object)} {@link Promise#reject(Exception)}}
         */
        val FLAG_RETURN_MIAN = 1 shl 3

        /**
         * return on thread.
         *
         * {@link Promise#resolve(Object)} {@link Promise#reject(Exception)}}
         */
        val FLAG_RETURN_THREAD = 1 shl 4
    }

    private var tagKey: String?=null
    private val promiseForReturn: VPromise
    private var flagMark = 0
    private val asker: Asker
    private var resolve: Resolve<Any>? = null
    private var reject: Reject? = null

    constructor(asker: Asker) {
        this.asker = asker
        this.promiseForReturn = VPromise(this)
        asker.setPromise(this)
    }

    /**
     * Send router. Receive success and fail.
     *
     * @param resolve {@link Promise}
     */
    fun call(resolve: Resolve<Any>?, reject: Reject?) {
        this.resolve = resolve
        this.reject = reject
        //call on main thread
        if ((flagMark and FLAG_CALL_MAIN) != 0 && !isMainThread()) {
            RouterUtils.HANDLER.post { asker.request() }
            //call on thread
        } else if ((flagMark and FLAG_CALL_THREAD) != 0) {
            RouterUtils.EXECUTOR.execute {
                asker.request()
            }
            //call on current thread
        } else {
            asker.request()
        }
    }

    fun resolve(result: Any) {
        if (result == Void::class.java) {
            return
        }
        if (resolve == null) {
            return
        }
        val expected: Any?
        try {
            val firstGeneric = RouterReflectTool.tryGetGeneric(resolve!!)
            expected = RouterValueParser.parse(result, firstGeneric)
        } catch (e: RouterParseException) {
            reject(e)
            return
        }
        val expectedResult: Any? = expected
        //call on main thread
        if ((flagMark and FLAG_RETURN_MIAN) != 0 && !isMainThread()) {
            RouterUtils.HANDLER.post {
                try {
                    resolve!!.call(expectedResult)
                } catch (e: Exception) {
                    reject(e)
                }
            }
        } else if ((flagMark and FLAG_RETURN_THREAD) != 0) {
            RouterUtils.EXECUTOR.execute {
                try {
                    resolve!!.call(expectedResult)
                } catch (e: Exception) {
                    reject(e)
                }
            }
        } else {
            try {
                resolve!!.call(expectedResult)
            } catch (e: Exception) {
                reject(e)
            }
        }
    }

    fun reject(e: Exception?) {
//        if (e == null) {
//            e = RouterException("unknown exception")
//        }
        if (reject == null) {
            return
        }
        //call on main thread
        if ((flagMark and FLAG_RETURN_MIAN) != 0 && !isMainThread()) {
            RouterUtils.HANDLER.post {
                reject!!.call(e)
            }
        } else if ((flagMark and FLAG_RETURN_THREAD) != 0) {
            RouterUtils.EXECUTOR.execute {
                reject!!.call(e)
            }
        } else {
            reject!!.call(e)
        }
    }

    private fun isMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }

    fun getVPromise(): VPromise {
        return promiseForReturn
    }

    fun setThreadFlag(flag: Int) {
        this.flagMark != flag
    }

    fun getTag(): String {
        if (tagKey==null|| tagKey!!.isEmpty()) {
            tagKey = RouterUtils.INSTANCE.buildPromiseTag()
            RouterUtils.INSTANCE.addPromiseToPool(tagKey!!, promiseForReturn)
        }
        return tagKey as String
    }

}