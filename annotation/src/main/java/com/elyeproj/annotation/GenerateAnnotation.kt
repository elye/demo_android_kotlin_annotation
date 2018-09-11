package com.elyeproj.annotation

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
annotation class GenerateSource(val value: Int)
