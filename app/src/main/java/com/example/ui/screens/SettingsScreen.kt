package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.viewmodel.ContactViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ContactViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sortByLastName by viewModel.sortByLastName.collectAsStateWithLifecycle()
    val rawContacts by viewModel.rawContacts.collectAsStateWithLifecycle()
    val favoriteContacts by viewModel.favoriteContacts.collectAsStateWithLifecycle()
    val categoryCounts by viewModel.categoryCounts.collectAsStateWithLifecycle()

    var showClearDialog by remember { mutableStateOf(false) }
    var csvExportPreview by remember { mutableStateOf<String?>(null) }

    // Permission launcher for READ_CONTACTS
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        viewModel.importDeviceContacts()
        if (!isGranted) {
            Toast.makeText(
                context,
                "Device permission not granted. Loaded sample contacts for preview.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SlateBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            // Header
            Text(
                text = "Settings",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SlateTextPrimary
            )
            Text(
                text = "Preferences, import/export & data",
                fontSize = 13.sp,
                color = SlateTextSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Sort Order Preference
            Text(
                text = "CONTACT SORT ORDER",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = SlateTextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SortOptionRow(
                        title = "First Name (A — Z)",
                        subtitle = "e.g. Alexander Wright, Amelia Chen",
                        isSelected = !sortByLastName,
                        onClick = { viewModel.setSortByLastName(false) },
                        testTag = "sort_by_first_name"
                    )

                    HorizontalDivider(
                        color = SlateBorder.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    SortOptionRow(
                        title = "Last Name (A — Z)",
                        subtitle = "e.g. Carter, Benjamin; Chen, Amelia",
                        isSelected = sortByLastName,
                        onClick = { viewModel.setSortByLastName(true) },
                        testTag = "sort_by_last_name"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Import & Export
            Text(
                text = "IMPORT & EXPORT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = SlateTextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Import Device Contacts
                    SettingsActionRow(
                        icon = Icons.Default.CloudDownload,
                        title = "Import from Device Contacts",
                        description = "Sync contacts stored on this phone or emulator",
                        iconBg = PrimaryBlue.copy(alpha = 0.1f),
                        iconTint = PrimaryBlue,
                        testTag = "import_device_contacts_button"
                    ) {
                        permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    }

                    HorizontalDivider(
                        color = SlateBorder.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // Export CSV
                    SettingsActionRow(
                        icon = Icons.Default.FileDownload,
                        title = "Export as CSV",
                        description = "Export all ${rawContacts.size} contacts to a CSV file or share sheet",
                        iconBg = Color(0xFF10B981).copy(alpha = 0.1f),
                        iconTint = Color(0xFF10B981),
                        testTag = "export_csv_button"
                    ) {
                        coroutineScope.launch {
                            val csv = viewModel.getExportCsvData()
                            csvExportPreview = csv

                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_SUBJECT, "MyContacts_Export.csv")
                                putExtra(Intent.EXTRA_TEXT, csv)
                                type = "text/csv"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Export MyContacts CSV"))
                        }
                    }

                    HorizontalDivider(
                        color = SlateBorder.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // Seed Starter Contacts
                    SettingsActionRow(
                        icon = Icons.Default.PersonAdd,
                        title = "Load Starter Contacts",
                        description = "Populate realistic sample contacts with categories & notes",
                        iconBg = Color(0xFF8B5CF6).copy(alpha = 0.1f),
                        iconTint = Color(0xFF8B5CF6),
                        testTag = "load_sample_contacts_button"
                    ) {
                        viewModel.loadSampleData()
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Database Statistics
            Text(
                text = "ADDRESS BOOK SUMMARY",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = SlateTextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    SummaryStatItem(count = rawContacts.size, label = "Total Contacts")
                    SummaryStatItem(count = favoriteContacts.size, label = "Favorites")
                    SummaryStatItem(count = categoryCounts.size, label = "Categories")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Danger Zone
            Text(
                text = "DATA MANAGEMENT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = SlateTextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsActionRow(
                        icon = Icons.Default.CleaningServices,
                        title = "Clear All Contacts",
                        description = "Delete all stored contacts from local database",
                        iconBg = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        iconTint = MaterialTheme.colorScheme.error,
                        testTag = "clear_all_contacts_button"
                    ) {
                        showClearDialog = true
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Clear Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Contacts?", fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently delete all contacts from the database. Are you sure?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        viewModel.clearAllContacts()
                    },
                    modifier = Modifier.testTag("confirm_clear_button")
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = SlateTextSecondary)
                }
            }
        )
    }

    // CSV Preview Dialog (gives quick copy option in case user wants to copy directly)
    csvExportPreview?.let { csv ->
        AlertDialog(
            onDismissRequest = { csvExportPreview = null },
            title = { Text("CSV Export Ready", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Your contacts were exported as CSV format.",
                        fontSize = 14.sp,
                        color = SlateTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateBackground)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = csv.lines().take(5).joinToString("\n") + "\n...",
                            fontSize = 11.sp,
                            color = SlateTextPrimary
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Contacts CSV", csv))
                        Toast.makeText(context, "CSV copied to clipboard", Toast.LENGTH_SHORT).show()
                        csvExportPreview = null
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy to Clipboard")
                }
            },
            dismissButton = {
                TextButton(onClick = { csvExportPreview = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun SortOptionRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isSelected) PrimaryBlue else SlateTextSecondary,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = SlateTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = SlateTextSecondary
            )
        }
    }
}

@Composable
fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    description: String,
    iconBg: Color,
    iconTint: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = SlateTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = SlateTextSecondary
            )
        }
    }
}

@Composable
fun SummaryStatItem(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = PrimaryBlue
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = SlateTextSecondary
        )
    }
}
