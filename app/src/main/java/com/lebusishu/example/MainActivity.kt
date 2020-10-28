package com.lebusishu.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.lebusishu.example.R
import com.lebusishu.router.ModuleRouter
import com.lebusishu.router.Reject
import com.lebusishu.router.Resolve
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {
        val tv = findViewById<TextView>(R.id.tv)
        val tv1 = findViewById<TextView>(R.id.tv_1)
        val tv2 = findViewById<TextView>(R.id.tv_2)
        val tv3 = findViewById<TextView>(R.id.tv_3)
        val tv4 = findViewById<TextView>(R.id.tv_4)
        val tv5 = findViewById<TextView>(R.id.tv_5)
        val tv6 = findViewById<TextView>(R.id.tv_6)
        val tv7 = findViewById<TextView>(R.id.tv_7)
        val tv8 = findViewById<TextView>(R.id.tv_8)
        val tv9 = findViewById<TextView>(R.id.tv_9)
        val tv10 = findViewById<TextView>(R.id.tv_10)
        //默认调用
        tv1.text = "android://minor"
        tv1.setOnClickListener {
            ModuleRouter.open<Any>(tv1.text.toString()).call(object : Resolve<Any> {
                override fun call(result: Any?) {
                    Toast.makeText(this@MainActivity, result.toString(), Toast.LENGTH_SHORT).show()
                }

            }, object : Reject {
                override fun call(e: Exception?) {
                    e?.printStackTrace()
                    tv.text = e?.message
                }

            })
        }
        //打开新的页面
        tv2.text = "android://minor/activity/minorActivity"
        tv2.setOnClickListener {
            ModuleRouter.open<Any>(tv2.text.toString()).call(object : Reject {
                override fun call(e: Exception?) {
                    e?.printStackTrace()
                    tv.text = e?.message
                }
            })
        }
        //URL追加参数
        tv3.text = "android://minor/string/minorActivity?params={'param'=minorActivity}"
        tv3.setOnClickListener {
            ModuleRouter.open<Any>(tv3.text.toString()).call(object : Reject {
                override fun call(e: Exception?) {
                    e?.printStackTrace()
                    tv.text = e?.message
                }
            })
        }
        //传递列表参数
        tv4.text = "android://minor/list/minorActivity"
        val map4 = HashMap<String, Any>()
        val list = ArrayList<Any>()
        list.add("list1")
        list.add("list2")
        list.add("list3")
        map4["list"] = list
        tv4.setOnClickListener {
            ModuleRouter.open<Any>(tv4.text.toString(), map4).call(object : Reject {
                override fun call(e: Exception?) {
                    e?.printStackTrace()
                    tv.text = e?.message
                }
            })
        }
        //传递对象参数
        tv5.text = "android://minor/bean/minorActivity"
        val map5 = HashMap<String, Any>()
        val bean = TestBean()
        bean.a = 123
        bean.b = "456"
        bean.c.add(0)
        map5["bean"] = bean
        tv5.setOnClickListener {
            ModuleRouter.open<Any>(tv5.text.toString(), map5).call(object : Reject {
                override fun call(e: Exception?) {
                    e?.printStackTrace()
                    tv.text = e?.message
                }
            })
        }
        //获取返回值
        tv6.text = "android://minor/get/getData"
        tv6.setOnClickListener {
            ModuleRouter.open<Any>(tv6.text.toString()).call(object : Resolve<TestBean> {
                override fun call(result: TestBean?) {
                    tv.text = result.toString()
                }
            }, object : Reject {
                override fun call(e: Exception?) {
                    e?.printStackTrace()
                    tv.text = e?.message
                }
            })
        }
        //阻塞获取值
        tv7.text = "android://minor/get/getValue"
        tv7.setOnClickListener {
            val value = ModuleRouter.open<Any>(tv7.text.toString()).getValue<Any>()
            tv.text = value?.toString() ?: "value is null"
        }
        //线程切换
        tv8.text = "android://minor/thread/getThread"
        tv8.setOnClickListener {
            ModuleRouter.open<Any>(tv8.text.toString()).callOnSubThread().returnOnMainThread()
                .call(object : Resolve<Any> {
                    override fun call(result: Any?) {
                        tv.text =
                            "call thread :${result.toString()}\nreturn thread:${Thread.currentThread()}"
                    }
                }, object : Reject {
                    override fun call(e: Exception?) {
                        e?.printStackTrace()
                        tv.text = e?.message
                    }
                })
        }
        //直接返回值
        tv9.text = "android://minor/autoReturn"
        tv9.setOnClickListener {
            ModuleRouter.open<Any>(tv9.text.toString()).call(object : Resolve<String> {
                override fun call(result: String?) {
                    tv.text = result.toString()
                }
            }, object : Reject {
                override fun call(e: Exception?) {
                    e?.printStackTrace()
                    tv.text = e?.message
                }
            })
        }
        val map10 = HashMap<String, Any>()
        val bean10 = TestBean()
        bean10.a = 123
        bean10.b = "456"
        bean10.c.add(0)
        map10["bean"] = bean10
        val list10 = ArrayList<Any>()
        list10.add("list1")
        list10.add("list2")
        list10.add("list3")
        map10["list"] = list10
        //综合调用
        tv10.text = "android://minor/complex/minorActivity?params={param=complexActivity}"
        tv10.setOnClickListener {
            ModuleRouter.open<Any>(tv10.text.toString(),map10).call(object : Resolve<String> {
                override fun call(result: String?) {
                    tv.text = result.toString()
                }
            }, object : Reject {
                override fun call(e: Exception?) {
                    e?.printStackTrace()
                    tv.text = e?.message
                }
            })
        }
    }
}