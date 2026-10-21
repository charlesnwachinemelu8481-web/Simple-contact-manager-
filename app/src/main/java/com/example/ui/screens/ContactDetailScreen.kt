package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(
    contactId: Long,
    viewModel: ContactViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val context = LocalContext.current
    val contactState by viewModel.getContactById(contactId).collectAsStateWithLifecycle(initialValue = null)
    var showDeleteDialog by remember { mutableStateOf(false) }

    val contact = contactState

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SlateBackground,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SlateTextPrimary
                        )
                    }
                },
                actions = {
                    if (contact != null) {
                        // Favorite toggle
                        IconButton(
                            onClick = { viewModel.toggleFavorite(contact.id, contact.isFavorite) },
                            modifier = Modifier.testTag("favorite_star_detail")
                        ) {
                            Icon(
                                imageVector = if (contact.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                                contentDescription = if (contact.isFavorite) "Starred" else "Star",
                                tint = if (contact.isFavorite) AccentGold else SlateTextSecondary
                            )
                        }

                        // Edit button
                        IconButton(
                            onClick = { viewModel.navigateTo(Screen.AddEditContact(contact.id)) },
                            modifier = Modifier.testTag("edit_contact_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit contact",
                                tint = PrimaryBlue
                            )
                        }

                        // Delete button
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.testTag("delete_contact_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete contact",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SlateBackground)
            )
        }
    ) { innerPadding ->
        if (contact == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Contact not found", color = SlateTextSecondary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large Avatar Header
                ContactAvatar(
                    initials = contact.initials,
                    nameKey = contact.fullName,
                    photoUri = contact.photoUri,
                    size = 104.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = contact.fullName,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateTextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (contact.category.isNotBlank()) {
                    CategoryTag(
                        category = contact.category,
                        showIcon = true,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Quick Action Icon Row: Call, Message, Email, Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuickActionButton(
                        icon = Icons.Default.Phone,
                        label = "Call",
                        backgroundColor = PrimaryBlue,
                        enabled = contact.phone.isNotBlank(),
                        testTag = "quick_action_call"
                    ) {
                        try {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Cannot dial number", Toast.LENGTH_SHORT).show()
                        }
                    }

                    QuickActionButton(
                        icon = Icons.Default.Message,
                        label = "Message",
                        backgroundColor = Color(0xFF6366F1),
                        enabled = contact.phone.isNotBlank(),
                        testTag = "quick_action_message"
                    ) {
                        try {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${contact.phone}"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Cannot open messaging", Toast.LENGTH_SHORT).show()
                        }
                    }

                    QuickActionButton(
                        icon = Icons.Default.Email,
                        label = "Email",
                        backgroundColor = Color(0xFF0D9488),
                        enabled = contact.email.isNotBlank(),
                        testTag = "quick_action_email"
                    ) {
                        try {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${contact.email}"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Cannot open email client", Toast.LENGTH_SHORT).show()
                        }
                    }

                    QuickActionButton(
                        icon = Icons.Default.Share,
                        label = "Share",
                        backgroundColor = SlateTextSecondary,
                        enabled = true,
                        testTag = "quick_action_share"
                    ) {
                        val shareText = buildString {
                            append("Contact: ${contact.fullName}\n")
                            if (contact.phone.isNotBlank()) append("Phone: ${contact.phone}\n")
                            if (contact.email.isNotBlank()) append("Email: ${contact.email}\n")
                            if (contact.category.isNotBlank()) append("Category: ${contact.category}\n")
                            if (contact.notes.isNotBlank()) append("Notes: ${contact.notes}\n")
                        }
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Contact"))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Full Details List Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Phone Detail
                        DetailItemRow(
                            icon = Icons.Default.Phone,
                            title = "Phone",
                            value = contact.phone.ifBlank { "No phone number added" },
                            isActionable = contact.phone.isNotBlank(),
                            onCopy = { copyToClipboard(contact.phone, "Phone number") }
                        )

                        HorizontalDivider(
                            color = SlateBorder.copy(alpha = 0.6f),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        // Email Detail
                        DetailItemRow(
                            icon = Icons.Default.Email,
                            title = "Email",
                            value = contact.email.ifBlank { "No email added" },
                            isActionable = contact.email.isNotBlank(),
                            onCopy = { copyToClipboard(contact.email, "Email address") }
                        )

                        if (contact.notes.isNotBlank()) {
                            HorizontalDivider(
                                color = SlateBorder.copy(alpha = 0.6f),
                                modifier = Modifier.padding(vertical = 12.dp)
                            )

                            // Notes Detail
                            DetailItemRow(
                                icon = Icons.Default.Notes,
                                title = "Notes",
                                value = contact.notes,
                                isActionable = true,
                                onCopy = { copyToClipboard(contact.notes, "Notes") }
                            )
                        }

                        HorizontalDivider(
                            color = SlateBorder.copy(alpha = 0.6f),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        // Created info
                        val formattedDate = remember(contact.createdAt) {
                            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            sdf.format(Date(contact.createdAt))
                        }
                        Text(
                            text = "Added on $formattedDate",
                            fontSize = 12.sp,
                            color = SlateTextSecondary,
                            modifier = Modifier.padding(start = 36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && contact != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Contact?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete ${contact.fullName}? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteContact(contact.id)
                    },
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = SlateTextSecondary)
                }
            }
        )
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    backgroundColor: Color,
    enabled: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .testTag(testTag)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (enabled) backgroundColor else SlateBorder),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (enabled) Color.White else SlateTextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (enabled) SlateTextPrimary else SlateTextSecondary.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun DetailItemRow(
    icon: ImageVector,
    title: String,
    value: String,
    isActionable: Boolean,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SlateBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = SlateTextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = SlateTextPrimary
            )
        }

        if (isActionable) {
            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy $title",
                    tint = SlateTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
