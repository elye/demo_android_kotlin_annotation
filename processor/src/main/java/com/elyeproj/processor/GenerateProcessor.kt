package com.elyeproj.processor

import com.elyeproj.annotation.CheckCamelSource
import com.elyeproj.annotation.GenerateSource
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.asTypeName
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(GenerateProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class GenerateProcessor : AbstractProcessor() {
    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(GenerateSource::class.java.canonicalName, CheckCamelSource::class.java.canonicalName)
    }

    private val generatedSourcesRoot by lazy { processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME].orEmpty() }

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {

        roundEnv.getElementsAnnotatedWith(CheckCamelSource::class.java).forEach { classElement ->
            if (classElement.kind != ElementKind.CLASS) {
                printError("Can only be applied to field, element: $classElement")
                return false
            } else {
                checkCamelVariable(classElement as TypeElement)
            }
        }

        if (generatedSourcesRoot.isEmpty()) {
            printError("Can't find the target directory for generated Kotlin files.")
            return false
        }

        roundEnv.getElementsAnnotatedWith(GenerateSource::class.java).forEach { fieldElement ->
            if (fieldElement.kind != ElementKind.FIELD) {
                printError("Can only be applied to field, element: $fieldElement")
                return false
            } else {
                prepareFieldInitialization(fieldElement)
            }
        }
        return false
    }

    private fun checkCamelVariable(classElement: TypeElement) {
        classElement.enclosedElements.filter {
            !it.simpleName.toString().isDefinedCamelCase()
        }.forEach {
            printWarning("Detected non-camelcase name: ${it.simpleName}.")
        }
    }

    private fun String.isDefinedCamelCase(): Boolean {
        val toCharArray = toCharArray()
        return toCharArray
                .mapIndexed { index, current -> current to toCharArray.getOrNull(index + 1) }
                .none { it.first.isUpperCase() && it.second?.isUpperCase() ?: false }
    }

    private fun prepareFieldInitialization(fieldElement: Element) {
        val packageOfMethod = processingEnv.elementUtils.getPackageOf(fieldElement).toString()

        val annotatedValue = fieldElement.getAnnotation(GenerateSource::class.java).value

        val funcBuilder = FunSpec.builder("bindGenerationValue")
                .addModifiers(KModifier.PUBLIC)
                .addParameter("parent", fieldElement.enclosingElement.asType().asTypeName())
                .addStatement("parent.%L = %L", fieldElement.simpleName, annotatedValue)

        val file = File(generatedSourcesRoot)
        file.mkdir()
        FileSpec.builder(packageOfMethod, "GeneratedFunction").addFunction(funcBuilder.build()).build().writeTo(file)
    }

    private fun printError(message: String) {
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, message)
    }

    private fun printWarning(message: String) {
        processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, message)
    }
}
