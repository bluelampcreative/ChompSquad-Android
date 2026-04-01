package com.bluelampcreative.chompsquad.feature.signin

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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

  val currentOnNavEvent by rememberUpdatedState(onNavEvent)
  LaunchedEffect(Unit) { viewModel.navEvents.collect { event -> currentOnNavEvent(event) } }

  val viewState by viewModel.viewState.collectAsStateWithLifecycle()

  SignInScreen(
      onNavEvent = onNavEvent,
      onHandleEvent = viewModel::handleEvent,
      viewState = viewState,
      modifier = modifier,
  )
}

@Composable
fun SignInScreen(
    onNavEvent: (NavEvent) -> Unit,
    onHandleEvent: (SignInUiEvent) -> Unit,
    viewState: SignInViewState,
    modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val activity = remember(context) { context.findActivity() }
  val scope = rememberCoroutineScope()
  val focusManager = LocalFocusManager.current

  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var showPassword by remember { mutableStateOf(false) }

  val passwordFocusRequester = remember { FocusRequester() }

  val submitEnabled = email.isNotBlank() && password.isNotBlank() && !viewState.isLoading

  fun submitEmailSignIn() {
    if (submitEnabled) {
      focusManager.clearFocus()
      onHandleEvent(SignInUiEvent.OnEmailSignInSubmitted(email.trim(), password))
    }
  }

  Box(modifier = modifier.fillMaxSize()) {
    Column(
        modifier =
            Modifier.fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Box(modifier = Modifier.fillMaxWidth()) {
        IconButton(onClick = { onNavEvent(NavEvent.GoBack) }) {
          Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

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

      OutlinedTextField(
          value = email,
          onValueChange = { email = it },
          label = { Text("Email") },
          singleLine = true,
          keyboardOptions =
              KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
          keyboardActions = KeyboardActions(onNext = { passwordFocusRequester.requestFocus() }),
          modifier = Modifier.fillMaxWidth(),
      )

      Spacer(modifier = Modifier.height(12.dp))

      OutlinedTextField(
          value = password,
          onValueChange = { password = it },
          label = { Text("Password") },
          singleLine = true,
          visualTransformation =
              if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
          keyboardOptions =
              KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
          keyboardActions = KeyboardActions(onDone = { submitEmailSignIn() }),
          trailingIcon = {
            IconButton(onClick = { showPassword = !showPassword }) {
              Icon(
                  imageVector =
                      if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                  contentDescription = if (showPassword) "Hide password" else "Show password",
              )
            }
          },
          modifier = Modifier.fillMaxWidth().focusRequester(passwordFocusRequester),
      )

      Spacer(modifier = Modifier.height(24.dp))

      Button(
          onClick = { submitEmailSignIn() },
          enabled = submitEnabled,
          modifier = Modifier.fillMaxWidth(),
      ) {
        if (viewState.isLoading) {
          CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
          Text("Sign in")
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      OrDivider()

      Spacer(modifier = Modifier.height(24.dp))

      GoogleSignInButton(
          onClick = {
            val act = activity ?: return@GoogleSignInButton
            scope.launch {
              launchGoogleSignIn(
                  activity = act,
                  onTokenReceived = { idToken ->
                    onHandleEvent(SignInUiEvent.OnGoogleTokenReceived(idToken))
                  },
                  onError = { message -> onHandleEvent(SignInUiEvent.OnSignInError(message)) },
              )
            }
          },
          enabled = !viewState.isLoading && activity != null,
          label = "Sign in with Google",
          modifier = Modifier.fillMaxWidth(),
      )

      Spacer(modifier = Modifier.weight(1f))
      Spacer(modifier = Modifier.height(16.dp))

      TextButton(onClick = { onNavEvent(NavEvent.NavigateToSignUp) }) {
        Text("New here? Create an account")
      }

      Spacer(modifier = Modifier.height(8.dp))
    }
  }

  viewState.errorMessage?.let { message ->
    AlertDialog(
        onDismissRequest = { onHandleEvent(SignInUiEvent.OnDismissError) },
        title = { Text("Sign in failed") },
        text = { Text(message) },
        confirmButton = {
          TextButton(onClick = { onHandleEvent(SignInUiEvent.OnDismissError) }) { Text("OK") }
        },
    )
  }
}

// ── Shared composables (reused by SignUpScreen) ───────────────────────────────

@Composable
internal fun OrDivider(modifier: Modifier = Modifier) {
  Row(
      modifier = modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    HorizontalDivider(modifier = Modifier.weight(1f))
    Text(
        text = "or",
        modifier = Modifier.padding(horizontal = 16.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    HorizontalDivider(modifier = Modifier.weight(1f))
  }
}

@Composable
internal fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String = "Continue with Google",
) {
  OutlinedButton(onClick = onClick, modifier = modifier, enabled = enabled) {
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
      Text(label)
    }
  }
}

/**
 * Resolves the nearest [Activity] from a [Context] by unwrapping [ContextWrapper] chains. Returns
 * null if no [Activity] is found (e.g. in Compose Previews or Fragment hosts).
 */
internal fun Context.findActivity(): Activity? {
  var ctx = this
  while (ctx is ContextWrapper) {
    if (ctx is Activity) return ctx
    ctx = ctx.baseContext
  }
  return null
}

/**
 * Launches the Credential Manager bottom sheet to obtain a Google ID token.
 *
 * Must be called from a coroutine with a resolved [Activity]. On success the ID token is delivered
 * via [onTokenReceived]; on cancellation nothing happens; on any other failure [onError] is called
 * with a human-readable message.
 */
internal suspend fun launchGoogleSignIn(
    activity: Activity,
    onTokenReceived: (idToken: String) -> Unit,
    onError: (message: String) -> Unit,
) {
  val credentialManager = CredentialManager.create(activity)

  val googleIdOption =
      GetGoogleIdOption.Builder()
          .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
          .setFilterByAuthorizedAccounts(false)
          .setAutoSelectEnabled(false)
          .setNonce(UUID.randomUUID().toString())
          .build()

  val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

  runCatching { credentialManager.getCredential(activity, request) }
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
    SignInScreen(
        onNavEvent = {},
        onHandleEvent = {},
        viewState = SignInViewState(),
        modifier = Modifier.fillMaxSize(),
    )
  }
}
