package com.lebusishu.router.operators

import com.lebusishu.router.Reject
import com.lebusishu.router.Resolve

/**
 * Description : 类描述必填
 * Create by wxh on 2020/10/20
 * Phone ：15233620521
 * Email：wangxiaohui1118@gmail.com
 * Person in charge : lebusishu
 */
class PromiseMap<T, R>(source: CPromise<T>) : AbstractSourcePromise<T, R>(source) {
    lateinit var map: Func<T, R>

    constructor(source: CPromise<T>, map: Func<T, R>) : this(source) {
        this.map = map
    }

    override fun callActual(resolve: Resolve<R>?, reject: Reject?) {
        source.call(object : Resolve<T> {
            override fun call(result: T?) {
                try {
                    resolve!!.call(map.call(result))
                } catch (e: Exception) {
                    reject?.call(e)
                }
            }
        }, reject)
    }
}