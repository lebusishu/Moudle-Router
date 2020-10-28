package com.lebusishu.router.operators

/**
 * Description : Interface indicating the implementor has an upstream CPromise-like source available via {@link
 * source()} method.
 * Create by wxh on 2020/10/16
 * Phone ：15233620521
 * Email：wangxiaohui1118@gmail.com
 * Person in charge : lebusishu
 */
interface SourcePromise<T> {
    /**
     * Returns the source CPromise.
     *
     * @return the source CPromise-like
     */
    fun source(): CPromise<T>
}