module-compiler
该模块负责在编译时期自动生成java源码，源码文件全路径为
com.lebusishu.router.java_auto_android_minor
如需生成kotlin，将APP模块内
kapt project(':module-compiler-java')
放开，将项目gradle.properties文件的配置修改为
AUTO_PREFIX=java_auto_