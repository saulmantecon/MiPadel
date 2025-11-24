package com.example.myapplication.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(

    titleLarge = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold
    ),

    titleMedium = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold
    ),

    bodyLarge = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    ),

    bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal
    ),

    labelLarge = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp
    ),

    labelMedium = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp
    )
)
