package com.bluelampcreative.chompsquad.feature.signin

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluelampcreative.chompsquad.BuildConfig
import com.bluelampcreative.chompsquad.R
import com.bluelampcreative.chompsquad.ui.navigation.NavEvent
import com.bluelampcreative.chompsquad.ui.theme.ChompSquadTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.util.UUID
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private const val TAG = "SignInScreen"

@Composable
fun SignInScreen(
    onNavEvent: (NavEvent) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignInViewModel = koinViewModel(),
) {
  val viewState by viewModel.viewState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  // rememberUpdatedState ensures the LaunchedEffect always calls the latest lambda even though
  // the effect key is stable (Unit).
  val currentOnNavEvent by rememberUpdatedState(onNavEvent)

  // Relay one-shot nav intents from the VM to the navigation layer — no routing logic here.
  LaunchedEffect(Unit) { viewModel.navEvents.collect { event -> currentOnNavEvent(event) } }

  Box(modifier = modifier.fillMaxSize()) {
    Column(
        modifier =
            Modifier.fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Box(modifier = Modifier.fillMaxWidth()) {
        IconButton(onClick = { onNavEvent(NavEvent.GoBack) }) {
          Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
          )
        }
      }

      Spacer(modifier = Modifier.weight(1f))

      Text(
          text = "Welcome back",
          style = MaterialTheme.typography.headlineLarge,
          textAlign = TextAlign.Center,
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
          text = "Sign in to access your recipes",
          style = MaterialTheme.typography.bodyLarge,
          textAlign = TextAlign.Center,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      Spacer(modifier = Modifier.weight(1f))

      // Credential Manager is a platform UI concern requiring Activity context — it lives here,
      // not in the ViewModel. Once a token is obtained, it is handed off via handleEvent.
      GoogleSignInButton(
          onClick = {
            scope.launch {
              launchGoogleSignIn(
                  context = context as Activity,
                  onTokenReceived = { idToken ->
                    viewModel.handleEvent(SignInUiEvent.OnGoogleTokenReceived(idToken))
                  },
                  onError = { message ->
                    viewModel.handleEvent(SignInUiEvent.OnSignInError(message))
                  },
              )
            }
          },
          isLoading = viewState.isLoading,
          modifier = Modifier.fillMaxWidth(),
      )

      Spacer(modifier = Modifier.height(24.dp))
    }
  }

  viewState.errorMessage?.let { message ->
    AlertDialog(
        onDismissRequest = { viewModel.handleEvent(SignInUiEvent.OnDismissError) },
        title = { Text("Sign in failed") },
        text = { Text(message) },
        confirmButton = {
          TextButton(onClick = { viewModel.handleEvent(SignInUiEvent.OnDismissError) }) {
            Text("OK")
          }
        },
    )
  }
}

@Composable
private fun GoogleSignInButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
  OutlinedButton(
      onClick = onClick,
      modifier = modifier,
      enabled = !isLoading,
  ) {
    if (isLoading) {
      CircularProgressIndicator(
          modifier = Modifier.size(20.dp),
          strokeWidth = 2.dp,
      )
    } else {
      Row(
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Image(
            painter = painterResource(R.drawable.ic_google_logo),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Sign in with Google")
      }
    }
  }
}

/**
 * Launches the Credential Manager bottom sheet to obtain a Google ID token.
 *
 * Must be called from a composable coroutine scope with an [Activity] context. On success the ID
 * token is delivered via [onTokenReceived]; on cancellation nothing happens; on any other failure
 * [onError] is called with a human-readable message.
 */
private suspend fun launchGoogleSignIn(
    context: Activity,
    onTokenReceived: (idToken: String) -> Unit,
    onError: (message: String) -> Unit,
) {
  val credentialManager = CredentialManager.create(context)

  val googleIdOption =
      GetGoogleIdOption.Builder()
          .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
          .setFilterByAuthorizedAccounts(false)
          .setAutoSelectEnabled(false)
          .setNonce(UUID.randomUUID().toString())
          .build()

  val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

  runCatching { credentialManager.getCredential(context, request) }
      .onSuccess { result ->
        val credential = result.credential
        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
          val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
          onTokenReceived(googleCredential.idToken)
        } else {
          onError("Unexpected credential type. Please try again.")
        }
      }
      .onFailure { error ->
        when (error) {
          is GetCredentialCancellationException -> Log.d(TAG, "Google Sign-In cancelled by user")
          else -> onError(error.message ?: "Sign in failed. Please try again.")
        }
      }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SignInScreenPreview() {
  ChompSquadTheme {
    Column(
        modifier =
            Modifier.fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
      Text(
          text = "Welcome back",
          style = MaterialTheme.typography.headlineLarge,
          textAlign = TextAlign.Center,
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
          text = "Sign in to access your recipes",
          style = MaterialTheme.typography.bodyLarge,
          textAlign = TextAlign.Center,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(modifier = Modifier.height(32.dp))
      GoogleSignInButton(onClick = {}, isLoading = false, modifier = Modifier.fillMaxWidth())
    }
  }
}
