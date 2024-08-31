package com.alon.filesviewer.runner

import android.app.Application
import android.content.Context
import androidx.databinding.ViewDataBinding
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import java.lang.reflect.Field
import java.lang.reflect.Modifier

class CustomAndroidTestRunner : AndroidJUnitRunner() {

    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }

    override fun onStart() {
        // Disable data binding Choreographer
        setFinalStatic(ViewDataBinding::class.java.getDeclaredField("USE_CHOREOGRAPHER"),false)

        // Set io thread as main thread
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        super.onStart()
    }

    private fun setFinalStatic(field: Field, newValue: Any?) {
        field.isAccessible = true
        val modifiersField: Field = try {
            Field::class.java.getDeclaredField("accessFlags")
        } catch (e: NoSuchFieldException) {
            //This is an emulator JVM  ¯\_(ツ)_/¯
            Field::class.java.getDeclaredField("modifiers")
        }
        modifiersField.isAccessible = true
        modifiersField.setInt(field, field.modifiers and Modifier.FINAL.inv())
        field.set(null, newValue)
    }
}