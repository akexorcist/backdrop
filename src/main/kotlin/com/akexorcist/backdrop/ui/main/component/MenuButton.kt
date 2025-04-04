package com.akexorcist.backdrop.ui.main.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.akexorcist.backdrop.resource.StringResource

@Composable
fun MenuButtonContainer(
    isFullScreen: Boolean,
    isToggleConsoleUiClickable: Boolean,
    isDeviceConsoleShowing: Boolean,
    isMenuHiding: Boolean,
    onHidingMenuHovered: (Boolean) -> Unit,
    onToggleConsoleUiClick: () -> Unit,
    onEnterFullscreen: () -> Unit,
    onExitFullscreenClick: () -> Unit,
    onCloseAppClick: () -> Unit,
) {
    val animatedButtonAlpha by animateFloatAsState(
        targetValue = if (isMenuHiding) 0f else 1f,
        animationSpec = tween(
            durationMillis = 300,
            easing = LinearEasing,
        )
    )
    Column(modifier = Modifier.alpha(animatedButtonAlpha)) {
        CloseButton(
            onHidingMenuHovered = onHidingMenuHovered,
            onClick = onCloseAppClick,
        )
        Spacer(Modifier.size(16.dp))
        FullscreenButton(
            isFullScreen = isFullScreen,
            onHidingMenuHovered = onHidingMenuHovered,
            onClick = when (isFullScreen) {
                true -> onExitFullscreenClick
                false -> onEnterFullscreen
            },
        )
        Spacer(Modifier.size(16.dp))
        ToggleUiButton(
            clickable = isToggleConsoleUiClickable,
            isDeviceConsoleShowing = isDeviceConsoleShowing,
            onHidingMenuHovered = onHidingMenuHovered,
            onClick = onToggleConsoleUiClick,
        )
    }
}

@Composable
private fun ToggleUiButton(
    clickable: Boolean,
    isDeviceConsoleShowing: Boolean,
    onHidingMenuHovered: (Boolean) -> Unit,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val animatedIconRotate by animateFloatAsState(
        targetValue = if (isDeviceConsoleShowing) 0f else 180f,
        animationSpec = tween(durationMillis = 300)
    )

    LaunchedEffect(isHovered) { onHidingMenuHovered(isHovered) }

    MenuIconButton(
        interactionSource = interactionSource,
        enabled = clickable,
        onClick = onClick,
    ) {
        Icon(
            modifier = Modifier.rotate(animatedIconRotate),
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = StringResource.menuToggleUiDisplayContentDescription,
        )
    }
}

@Composable
private fun CloseButton(
    onHidingMenuHovered: (Boolean) -> Unit,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    LaunchedEffect(isHovered) { onHidingMenuHovered(isHovered) }

    MenuIconButton(
        interactionSource = interactionSource,
        enabled = true,
        colors = IconButtonColors.Close,
        onClick = onClick,
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = StringResource.menuCloseAppContentDescription,
        )
    }
}

@Composable
private fun FullscreenButton(
    isFullScreen: Boolean,
    onHidingMenuHovered: (Boolean) -> Unit,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    LaunchedEffect(isHovered) { onHidingMenuHovered(isHovered) }

    MenuIconButton(
        interactionSource = interactionSource,
        enabled = true,
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(
                when (isFullScreen) {
                    true -> "image/ic_exit_fullscreen.svg"
                    false -> "image/ic_enter_fullscreen.svg"
                }
            ),
            contentDescription = when (isFullScreen) {
                true -> StringResource.menuExitFromFullscreenContentDescription
                false -> StringResource.menuEnterToFullscreenContentDescription
            },
        )
    }
}
