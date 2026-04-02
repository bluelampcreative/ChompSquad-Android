package com.bluelampcreative.chompsquad.feature.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluelampcreative.chompsquad.BuildConfig
import com.bluelampcreative.chompsquad.ui.navigation.NavEvent
import com.bluelampcreative.chompsquad.ui.theme.ChompSpacing
import com.bluelampcreative.chompsquad.ui.theme.ChompSquadTheme
import com.bluelampcreative.chompsquad.ui.theme.debugAmber
import org.koin.androidx.compose.koinViewModel

private val URL_MANAGE_BILLING =
    "https://play.google.com/store/account/subscriptions?package=${BuildConfig.APPLICATION_ID}"
private const val URL_PRIVACY_POLICY = "https://chompsquad.app/privacy"
private const val URL_TERMS_OF_SERVICE = "https://chompsquad.app/terms"
private const val EMAIL_SUPPORT = "support@chompsquad.app"

@Composable
fun SettingsScreen(
    onNavEvent: (NavEvent) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
) {
  val currentOnNavEvent by rememberUpdatedState(onNavEvent)
  LaunchedEffect(Unit) { viewModel.navEvents.collect { currentOnNavEvent(it) } }
  val viewState by viewModel.viewState.collectAsStateWithLifecycle()

  val context = LocalContext.current

  SettingsScreen(
      onHandleEvent = { event ->
        when (event) {
          SettingsUiEvent.OnManageBilling ->
              runCatching {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(URL_MANAGE_BILLING)))
              }
          SettingsUiEvent.OnContactSupport ->
              runCatching {
                context.startActivity(
                    Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("mailto:$EMAIL_SUPPORT") }
                )
              }
          SettingsUiEvent.OnPrivacyPolicy ->
              runCatching {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(URL_PRIVACY_POLICY)))
              }
          SettingsUiEvent.OnTermsOfService ->
              runCatching {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(URL_TERMS_OF_SERVICE)))
              }
          else -> viewModel.handleEvent(event)
        }
      },
      viewState = viewState,
      modifier = modifier,
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onHandleEvent: (SettingsUiEvent) -> Unit,
    viewState: SettingsViewState,
    modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.fillMaxSize()) {
    Scaffold(
        topBar = {
          TopAppBar(
              title = { Text("Settings") },
              navigationIcon = {
                IconButton(onClick = { onHandleEvent(SettingsUiEvent.OnBack) }) {
                  Icon(
                      imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                      contentDescription = "Back",
                  )
                }
              },
          )
        },
    ) { innerPadding ->
      LazyColumn(
          contentPadding = innerPadding,
          modifier = Modifier.fillMaxSize().padding(horizontal = ChompSpacing.md),
      ) {
        item { SectionHeader("Account") }
        item { AccountSection(viewState = viewState, onHandleEvent = onHandleEvent) }

        item { SectionHeader("Subscription") }
        item { SubscriptionSection(viewState = viewState, onHandleEvent = onHandleEvent) }

        item { SectionHeader("Preferences") }
        item { PreferencesSection(onHandleEvent = onHandleEvent) }

        item { SectionHeader("Support") }
        item { SupportSection(onHandleEvent = onHandleEvent) }

        item { SectionHeader("Danger Zone") }
        item { DangerZoneSection(viewState = viewState, onHandleEvent = onHandleEvent) }

        item { VersionFooter() }
      }
    }

    if (viewState.showSignOutDialog) {
      AlertDialog(
          onDismissRequest = { onHandleEvent(SettingsUiEvent.OnDismissSignOutDialog) },
          title = { Text("Sign out?") },
          text = { Text("You will need to sign in again to access your account.") },
          confirmButton = {
            TextButton(onClick = { onHandleEvent(SettingsUiEvent.OnConfirmSignOut) }) {
              Text("Sign out", color = MaterialTheme.colorScheme.error)
            }
          },
          dismissButton = {
            TextButton(onClick = { onHandleEvent(SettingsUiEvent.OnDismissSignOutDialog) }) {
              Text("Cancel")
            }
          },
      )
    }

    if (viewState.showDeleteAccountDialog) {
      AlertDialog(
          onDismissRequest = { onHandleEvent(SettingsUiEvent.OnDismissDeleteAccountDialog) },
          title = { Text("Delete account?") },
          text = {
            Text(
                "This will permanently delete your account and all your recipes. This action cannot be undone."
            )
          },
          confirmButton = {
            TextButton(onClick = { onHandleEvent(SettingsUiEvent.OnConfirmDeleteAccount) }) {
              Text("Delete account", color = MaterialTheme.colorScheme.error)
            }
          },
          dismissButton = {
            TextButton(onClick = { onHandleEvent(SettingsUiEvent.OnDismissDeleteAccountDialog) }) {
              Text("Cancel")
            }
          },
      )
    }

    if (viewState.errorMessage != null) {
      AlertDialog(
          onDismissRequest = { onHandleEvent(SettingsUiEvent.OnDismissError) },
          title = { Text("Something went wrong") },
          text = { Text(viewState.errorMessage) },
          confirmButton = {
            TextButton(onClick = { onHandleEvent(SettingsUiEvent.OnDismissError) }) { Text("OK") }
          },
      )
    }
  }
}

