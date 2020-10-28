package com.lebusishu.example

import android.app.Application
import android.content.Intent
import android.os.Bundle
import com.lebusishu.annotations.RouterModule
import com.lebusishu.annotations.RouterPath
import com.lebusishu.router.VPromise
import com.lebusishu.router.interfaces.IRouter
import java.io.Serializable


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

    @RouterPath("/string/minorActivity")
    fun openMinorActivity(context: Application, param: String, promise: VPromise) {
        val tag = promise.getTag()
        val intent = Intent(context, MinorActivity::class.java)
        intent.putExtra("tag", tag)
        intent.putExtra("string", param)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    @RouterPath("/list/minorActivity")
    fun openMinorActivity(context: Application, list: ArrayList<String>, promise: VPromise) {
        val tag = promise.getTag()
        val intent = Intent(context, MinorActivity::class.java)
        intent.putExtra("tag", tag)
        intent.putStringArrayListExtra("list", list)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    @RouterPath("/bean/minorActivity")
    fun openMinorActivity(context: Application, bean: TestBean, promise: VPromise) {
        val tag = promise.getTag()
        val intent = Intent(context, MinorActivity::class.java)
        intent.putExtra("tag", tag)
        intent.putExtra("bean", bean)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    @RouterPath("/get/getData")
    fun openGetData(promise: VPromise) {
        promise.resolve(TestBean())
    }

    @RouterPath("/get/getValue")
    fun openGetValue(promise: VPromise) {
        promise.resolve(TestBean())
    }

    @RouterPath("/thread/getThread")
    fun openGetThread(promise: VPromise) {
        promise.resolve(Thread.currentThread())
    }

    @RouterPath("/autoReturn")
    fun openAutoReturn(promise: VPromise): String {
        return "auto return:${TestBean().toString()}"
    }
    @RouterPath("/complex/minorActivity")
    fun openComplexParams(
        application: Application,
        param: String,
        list: ArrayList<String>,
        bean: TestBean,
        promise: VPromise
    ) {
        val tag = promise.getTag()
        val intent = Intent(application, MinorActivity::class.java)
        intent.putExtra("tag", tag)
        intent.putExtra("string", param)
        intent.putStringArrayListExtra("list", list)
        intent.putExtra("bean", bean)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        application.startActivity(intent)
    }
}