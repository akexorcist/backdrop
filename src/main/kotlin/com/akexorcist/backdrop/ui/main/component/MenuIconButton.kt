package com.akexorcist.backdrop.ui.main.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class IconButtonColors(
    val buttonColors: @Composable () -> ButtonColors,
) {
    Normal(
        buttonColors = {
            ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.7f),
                contentColor = MaterialTheme.colors.onSurface,
                disabledBackgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.125f),
                disabledContentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.125f),
            )
        }
    ),
    Close(
        buttonColors = {
            ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.3f),
                contentColor = MaterialTheme.colors.onError,
                disabledBackgroundColor = MaterialTheme.colors.error.copy(alpha = 0.1f),
                disabledContentColor = MaterialTheme.colors.onError.copy(alpha = 0.1f),
            )
        }
    );
}

@Composable
fun MenuIconButton(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource,
    enabled: Boolean,
    colors: IconButtonColors = IconButtonColors.Normal,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Button(
        modifier = Modifier.size(48.dp).then(modifier),
        colors = colors.buttonColors(),
        elevation = ButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp,
            hoveredElevation = 0.dp,
            focusedElevation = 0.dp,
        ),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        interactionSource = interactionSource,
        onClick = onClick,
    ) {
        content()
    }
}