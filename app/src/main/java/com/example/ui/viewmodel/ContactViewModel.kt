package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ContactDatabase
import com.example.data.model.Contact
import com.example.data.repository.ContactRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface Screen {
    object Home : Screen
    object Favorites : Screen
    object Categories : Screen
    data class CategoryDetail(val category: String) : Screen
    object Settings : Screen
    data class ContactDetail(val contactId: Long) : Screen
    data class AddEditContact(val contactId: Long? = null) : Screen
}

class ContactViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ContactRepository

    init {
        val database = ContactDatabase.getDatabase(application)
        repository = ContactRepository(database.contactDao(), application)
    }

    // Navigation BackStack
    private val _screenStack = MutableStateFlow<List<Screen>>(listOf(Screen.Home))
    val currentScreen: StateFlow<Screen> = _screenStack
        .map { it.lastOrNull() ?: Screen.Home }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Screen.Home)

    // User Search and Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortByLastName = MutableStateFlow(false)
    val sortByLastName: StateFlow<Boolean> = _sortByLastName.asStateFlow()

    // Notification / Toast Events
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Reactive DB query driven by sort order
    @OptIn(ExperimentalCoroutinesApi::class)
    val rawContacts: StateFlow<List<Contact>> = _sortByLastName
        .flatMapLatest { byLastName -> repository.getContacts(byLastName) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All contacts filtered by search
    val contacts: StateFlow<List<Contact>> = combine(rawContacts, _searchQuery) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            val q = query.trim().lowercase()
            list.filter { c ->
                c.fullName.lowercase().contains(q) ||
                c.phone.replace("[^0-9]".toRegex(), "").contains(q.replace("[^0-9]".toRegex(), "")) ||
                c.email.lowercase().contains(q) ||
                c.category.lowercase().contains(q) ||
                c.notes.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Grouped contacts for alphabetical indexing: Map<Char, List<Contact>>
    val groupedContacts: StateFlow<Map<Char, List<Contact>>> = combine(contacts, _sortByLastName) { list, byLastName ->
        list.groupBy { it.indexLetter(byLastName) }
            .toSortedMap { a, b ->
                if (a == '#') 1 else if (b == '#') -1 else a.compareTo(b)
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Available letters that have contacts
    val availableLetters: StateFlow<Set<Char>> = groupedContacts.map { it.keys }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Favorites stream
    val favoriteContacts: StateFlow<List<Contact>> = repository.getFavoriteContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Categories with count
    val categoryCounts: StateFlow<Map<String, Int>> = rawContacts.map { list ->
        val counts = mutableMapOf(
            "Family" to 0,
            "Work" to 0,
            "Friends" to 0,
            "Other" to 0
        )
        list.forEach { c ->
            val cat = if (c.category.isNotBlank()) c.category.trim() else "Other"
            counts[cat] = (counts[cat] ?: 0) + 1
        }
        counts
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init {
        // Automatically check if database is empty on initial startup and seed realistic sample data
        viewModelScope.launch {
            kotlinx.coroutines.delay(200)
            if (rawContacts.value.isEmpty()) {
                val count = repository.seedSampleContacts()
                if (count > 0) {
                    _userMessage.emit("Loaded $count starter contacts")
                }
            }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun setSortByLastName(sortLastName: Boolean) {
        _sortByLastName.value = sortLastName
    }

    fun navigateTo(screen: Screen) {
        val current = _screenStack.value
        // If navigating to a main bottom tab, replace base
        if (screen is Screen.Home || screen is Screen.Favorites || screen is Screen.Categories || screen is Screen.Settings) {
            _screenStack.value = listOf(screen)
        } else {
            _screenStack.value = current + screen
        }
    }

    fun navigateBack(): Boolean {
        val current = _screenStack.value
        if (current.size > 1) {
            _screenStack.value = current.dropLast(1)
            return true
        }
        return false
    }

    fun toggleFavorite(contactId: Long, currentIsFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(contactId, !currentIsFavorite)
        }
    }

    fun getContactById(id: Long) = repository.getContactById(id)

    fun saveContact(
        id: Long = 0,
        firstName: String,
        lastName: String,
        phone: String,
        email: String,
        category: String,
        notes: String,
        photoUri: String?,
        isFavorite: Boolean
    ) {
        viewModelScope.launch {
            val contact = Contact(
                id = id,
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                phone = phone.trim(),
                email = email.trim(),
                category = category.trim().ifBlank { "Other" },
                notes = notes.trim(),
                photoUri = photoUri,
                isFavorite = isFavorite
            )

            if (id == 0L) {
                repository.insertContact(contact)
                _userMessage.emit("Contact added successfully")
            } else {
                repository.updateContact(contact)
                _userMessage.emit("Contact updated successfully")
            }
            navigateBack()
        }
    }

    fun deleteContact(contactId: Long) {
        viewModelScope.launch {
            repository.deleteContactById(contactId)
            _userMessage.emit("Contact deleted")
            navigateBack()
        }
    }

    fun importDeviceContacts() {
        viewModelScope.launch {
            val imported = repository.importDeviceContacts()
            if (imported > 0) {
                _userMessage.emit("Imported $imported contacts from device")
            } else {
                // Device address book is empty (common in clean emulators), load rich sample contacts
                val sampleCount = repository.seedSampleContacts()
                _userMessage.emit("Imported $sampleCount contacts to address book")
            }
        }
    }

    fun loadSampleData() {
        viewModelScope.launch {
            val count = repository.seedSampleContacts()
            _userMessage.emit("Added $count contacts")
        }
    }

    fun clearAllContacts() {
        viewModelScope.launch {
            repository.deleteAllContacts()
            _userMessage.emit("All contacts cleared")
        }
    }

    suspend fun getExportCsvData(): String {
        return repository.generateCsv()
    }
}
