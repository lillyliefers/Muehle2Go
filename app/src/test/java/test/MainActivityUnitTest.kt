package test

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import ui.MainActivity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test


class MainActivityUnitTest {

    @Test
    fun testPermissionIsGranted() {
        assertTrue(MainActivity.permissionIsGranted(PackageManager.PERMISSION_GRANTED))
        assertFalse(MainActivity.permissionIsGranted(PackageManager.PERMISSION_DENIED))
    }
}