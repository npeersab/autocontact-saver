package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.preferences.AppSettings
import com.example.data.preferences.NamingFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Auto Contact Saver", appName)
  }

  @Test
  fun `test app settings default and display name generation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val settings = AppSettings.getInstance(context)

    settings.setNamingFormat(NamingFormat.CALLER_NUMBER)
    val displayName = settings.generateDisplayName("+15551234567")
    assertEquals("Caller +15551234567", displayName)

    settings.setNamingFormat(NamingFormat.PREFIX_NUMBER)
    settings.setCustomPrefix("Inbound Lead")
    val customName = settings.generateDisplayName("+15551234567")
    assertEquals("Inbound Lead +15551234567", customName)
  }

  @Test
  fun `test phone normalization`() {
    val raw = "+1 (555) 123-4567"
    val normalized = com.example.contact.ContactHelper.normalizePhoneNumber(raw)
    assertEquals("+15551234567", normalized)
  }
}
