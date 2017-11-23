package com.sortedwork.coffeetalk.base.kotlin

import android.app.Activity
import android.app.ProgressDialog
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 * Created by Alok on 07/09/17.
 */
fun View.isVisible() = visibility == View.VISIBLE

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.getString(res: Int) = resources.getString(res)

fun View.hideKeyboard(inputMethodManager: InputMethodManager) {
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}

fun View.showKeyboard(inputMethodManager: InputMethodManager) {
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}

var progressDialog: ProgressDialog? = null

fun Activity.showProgressDialog(message: String) {
    try {

        if( progressDialog != null )
            hideProgressDialog()

        progressDialog = ProgressDialog(window.context)
        progressDialog!!.setCanceledOnTouchOutside(false)
        progressDialog!!.setCancelable(false)
        progressDialog!!.setMessage(message)
        progressDialog!!.show()
    } catch ( e : Exception ) {
        e.printStackTrace()
    }

}

fun Activity.hideProgressDialog() {
    try {
        progressDialog!!.cancel()
    } catch ( e: Exception ) {
        e.printStackTrace()
    }
}
