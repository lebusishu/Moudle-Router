package com.lebusishu.annotations

/**
 * Description : Route protocol format
 * Create by wxh on 2020/10/13
 * Phone ：15233620521
 * Email：wangxiaohui1118@gmail.com
 * Person in charge : lebusishu
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class RouterPath (val value:String=""){

}