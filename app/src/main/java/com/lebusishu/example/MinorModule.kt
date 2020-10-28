package com.lebusishu.example

import android.app.Application
import android.content.Intent
import com.lebusishu.annotations.RouterModule
import com.lebusishu.annotations.RouterPath
import com.lebusishu.router.VPromise
import com.lebusishu.router.interfaces.IRouter


/**
 * Description : 类描述必填
 * Create by wxh on 2020/10/20
 * Phone ：15233620521
 * Email：wangxiaohui1118@gmail.com
 * Person in charge : lebusishu
 */
@RouterModule(scheme = "android", host = "minor")
class MinorModule : IRouter {

    @RouterPath
    fun def(application: Application, scheme: String, promise: VPromise) {
        promise.resolve("from scheme[:$scheme ] path is null")
    }

    @RouterPath("/activity/minorActivity")
    fun openMinorActivity(context: Application, promise: VPromise) {
        val tag = promise.getTag()
        val intent = Intent(context, MinorActivity::class.java)
        intent.putExtra("tag", tag)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}