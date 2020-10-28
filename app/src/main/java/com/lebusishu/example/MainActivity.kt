package com.lebusishu.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.lebusishu.example.R
import com.lebusishu.router.ModuleRouter
import com.lebusishu.router.Reject
import com.lebusishu.router.Resolve

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_minor)
        initView()
    }

    private fun initView() {
        val tv1 = findViewById<TextView>(R.id.tv_1)
        val tv2 = findViewById<TextView>(R.id.tv_2)
//        val tv3 = findViewById<TextView>(R.id.tv_3)
//        val tv4 = findViewById<TextView>(R.id.tv_4)
//        val tv5 = findViewById<TextView>(R.id.tv_5)
//        val tv6 = findViewById<TextView>(R.id.tv_6)
//        val tv7 = findViewById<TextView>(R.id.tv_7)
//        val tv8 = findViewById<TextView>(R.id.tv_8)
//        val tv9 = findViewById<TextView>(R.id.tv_9)
//        val tv10 = findViewById<TextView>(R.id.tv_10)
        tv1.text = "android://minor"
        tv1.setOnClickListener {
            ModuleRouter.open<Any>(tv1.text.toString()).call(object : Resolve<Any> {
                override fun call(result: Any?) {
                    Toast.makeText(this@MainActivity, result.toString(), Toast.LENGTH_SHORT).show()
                }

            }, object : Reject {
                override fun call(e: Exception?) {
                    e?.printStackTrace()
                }

            })
        }
        tv2.text = "android://minor/activity/minorActivity"
        tv2.setOnClickListener {
            ModuleRouter.open<Any>(tv2.text.toString()).call(object : Resolve<Any> {
                override fun call(result: Any?) {
                    tv2.text = result.toString()
                }
            }, object : Reject {
                override fun call(e: Exception?) {
                    e?.printStackTrace()
                }
            })
        }
    }
}