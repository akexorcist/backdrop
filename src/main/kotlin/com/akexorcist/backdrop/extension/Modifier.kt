package com.akexorcist.backdrop.extension

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.surfaceBackground() = this.background(
    color = MaterialTheme.colors.surface.copy(alpha = 0.7f),
    shape = RoundedCornerShape(16.dp),
).padding(horizontal = 8.dp, vertical = 16.dp)
