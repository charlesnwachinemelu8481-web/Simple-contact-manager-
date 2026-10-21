package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ContactAvatar
import com.example.ui.components.getCategoryBg
import com.example.ui.components.getCategoryColor
import com.example.ui.theme.AccentGold
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.viewmodel.ContactViewModel
import kotlinx.coroutines.flow.first

val PredefinedCategories = listOf("Family", "Work", "Friends", "Other")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditContactScreen(
    contactId: Long?,
    viewModel: ContactViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Friends") }
    var customCategoryText by remember { mutableStateOf("") }
    var isCustomCategory by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }
    var isFavorite by remember { mutableStateOf(false) }

    var isLoaded by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    // Load existing contact data if editing
    LaunchedEffect(contactId) {
        if (contactId != null && contactId > 0 && !isLoaded) {
            val contact = viewModel.getContactById(contactId).first()
            if (contact != null) {
                firstName = contact.firstName
                lastName = contact.lastName
                phone = contact.phone
                email = contact.email
                notes = contact.notes
                photoUri = contact.photoUri
                isFavorite = contact.isFavorite

                if (contact.category in PredefinedCategories) {
                    selectedCategory = contact.category
                    isCustomCategory = false
                } else if (contact.category.isNotBlank()) {
                    isCustomCategory = true
                    customCategoryText = contact.category
                    selectedCategory = contact.category
                }
            }
            isLoaded = true
        }
    }

    // Modern zero-permission Android Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri.toString()
        }
    }

    fun submitSave() {
        if (firstName.isBlank() && lastName.isBlank()) {
            showError = true
            return
        }

        val categoryToSave = if (isCustomCategory) {
            customCategoryText.trim().ifBlank { "Other" }
        } else {
            selectedCategory
        }

        viewModel.saveContact(
            id = contactId ?: 0L,
            firstName = firstName,
            lastName = lastName,
            phone = phone,
            email = email,
            category = categoryToSave,
            notes = notes,
            photoUri = photoUri,
            isFavorite = isFavorite
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SlateBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (contactId != null && contactId > 0) "Edit Contact" else "New Contact",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = SlateTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Cancel and go back",
                            tint = SlateTextPrimary
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { submitSave() },
                        modifier = Modifier.testTag("save_contact_top_button")
                    ) {
                        Text(
                            text = "Save",
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SlateBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            // Photo Picker Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .testTag("photo_picker_avatar")
                    ) {
                        val previewInitials = remember(firstName, lastName) {
                            val f = firstName.trim().firstOrNull()?.uppercaseChar()
                            val l = lastName.trim().firstOrNull()?.uppercaseChar()
                            when {
                                f != null && l != null -> "$f$l"
                                f != null -> "$f"
                                l != null -> "$l"
                                else -> "?"
                            }
                        }

                        ContactAvatar(
                            initials = previewInitials,
                            nameKey = "$firstName $lastName",
                            photoUri = photoUri,
                            size = 96.dp
                        )

                        // Camera icon badge
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue)
                                .border(2.dp, SlateBackground, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Pick Photo",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        ) {
                            Text(
                                text = if (photoUri != null) "Change Photo" else "Add Photo",
                                color = PrimaryBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (photoUri != null) {
                            TextButton(
                                onClick = { photoUri = null }
                            ) {
                                Text(
                                    text = "Remove",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Name Inputs Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = {
                            firstName = it
                            if (showError) showError = false
                        },
                        label = { Text("First Name") },
                        isError = showError && firstName.isBlank() && lastName.isBlank(),
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue)
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            keyboardType = KeyboardType.Text
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("first_name_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = lastName,
                        onValueChange = {
                            lastName = it
                            if (showError) showError = false
                        },
                        label = { Text("Last Name") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue)
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            keyboardType = KeyboardType.Text
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("last_name_input")
                    )

                    if (showError) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Please enter at least a first or last name.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contact Info (Phone & Email) Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = PrimaryBlue)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("phone_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = PrimaryBlue)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category & Favorite Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Category / Label",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SlateTextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PredefinedCategories.forEach { category ->
                            val isSelected = !isCustomCategory && selectedCategory.equals(category, ignoreCase = true)
                            val catColor = getCategoryColor(category)
                            val catBg = getCategoryBg(category)

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) catColor else catBg)
                                    .border(
                                        1.dp,
                                        if (isSelected) catColor else SlateBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        selectedCategory = category
                                        isCustomCategory = false
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                    .testTag("category_chip_$category")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = category,
                                        color = if (isSelected) Color.White else catColor,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        // Custom Category Chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isCustomCategory) PrimaryBlue else SlateBackground)
                                .border(1.dp, if (isCustomCategory) PrimaryBlue else SlateBorder, RoundedCornerShape(10.dp))
                                .clickable { isCustomCategory = true }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "+ Custom",
                                color = if (isCustomCategory) Color.White else SlateTextSecondary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    if (isCustomCategory) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = customCategoryText,
                            onValueChange = { customCategoryText = it },
                            label = { Text("Custom Category Name") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Favorite Toggle row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SlateBackground)
                            .clickable { isFavorite = !isFavorite }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                                contentDescription = null,
                                tint = if (isFavorite) AccentGold else SlateTextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Mark as Favorite",
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                                color = SlateTextPrimary
                            )
                        }

                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Check else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            tint = if (isFavorite) AccentGold else Color.Transparent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes & Details") },
                        placeholder = { Text("e.g. Birthday, meeting notes, address...") },
                        leadingIcon = {
                            Icon(Icons.Default.Notes, contentDescription = null, tint = PrimaryBlue)
                        },
                        minLines = 3,
                        maxLines = 6,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("notes_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Contact primary button
            Button(
                onClick = { submitSave() },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_contact_button")
            ) {
                Text(
                    text = "Save Contact",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
