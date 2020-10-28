package com.lebusishu.example

import com.lebusishu.router.interfaces.IRouter
import java.io.Serializable

/**
 * Description : 测试对象
 * Create by wxh on 2020/10/28
 * Phone ：15233620521
 * Email：wangxiaohui1118@gmail.com
 * Person in charge : lebusishu
 */
class TestBean : IRouter,Serializable {
    var a = 1
    var b = "123"
    var c = arrayListOf(4, 5, 6)
    override fun toString(): String {
        return "TestBean(a=$a, b='$b', c=$c)"
    }

}