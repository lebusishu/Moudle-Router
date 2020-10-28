package com.lebusishu.ktcompiler

import com.google.auto.common.SuperficialValidation
import com.google.auto.service.AutoService
import com.lebusishu.annotations.RouterModule
import com.lebusishu.annotations.RouterPath
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File

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
 * Description : Process the kotlin file with {@link RouterModule} and {@link RouterPath} annotation
 * Create by wxh on 2020/10/13
 * Phone ：15233620521
 * Email：wangxiaohui1118@gmail.com
 * Person in charge : lebusishu
 */
@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class RouterKotlinProcessor : AbstractProcessor() {

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
        val files: List<FileSpec>? = roundEnv?.let { findAndParseTargets(it) }
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
    private fun findAndParseTargets(env: RoundEnvironment): List<FileSpec> {
        val files: ArrayList<FileSpec> = ArrayList()
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
        files: ArrayList<FileSpec>
    ) {
        val annotation: RouterModule = ele.getAnnotation(RouterModule::class.java)
        val scheme: String = annotation.scheme
        val host: String = annotation.host
        if (scheme.isEmpty() || host.isEmpty()) {
            return
        }
        val schemeMain = if (scheme.contains("|")) scheme.split("\\|")[0] else scheme
        val nameFile = PREFIX + schemeMain + "_$host"
        // 获取原始的注解类
        val obj =
            ClassName(elementUtils.getPackageOf(ele).toString(), ele.simpleName.toString())
        //构造函数
        val constructor = FunSpec.constructorBuilder()
        constructor
            .addStatement("this.original = %T::class.java.newInstance()", obj)
            .addStatement("this.mapping = %T<String,Any>()", HashMap::class)
        // parse RouterPath：解析注解的方法并添加到集合里
        allFile.forEachIndexed { index, element ->
            val path: RouterPath =
                element.getAnnotation(RouterPath::class.java) ?: return@forEachIndexed
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
                    "mapping.put(%S +%T._METHOD,original::class.java.getMethod(%S$methodFullTypes",
                    methodKey,
                    MODULE_DELEGATER,
                    methodName
                )
                // add params name
                //$L for Literals: $L 来接受一个 literal 值
                .addStatement("val args%L = %S", index, args)
                .addStatement(
                    "mapping.put(%S + %T._ARGS,args%L)",
                    methodKey,
                    MODULE_DELEGATER,
                    index
                )
                .addStatement("val type%L = %S", index, types)
                .addStatement(
                    "mapping.put(%S + %T._TYPES,type%L)",
                    methodKey,
                    MODULE_DELEGATER,
                    index
                )
                .addCode("\n")
        }
        //反射方法
        val function = FunSpec.builder("invoke")
            .addModifiers(KModifier.OPERATOR)
            .addParameter(
                ParameterSpec.builder("path", String::class)
                    .build()
            )
            .addParameter("params", PARAMS_WRAPPER)
            .addStatement(
                "%T.invoke(path,params,original,mapping)",
                MODULE_DELEGATER_COMPANION
            )
            .build()
        //指定生成的文件名
        val file = FileSpec.builder(PACKAGE_NAME, nameFile)
            .addType(
                TypeSpec.classBuilder(nameFile)
                    .addSuperinterface(IMIRROR)
                    .primaryConstructor(constructor.build())
                    .addFunction(function)
                    //添加属性
                    .addProperties(buildRouterModuleFields())
                    .addKdoc(FILE_DOC)
                    .build()
            )
            .build()
        files.add(file)
        if (scheme != nameFile) {
            makeSubFile(scheme, host, ClassName(PACKAGE_NAME, nameFile), files)
        }
    }

    /**
     * build fields
     */
    private fun buildRouterModuleFields(): Iterable<PropertySpec> {
        val fieldSpecs: ArrayList<PropertySpec> = ArrayList()
        val mapping = PropertySpec.builder("mapping", HashMap::class.parameterizedBy(String::class,Any::class), KModifier.FINAL)
            .build()
        fieldSpecs.add(mapping)
        val className = PropertySpec.builder("original", Any::class, KModifier.FINAL)
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
        files: ArrayList<FileSpec>
    ) {
        val schemes = scheme.split("\\|")
        for (index in 1 until schemes.size) {
            val subScheme = schemes[index]
            val nameSub = PREFIX + subScheme + "_$host"
            val file = FileSpec.builder(PACKAGE_NAME, nameSub)
            val clazz = TypeSpec
                .classBuilder(nameSub)
                .addModifiers(KModifier.PUBLIC, KModifier.FINAL)
                .addSuperinterface(IMIRROR)
                .addFunction(
                    FunSpec
                        .constructorBuilder()
                        .addStatement("this.main=%T::class.java.newInstance()", main)
                        .build()
                )
                .addFunction(
                    FunSpec.builder("invoke")
                        .addModifiers(KModifier.OPERATOR)
                        .addParameter(
                            ParameterSpec.builder("path", String::class)
                                .build()
                        )
                        .addParameter("params", PARAMS_WRAPPER)
                        .addStatement(
                            "%T.invoke(path,params,original,mapping)",
                            MODULE_DELEGATER_COMPANION
                        )
                        .build()
                )
                .addKdoc(FILE_DOC).build()
            files.add(file.addType(clazz).build())
        }
    }

    companion object {
        const val FILE_DOC = "DO NOT EDIT THIS FILE!!! IT WAS GENERATED BY ANDROID ROUTER."
        const val PACKAGE_NAME = "com.lebusishu.router"
        const val PREFIX = "kt_auto_"

        val MODULE_DELEGATER: ClassName = ClassName(PACKAGE_NAME, "ModuleDelegater")

        val MODULE_DELEGATER_COMPANION: ClassName =
            ClassName(PACKAGE_NAME, "ModuleDelegater.Companion")
        val PARAMS_WRAPPER: ClassName = ClassName(PACKAGE_NAME, "ParamsWrapper")
        val IMIRROR: ClassName = ClassName("$PACKAGE_NAME.interfaces", "IMirror")
    }

}