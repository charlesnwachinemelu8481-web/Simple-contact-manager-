package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryBlue

val AlphabetLetters = ('A'..'Z').toList() + listOf('#')

@Composable
fun AlphabeticalIndexBar(
    availableLetters: Set<Char>,
    selectedLetter: Char?,
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    var totalHeightPx by remember { mutableStateOf(1f) }
    var activeBubbleLetter by remember { mutableStateOf<Char?>(null) }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(28.dp)
            .padding(vertical = 8.dp)
            .onGloballyPositioned { coordinates ->
                totalHeightPx = coordinates.size.height.toFloat().coerceAtLeast(1f)
            }
            .pointerInput(availableLetters) {
                fun handleY(y: Float) {
                    val step = totalHeightPx / AlphabetLetters.size
                    val index = (y / step).toInt().coerceIn(0, AlphabetLetters.lastIndex)
                    val letter = AlphabetLetters[index]
                    activeBubbleLetter = letter
                    onLetterSelected(letter)
                }

                detectTapGestures(
                    onPress = { offset ->
                        handleY(offset.y)
                        tryAwaitRelease()
                        activeBubbleLetter = null
                    }
                )
            }
            .pointerInput(availableLetters) {
                fun handleY(y: Float) {
                    val step = totalHeightPx / AlphabetLetters.size
                    val index = (y / step).toInt().coerceIn(0, AlphabetLetters.lastIndex)
                    val letter = AlphabetLetters[index]
                    activeBubbleLetter = letter
                    onLetterSelected(letter)
                }

                detectDragGestures(
                    onDragStart = { offset -> handleY(offset.y) },
                    onDragEnd = { activeBubbleLetter = null },
                    onDragCancel = { activeBubbleLetter = null },
                    onDrag = { change, _ ->
                        change.consume()
                        handleY(change.position.y)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AlphabetLetters.forEach { char ->
                val isPresent = char in availableLetters
                val isHighlighted = char == selectedLetter

                Text(
                    text = char.toString(),
                    fontSize = 10.sp,
                    fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
                    color = when {
                        isHighlighted -> PrimaryBlue
                        isPresent -> MaterialTheme.colorScheme.onSurface
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    },
                    modifier = Modifier
                        .testTag("alphabet_index_$char")
                        .padding(vertical = 0.5.dp)
                )
            }
        }

        // Active dragging bubble popup
        activeBubbleLetter?.let { char ->
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(end = 40.dp)
                    .shadow(6.dp, CircleShape)
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = char.toString(),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
