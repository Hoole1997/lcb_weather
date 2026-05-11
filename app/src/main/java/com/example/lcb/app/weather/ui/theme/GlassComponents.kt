package com.example.lcb.app.weather.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Foreground/text/icon color used over the weather gradient backgrounds.
 * The whole UI lives on top of a saturated gradient sky, so we keep the
 * primary content colour in a near-white tone for strong contrast.
 */
val GlassOnSurface: Color = Color(0xFFF6F9FB)
val GlassOnSurfaceMuted: Color = Color(0xCCF6F9FB)
val GlassOnSurfaceFaint: Color = Color(0x99F6F9FB)

/**
 * Translucent glass surface used for cards, pills and rows. We tint the
 * surface with a very light alpha so the gradient shows through, and add
 * a hairline border to suggest a frosted glass edge.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    tint: Color = Color.White.copy(alpha = 0.14f),
    borderColor: Color = Color.White.copy(alpha = 0.22f),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    CompositionLocalProvider(LocalContentColor provides GlassOnSurface) {
        Column(
            modifier = modifier
                .clip(RoundedCornerShape(cornerRadius))
                .background(tint)
                .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(cornerRadius))
                .padding(contentPadding),
            content = content
        )
    }
}

/**
 * Glass card with a section title in the header.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    tint: Color = Color.White.copy(alpha = 0.13f),
    borderColor: Color = Color.White.copy(alpha = 0.20f),
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable ColumnScope.() -> Unit
) = GlassSurface(
    modifier = modifier,
    cornerRadius = cornerRadius,
    tint = tint,
    borderColor = borderColor,
    contentPadding = contentPadding,
    content = content
)

/**
 * Top app bar icon button used over the gradient sky. Flat / borderless,
 * matches the Android Material 3 top app bar style with a 48dp tap target,
 * 24dp icon and the standard ripple on press.
 */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = GlassOnSurface
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp)
        )
    }
}
