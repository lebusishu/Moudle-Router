package com.lebusishu.annotations


/**
 * Description : Route protocol format
 * Create by wxh on 2020/10/13
 * Phone ：15233620521
 * Email：wangxiaohui1118@gmail.com
 * Person in charge : lebusishu
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
annotation class RouterModule(val scheme: String, val host: String) {

}