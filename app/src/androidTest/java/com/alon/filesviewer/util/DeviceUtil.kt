package com.alon.filesviewer.util

import android.app.Instrumentation
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until.hasObject
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import java.io.File

object DeviceUtil {

    fun getDevice():UiDevice {
        return UiDevice.getInstance(getInstrumentation())
    }

    fun launchApp() {
        val timeout = 5000L
        val launcherPackage =
            getLaunchPackageName()
        assertThat(launcherPackage, notNullValue())
        UiDevice.getInstance(getInstrumentation())
            .wait(hasObject(By.pkg(launcherPackage).depth(0)), timeout)

        // Launch the blueprint app
        val context = getApplicationContext<Context>()
        val appPackage = context.packageName
        val intent = context.packageManager
            .getLaunchIntentForPackage(appPackage)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)    // Clear out any previous instances
        context.startActivity(intent)

        // Wait for the app to appear
        UiDevice.getInstance(getInstrumentation())
            .wait(hasObject(By.pkg(appPackage).depth(0)), timeout)
    }

    private fun getLaunchPackageName(): String {
        // Create launcher Intent
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)

        // Use PackageManager to get the launcher package name
        val pm = getInstrumentation().context.packageManager
        val resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)

        return resolveInfo!!.activityInfo.packageName
    }

    fun copyImageFileToDevice(path: String): Pair<String,Uri> {
        val fileName = path.split("/").last()
        val filePath = "/storage/emulated/0/Pictures/".plus(fileName)
        val context = getApplicationContext<Context>()
        val contentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val values = ContentValues(2)

        values.put(MediaStore.Images.Media.TITLE, fileName)
        values.put(MediaStore.Images.Media.DATA,filePath)

        val resolver = context.contentResolver
        val uri = resolver.insert(contentUri, values)

        resolver.openOutputStream(uri!!).use { fos ->
            val fis = javaClass.classLoader!!.getResourceAsStream(path)
            val readData = ByteArray(1024 * 500)
            var i = fis.read(readData)

            while (i != -1) {
                fos!!.write(readData, 0, i)
                i = fis.read(readData)
            }

            fos!!.close()
        }

        return Pair(filePath,uri)
    }

    fun deleteFilesFromDevice(path: String) {
        File(path).delete()
    }

    fun deleteFromMediaStore(uri: Uri) {
        getApplicationContext<Context>()
            .contentResolver.delete(uri,null,null)
    }

    fun createFolder(name: String): String {
        val folder = File(Environment.getExternalStorageDirectory(), name)

        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                throw IllegalStateException("Failed to create $name folder!")
            }
        }

        return folder.path
    }

    fun createFiles(folder: String, files: List<String>) {
        files.forEach { fileName ->
            val file = File(folder.plus("/").plus(fileName))

            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw IllegalStateException("Failed to create $fileName file!")
                }
            }
        }
    }
}