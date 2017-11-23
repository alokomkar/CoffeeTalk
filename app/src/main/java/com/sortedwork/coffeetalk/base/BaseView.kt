package com.sortedwork.coffeetalk.base

/**
 * Created by Alok on 23/11/17.
 */
interface BaseView {
    fun showProgress( message : String )
    fun hideProgress( )
    fun onError( message: String )
}