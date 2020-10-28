package com.lebusishu.router.operators

/**
 * Description : Abstract base class for operators that take an upstream source
 * Create by wxh on 2020/10/20
 * Phone ：15233620521
 * Email：wangxiaohui1118@gmail.com
 * Person in charge : lebusishu
 */
abstract class AbstractSourcePromise<T, R>
/**
 * Constructs a CPromiseSource wrapping the given non-null source CPromise.
 *
 * @param source the source CPromise instance, not null
 */(
    var source: CPromise<T>
) : CPromise<R>(), SourcePromise<T> {

    override fun source(): CPromise<T> {
        return source
    }
}