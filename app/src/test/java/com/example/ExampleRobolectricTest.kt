package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Smart Corridor", appName)
  }

  @Test
  fun `corridor 2km forward activation calculation`() {
    val distanceMeters = 1500.0 // 1.5 km ahead
    val isActive = distanceMeters in 0.0..2000.0
    assertEquals(true, isActive)
  }
}
