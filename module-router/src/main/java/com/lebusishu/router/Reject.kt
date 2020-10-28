package com.lebusishu.router

/**
 * Description : Third party call the error.
 * Create by wxh on 2020/10/15
 * Phone ：15233620521
 * Email：wangxiaohui1118@gmail.com
 * Person in charge : lebusishu
 */
interface Reject {
    /**
     *
     * Returns the error,Called in main thread.
     */
    fun call(e: Exception?)
}