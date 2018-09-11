package com.elyeproj.annotationexperiment

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {

    @ReflectSource(5)
    val sourceTest: Int = 0

    @ReflectBinary(5)
    val binaryTest: Int = 0

    @ReflectRuntime(5)
    val runtimeTest: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("TAG", "Before $sourceTest $binaryTest $runtimeTest")
        bindValue(this)
        Log.d("TAG", "After $sourceTest $binaryTest $runtimeTest")
    }

    private fun bindValue(target: Activity) {
        val declaredFields = target::class.java.declaredFields

        for (field in declaredFields) {
            for (annotation in field.annotations) {

                when(annotation) {
                    is ReflectSource -> {
                        field.isAccessible = true
                        field.set(target, annotation.value)
                    }
                    is ReflectBinary -> {
                        field.isAccessible = true
                        field.set(target, annotation.value)
                    }
                    is ReflectRuntime -> {
                        field.isAccessible = true
                        field.set(target, annotation.value)
                    }
                }
            }
        }
    }
}

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
annotation class ReflectSource(val value: Int)

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FIELD)
annotation class ReflectBinary(val value: Int)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class ReflectRuntime(val value: Int)
