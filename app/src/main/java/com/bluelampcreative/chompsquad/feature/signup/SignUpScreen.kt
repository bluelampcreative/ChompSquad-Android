package com.bluelampcreative.chompsquad.feature.signup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluelampcreative.chompsquad.feature.signin.GoogleSignInButton
import com.bluelampcreative.chompsquad.feature.signin.OrDivider
import com.bluelampcreative.chompsquad.feature.signin.findActivity
import com.bluelampcreative.chompsquad.feature.signin.launchGoogleSignIn
import com.bluelampcreative.chompsquad.ui.navigation.NavEvent
import com.bluelampcreative.chompsquad.ui.theme.ChompSquadTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignUpScreen(
    onNavEvent: (NavEvent) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignUpViewModel = koinViewModel(),
) {

  val currentOnNavEvent by rememberUpdatedState(onNavEvent)
  LaunchedEffect(Unit) { viewModel.navEvents.collect { event -> currentOnNavEvent(event) } }

  val viewState by viewModel.viewState.collectAsStateWithLifecycle()

  SignUpScreen(
      onNavEvent = onNavEvent,
      onHandleEvent = viewModel::handleEvent,
      viewState = viewState,
      modifier = modifier,
  )
}

@Composable
fun SignUpScreen(
    onNavEvent: (NavEvent) -> Unit,
    onHandleEvent: (SignUpUiEvent) -> Unit,
    viewState: SignUpViewState,
    modifier: Modifier = Modifier,
) {

  val context = LocalContext.current
  val activity = remember(context) { context.findActivity() }
  val scope = rememberCoroutineScope()
  val focusManager = LocalFocusManager.current

  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var confirmPasswordError by remember { mutableStateOf<String?>(null) }

  val passwordFocusRequester = remember { FocusRequester() }
  val confirmPasswordFocusRequester = remember { FocusRequester() }

  val submitEnabled =
      email.isNotBlank() &&
          password.isNotBlank() &&
          confirmPassword.isNotBlank() &&
          !viewState.isLoading

  fun submitSignUp() {
    if (!submitEnabled) return
    if (password != confirmPassword) {
      confirmPasswordError = "Passwords don't match"
      return
    }
    confirmPasswordError = null
    focusManager.clearFocus()
    onHandleEvent(SignUpUiEvent.OnSubmit(email.trim(), password))
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
          text = "Create account",
          style = MaterialTheme.typography.headlineLarge,
          textAlign = TextAlign.Center,
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
          text = "Start saving and organizing your recipes",
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

      PasswordTextField(
          value = password,
          onValueChange = { password = it },
          label = "Password",
          imeAction = ImeAction.Next,
          onImeAction = { confirmPasswordFocusRequester.requestFocus() },
          modifier = Modifier.fillMaxWidth().focusRequester(passwordFocusRequester),
      )

      Spacer(modifier = Modifier.height(12.dp))

      PasswordTextField(
          value = confirmPassword,
          onValueChange = {
            confirmPassword = it
            if (confirmPasswordError != null) confirmPasswordError = null
          },
          label = "Confirm password",
          isError = confirmPasswordError != null,
          supportingText = confirmPasswordError?.let { error -> { Text(error) } },
          imeAction = ImeAction.Done,
          onImeAction = { submitSignUp() },
          modifier = Modifier.fillMaxWidth().focusRequester(confirmPasswordFocusRequester),
      )

      Spacer(modifier = Modifier.height(24.dp))

      Button(
          onClick = { submitSignUp() },
          enabled = submitEnabled,
          modifier = Modifier.fillMaxWidth(),
      ) {
        if (viewState.isLoading) {
          CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
          Text("Create account")
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
                    onHandleEvent(SignUpUiEvent.OnGoogleTokenReceived(idToken))
                  },
                  onError = { message ->
                    onHandleEvent(SignUpUiEvent.OnGoogleSignInError(message))
                  },
              )
            }
          },
          enabled = !viewState.isLoading && activity != null,
          label = "Sign up with Google",
          modifier = Modifier.fillMaxWidth(),
      )

      Spacer(modifier = Modifier.weight(1f))
      Spacer(modifier = Modifier.height(16.dp))

      TextButton(onClick = { onNavEvent(NavEvent.NavigateToSignIn) }) {
        Text("Already have an account? Sign in")
      }

      Spacer(modifier = Modifier.height(8.dp))
    }
  }

  viewState.errorMessage?.let { message ->
    AlertDialog(
        onDismissRequest = { onHandleEvent(SignUpUiEvent.OnDismissError) },
        title = { Text("Account creation failed") },
        text = { Text(message) },
        confirmButton = {
          TextButton(onClick = { onHandleEvent(SignUpUiEvent.OnDismissError) }) { Text("OK") }
        },
    )
  }
}

// ── Private composables ───────────────────────────────────────────────────────

/** Password field with show/hide toggle. Owns all the visibility-branching logic internally. */
@Composable
private fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
) {
  var showPassword by remember { mutableStateOf(false) }
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      label = { Text(label) },
      singleLine = true,
      isError = isError,
      supportingText = supportingText,
      visualTransformation =
          if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
      keyboardOptions =
          KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
      keyboardActions = KeyboardActions(onNext = { onImeAction() }, onDone = { onImeAction() }),
      trailingIcon = {
        IconButton(onClick = { showPassword = !showPassword }) {
          Icon(
              imageVector =
                  if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
              contentDescription = if (showPassword) "Hide password" else "Show password",
          )
        }
      },
      modifier = modifier,
  )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SignUpScreenPreview() {
  ChompSquadTheme {
    SignUpScreen(
        onNavEvent = {},
        onHandleEvent = {},
        viewState = SignUpViewState(),
        modifier = Modifier.fillMaxSize(),
    )
  }
}
