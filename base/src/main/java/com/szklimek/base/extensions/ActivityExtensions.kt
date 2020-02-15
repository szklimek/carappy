package com.szklimek.base.extensions

import android.app.Activity
import android.content.Intent
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import com.szklimek.base.BaseActivity.KEY_EXITING_ANIM_ENTER_SCREEN
import com.szklimek.base.BaseActivity.KEY_EXITING_ANIM_EXIT_SCREEN
import com.szklimek.base.Log
import com.szklimek.base.R

data class ActivityTransitionAnimation(
    val enteringScreenAnim: Int = 0,
    val exitingScreenAnim: Int = 0
)

val defaultEnteringAnimation =
    ActivityTransitionAnimation(R.anim.slide_in_right, R.anim.slide_out_left)
val defaultExitingAnimation =
    ActivityTransitionAnimation(R.anim.slide_in_left, R.anim.slide_out_right)

fun Activity.startWithTransition(
    intent: Intent,
    enterTransition: ActivityTransitionAnimation = defaultEnteringAnimation,
    exitTransition: ActivityTransitionAnimation = defaultExitingAnimation
) {
    Log.d("Starting new activity from: ${this.javaClass.simpleName}")
    val ops = ActivityOptionsCompat.makeCustomAnimation(
        this,
        enterTransition.enteringScreenAnim,
        enterTransition.exitingScreenAnim
    )
    intent.putExtra(KEY_EXITING_ANIM_ENTER_SCREEN, exitTransition.enteringScreenAnim)
    intent.putExtra(KEY_EXITING_ANIM_EXIT_SCREEN, exitTransition.exitingScreenAnim)
    startActivity(intent, ops.toBundle())
}

fun Fragment.startActivityWithTransition(
    intent: Intent,
    enterTransition: ActivityTransitionAnimation = defaultEnteringAnimation,
    exitTransition: ActivityTransitionAnimation = defaultExitingAnimation
) {
    Log.d("Starting activity from fragment: ${this.javaClass.simpleName}")
    activity!!.startWithTransition(intent, enterTransition, exitTransition)
}

fun Fragment.finishActivity() {
    Log.d("Finishing activity from fragment: ${this.javaClass.simpleName}")
    activity!!.finish()
}