package com.solidscorpion.medic.services

import android.util.Log

import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

class FirebaseInstanceService : FirebaseInstanceIdService() {
    private val TAG = "FIREBASE"
    override fun onTokenRefresh() {
        val refreshedToken = FirebaseInstanceId.getInstance().token
        Log.d(TAG, "Refreshed token: $refreshedToken")
    }
}
