package it.cammino.risuscito.ui

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.gson.GsonBuilder
import it.cammino.risuscito.R
import it.cammino.risuscito.utils.Utility
import it.cammino.risuscito.viewmodels.MainActivityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CredentialCacheManager(
    private val viewModel: MainActivityViewModel,
    private val context: AppCompatActivity,
    private val credentialManager: CredentialManager
) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("credential_cache", MODE_PRIVATE)

    // In-memory cache
    private var cachedCredential: CredendialObject? = null

    suspend fun getCredential(
        request: GetCredentialRequest, forceRefresh: Boolean = false
    ): CredendialObject? {
        viewModel.loginState.postValue(MainActivityViewModel.LOGIN_STATE_STARTED)
        //check if the idToken is still valid
//        if (!forceRefresh) {
//            return cachedCredentialResponse ?: let {
//                Log.d(TAG, "Retrieving credential from in-memory cache after validation.")
//                cachedCredentialResponse
//            }
//        }

        //check if we have the cached credentials
        if (!forceRefresh && isCredentialCached() && getCachedCredential() != null) {
            Log.d(TAG, "Retrieving credential from in-memory cache.")
            viewModel.loginState.postValue(MainActivityViewModel.LOGIN_STATE_OK_SILENT)
            return cachedCredential
        }

        // Retrieve credentials from the credential manager.
        return try {
            Log.d(TAG, "Retrieving credentials...")
            val result = credentialManager.getCredential(request = request, context = context)

            // Cache the result in memory.
            when (val credential = result.credential) {
                is CustomCredential -> {
                    Log.d(TAG, "getCredential: isCustomCredential ${credential.type}")
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        try {
                            // Cache the fact that credentials were obtained in sharedPreferences
                            val credentialResponse =
                                GoogleIdTokenCredential.createFrom(credential.data)
                            cachedCredential =
                                CredendialObject.generateFromCredentials(credentialResponse)
                            cacheCredentials()
                            viewModel.loginState.postValue(MainActivityViewModel.LOGIN_STATE_OK)
                            cachedCredential
                        } catch (e: GoogleIdTokenParsingException) {
                            Log.e(
                                TAG,
                                "getCredential: Received an invalid google id token response",
                                e
                            )
                            viewModel.loginState.postValue(e.localizedMessage ?: "")
                            null
                        }
                    } else {
                        // Catch any unrecognized credential type here.
                        Log.e(TAG, "getCredential: $UNEXPECTED_CREDENTIAL")
                        viewModel.loginState.postValue(UNEXPECTED_CREDENTIAL)
                        null
                    }
                }

                else -> {
                    // Catch any unrecognized credential type here.
                    Log.e(TAG, "getCredential: $UNEXPECTED_CREDENTIAL")
                    viewModel.loginState.postValue(UNEXPECTED_CREDENTIAL)
                    null
                }
            }
        } catch (e: GetCredentialCancellationException) {
            // Handle cancellation.
            Log.d(TAG, "getCredential: Retrieving credentials was cancelled.")
            viewModel.loginState.postValue(e.localizedMessage ?: "")
            null
        } catch (e: NoCredentialException) {
            // Handle no credentials.
            Log.d(TAG, "getCredential: No credentials found.")
            viewModel.loginState.postValue(e.localizedMessage ?: "")
            null
        } catch (e: GetCredentialException) {
            // Handle other exceptions.
            Log.e(TAG, "getCredential: Error retrieving credentials.", e)
            viewModel.loginState.postValue(e.localizedMessage ?: "")
            null
        }
    }

    fun clearCache() {
        cachedCredential = null
        sharedPreferences.edit {
            remove(CREDENTIAL_RESPONSE) // remove if present
            apply()
        }
    }

    private fun cacheCredentials() {
        sharedPreferences.edit {
            val credentialResponse = GsonBuilder().create().toJson(cachedCredential)
            putString(CREDENTIAL_RESPONSE, credentialResponse)
            apply()
        }
    }

    private fun isCredentialCached(): Boolean {
        return sharedPreferences.contains(CREDENTIAL_RESPONSE)
    }

    fun getCachedCredential(): CredendialObject? { //retrieve object from shared preferences
        if (cachedCredential == null) {
            val json = sharedPreferences.getString(CREDENTIAL_RESPONSE, "")
            cachedCredential = GsonBuilder().create().fromJson(json, CredendialObject::class.java)
            if (cachedCredential == null) {
                Log.w(TAG, "Error retrieving GetCredentialResponse from cache")
            } else {
                Log.d(TAG, "GetCredentialResponse correctly retrieved from cache")
            }
        }
        return cachedCredential
    }

    //    fun validateToken(retrieveLastAccount: Boolean) {
    fun validateToken() {
        Log.d(TAG, "validateToken")
        // Verify if token is valid, if it's expired, or if is revoked.
        if (getCachedCredential()?.getAccountIdToken().isNullOrEmpty()) {
            Log.e(TAG, "Token cannot be empty!")
            return
        }

        //check token signature
        //check if the user is blacklisted
        viewModel.httpRequestState.value = MainActivityViewModel.ClientState.STARTED
        context.lifecycleScope.launch(Dispatchers.IO) {
            viewModel.sub = Utility.validateToken(
                getCachedCredential()?.getAccountIdToken() ?: "",
                context.getString(R.string.default_web_client_id)
            )
            viewModel.httpRequestState.postValue(MainActivityViewModel.ClientState.COMPLETED)
        }
    }

    companion object {
        private val TAG = CredentialCacheManager::class.java.canonicalName
        private const val CREDENTIAL_RESPONSE = "credential_response_cache"
        const val UNEXPECTED_CREDENTIAL = "Unexpected type of credential"
    }
}