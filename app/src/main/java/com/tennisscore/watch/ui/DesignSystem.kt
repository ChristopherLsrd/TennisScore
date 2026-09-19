package com.tennisscore.watch.ui

import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text

/** Accent colour of the "Tennis Watch" design (default value of its `accentColor` prop). */
val Accent = Color(0xFFE0622F)

/** Condensed bold system face standing in for the design's Oswald (no font asset is bundled). */
val ScoreFont = FontFamily(Typeface.create("sans-serif-condensed", Typeface.BOLD))

fun muted(alpha: Float): Color = Color.White.copy(alpha = alpha)

private const val DESIGN_WATCH_PX = 438f

/**
 * The design is drawn on a 438px round face. Layout values are scaled to the real screen,
 * while type is scaled less aggressively and never drops below a legible size.
 */
@Immutable
class DesignScale(private val k: Float) {
    fun dp(px: Float): Dp = (px * k).dp
    fun dp(px: Int): Dp = dp(px.toFloat())
    fun sp(px: Float): TextUnit = maxOf(px * k * 1.15f, minOf(px, 8.5f)).sp
    fun spacing(px: Float): TextUnit = (px * k).sp
}

@Composable
fun rememberDesignScale(): DesignScale {
    val config = LocalConfiguration.current
    val k = (minOf(config.screenWidthDp, config.screenHeightDp) / DESIGN_WATCH_PX).coerceAtMost(1f)
    return remember(k) { DesignScale(k) }
}

/** Small upper-case caption ("NEW MATCH", "SERVING FIRST", …). */
@Composable
fun Caption(
    text: String,
    size: Float,
    letterSpacing: Float,
    alpha: Float,
    weight: FontWeight = FontWeight.Normal
) {
    val ds = rememberDesignScale()
    Text(
        text = text,
        fontSize = ds.sp(size),
        letterSpacing = ds.spacing(letterSpacing),
        fontWeight = weight,
        color = muted(alpha),
        textAlign = TextAlign.Center
    )
}

/** Rounded pill; outlined when unselected, filled with [Accent] when selected. */
@Composable
fun Chip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    fontSize: Float,
    hPad: Float,
    vPad: Float,
    radius: Float = 14f,
    modifier: Modifier = Modifier,
    minHPad: Dp = 8.dp
) {
    val ds = rememberDesignScale()
    val shape = RoundedCornerShape(ds.dp(radius))
    val outline = if (selected) Accent else muted(0.25f)
    Box(
        modifier = modifier
            .heightIn(min = 24.dp)
            .clip(shape)
            .background(if (selected) Accent else Color.Transparent)
            .border(1.dp, outline, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = ds.dp(hPad).coerceAtLeast(minHPad), vertical = ds.dp(vPad)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = ds.sp(fontSize),
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false
        )
    }
}

/** Solid [Accent] call-to-action pill ("Start match", "Save", "New match"). */
@Composable
fun AccentButton(
    text: String,
    onClick: () -> Unit,
    fontSize: Float,
    hPad: Float,
    vPad: Float,
    radius: Float,
    modifier: Modifier = Modifier
) {
    val ds = rememberDesignScale()
    Box(
        modifier = modifier
            .heightIn(min = 28.dp)
            .clip(RoundedCornerShape(ds.dp(radius)))
            .background(Accent)
            .clickable(onClick = onClick)
            .padding(horizontal = ds.dp(hPad).coerceAtLeast(12.dp), vertical = ds.dp(vPad)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = ds.sp(fontSize),
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

/** Outlined secondary pill ("Cancel"). */
@Composable
fun OutlineButton(text: String, onClick: () -> Unit, fontSize: Float, hPad: Float, vPad: Float, radius: Float, modifier: Modifier = Modifier) {
    Chip(text, selected = false, onClick = onClick, fontSize = fontSize, hPad = hPad, vPad = vPad, radius = radius, modifier = modifier)
}

/** Translucent round icon/glyph button (gear, −/+, undo, swap, home). */
@Composable
fun CircleButton(
    glyph: String,
    onClick: () -> Unit,
    size: Float,
    glyphSize: Float,
    background: Float,
    minSize: Dp = 28.dp,
    enabled: Boolean = true
) {
    val ds = rememberDesignScale()
    Box(
        modifier = Modifier
            .size(ds.dp(size).coerceAtLeast(minSize))
            .alpha(if (enabled) 1f else 0.3f)
            .clip(CircleShape)
            .background(muted(background))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = glyph, fontSize = ds.sp(glyphSize), color = Color.White, textAlign = TextAlign.Center)
    }
}
