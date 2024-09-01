package com.alon.filesviewer.util

import android.content.Context
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement


class GrantManageStoragePermissionRule : TestRule {
    override fun apply(base: Statement?, description: Description?): Statement {
        return RequestManageStoragePermStatement(base!!)
    }

    private class RequestManageStoragePermStatement(private val base: Statement) : Statement() {
        @Throws(Throwable::class)
        override fun evaluate() {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val command = "appops set --uid ${context.packageName} MANAGE_EXTERNAL_STORAGE allow"
            val desc: ParcelFileDescriptor =
                getInstrumentation().uiAutomation.executeShellCommand(command)
            desc.close()
            base.evaluate()
        }
    }

}