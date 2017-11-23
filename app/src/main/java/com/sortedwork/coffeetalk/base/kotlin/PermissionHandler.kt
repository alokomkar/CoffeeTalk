package com.sortedwork.coffeetalk.base.kotlin

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityCompat.requestPermissions
import android.support.v4.app.ActivityCompat.shouldShowRequestPermissionRationale
import android.support.v4.content.PermissionChecker


/**************************************
 * HANDLE PERMISSIONS IN v4 FRAGMENTS *
 *************************************/

fun android.support.v4.app.Fragment.isPermissionGranted(permission: AppPermission) = (PermissionChecker.checkSelfPermission(activity, permission.permissionName) == PackageManager.PERMISSION_GRANTED)

fun android.support.v4.app.Fragment.isRationaleNeeded(permission: AppPermission) = shouldShowRequestPermissionRationale(activity, permission.permissionName)

fun android.support.v4.app.Fragment.requestPermission(permission: AppPermission) = requestPermissions(arrayOf(permission.permissionName), permission.requestCode)


fun android.support.v4.app.Fragment.handlePermission(permission: AppPermission,
                                                     onGranted: (AppPermission) -> Unit,
                                                     onDenied: (AppPermission) -> Unit,
                                                     onRationaleNeeded: (AppPermission) -> Unit) {
    when {
        isLollipopOrBellow() || isPermissionGranted(permission) -> onGranted(permission)
        isRationaleNeeded(permission) -> onRationaleNeeded(permission)
        else -> onDenied(permission)
    }
}

fun android.support.v4.app.Fragment.handlePermission(permission: AppPermission,
                                                     onGranted: (AppPermission) -> Unit,
                                                     onRationaleNeeded: (AppPermission) -> Unit) {
    when {
        isLollipopOrBellow() || isPermissionGranted(permission) -> onGranted(permission)
        isRationaleNeeded(permission) -> onRationaleNeeded(permission)
        else -> requestPermission(permission)
    }
}

/************************************
 * HANDLE PERMISSIONS IN ACTIVITIES *
 ***********************************/

fun Activity.isPermissionGranted(permission: AppPermission) = (PermissionChecker.checkSelfPermission(this, permission.permissionName) == PackageManager.PERMISSION_GRANTED)

fun Activity.isRationaleNeeded(permission: AppPermission) = ActivityCompat.shouldShowRequestPermissionRationale(this, permission.permissionName)

@TargetApi(Build.VERSION_CODES.M)
fun Activity.requestPermission(permission: AppPermission) = requestPermissions(this, arrayOf(permission.permissionName), permission.requestCode)

fun Activity.handlePermission(permission: AppPermission,
                              onGranted: (AppPermission) -> Unit,
                              onDenied: (AppPermission) -> Unit,
                              onRationaleNeeded: (AppPermission) -> Unit) {
    when {
        isLollipopOrBellow() || isPermissionGranted(permission) -> onGranted(permission)
        isRationaleNeeded(permission) -> onRationaleNeeded(permission)
        else -> onDenied(permission)
    }
}

fun Activity.handlePermission(permission: AppPermission,
                              onGranted: (AppPermission) -> Unit,
                              onRationaleNeeded: (AppPermission) -> Unit) {
    when {
        isLollipopOrBellow() || isPermissionGranted(permission) -> onGranted(permission)
        isRationaleNeeded(permission) -> onRationaleNeeded(permission)
        else -> requestPermission(permission)
    }
}

fun handleMultiplePermission(context: Context, permissions: ArrayList<String>): Boolean {
    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
    }
    return true
}

/*********************************************
 * HANDLE onRequestPermissionResult CALLBACK *
 ********************************************/

fun onRequestPermissionsResultReceived(requestCode: Int, permissions: Array<out String>,
                                       grantResults: IntArray,
                                       onPermissionGranted: (AppPermission) -> Unit,
                                       onPermissionDenied: (AppPermission) -> Unit) {
    AppPermission.permissions.find {
        it.requestCode == requestCode
    }?.let {
        val permissionGrantResult = mapPermissionsAndResults(permissions, grantResults)[it.permissionName]
        if (PackageManager.PERMISSION_GRANTED == permissionGrantResult) {
            onPermissionGranted(it)
        } else {
            onPermissionDenied(it)
        }
    }
}

private fun mapPermissionsAndResults(permissions: Array<out String>, grantResults: IntArray): Map<String, Int>
        = permissions.mapIndexedTo(mutableListOf<Pair<String, Int>>()) { index, permission -> permission to grantResults[index] }.toMap()
