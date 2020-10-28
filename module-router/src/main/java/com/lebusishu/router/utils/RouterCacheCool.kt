package com.lebusishu.router.utils

import android.util.LruCache
import com.lebusishu.router.VPromise
import com.lebusishu.router.interfaces.IMirror

/**
 * Description : Cache pool of {@link Promise} and {@link IMirror}
 * Create by wxh on 2020/10/16
 * Phone ：15233620521
 * Email：wangxiaohui1118@gmail.com
 * Person in charge : lebusishu
 */
class RouterCacheCool {
    private val promisePool = HashMap<String, VPromise>()
    private val mirrorPool = LruCache<String, IMirror>(20)

    fun addPromiseToCool(tag: String, promise: VPromise) {
        promisePool[tag] = promise
    }

    fun popPromise(tag: String): VPromise? {
        return promisePool.remove(tag)
    }

    fun removePromiseByTag(tag: String) {
        popPromise(tag)
    }

    fun addMirrorToCool(key: String, mirror: IMirror) {
        if (mirrorPool[key] == null) {
            mirrorPool.put(key, mirror)
        }
    }

    fun getMirrorByKey(key: String): IMirror? {
        return mirrorPool[key]
    }

    fun getPromisePoolSize(): Int {
        return promisePool.size
    }
}