package com.szklimek.base

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.fragment.app.Fragment

/**
 * Base class for all fragments. All fragments should extends this class.
 *
 * @author szklimek
 */

abstract class BaseFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("onCreate: " + javaClass.simpleName)
    }

    override fun onStart() {
        super.onStart()
        Log.d("onStart: " + javaClass.simpleName)
    }

    override fun onResume() {
        super.onResume()
        Log.d("onResume: " + javaClass.simpleName)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d("onActivityCreated at " + javaClass.simpleName + " "
                + activity!!.javaClass.simpleName)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("onAttach: " + javaClass.simpleName)
    }

    override fun onPause() {
        super.onPause()
        Log.d("onPause: " + javaClass.simpleName)
    }

    override fun onStop() {
        super.onStop()
        Log.d("onStop: " + javaClass.simpleName)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("onDestroy: " + javaClass.simpleName)
    }

    fun showToastMessage(message : String) {
        Toast.makeText(context, message, LENGTH_SHORT).show()
    }
}
