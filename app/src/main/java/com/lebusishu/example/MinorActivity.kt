package com.lebusishu.example

import android.os.Bundle
import android.text.TextUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.lebusishu.example.R

/**
 * Description : 类描述必填
 * Create by wxh on 2020/10/20
 * Phone ：15233620521
 * Email：wangxiaohui1118@gmail.com
 * Person in charge : lebusishu
 */
class MinorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_minor)
        init()
    }

    private fun init() {
        val builder = StringBuffer()
        val tv = findViewById<TextView>(R.id.tv0)
        val param = intent.getStringExtra("string")
        if (!TextUtils.isEmpty(param)) {
           builder.append(param).append("\n")
        }
        val list = intent.getStringArrayListExtra("list")
        if (list != null) {
            for (l in list) {
                builder.append(l).append("\n");
            }
        }
        val bean = intent.getSerializableExtra("bean") as TestBean?
        if (bean != null) {
            builder.append(bean.a).append("\n");
            builder.append(bean.b).append("\n");
            for (l in bean.c) {
                builder.append(l).append("\n")
            }
        }
        tv.text = builder.toString()
    }
}