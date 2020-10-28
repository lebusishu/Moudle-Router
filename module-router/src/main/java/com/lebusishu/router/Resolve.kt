package com.lebusishu.router

/**
 * Description : Third party call the result.
 * Create by wxh on 2020/10/15
 * Phone ：15233620521
 * Email：wangxiaohui1118@gmail.com
 * Person in charge : lebusishu
 */
interface Resolve<T> {
    /**
     * Note:Support different types transformation.eg:A cast to B.
     */
    fun call(result: T?)
}