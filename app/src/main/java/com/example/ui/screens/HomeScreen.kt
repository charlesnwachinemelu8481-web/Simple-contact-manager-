package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Contact
import com.example.ui.components.AlphabeticalIndexBar
import com.example.ui.components.CategoryTag
import com.example.ui.components.ContactAvatar
import com.example.ui.theme.AccentGold
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.viewmodel.ContactViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ContactViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortByLastName by viewModel.sortByLastName.collectAsStateWithLifecycle()
    val groupedContacts by viewModel.groupedContacts.collectAsStateWithLifecycle()
    val favoriteContacts by viewModel.favoriteContacts.collectAsStateWithLifecycle()
    val availableLetters by viewModel.availableLetters.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Compute the index of each letter section header inside the LazyColumn
    val letterSectionIndices by remember(groupedContacts, favoriteContacts, searchQuery) {
        derivedStateOf {
            val map = mutableMapOf<Char, Int>()
            var currentIndex = 0

            // If favorites row is shown (when not searching and favorites exist)
            if (searchQuery.isBlank() && favoriteContacts.isNotEmpty()) {
                currentIndex += 1 // 1 for the Favorites Section
            }

            groupedContacts.forEach { (char, contactsForChar) ->
                map[char] = currentIndex
                currentIndex += 1 // Header item
                currentIndex += contactsForChar.size // Contact rows
            }
            map
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SlateBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateTo(Screen.AddEditContact(null)) },
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .testTag("add_contact_fab")
                    .padding(bottom = 12.dp, end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Contact",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add Contact",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // App Title Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "MyContacts",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SlateTextPrimary
                    )
                    Text(
                        text = if (sortByLastName) "Sorted by Last Name" else "Sorted by First Name",
                        fontSize = 12.sp,
                        color = SlateTextSecondary
                    )
                }
            }

            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = {
                        Text(
                            text = "Search contacts...",
                            color = SlateTextSecondary,
                            fontSize = 15.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = PrimaryBlue
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.onSearchQueryChanged("") },
                                modifier = Modifier.testTag("clear_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = SlateTextSecondary
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SlateSurface,
                        unfocusedContainerColor = SlateSurface,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = SlateBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input")
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Content Area with Alphabetical Index Rail
            Box(modifier = Modifier.weight(1f)) {
                if (groupedContacts.isEmpty()) {
                    // Empty state
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryBlue.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonOutline,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "No contacts found" else "No contacts yet",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (searchQuery.isNotBlank())
                                    "Try searching with a different name or number"
                                else
                                    "Tap '+ Add Contact' to create your first contact",
                                fontSize = 14.sp,
                                color = SlateTextSecondary
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(end = 30.dp), // Leave space for index bar
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        // 1. Favorites Horizontal Scroll at Top
                        if (searchQuery.isBlank() && favoriteContacts.isNotEmpty()) {
                            item(key = "favorites_section") {
                                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = AccentGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "FAVORITES",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                            color = SlateTextSecondary
                                        )
                                    }

                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        items(favoriteContacts, key = { "fav_${it.id}" }) { contact ->
                                            FavoriteAvatarItem(
                                                contact = contact,
                                                onClick = {
                                                    viewModel.navigateTo(Screen.ContactDetail(contact.id))
                                                }
                                            )
                                        }
                                    }
                                    HorizontalDivider(
                                        color = SlateBorder.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        // 2. Alphabetically Grouped Contacts
                        groupedContacts.forEach { (letter, contactsInLetter) ->
                            item(key = "header_$letter") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SlateBackground)
                                        .padding(horizontal = 20.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = letter.toString(),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlue
                                    )
                                }
                            }

                            items(contactsInLetter, key = { it.id }) { contact ->
                                ContactRowItem(
                                    contact = contact,
                                    sortByLastName = sortByLastName,
                                    onClick = {
                                        viewModel.navigateTo(Screen.ContactDetail(contact.id))
                                    },
                                    onFavoriteToggle = {
                                        viewModel.toggleFavorite(contact.id, contact.isFavorite)
                                    }
                                )
                            }
                        }
                    }

                    // Alphabetical index rail along the right edge
                    AlphabeticalIndexBar(
                        availableLetters = availableLetters,
                        selectedLetter = null,
                        onLetterSelected = { letter ->
                            letterSectionIndices[letter]?.let { targetIndex ->
                                coroutineScope.launch {
                                    listState.scrollToItem(targetIndex)
                                }
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteAvatarItem(
    contact: Contact,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .width(62.dp)
            .testTag("favorite_avatar_${contact.id}")
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            ContactAvatar(
                initials = contact.initials,
                nameKey = contact.fullName,
                photoUri = contact.photoUri,
                size = 52.dp
            )
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(SlateSurface)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = contact.firstName.ifBlank { contact.fullName },
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = SlateTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ContactRowItem(
    contact: Contact,
    sortByLastName: Boolean,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .clickable(onClick = onClick)
            .testTag("contact_item_${contact.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ContactAvatar(
                initials = contact.initials,
                nameKey = contact.fullName,
                photoUri = contact.photoUri,
                size = 46.dp
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.displayName(sortByLastName),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SlateTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (contact.phone.isNotBlank()) {
                        Text(
                            text = contact.phone,
                            fontSize = 13.sp,
                            color = SlateTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (contact.category.isNotBlank()) {
                        CategoryTag(category = contact.category)
                    }
                }
            }

            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("favorite_toggle_${contact.id}")
            ) {
                Icon(
                    imageVector = if (contact.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (contact.isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (contact.isFavorite) AccentGold else SlateTextSecondary.copy(alpha = 0.6f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
