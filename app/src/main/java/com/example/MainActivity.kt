package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AddEditContactScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.CategoryDetailScreen
import com.example.ui.screens.ContactDetailScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AccentGold
import com.example.ui.theme.MyContactsTheme
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.viewmodel.ContactViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyContactsTheme {
                MyContactsApp()
            }
        }
    }
}

@Composable
fun MyContactsApp(
    viewModel: ContactViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val showBottomBar = currentScreen is Screen.Home ||
            currentScreen is Screen.Favorites ||
            currentScreen is Screen.Categories ||
            currentScreen is Screen.Settings

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground),
        contentAlignment = Alignment.TopCenter
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp),
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = SlateBackground,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = SlateSurface,
                        tonalElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        NavigationBarItem(
                            selected = currentScreen is Screen.Home,
                            onClick = { viewModel.navigateTo(Screen.Home) },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen is Screen.Home) Icons.Filled.Contacts else Icons.Outlined.Contacts,
                                    contentDescription = "Contacts"
                                )
                            },
                            label = { Text("Contacts") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryBlue,
                                selectedTextColor = PrimaryBlue,
                                indicatorColor = PrimaryBlue.copy(alpha = 0.12f),
                                unselectedIconColor = SlateTextSecondary,
                                unselectedTextColor = SlateTextSecondary
                            ),
                            modifier = Modifier.testTag("nav_contacts")
                        )

                        NavigationBarItem(
                            selected = currentScreen is Screen.Favorites,
                            onClick = { viewModel.navigateTo(Screen.Favorites) },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen is Screen.Favorites) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "Favorites"
                                )
                            },
                            label = { Text("Favorites") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AccentGold,
                                selectedTextColor = SlateTextPrimary,
                                indicatorColor = AccentGold.copy(alpha = 0.18f),
                                unselectedIconColor = SlateTextSecondary,
                                unselectedTextColor = SlateTextSecondary
                            ),
                            modifier = Modifier.testTag("nav_favorites")
                        )

                        NavigationBarItem(
                            selected = currentScreen is Screen.Categories,
                            onClick = { viewModel.navigateTo(Screen.Categories) },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen is Screen.Categories) Icons.Filled.Category else Icons.Outlined.Category,
                                    contentDescription = "Categories"
                                )
                            },
                            label = { Text("Categories") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryBlue,
                                selectedTextColor = PrimaryBlue,
                                indicatorColor = PrimaryBlue.copy(alpha = 0.12f),
                                unselectedIconColor = SlateTextSecondary,
                                unselectedTextColor = SlateTextSecondary
                            ),
                            modifier = Modifier.testTag("nav_categories")
                        )

                        NavigationBarItem(
                            selected = currentScreen is Screen.Settings,
                            onClick = { viewModel.navigateTo(Screen.Settings) },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen is Screen.Settings) Icons.Filled.Settings else Icons.Outlined.Settings,
                                    contentDescription = "Settings"
                                )
                            },
                            label = { Text("Settings") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryBlue,
                                selectedTextColor = PrimaryBlue,
                                indicatorColor = PrimaryBlue.copy(alpha = 0.12f),
                                unselectedIconColor = SlateTextSecondary,
                                unselectedTextColor = SlateTextSecondary
                            ),
                            modifier = Modifier.testTag("nav_settings")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        is Screen.Home -> HomeScreen(viewModel = viewModel)
                        is Screen.Favorites -> FavoritesScreen(viewModel = viewModel)
                        is Screen.Categories -> CategoriesScreen(viewModel = viewModel)
                        is Screen.CategoryDetail -> CategoryDetailScreen(
                            category = screen.category,
                            viewModel = viewModel
                        )
                        is Screen.Settings -> SettingsScreen(viewModel = viewModel)
                        is Screen.ContactDetail -> ContactDetailScreen(
                            contactId = screen.contactId,
                            viewModel = viewModel
                        )
                        is Screen.AddEditContact -> AddEditContactScreen(
                            contactId = screen.contactId,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}
