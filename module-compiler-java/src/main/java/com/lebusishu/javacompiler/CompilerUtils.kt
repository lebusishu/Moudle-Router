package com.lebusishu.javacompiler

/**
 * Description : 提供一些工具方法
 * Create by wxh on 2020/10/13
 * Phone ：15233620521
 * Email：wangxiaohui1118@gmail.com
 * Person in charge : lebusishu
 */
class CompilerUtils {
    companion object{
        /**
         * get class full type :com.lebusishu.router.VPromise.class
         */
        fun getFullTypesString(types:String):String{
            if (types.contains(",")){
               val arr=types.split(",")
                val builder:StringBuilder=StringBuilder()
                var appear=false
                for (a in arr){
                    if (appear){
                        appear=false
                        continue
                    }
                    if (a.contains("Map")){
                        appear=true
                    }
                    builder.append(removeGeneric(a)).append(".class,")
                }
                return builder.substring(0,builder.length-1)
            }else{
                return removeGeneric(types)+".class"
            }
        }
       private fun removeGeneric(type:String):String{
            val index=type.indexOf("<")
            if (index!=-1){
                return type.substring(0,index)
            }
            return type
        }
    }
}