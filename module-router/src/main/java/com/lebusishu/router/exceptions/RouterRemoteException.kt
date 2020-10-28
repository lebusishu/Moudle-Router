package com.lebusishu.router.exceptions

/**
 * Description : v
 * Create by wxh on 2020/10/15
 * Phone ：15233620521
 * Email：wangxiaohui1118@gmail.com
 * Person in charge : lebusishu
 */
class RouterRemoteException : Exception {
    var code: Int = 0

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
    constructor(
        message: String?,
        cause: Throwable?,
        enableSuppression: Boolean,
        writableStackTrace: Boolean
    ) : super(message, cause, enableSuppression, writableStackTrace)

    constructor(code: Int) : super() {
        this.code = code
    }

    constructor(code: Int, message: String?) : super(message) {
        this.code = code
    }

    constructor(code: Int, message: String?, cause: Throwable?) : super(message, cause) {
        this.code = code
    }
}