package com.sortedwork.coffeetalk.base.kotlin

import android.net.Uri
import com.fitlinks.FitLinksApplication
import com.fitlinks.utils.FileUploadListener
import com.github.ajalt.timberkt.Timber
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.lang.Exception


/**
 * Created by Binay on 11/07/17.
 */

class FirebaseHandler {
    companion object {

        fun getFirebaseAuth(): FirebaseAuth {
            val mAuth = FirebaseAuth.getInstance()
            return mAuth
        }

        fun getFirebaseStorage(): StorageReference {
            val mStorageRef = FirebaseStorage.getInstance().reference
            return mStorageRef
        }

        fun getFirebaseDatabase(): DatabaseReference {
            val mDatabaseRef = FirebaseDatabase.getInstance().reference
            return mDatabaseRef
        }

        fun getLocations(): DatabaseReference{
            val mDatabaseRef = FirebaseDatabase.getInstance().reference
            return mDatabaseRef.child("users")
        }

        fun getNotifications(): DatabaseReference {
            val mDatabaseRef = FirebaseDatabase.getInstance().reference
            return mDatabaseRef.child("notifications")
        }

        fun getCoachInvitations(): DatabaseReference {
            val mDatabaseRef = FirebaseDatabase.getInstance().reference
            return mDatabaseRef.child("invitationRequestByCoach")
        }

        fun getPlayerInvitations(): DatabaseReference {
            val mDatabaseRef = FirebaseDatabase.getInstance().reference
            return mDatabaseRef.child("invitationRequestByPlayer")
        }

        fun getUserRelations(): DatabaseReference {
            val mDatabaseRef = FirebaseDatabase.getInstance().reference
            return mDatabaseRef.child("user_relations")
        }

        fun getCoachPendingRequests(): DatabaseReference {
            val mDatabaseRef = FirebaseDatabase.getInstance().reference
            return mDatabaseRef.child("coachInvitations")
        }

        fun getPlayerPendingRequests(): DatabaseReference {
            val mDatabaseRef = FirebaseDatabase.getInstance().reference
            return mDatabaseRef.child("playerInvitations")
        }

        fun getMessageDialogs(): DatabaseReference {
            val mDatabaseRef = FirebaseDatabase.getInstance().reference
            return mDatabaseRef.child("Message_Dialogs")
        }

        fun getMessages(): DatabaseReference {
            val mDatabaseRef = FirebaseDatabase.getInstance().reference
            return mDatabaseRef.child("Messages")
        }

        fun getUserForKey(emailKey: String): DatabaseReference {
            val mDatabaseRef = FirebaseDatabase.getInstance().reference
            return mDatabaseRef.child("users").child(emailKey)
        }

        fun getAssessmentQuestionsFirebaseDatabase(): DatabaseReference {
            val mDatabaseRef = FirebaseDatabase.getInstance().reference
            return mDatabaseRef.child("Assessment_Questions")
        }

        fun getUserAssessmentQuestionsFirebaseDatabase(): DatabaseReference {
            val mDatabaseRef = FirebaseDatabase.getInstance().reference
            return mDatabaseRef.child("User_Assessment_Questions")
        }

        fun getGameQuestionsFirebaseDatabase(): DatabaseReference {
            val mDatabaseRef = FirebaseDatabase.getInstance().reference
            return mDatabaseRef.child("Game_Questions")
        }

        fun getGameQuestionsForUserFirebaseDatabase(): DatabaseReference {
            val mDatabaseRef = FirebaseDatabase.getInstance().reference
            return mDatabaseRef.child("Game_Questions_Per_User")
        }

        fun getCategoryOptionsFirebaseDatabase(): DatabaseReference {
            val mDatabaseRef = FirebaseDatabase.getInstance().reference
            return mDatabaseRef.child("Build_Strength_Audio_List")
        }

        fun getOnBoardingQuestionsFirebaseDatabase(): DatabaseReference {
            val mDatabaseRef = FirebaseDatabase.getInstance().reference
            return mDatabaseRef.child("OnBoardingQuestions")
        }

        fun getUserAnswersFirebaseDatabase(): DatabaseReference {
            val mDatabaseRef = FirebaseDatabase.getInstance().reference
            return mDatabaseRef.child("OnBoardingUserAnswers")
        }

        fun getFirebaseBaseUrl(): String {
            val BASE_URL = "https://atom-f2b69.firebaseio.com"
            return BASE_URL
        }

        fun getUserGameFirebaseDatabase(): DatabaseReference {
            val mDatabaseRef = FirebaseDatabase.getInstance().reference
            return mDatabaseRef.child("game_per_user")
        }

        fun getCoursesFirebaseDatabase(): DatabaseReference {
            val mDatabaseRef = FirebaseDatabase.getInstance().reference
            return mDatabaseRef.child("courses")
        }

        fun getCourseModulesFirebaseDatabase(): DatabaseReference {
            val mDatabaseRef = FirebaseDatabase.getInstance().reference
            return mDatabaseRef.child("courses_modules")
        }

        fun cancelUploadForFile(fileUri: String) {
            var uploadTask = FitLinksApplication.instance.uploadTasks!!.get(fileUri)
            if (uploadTask != null) {
                Timber.tag("UPLOAD").d("Cancelling upload for " + fileUri)
                uploadTask.cancel()
            } else {
                uploadTask = FitLinksApplication.instance.uploadTasks.get(fileUri + ".webp")
                if (uploadTask != null) {
                    Timber.tag("UPLOAD").d("Cancelling upload for $fileUri.webp")
                    uploadTask.cancel()
                }
            }
        }

        fun uploadFileToFirebase(filePath: Uri, path: String, fileUploadListener: FileUploadListener) {
            val fileStorageReference: StorageReference = getFirebaseStorage()!!.child(path)
            val fileUploadTask = fileStorageReference.putFile(filePath!!)
            FitLinksApplication.instance.uploadTasks.put(filePath.toString(), fileUploadTask)
            Timber.tag("UPLOAD").d(filePath.toString() + " " + path + " ")
            fileUploadTask.addOnFailureListener({ exception: Exception ->
                exception.printStackTrace()
                fileUploadListener.onError(exception)
            })
            fileUploadTask.addOnProgressListener({ tProgress ->
                Timber.tag("UPLOAD").d("uploading " + tProgress.bytesTransferred)
                val progress = 100.0 * tProgress.bytesTransferred / tProgress.totalByteCount
                fileUploadListener.onProgressUpdate(progress)
            })
            fileUploadTask.addOnCompleteListener({ task ->
                if (task.isSuccessful) {
                    fileUploadListener.onSuccess(task.result.downloadUrl!!)
                } else {
                    fileUploadListener.onError(null!!)
                }
            })
        }

        fun uploadBitmapToFirebase(filePath: ByteArray, uploadPath: String, path: String, fileUploadListener: FileUploadListener) {

            val fileStorageReference: StorageReference = getFirebaseStorage().child(path)
            val fileUploadTask = fileStorageReference.putBytes(filePath)
            FitLinksApplication.instance.uploadTasks.put(uploadPath, fileUploadTask)
            Timber.tag("UPLOAD").d(filePath.toString() + " " + path + " ")
            fileUploadTask.addOnFailureListener({ exception: Exception ->
                exception.printStackTrace()
                fileUploadListener.onError(exception)
            })
            fileUploadTask.addOnProgressListener({ tProgress ->
                val progress = 100.0 * tProgress.bytesTransferred / tProgress.totalByteCount
                Timber.tag("UPLOAD").d("uploading " + progress)
                fileUploadListener.onProgressUpdate(progress)
            })
            fileUploadTask.addOnCompleteListener({ task ->
                if (task.isSuccessful) {
                    fileUploadListener.onSuccess(task.result.downloadUrl!!)
                } else {
                    fileUploadListener.onError(null!!)
                }
            })
        }

        fun deleteStorageFile(fileUri: String) {
            Timber.tag("Upload").d(fileUri)
            if (fileUri.startsWith("/storage")) {
                cancelUploadForFile(fileUri)
                return
            }
            val fileStorageReference: StorageReference = getFirebaseStorage().storage.getReferenceFromUrl(fileUri)
            fileStorageReference.delete().addOnSuccessListener(OnSuccessListener<Void> {
                // File deleted successfully
                Timber.tag("Storage").d("Deleted : " + fileUri)
            }).addOnFailureListener(object : OnFailureListener {
                override fun onFailure(exception: Exception) {
                    // Uh-oh, an error occurred!
                    Timber.tag("Storage").d("Failed to delete : " + fileUri)
                    exception.printStackTrace()
                }
            })
        }

    }
}
