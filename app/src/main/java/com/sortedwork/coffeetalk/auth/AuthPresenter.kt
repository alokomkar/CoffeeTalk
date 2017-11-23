package com.sortedwork.coffeetalk.auth

import android.support.v4.app.FragmentActivity
import android.util.Log
import android.widget.Toast
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.sortedwork.coffeetalk.R
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import java.util.concurrent.TimeUnit

/**
 * Created by Alok on 07/09/17.
 */
class AuthPresenter(val activity: FragmentActivity, val authView: AuthView) : GoogleApiClient.OnConnectionFailedListener {



    private var mAuth: FirebaseAuth? = null
    private val RC_SIGN_IN = 9001
    private var mGoogleApiClient: GoogleApiClient?
    var mFbPermissions: ArrayList<String> = ArrayList()

    private var mPhoneVerificationId: String? = null
    private var mPhoneToken: PhoneAuthProvider.ForceResendingToken? = null
    private var mPhoneAuthProvider: PhoneAuthProvider = PhoneAuthProvider.getInstance()

    init {
        mAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build()
        mFbPermissions = arrayListOf("email", "public_profile", "user_birthday")
        mGoogleApiClient = GoogleApiClient.Builder(activity)
                .enableAutoManage(activity, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()
    }

    lateinit var mTwitterAuthClient: TwitterAuthClient

    fun authorizeWithTwitter(twitterAuthClient: TwitterAuthClient) {
        mTwitterAuthClient = twitterAuthClient
        authView.showProgress(getString(R.string.signing_in))
        twitterAuthClient.authorize(activity, object : Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession>?) {
                handleTwitterSession(result!!.data)
            }

            override fun failure(exception: TwitterException?) {
                Log.e(TAG, "authorizeWithTwitter")
                Log.d(TAG,"Inside twitter authorization... "+exception!!.printStackTrace())
                exception!!.printStackTrace()
                authView.hideProgress()
                authView.onError(exception.message!!)
            }
        })
    }

    private fun getString(signing_in: Int): String {

    }

    private val TAG: String = "AuthPresenter"

    private fun handleTwitterSession(data: TwitterSession?) {
        mTwitterAuthClient.requestEmail(data, object : Callback<String>() {
            override fun success(result: Result<String>) {
                connectUserToFirebase(data)
                // Do something with the result, which provides the email address
            }

            override fun failure(exception: TwitterException) {
                // Do something on failure
                Log.e(TAG, "handleTwitterSession")
                exception.printStackTrace()
                authView.hideProgress()
                authView.onError(exception.message!!)
            }
        })
    }


    private fun connectUserToFirebase(session: TwitterSession?) {
        val credential = TwitterAuthProvider.getCredential(
                session!!.authToken.token,
                session.authToken.secret)
        val twitterName = session.userName
        signInWithCredential(credential)

    }

    private fun signInWithCredential(credential: AuthCredential?) {
        authView.showProgress("Signing in")
        mAuth!!.signInWithCredential(credential!!)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        Log.i("LOGIN SUCCESS",task.isSuccessful.toString())
                        val user = mAuth!!.getCurrentUser()
                        fetchUserDetails(user)
                    } else {
                        //authView.hideProgressDialog()
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        //if(task.exception.toString().equals(activity.getString(R.string.invalid_otp_entered))) {
                            Toast.makeText(activity, R.string.invalid_otp, Toast.LENGTH_SHORT).show()
                            Log.i("LOGIN FAILURE",task.isSuccessful.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "signInWithCredential")
                    exception.printStackTrace()
                    authView.hideProgress()
                    if (exception.message.equals(activity.getString(R.string.link_msg))) {
                        Log.d("TWITTER ERROR",""+exception.printStackTrace())
                        authView.onError(activity.getString(R.string.link_msg))
                    }
                }

    }

    private fun fetchUserDetails(user: FirebaseUser?) {

        fetchPersonalDetails(user)

        FirebaseHandler.getFirebaseDatabase()
                .child("users")
                .child(user!!.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError?) {

                        if (error != null)
                            authView.onError(error.message)
                        authView.hideProgress()
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot?) {

                        if (dataSnapshot != null && dataSnapshot.value != null) {
                            try {
                                val flUser = dataSnapshot.getValue(User::class.java) as User
                                FitLinksApplication.getPreferences().setUser(flUser)
                                authView.updateUI(user)
                            }
                            catch(e:Exception)
                            {

                            }

                        } else {
                            authView.onError("Signing up")
                            authView.updateUI(user)
                        }
                        authView.hideProgress()

                    }

                })
    }

    private fun fetchPersonalDetails(user: FirebaseUser?) {
        FirebaseHandler.getFirebaseDatabase()
                .child("personal_interest")
                .child(user!!.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError?) {

                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot?) {
                        if (dataSnapshot != null && dataSnapshot.value != null) {
                            val personalDetails = dataSnapshot.getValue(PersonalInterest::class.java) as PersonalInterest
                            FitLinksApplication.getPreferences().setPersonalInterest(personalDetails, user.uid)
                        }
                    }

                })

    }

    fun authorizeWithFB(callbackManager: CallbackManager) {
        authView.showProgress(activity.getString(R.string.signing_in))
        LoginManager.getInstance().logOut()
        LoginManager.getInstance().logInWithReadPermissions(activity, mFbPermissions)
        LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        val credential = FacebookAuthProvider.getCredential(loginResult.accessToken.token)
                        signInWithCredential(credential)
                    }

                    override fun onCancel() {
                        Toast.makeText(activity, "Facebook authentication cancelled", Toast.LENGTH_SHORT).show()
                    }


                    override fun onError(exception: FacebookException) {
                        authView.onError(exception.message!!)
                    }
                })
    }

    fun signInWithGoogle() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        activity.startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        authView.onError(result.errorMessage!!)
    }

    fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        authView.showProgress(activity.getString(R.string.signing_in))
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        signInWithCredential(credential)
    }

    fun onDestroy() {
        mGoogleApiClient!!.stopAutoManage(activity);
        mGoogleApiClient!!.disconnect()
    }

    fun signInWithPhoneNumber(phoneNumber: String) {
        authView.showProgress(activity.getString(R.string.signing_in))
        mPhoneAuthProvider.verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                activity,
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    override fun onVerificationCompleted(authCredential: PhoneAuthCredential?) {
                        signInWithCredential(authCredential)
                    }

                    override fun onVerificationFailed(exception: FirebaseException?) {
                        authView.hideProgress()
                        if (exception != null) {
                            Log.i("onVerifFail","..........."+exception.printStackTrace())
                            authView.onError(exception.message!!)
                            exception.printStackTrace()
                        }

                    }

                    override fun onCodeSent(verificationId: String?, token: PhoneAuthProvider.ForceResendingToken?) {
                        super.onCodeSent(verificationId, token)
                        mPhoneVerificationId = verificationId
                        mPhoneToken = token
                    }

                    override fun onCodeAutoRetrievalTimeOut(verificationId: String?) {
                        super.onCodeAutoRetrievalTimeOut(verificationId)
                        authView.onError("SMS not received.")
                    }
                })
    }

    fun verifyPhoneWithCode( otpCode : String ) {
        Log.i("VERIFYPHONE","......................OTP IN AUTHPRESENTER"+otpCode)
        signInWithCredential(PhoneAuthProvider.getCredential(mPhoneVerificationId!!, otpCode))
    }

    fun resendOtp(phoneNumber: String?) {
        authView.showProgress(activity.getString(R.string.signing_in))
        mPhoneAuthProvider.verifyPhoneNumber(
                phoneNumber!!,
                60,
                TimeUnit.SECONDS,
                activity,
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    override fun onVerificationCompleted(authCredential: PhoneAuthCredential?) {
                        signInWithCredential(authCredential)
                    }

                    override fun onVerificationFailed(exception: FirebaseException?) {
                        authView.hideProgress()
                        if (exception != null) {
                            authView.onError(exception.message!!)
                            exception.printStackTrace()
                        }

                    }

                    override fun onCodeSent(verificationId: String?, token: PhoneAuthProvider.ForceResendingToken?) {
                        super.onCodeSent(verificationId, token)
                        mPhoneVerificationId = verificationId
                        mPhoneToken = token
                    }

                    override fun onCodeAutoRetrievalTimeOut(verificationId: String?) {
                        super.onCodeAutoRetrievalTimeOut(verificationId)
                        authView.onError("SMS not received.")
                    }
                },
                mPhoneToken)
    }

    fun onStop() {
        if( mGoogleApiClient != null && mGoogleApiClient!!.isConnected() ) {
            mGoogleApiClient!!.stopAutoManage(activity)
            mGoogleApiClient!!.disconnect()
        }

    }

}