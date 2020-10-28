package com.lebusishu.javacompiler

import com.google.auto.common.SuperficialValidation
import com.google.auto.service.AutoService
import com.lebusishu.annotations.RouterModule
import com.lebusishu.annotations.RouterPath
import com.squareup.javapoet.*

import java.io.IOException
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.tools.Diagnostic
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashSet

/**
 * Description : Process the java file with {@link RouterModule} and {@link RouterPath} annotation
 * Create by wxh on 2020/10/13
 * Phone ：15233620521
 * Email：wangxiaohui1118@gmail.com
 * Person in charge : lebusishu
 */
@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class RouterJavaProcessor : AbstractProcessor() {

    /**
     * 元素相关
     */
    lateinit var elementUtils: Elements

    /**
     * 生成文件的工具类
     */
    lateinit var filer: Filer

    override fun getSupportedAnnotationTypes(): Set<String> {
        val types: LinkedHashSet<String> = LinkedHashSet()
        for (annotation in getSupportedAnnotations()) {
            types.add(annotation.canonicalName)
        }
        return types
    }

    /**
     * 设置支持的注解类型
     *
     */
    private fun getSupportedAnnotations(): Set<Class<out Annotation>> {
        return setOf(RouterModule::class.java)
    }

    /**
     * 设置支持的版本
     *
     * @return 这里用最新的就好
     */
    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
        elementUtils = processingEnv?.elementUtils!!
        filer = processingEnv.filer
    }

    /**
     * 注解内部逻辑的实现
     * <p>
     * Element代表程序的一个元素，可以是package, class, interface, method.只在编译期存在
     * TypeElement：变量；TypeElement：类或者接口
     */
    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ): Boolean {
        val files: List<JavaFile>? = roundEnv?.let { findAndParseTargets(it) }
        if (files != null) {
            for (file in files) {
                try {
                    file.writeTo(filer)
                } catch (e: IOException) {
                    e.message?.let {
                        error(
                            "Unable to write same name %s: %s", file.packageName,
                            it
                        )
                    }
                }
            }

        }
        return false
    }

    private fun error(msg: String, vararg args: Any) {
        recordMessage(msg, args)
    }

    private fun recordMessage(msg: String, vararg args: Any) {
        var message = msg
        if (args.isNotEmpty()) {
            message = String.format(msg, args)
        }
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, message)
    }

    /**
     * find all router module
     */
    private fun findAndParseTargets(env: RoundEnvironment): List<JavaFile> {
        val files: ArrayList<JavaFile> = ArrayList()
        // 1、获取要处理的注解的元素的集合
        for (element in env.getElementsAnnotatedWith(RouterModule::class.java)) {
            if (!SuperficialValidation.validateElement(element)) {
                continue
            }
            //返回在此类或接口中直接声明的字段，方法，构造函数和成员类型
            val allFile: List<Element> = element.enclosedElements
            parseRouterModule(element, allFile, files)
        }
        return files
    }

    /**
     * parse router module class
     */
    private fun parseRouterModule(
        ele: Element,
        allFile: List<Element>,
        files: ArrayList<JavaFile>
    ) {
        val annotation: RouterModule = ele.getAnnotation(RouterModule::class.java)
        val scheme: String = annotation.scheme
        val host: String = annotation.host
        if (scheme.isEmpty() || host.isEmpty()) {
            return
        }
        val constructor: MethodSpec.Builder = MethodSpec.constructorBuilder()
        constructor
            .addModifiers(Modifier.PUBLIC)
            .addException(Exception::class.java)
        // 获取原始的注解类
        val obj: ClassName =
            ClassName.get(elementUtils.getPackageOf(ele).toString(), ele.simpleName.toString())
        constructor
            //初始化原始类
            .addStatement("this.object = \$T.class.newInstance()", obj)
            //初始化存储方法集合
            .addStatement("this.mapping = new \$T()", HashMap::class.java)
        // parse RouterPath：解析注解的方法并添加到集合里
        allFile.forEachIndexed { index, element ->
            val path: RouterPath = element.getAnnotation(RouterPath::class.java) ?: return@forEachIndexed
            val args: String = (element as ExecutableElement).parameters.toString()
            lateinit var types: String
            var methodFullTypes: String = element.toString()
            val start: Int = methodFullTypes.indexOf("(")
            val end: Int = methodFullTypes.indexOf(")")
            if (end - start > 1) {
                types = methodFullTypes.substring(start + 1, end)
                if (types.lastIndexOf("...") != -1) {
                    types = types.replace("...", "[]")
                }
                methodFullTypes = "," + CompilerUtils.getFullTypesString(types) + "))"
            } else {
                methodFullTypes = "))"
            }
            val methodKey = path.value.toLowerCase(Locale.ROOT)
            val methodName = element.simpleName.toString()
            // add method
            //$S for Strings:$S 表示一个 string
            //$T for Types:类型，通过 $T 进行映射，会自动import声明
            constructor
                .addStatement(
                    "mapping.put(\$S +\$T._METHOD,object.getClass().getMethod(\$S$methodFullTypes",
                    methodKey,
                    MODULE_DELEGATER,
                    methodName
                )
                // add params name
                //$L for Literals: $L 来接受一个 literal 值
                .addStatement("String args\$L = \$S", index, args)
                .addStatement(
                    "mapping.put(\$S + \$T._ARGS,args\$L)",
                    methodKey,
                    MODULE_DELEGATER,
                    index
                )
                .addStatement("String type\$L = \$S", index, types)
                .addStatement(
                    "mapping.put(\$S + \$T._TYPES,type\$L)",
                    methodKey,
                    MODULE_DELEGATER,
                    index
                )
                .addCode("\n")
        }
        // method body:添加代理方法
        val invoke: MethodSpec.Builder = MethodSpec.methodBuilder("invoke")
        invoke
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addParameter(String::class.java, "path")
            .addParameter(PARAMS_WRAPPER, "params")
            .addException(Exception::class.java)
            .returns(TypeName.VOID)
        invoke.addStatement(
            "\$T.invoke(path,params,object,mapping)",
            MODULE_DELEGATER_COMPANION
        )
        val schemeMain = if (scheme.contains("|")) scheme.split("\\|")[0] else scheme
        val nameMain = PREFIX + schemeMain + "_$host"
        // JavaFile 对应的 Java 文件。 包名直接用一个字符串表示。
        // TypeSpec 表示一个 class 定义，用于创建类或者接口。
        val clazz = TypeSpec
            .classBuilder(nameMain)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            //实现接口
            .addSuperinterface(IMIRROR)
            // Fields，用来创建字段
            .addFields(buildRouterModuleFields())
            // constructor用来创建方法和构造函数
            .addMethod(constructor.build())
            .addMethod(invoke.build())
            .addJavadoc(FILE_DOC).build()
        val javaFile = JavaFile.builder(PACKAGE_NAME, clazz).build()
        files.add(javaFile)
        if (scheme != nameMain) {
            makeSubFile(scheme, host, ClassName.get(PACKAGE_NAME, nameMain), files)
        }
    }

    /**
     * build fields
     */
    private fun buildRouterModuleFields(): Iterable<FieldSpec> {
        val fieldSpecs: ArrayList<FieldSpec> = ArrayList()
        val mapping = FieldSpec
            .builder(HashMap::class.java, "mapping")
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .build()
        fieldSpecs.add(mapping)
        val className = FieldSpec
            .builder(TypeName.OBJECT, "object")
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .build()
        fieldSpecs.add(className)
        return fieldSpecs
    }

    /**
     * if has multi schemes. if contains "|"
     */
    private fun makeSubFile(
        scheme: String,
        host: String,
        main: ClassName,
        files: ArrayList<JavaFile>
    ) {
        val schemes = scheme.split("\\|")
        for (index in 1 until schemes.size) {
            val subScheme = schemes[index]
            val nameSub = PREFIX + subScheme + "_$host"
            val clazz = TypeSpec
                .classBuilder(nameSub)
                .addField(
                    FieldSpec
                        .builder(TypeName.OBJECT::class.java, "main")
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                        .build()
                )
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(IMIRROR)
                .addMethod(
                    MethodSpec
                        .constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addException(IllegalAccessException::class.java)
                        .addException(InstantiationException::class.java)
                        .addStatement("this.main=\$T.class.newInstance()", main).build()
                )
                .addMethod(
                    MethodSpec
                        .methodBuilder("invoke")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addParameter(String::class.java, "path")
                        .addParameter(PARAMS_WRAPPER, "params")
                        .addException(Exception::class.java)
                        .addStatement(
                            "main.getClass().getMethod(\"invoke\",String.class,\$T.class).invoke(main, path, params)",
                            PARAMS_WRAPPER
                        ).returns(TypeName.VOID::class.java).build()
                )
                .addJavadoc(FILE_DOC).build()
            val javaFile = JavaFile.builder(PACKAGE_NAME, clazz).build()
            files.add(javaFile)
        }
    }

    companion object {
        const val FILE_DOC = "DO NOT EDIT THIS FILE!!! IT WAS GENERATED BY ANDROID ROUTER."
        const val PACKAGE_NAME = "com.lebusishu.router"
        const val PREFIX = "java_auto_"
        val MODULE_DELEGATER: ClassName = ClassName.get(PACKAGE_NAME, "ModuleDelegater")
        val MODULE_DELEGATER_COMPANION: ClassName = ClassName.get(PACKAGE_NAME, "ModuleDelegater.Companion")
        val PARAMS_WRAPPER: ClassName = ClassName.get(PACKAGE_NAME, "ParamsWrapper")
        val IMIRROR: ClassName = ClassName.get("$PACKAGE_NAME.interfaces", "IMirror")
    }

}