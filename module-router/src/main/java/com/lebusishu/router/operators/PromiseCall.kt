package com.lebusishu.router.operators

import com.lebusishu.router.Reject
import com.lebusishu.router.Resolve
import com.lebusishu.router.exceptions.RouterParseException
import com.lebusishu.router.utils.RouterReflectTool
import com.lebusishu.router.utils.RouterValueParser

/**
 * Description : 类描述必填
 * Create by wxh on 2020/10/20
 * Phone ：15233620521
 * Email：wangxiaohui1118@gmail.com
 * Person in charge : lebusishu
 */
class PromiseCall<T, R>(source: CPromise<T>) : AbstractSourcePromise<T, R>(source) {
    lateinit var func: Func<T, R>;

    constructor(source: CPromise<T>, func: Func<T, R>) : this(source) {
        this.func = func
    }

    override fun callActual(resolve: Resolve<R>?, reject: Reject?) {
        source.call(object : Resolve<Any> {
            override fun call(result: Any?) {
                try {
                    val reflect = RouterReflectTool.tryGetGeneric(func)
                    resolve?.call(func.call(RouterValueParser.parse(result,reflect) as T))
                } catch (e: RouterParseException) {
                    reject?.call(e)
                }
            }
        }, reject)
    }
}