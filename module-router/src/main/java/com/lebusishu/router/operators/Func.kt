package com.lebusishu.router.operators

/**
 * Description : All Func extend from this.
 * Create by wxh on 2020/10/16
 * Phone ：15233620521
 * Email：wangxiaohui1118@gmail.com
 * Person in charge : lebusishu
 */
interface Func<T, R> {
    /**
     * Apply some calculation to the input value and return some other value.
     *
     * @param t the input value
     * @return the output value
     */
    fun call(t: T?): R?
}