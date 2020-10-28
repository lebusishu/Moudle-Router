package com.lebusishu.router

import com.lebusishu.router.operators.CPromise
import com.lebusishu.router.utils.RouterUtils

/**
 * Description : Module Router Facade:Protocol Format: scheme://host/path?params=json
 * Create by wxh on 2020/10/20
 * Phone ：15233620521
 * Email：wangxiaohui1118@gmail.com
 * Person in charge : lebusishu
 * Leader：肖辉
 */
class ModuleRouter {
    companion object {
        /**
         * Open url, usually invoked externally. like from browser.
         *
         * @param url scheme://host/path?params=json
         * @param <R> the output type
         * @return {@link CPromise}
         */
        fun <R> open(url: String): CPromise<R> {
            val promise = Promise(Asker(url))
            return CPromise(promise)
        }

        /**
         * Open url, usually invoked by inner.
         *
         * @param url scheme://host/path
         * @param <R> the output type
         * @return {@link CPromise}
         */
        fun <R> open(url: String, append: String): CPromise<R> {
            val promise = Promise(Asker(url, append))
            return CPromise(promise)
        }

        /**
         * Open url, usually invoked by inner.
         *
         * @param url scheme://host/path
         * @param <R> the output type
         * @return {@link CPromise}
         */
        fun <R> open(url: String, append: Map<String, Any>): CPromise<R> {
            val promise = Promise(Asker(url, append))
            return CPromise(promise)
        }

        /**
         * Open url, usually invoked by inner.
         *
         * @param url scheme://host/path
         * @param <R> the output type
         * @return {@link CPromise}
         */
        fun <R> open(url: String, append: List<Any>): CPromise<R> {
            val promise = Promise(Asker(url, append))
            return CPromise(promise)
        }

        /**
         * Usually invoked inner, Empty params.
         *
         * @param scheme The scheme of protocol
         * @param host   The host of protocol
         * @param path   The path of protocol
         * @param <R>    the output type
         * @return {@link CPromise}
         */
        fun <R> open(scheme: String, host: String, path: String): CPromise<R> {
            val promise = Promise(Asker(scheme, host, path, null))
            return CPromise(promise)
        }

        /**
         * Usually invoked inner.
         *
         * @param scheme The scheme of protocol
         * @param host   The host of protocol
         * @param path   The path of protocol
         * @param params The jsonObject params of protocol
         * @param <R>    the output type
         * @return {@link CPromise}
         */
        fun <R> open(
            scheme: String,
            host: String,
            path: String,
            params: Map<String, Any>
        ): CPromise<R> {
            val promise = Promise(Asker(scheme, host, path, params))
            return CPromise(promise)
        }

        /**
         * Usually invoked inner.
         *
         * @param scheme The scheme of protocol
         * @param host   The host of protocol
         * @param path   The path of protocol
         * @param params The jsonArray params of protocol
         * @param <R>    the output type
         * @return {@link CPromise}
         */
        fun <R> open(scheme: String, host: String, path: String, params: List<Any>): CPromise<R> {
            val promise = Promise(Asker(scheme, host, path, params))
            return CPromise(promise)
        }

        /**
         * Usually invoked inner.
         *
         * @param scheme The scheme of protocol
         * @param host   The host of protocol
         * @param path   The path of protocol
         * @param json   The json params of protocol
         * @param <R>    the output type
         * @return {@link CPromise}
         */
        fun <R> open(scheme: String, host: String, path: String, json: String): CPromise<R> {
            val promise = Promise(Asker(scheme, host, path, json))
            return CPromise(promise)
        }

        /**
         * Find from cache pool
         *
         * @param tag The tag of {@link Promise}
         * @return {@link VPromise} if not find, return null.
         */
        fun findPromiseByTag(tag: String?): VPromise? {
            if (tag == null || tag.isEmpty()) {
                return null
            }
            return RouterUtils.INSTANCE.popPromiseByTag(tag)
        }

        /**
         * Remove from cache pool
         *
         * @param tag The tag of {@link Promise}
         */
        fun removePromiseByTag(tag: String) {
            RouterUtils.INSTANCE.removePromiseByTag(tag)
        }
    }
}