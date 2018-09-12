package com.elyeproj.annotationexperiment

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.elyeproj.annotation.CheckCamelSource
import com.elyeproj.annotation.GenerateSource

@CheckCamelSource
class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "AnnotateTest"
    }

    @ReflectRuntime(5)
    val reflectTest: Int = 0

    @GenerateSource(5)
    var generateTest: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Before $reflectTest $generateTest")
        bindReflectionValue(this)
        Log.d(TAG, "After $reflectTest $generateTest")
        bindGenerationValue(this)
        Log.d(TAG, "Finally $reflectTest $generateTest")
    }

    private fun bindReflectionValue(target: Activity) {
        val declaredFields = target::class.java.declaredFields

        for (field in declaredFields) {
            for (annotation in field.annotations) {
                when(annotation) {
                    is ReflectRuntime -> {
                        field.isAccessible = true
                        field.set(target, annotation.value)
                    }
                }
            }
        }
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class ReflectRuntime(val value: Int)
