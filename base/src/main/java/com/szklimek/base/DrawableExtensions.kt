package com.szklimek.base

import android.content.Context
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.szklimek.base.extensions.getColorFromAttr

fun Context.getAttrColoredDrawable(@DrawableRes drawableResId: Int, @AttrRes attrColorResId: Int) =
    ContextCompat.getDrawable(this, drawableResId)!!.apply {
        setTint(getColorFromAttr(attrColorResId))
    }

fun Context.getColoredDrawable(@DrawableRes drawableResId: Int, @ColorRes colorResId: Int) =
    ContextCompat.getDrawable(this, drawableResId)!!.also {
        it.setTint(ContextCompat.getColor(this, colorResId))
    }
