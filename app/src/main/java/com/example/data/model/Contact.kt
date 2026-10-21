package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firstName: String,
    val lastName: String = "",
    val phone: String = "",
    val email: String = "",
    val category: String = "Other", // "Family", "Work", "Friends", "Other", or custom
    val notes: String = "",
    val photoUri: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val fullName: String
        get() = when {
            firstName.isNotBlank() && lastName.isNotBlank() -> "$firstName $lastName"
            firstName.isNotBlank() -> firstName
            lastName.isNotBlank() -> lastName
            else -> "Unnamed Contact"
        }

    fun displayName(sortByLastName: Boolean): String {
        return if (sortByLastName && lastName.isNotBlank()) {
            if (firstName.isNotBlank()) "$lastName, $firstName" else lastName
        } else {
            fullName
        }
    }

    val initials: String
        get() {
            val f = firstName.trim().firstOrNull()?.uppercaseChar()
            val l = lastName.trim().firstOrNull()?.uppercaseChar()
            return when {
                f != null && l != null -> "$f$l"
                f != null -> "$f"
                l != null -> "$l"
                else -> "?"
            }
        }

    fun indexLetter(sortByLastName: Boolean): Char {
        val target = if (sortByLastName && lastName.isNotBlank()) lastName.trim() else firstName.trim()
        val firstChar = target.firstOrNull()?.uppercaseChar() ?: '#'
        return if (firstChar in 'A'..'Z') firstChar else '#'
    }
}
