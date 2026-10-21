package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.AvatarColors
import kotlin.math.abs

@Composable
fun ContactAvatar(
    initials: String,
    nameKey: String,
    photoUri: String? = null,
    size: Dp = 50.dp,
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Transparent,
    modifier: Modifier = Modifier
) {
    // Generate deterministic avatar color from name or initials
    val avatarColor = remember(nameKey, initials) {
        val hash = (nameKey.ifBlank { initials }).hashCode()
        AvatarColors[abs(hash) % AvatarColors.size]
    }

    val textSize = remember(size) {
        when {
            size >= 90.dp -> 32.sp
            size >= 70.dp -> 24.sp
            size >= 48.dp -> 18.sp
            else -> 14.sp
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (borderWidth > 0.dp) {
                    Modifier.border(borderWidth, borderColor, CircleShape)
                } else {
                    Modifier
                }
            )
            .background(avatarColor),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUri.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photoUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Contact photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
            )
        } else {
            Text(
                text = initials.take(2),
                color = Color.White,
                fontSize = textSize,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
