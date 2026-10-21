package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CategoryFamily
import com.example.ui.theme.CategoryFamilyBg
import com.example.ui.theme.CategoryFriends
import com.example.ui.theme.CategoryFriendsBg
import com.example.ui.theme.CategoryOther
import com.example.ui.theme.CategoryOtherBg
import com.example.ui.theme.CategoryWork
import com.example.ui.theme.CategoryWorkBg

fun getCategoryColor(category: String): Color {
    return when (category.lowercase().trim()) {
        "family" -> CategoryFamily
        "work" -> CategoryWork
        "friends" -> CategoryFriends
        else -> CategoryOther
    }
}

fun getCategoryBg(category: String): Color {
    return when (category.lowercase().trim()) {
        "family" -> CategoryFamilyBg
        "work" -> CategoryWorkBg
        "friends" -> CategoryFriendsBg
        else -> CategoryOtherBg
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase().trim()) {
        "family" -> Icons.Default.Home
        "work" -> Icons.Default.Business
        "friends" -> Icons.Default.Group
        else -> Icons.Default.Label
    }
}

@Composable
fun CategoryTag(
    category: String,
    modifier: Modifier = Modifier,
    showIcon: Boolean = false
) {
    val color = getCategoryColor(category)
    val bg = getCategoryBg(category)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showIcon) {
                Icon(
                    imageVector = getCategoryIcon(category),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            } else {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(5.dp))
            }
            Text(
                text = category,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
