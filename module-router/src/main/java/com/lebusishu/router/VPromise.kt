package com.lebusishu.router

import java.lang.Exception

/**
 * Description : Proxy of {@link Promise}
 * Create by wxh on 2020/10/15
 * Phone ：15233620521
 * Email：wangxiaohui1118@gmail.com
 * Person in charge : lebusishu
 */
class VPromise {
     var target: Promise

    constructor(target: Promise) {
        this.target = target
    }

    /**
     * return result
     *
     * @param result The result of whatever you want.
     * @param <R>    the output type
     */
    fun resolve(any: Any?) {
        target.resolve(any)
    }

    /**
     * return exception
     */
    fun reject(e: Exception) {
        target.reject(e)
    }

    fun getTag(): String {
        return target.getTag()
    }
}