@Composable
private fun SectionHeader(title: String) {
  Text(
      text = title,
      style =
          MaterialTheme.typography.labelMedium.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = FontWeight.SemiBold,
          ),
      modifier =
          Modifier.padding(
              start = ChompSpacing.xs,
              top = ChompSpacing.lg,
              bottom = ChompSpacing.xs,
          ),
  )
}

@Composable
private fun AccountSection(
    viewState: SettingsViewState,
    onHandleEvent: (SettingsUiEvent) -> Unit,
) {
  ElevatedCard(modifier = Modifier.fillMaxWidth()) {
    Column {
      ListItem(
          headlineContent = { Text("Email") },
          trailingContent = {
            Text(
                text = viewState.email.ifEmpty { "—" },
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
            )
          },
          colors = ListItemDefaults.colors(containerColor = Color.Transparent),
      )
      HorizontalDivider()
      ListItem(
          headlineContent = { Text("Display Name") },
          trailingContent = {
            Text(
                text = viewState.displayName.ifEmpty { "—" },
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
            )
          },
          colors = ListItemDefaults.colors(containerColor = Color.Transparent),
      )
      HorizontalDivider()
      ListItem(
          headlineContent = { Text("Manage Profile") },
          trailingContent = {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          },
          colors = ListItemDefaults.colors(containerColor = Color.Transparent),
          modifier = Modifier.clickable { onHandleEvent(SettingsUiEvent.OnManageProfile) },
      )
    }
  }
}

