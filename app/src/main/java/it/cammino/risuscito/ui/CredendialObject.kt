package it.cammino.risuscito.ui

import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.io.Serializable

data class CredendialObject(
    private val accountId: String,
    private val accountIdToken: String,
    private val displayName: String,
    private val profilePictureUri: String
) : Serializable {

    fun getAccountId(): String {
        return accountId
    }

    fun getAccountIdToken(): String {
        return accountIdToken
    }

    fun getDisplayName(): String {
        return displayName
    }

    fun getProfilePictureUri(): String {
        return profilePictureUri
    }

    companion object {
        fun generateFromCredentials(credentials: GoogleIdTokenCredential): CredendialObject {
            return CredendialObject(
                credentials.id,
                credentials.idToken,
                credentials.displayName.orEmpty(),
                credentials.profilePictureUri.toString()
            )
        }
    }

}

