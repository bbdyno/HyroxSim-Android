//
//  HyroxMobileComponents.kt
//  ui-mobile
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.ui.mobile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun HyroxSurfaceCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        )
    } else {
        Modifier
    }

    Surface(
        modifier = modifier.then(clickableModifier),
        shape = RoundedCornerShape(HyroxMobileDesign.Radius.Card),
        color = HyroxMobileDesign.Colors.Surface,
        border = BorderStroke(1.dp, HyroxMobileDesign.Colors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}

@Composable
fun HyroxSectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = HyroxMobileDesign.Typography.Section,
        color = HyroxMobileDesign.Colors.TextTertiary,
    )
}

@Composable
fun HyroxBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = HyroxMobileDesign.Colors.Accent,
    contentColor: Color = Color.Black,
) {
    Box(
        modifier = modifier
            .background(
                color = containerColor,
                shape = RoundedCornerShape(HyroxMobileDesign.Radius.Badge),
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = HyroxMobileDesign.Typography.Label,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun HyroxPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(HyroxMobileDesign.Radius.Button),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = HyroxMobileDesign.Colors.Accent,
            contentColor = Color.Black,
            disabledContainerColor = HyroxMobileDesign.Colors.AccentDim,
            disabledContentColor = Color.Black.copy(alpha = 0.55f),
        ),
    ) {
        Text(text = text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HyroxSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(HyroxMobileDesign.Radius.Button),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        border = BorderStroke(1.dp, HyroxMobileDesign.Colors.Hairline),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = HyroxMobileDesign.Colors.SurfaceElevated,
            contentColor = HyroxMobileDesign.Colors.TextPrimary,
            disabledContainerColor = HyroxMobileDesign.Colors.SurfaceElevated.copy(alpha = 0.6f),
            disabledContentColor = HyroxMobileDesign.Colors.TextSecondary,
        ),
    ) {
        Text(text = text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HyroxDivider(
    modifier: Modifier = Modifier,
    color: Color = HyroxMobileDesign.Colors.Divider,
    thickness: Dp = 1.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color)
    )
}

@Composable
fun HyroxPageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    if (pageCount <= 1) return

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { page ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (page == currentPage) 8.dp else 6.dp)
                    .background(
                        color = if (page == currentPage) {
                            HyroxMobileDesign.Colors.TextPrimary
                        } else {
                            HyroxMobileDesign.Colors.TextPrimary.copy(alpha = 0.2f)
                        },
                        shape = CircleShape,
                    ),
            )
        }
    }
}

@Composable
fun HyroxChevron(
    modifier: Modifier = Modifier,
) {
    Text(
        text = "›",
        modifier = modifier,
        color = HyroxMobileDesign.Colors.TextTertiary,
        style = HyroxMobileDesign.Typography.Headline,
        textAlign = TextAlign.Center,
    )
}