@Composable
private fun SubscriptionSection(
    viewState: SettingsViewState,
    onHandleEvent: (SettingsUiEvent) -> Unit,
) {
  ElevatedCard(modifier = Modifier.fillMaxWidth()) {
    Column {
      if (!viewState.hasPro) {
        ListItem(
            headlineContent = {
              Text(
                  "Upgrade to Pro",
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.SemiBold,
              )
            },
            trailingContent = {
              Icon(
                  Icons.Default.ChevronRight,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
              )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.clickable { onHandleEvent(SettingsUiEvent.OnUpgradeToPro) },
        )
        HorizontalDivider()
      }
      ListItem(
          headlineContent = {
            Text("Manage Billing & Subscription", color = MaterialTheme.colorScheme.primary)
          },
          colors = ListItemDefaults.colors(containerColor = Color.Transparent),
          modifier = Modifier.clickable { onHandleEvent(SettingsUiEvent.OnManageBilling) },
      )
      HorizontalDivider()
      ListItem(
          headlineContent = {
            if (viewState.isRestoringPurchases) {
              CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
              Text("Restore Purchases", color = MaterialTheme.colorScheme.primary)
            }
          },
          colors = ListItemDefaults.colors(containerColor = Color.Transparent),
          modifier =
              Modifier.clickable(enabled = !viewState.isRestoringPurchases) {
                onHandleEvent(SettingsUiEvent.OnRestorePurchases)
              },
      )
    }
  }
}

@Composable
private fun PreferencesSection(onHandleEvent: (SettingsUiEvent) -> Unit) {
  ElevatedCard(modifier = Modifier.fillMaxWidth()) {
    ListItem(
        headlineContent = { Text("Notifications") },
        trailingContent = {
          Icon(
              Icons.Default.ChevronRight,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable { onHandleEvent(SettingsUiEvent.OnNotifications) },
    )
  }
}

@Composable
private fun SupportSection(onHandleEvent: (SettingsUiEvent) -> Unit) {
  ElevatedCard(modifier = Modifier.fillMaxWidth()) {
    Column {
      ListItem(
          headlineContent = { Text("Contact Support", color = MaterialTheme.colorScheme.primary) },
          colors = ListItemDefaults.colors(containerColor = Color.Transparent),
          modifier = Modifier.clickable { onHandleEvent(SettingsUiEvent.OnContactSupport) },
      )
      HorizontalDivider()
      ListItem(
          headlineContent = { Text("Privacy Policy", color = MaterialTheme.colorScheme.primary) },
          colors = ListItemDefaults.colors(containerColor = Color.Transparent),
          modifier = Modifier.clickable { onHandleEvent(SettingsUiEvent.OnPrivacyPolicy) },
      )
      HorizontalDivider()
      ListItem(
          headlineContent = { Text("Terms of Service", color = MaterialTheme.colorScheme.primary) },
          colors = ListItemDefaults.colors(containerColor = Color.Transparent),
          modifier = Modifier.clickable { onHandleEvent(SettingsUiEvent.OnTermsOfService) },
      )
    }
  }
}

@Composable
private fun DangerZoneSection(
    viewState: SettingsViewState,
    onHandleEvent: (SettingsUiEvent) -> Unit,
) {
  ElevatedCard(modifier = Modifier.fillMaxWidth()) {
    Column {
      ListItem(
          headlineContent = {
            if (viewState.isSigningOut) {
              CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
              Text(
                  "Sign Out",
                  color = MaterialTheme.colorScheme.error,
                  fontWeight = FontWeight.Medium,
              )
            }
          },
          colors = ListItemDefaults.colors(containerColor = Color.Transparent),
          modifier =
              Modifier.clickable(enabled = !viewState.isSigningOut) {
                onHandleEvent(SettingsUiEvent.OnSignOut)
              },
      )
      HorizontalDivider()
      ListItem(
          headlineContent = {
            if (viewState.isDeletingAccount) {
              CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
              Text(
                  "Delete Account",
                  color = MaterialTheme.colorScheme.error,
                  fontWeight = FontWeight.Medium,
              )
            }
          },
          colors = ListItemDefaults.colors(containerColor = Color.Transparent),
          modifier =
              Modifier.clickable(enabled = !viewState.isDeletingAccount) {
                onHandleEvent(SettingsUiEvent.OnDeleteAccount)
              },
      )
    }
  }
}

@Composable
private fun VersionFooter() {
  ElevatedCard(
      modifier = Modifier.fillMaxWidth().padding(top = ChompSpacing.lg, bottom = ChompSpacing.xl),
  ) {
    Column {
      ListItem(
          headlineContent = { Text("Version", style = MaterialTheme.typography.bodyMedium) },
          trailingContent = {
            Text(
                text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
            )
          },
          colors = ListItemDefaults.colors(containerColor = Color.Transparent),
      )
      if (BuildConfig.DEBUG) {
        HorizontalDivider()
        ListItem(
            headlineContent = {
              Text(
                  "Environment",
                  style = MaterialTheme.typography.bodyMedium.copy(color = debugAmber),
              )
            },
            trailingContent = {
              Text("Debug", style = MaterialTheme.typography.bodySmall.copy(color = debugAmber))
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
  ChompSquadTheme {
    SettingsScreen(
        onHandleEvent = {},
        viewState =
            SettingsViewState(
                email = "sean@example.com",
                displayName = "Sean",
                hasPro = false,
            ),
    )
  }
}
