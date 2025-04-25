package com.example.mentalhealthemotion.Data

import androidx.annotation.DrawableRes

data class BottomNavItem(
    val label: String,
    @DrawableRes val iconRes: Int,
    val route: String
)