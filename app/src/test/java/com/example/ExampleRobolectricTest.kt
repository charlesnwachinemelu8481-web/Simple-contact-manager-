package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Contact
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
    assertEquals("MyContacts", appName)
  }

  @Test
  fun `contact initials and display name are generated correctly`() {
    val contact = Contact(
      firstName = "Alexander",
      lastName = "Wright",
      phone = "+1 555-0100",
      category = "Work"
    )

    assertEquals("AW", contact.initials)
    assertEquals("Alexander Wright", contact.fullName)
    assertEquals("Wright, Alexander", contact.displayName(sortByLastName = true))
    assertEquals("Alexander Wright", contact.displayName(sortByLastName = false))
    assertEquals('A', contact.indexLetter(sortByLastName = false))
    assertEquals('W', contact.indexLetter(sortByLastName = true))
  }
}
