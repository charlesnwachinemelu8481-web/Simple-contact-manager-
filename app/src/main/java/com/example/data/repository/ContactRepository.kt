package com.example.data.repository

import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import com.example.data.local.ContactDao
import com.example.data.model.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ContactRepository(
    private val contactDao: ContactDao,
    private val context: Context
) {

    fun getContacts(sortByLastName: Boolean): Flow<List<Contact>> {
        return if (sortByLastName) {
            contactDao.getAllContactsSortedByLastName()
        } else {
            contactDao.getAllContactsSortedByFirstName()
        }
    }

    fun getFavoriteContacts(): Flow<List<Contact>> = contactDao.getFavoriteContacts()

    fun getContactById(id: Long): Flow<Contact?> = contactDao.getContactById(id)

    suspend fun getContactByIdOnce(id: Long): Contact? = contactDao.getContactByIdOnce(id)

    fun getContactsByCategory(category: String): Flow<List<Contact>> =
        contactDao.getContactsByCategory(category)

    fun getAllCategories(): Flow<List<String>> = contactDao.getAllCategories()

    fun getContactCount(): Flow<Int> = contactDao.getContactCount()

    suspend fun insertContact(contact: Contact): Long = withContext(Dispatchers.IO) {
        contactDao.insertContact(contact)
    }

    suspend fun updateContact(contact: Contact) = withContext(Dispatchers.IO) {
        contactDao.updateContact(contact)
    }

    suspend fun deleteContact(contact: Contact) = withContext(Dispatchers.IO) {
        contactDao.deleteContact(contact)
    }

    suspend fun deleteContactById(id: Long) = withContext(Dispatchers.IO) {
        contactDao.deleteContactById(id)
    }

    suspend fun deleteAllContacts() = withContext(Dispatchers.IO) {
        contactDao.deleteAllContacts()
    }

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        contactDao.setFavorite(id, isFavorite)
    }

    /**
     * Import contacts from system ContentResolver
     */
    suspend fun importDeviceContacts(): Int = withContext(Dispatchers.IO) {
        val resolver: ContentResolver = context.contentResolver
        val importedList = mutableListOf<Contact>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )

        try {
            val cursor = resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )

            cursor?.use { c ->
                val nameIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

                while (c.moveToNext()) {
                    val rawName = if (nameIndex >= 0) c.getString(nameIndex) ?: "" else ""
                    val phone = if (numberIndex >= 0) c.getString(numberIndex) ?: "" else ""
                    val photoUri = if (photoIndex >= 0) c.getString(photoIndex) else null

                    if (rawName.isNotBlank() || phone.isNotBlank()) {
                        val parts = rawName.trim().split(" ", limit = 2)
                        val firstName = parts.getOrNull(0) ?: "Contact"
                        val lastName = parts.getOrNull(1) ?: ""

                        importedList.add(
                            Contact(
                                firstName = firstName,
                                lastName = lastName,
                                phone = phone,
                                email = "",
                                category = "Other",
                                notes = "Imported from device contacts",
                                photoUri = photoUri,
                                isFavorite = false
                            )
                        )
                    }
                }
            }
        } catch (e: SecurityException) {
            // Permission not granted or query failure
            return@withContext 0
        } catch (e: Exception) {
            return@withContext 0
        }

        if (importedList.isNotEmpty()) {
            contactDao.insertContacts(importedList)
        }
        importedList.size
    }

    /**
     * Seed realistic sample contacts for testing or instant showcase
     */
    suspend fun seedSampleContacts(): Int = withContext(Dispatchers.IO) {
        val sampleContacts = listOf(
            Contact(
                firstName = "Alexander",
                lastName = "Wright",
                phone = "+1 (555) 234-5678",
                email = "alex.wright@techcorp.io",
                category = "Work",
                notes = "Senior Product Designer. Lead for mobile UI redesign.",
                isFavorite = true
            ),
            Contact(
                firstName = "Amelia",
                lastName = "Chen",
                phone = "+1 (555) 876-5432",
                email = "amelia.chen@family.org",
                category = "Family",
                notes = "Cousin Amelia. Birthday is July 14.",
                isFavorite = true
            ),
            Contact(
                firstName = "Benjamin",
                lastName = "Carter",
                phone = "+1 (555) 345-6789",
                email = "ben.carter@studio.design",
                category = "Friends",
                notes = "Met at the photography workshop in Seattle.",
                isFavorite = true
            ),
            Contact(
                firstName = "Charlotte",
                lastName = "Davis",
                phone = "+1 (555) 987-6543",
                email = "charlotte.davis@lawfirm.com",
                category = "Work",
                notes = "Corporate Counsel. Office 4B.",
                isFavorite = false
            ),
            Contact(
                firstName = "Daniel",
                lastName = "Evans",
                phone = "+1 (555) 456-7890",
                email = "daniel.evans@runclub.org",
                category = "Friends",
                notes = "Weekend marathon training partner.",
                isFavorite = false
            ),
            Contact(
                firstName = "Eleanor",
                lastName = "Garcia",
                phone = "+1 (555) 654-3210",
                email = "eleanor.g@acmehealth.com",
                category = "Work",
                notes = "Clinical project manager at Acme Health.",
                isFavorite = false
            ),
            Contact(
                firstName = "Grace",
                lastName = "Kim",
                phone = "+1 (555) 567-8901",
                email = "grace.kim@creative.co",
                category = "Friends",
                notes = "Graphic artist and illustrator. Loves matcha.",
                isFavorite = true
            ),
            Contact(
                firstName = "Henry",
                lastName = "Miller",
                phone = "+1 (555) 765-4321",
                email = "henry.miller@family.org",
                category = "Family",
                notes = "Uncle Henry. Gardening enthusiast.",
                isFavorite = false
            ),
            Contact(
                firstName = "Isabella",
                lastName = "Nolan",
                phone = "+1 (555) 678-9012",
                email = "isabella.nolan@finance.net",
                category = "Work",
                notes = "Financial Advisor & Portfolio manager.",
                isFavorite = false
            ),
            Contact(
                firstName = "Lucas",
                lastName = "Patel",
                phone = "+1 (555) 789-0123",
                email = "lucas.patel@codebase.dev",
                category = "Friends",
                notes = "Android engineer & open-source contributor.",
                isFavorite = false
            ),
            Contact(
                firstName = "Maya",
                lastName = "Robinson",
                phone = "+1 (555) 890-1234",
                email = "maya.robinson@family.org",
                category = "Family",
                notes = "Sister Maya. Always call on Sunday afternoon.",
                isFavorite = true
            ),
            Contact(
                firstName = "Noah",
                lastName = "Scott",
                phone = "+1 (555) 901-2345",
                email = "noah.scott@contractor.com",
                category = "Other",
                notes = "Home renovation contractor. Highly recommended.",
                isFavorite = false
            ),
            Contact(
                firstName = "Sophia",
                lastName = "Taylor",
                phone = "+1 (555) 012-3456",
                email = "sophia.t@venture.io",
                category = "Work",
                notes = "Partner at Horizon Ventures.",
                isFavorite = false
            ),
            Contact(
                firstName = "William",
                lastName = "Zhao",
                phone = "+1 (555) 123-4567",
                email = "william.zhao@bookclub.org",
                category = "Friends",
                notes = "Host of Thursday science fiction book club.",
                isFavorite = false
            )
        )
        contactDao.insertContacts(sampleContacts)
        sampleContacts.size
    }

    /**
     * Export all contacts to CSV formatted string
     */
    suspend fun generateCsv(): String = withContext(Dispatchers.IO) {
        val contacts = contactDao.getAllContactsSortedByFirstName()
        val stringBuilder = StringBuilder()
        stringBuilder.append("First Name,Last Name,Phone,Email,Category,Favorite,Notes\n")

        val list = contactDao.getAllContactsSortedByFirstName().first()
        for (c in list) {
            val fn = escapeCsv(c.firstName)
            val ln = escapeCsv(c.lastName)
            val ph = escapeCsv(c.phone)
            val em = escapeCsv(c.email)
            val cat = escapeCsv(c.category)
            val fav = if (c.isFavorite) "Yes" else "No"
            val notes = escapeCsv(c.notes)
            stringBuilder.append("$fn,$ln,$ph,$em,$cat,$fav,$notes\n")
        }
        stringBuilder.toString()
    }

    private fun escapeCsv(value: String): String {
        var str = value.replace("\"", "\"\"")
        if (str.contains(",") || str.contains("\n") || str.contains("\"")) {
            str = "\"$str\""
        }
        return str
    }
}
