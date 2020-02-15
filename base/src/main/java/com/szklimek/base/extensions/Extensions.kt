package com.szklimek.base.extensions

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.LayoutRes
import com.szklimek.base.Log

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View =
    LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)

fun Throwable.log(): String = "${this.javaClass.simpleName}: $message"

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.enable() {
    isEnabled = true
}

fun View.disable() {
    isEnabled = false
}

fun View.select() {
    isSelected = true
}

fun View.unselect() {
    isSelected = false
}

fun Context.getColorFromAttr(
    @AttrRes attrColor: Int, typedValue: TypedValue = TypedValue(),
    resolveRefs: Boolean = true
): Int {
    theme.resolveAttribute(attrColor, typedValue, resolveRefs)
    return typedValue.data
}

fun Context.getApplicationName(): String {
    val labelId = applicationInfo.labelRes
    return if (labelId == 0) applicationInfo.nonLocalizedLabel.toString() else getString(labelId)
}

fun Context.showShortToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun Context.showKeyboard() {
    Log.d("showKeyboard")
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
}

fun Activity.hideKeyboard() {
    Log.d("hideKeyboard: ")
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager

    // Find the currently focused view, so we can grab the correct window token from it.
    var view = currentFocus

    // If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(this)
    }

    imm.hideSoftInputFromWindow(view.windowToken, 0)
}
