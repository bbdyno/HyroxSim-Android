//
//  HyroxMobileTheme.kt
//  ui-mobile
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.ui.mobile

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val noFontPadding = PlatformTextStyle(includeFontPadding = false)

object HyroxMobileDesign {
    object Colors {
        val Background = Color(0xFF000000)
        val Surface = Color(0xFF141414)
        val SurfaceElevated = Color(0xFF1F1F1F)
        val Accent = Color(0xFFFFD700)
        val AccentDim = Color(0x4DFFD700)
        val TextPrimary = Color(0xFFFFFFFF)
        val TextSecondary = Color(0xFF8C8C8C)
        val TextTertiary = Color(0xFF595959)
        val RunAccent = Color(0xFF4D99FF)
        val RoxZoneAccent = Color(0xFFFF9933)
        val StationAccent = Accent
        val RunBackground = Color(0xFF0D264D)
        val RoxZoneBackground = Color(0xFF402600)
        val StationBackground = Color(0xFF261F00)
        val Hairline = Color(0x14FFFFFF)
        val Divider = Color(0x1AFFFFFF)
        val Destructive = Color(0xFFFF453A)
        val Success = Color(0xFF33CC66)
    }

    object Radius {
        val Card = 16.dp
        val Badge = 4.dp
        val Pill = 24.dp
        val Button = 24.dp
        val Circle = 28.dp
    }

    object Typography {
        val Title = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Black,
            fontSize = 34.sp,
            lineHeight = 38.sp,
            letterSpacing = (-0.4).sp,
            platformStyle = noFontPadding,
        )
        val Headline = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 26.sp,
            platformStyle = noFontPadding,
        )
        val CardTitle = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Black,
            fontSize = 26.sp,
            lineHeight = 28.sp,
            platformStyle = noFontPadding,
        )
        val Section = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 1.2.sp,
            platformStyle = noFontPadding,
        )
        val Body = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            platformStyle = noFontPadding,
        )
        val Caption = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            platformStyle = noFontPadding,
        )
        val Label = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 0.6.sp,
            platformStyle = noFontPadding,
        )
        val LargeNumber = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 48.sp,
            lineHeight = 52.sp,
            platformStyle = noFontPadding,
        )
        val MediumNumber = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            lineHeight = 32.sp,
            platformStyle = noFontPadding,
        )
        val SmallNumber = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            platformStyle = noFontPadding,
        )
    }
}

private val hyroxColorScheme = darkColorScheme(
    primary = HyroxMobileDesign.Colors.Accent,
    onPrimary = Color.Black,
    secondary = HyroxMobileDesign.Colors.RoxZoneAccent,
    onSecondary = Color.Black,
    tertiary = HyroxMobileDesign.Colors.RunAccent,
    onTertiary = Color.White,
    background = HyroxMobileDesign.Colors.Background,
    onBackground = HyroxMobileDesign.Colors.TextPrimary,
    surface = HyroxMobileDesign.Colors.Surface,
    onSurface = HyroxMobileDesign.Colors.TextPrimary,
    surfaceVariant = HyroxMobileDesign.Colors.SurfaceElevated,
    onSurfaceVariant = HyroxMobileDesign.Colors.TextSecondary,
    outline = HyroxMobileDesign.Colors.Hairline,
    error = HyroxMobileDesign.Colors.Destructive,
)

private val hyroxTypography = Typography(
    displaySmall = HyroxMobileDesign.Typography.Title,
    headlineSmall = HyroxMobileDesign.Typography.Headline,
    titleLarge = HyroxMobileDesign.Typography.CardTitle,
    titleMedium = HyroxMobileDesign.Typography.Body.copy(
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Bold,
    ),
    titleSmall = HyroxMobileDesign.Typography.Body.copy(
        fontSize = 14.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Bold,
    ),
    bodyLarge = HyroxMobileDesign.Typography.Body.copy(fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = HyroxMobileDesign.Typography.Body,
    bodySmall = HyroxMobileDesign.Typography.Caption,
    labelLarge = HyroxMobileDesign.Typography.Label.copy(fontSize = 12.sp),
    labelMedium = HyroxMobileDesign.Typography.Label,
    labelSmall = HyroxMobileDesign.Typography.Label.copy(fontSize = 10.sp),
)

private val hyroxShapes = Shapes(
    extraSmall = RoundedCornerShape(HyroxMobileDesign.Radius.Badge),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(HyroxMobileDesign.Radius.Card),
    large = RoundedCornerShape(HyroxMobileDesign.Radius.Button),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun HyroxMobileTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = hyroxColorScheme,
        typography = hyroxTypography,
        shapes = hyroxShapes,
        content = content,
    )
}
