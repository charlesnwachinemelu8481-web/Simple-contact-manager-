package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Contact
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Query("SELECT * FROM contacts ORDER BY firstName COLLATE NOCASE ASC, lastName COLLATE NOCASE ASC")
    fun getAllContactsSortedByFirstName(): Flow<List<Contact>>

    @Query("SELECT * FROM contacts ORDER BY CASE WHEN lastName = '' THEN firstName ELSE lastName END COLLATE NOCASE ASC, firstName COLLATE NOCASE ASC")
    fun getAllContactsSortedByLastName(): Flow<List<Contact>>

    @Query("SELECT * FROM contacts WHERE isFavorite = 1 ORDER BY firstName COLLATE NOCASE ASC")
    fun getFavoriteContacts(): Flow<List<Contact>>

    @Query("SELECT * FROM contacts WHERE id = :id LIMIT 1")
    fun getContactById(id: Long): Flow<Contact?>

    @Query("SELECT * FROM contacts WHERE id = :id LIMIT 1")
    suspend fun getContactByIdOnce(id: Long): Contact?

    @Query("SELECT * FROM contacts WHERE category = :category ORDER BY firstName COLLATE NOCASE ASC")
    fun getContactsByCategory(category: String): Flow<List<Contact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<Contact>): List<Long>

    @Update
    suspend fun updateContact(contact: Contact)

    @Delete
    suspend fun deleteContact(contact: Contact)

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteContactById(id: Long)

    @Query("DELETE FROM contacts")
    suspend fun deleteAllContacts()

    @Query("UPDATE contacts SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("SELECT DISTINCT category FROM contacts WHERE category != '' ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM contacts")
    fun getContactCount(): Flow<Int>
}
