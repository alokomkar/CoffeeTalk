package com.sortedwork.coffeetalk.auth

import com.google.firebase.auth.FirebaseUser
import com.sortedwork.coffeetalk.base.BaseView

/**
 * Created by Alok on 07/09/17.
 */
interface AuthView : BaseView {
    fun updateUI(user: FirebaseUser?)
}

