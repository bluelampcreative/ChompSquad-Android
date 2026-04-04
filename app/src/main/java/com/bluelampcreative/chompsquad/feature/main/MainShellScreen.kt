package com.bluelampcreative.chompsquad.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.bluelampcreative.chompsquad.feature.catalog.CatalogScreen
import com.bluelampcreative.chompsquad.feature.profile.ProfileScreen
import com.bluelampcreative.chompsquad.ui.navigation.NavEvent

/**
 * Root shell for the authenticated app. Hosts a 4-tab [NavigationBar] (Catalog, Scan, Planner,
 * Profile). Tapping the Scan tab fires [NavEvent.NavigateToCameraCapture] rather than switching tab
 * content — the camera flow is a full-screen pushed destination on top of this shell. Selected tab
 * is preserved across configuration changes via [rememberSaveable].
 *
 * Catalog and Planner are placeholder stubs pending tasks 3.1 and Phase 6.
 */
@Suppress(
    "ModifierMissing",
    "ComposeModifierMissing",
) // Root navigation composable — no modifier parameter needed
@Composable
fun MainShellScreen(onNavEvent: (NavEvent) -> Unit) {
  val currentOnNavEvent by rememberUpdatedState(onNavEvent)
  var selectedTab by rememberSaveable { mutableStateOf(MainTab.CATALOG) }

  Scaffold(
      bottomBar = {
        NavigationBar {
          MainTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = {
                  if (tab == MainTab.SCAN) {
                    currentOnNavEvent(NavEvent.NavigateToCameraCapture)
                  } else {
                    selectedTab = tab
                  }
                },
                icon = { Icon(imageVector = tab.icon, contentDescription = null) },
                label = { Text(tab.label) },
            )
          }
        }
      },
  ) { innerPadding ->
    when (selectedTab) {
      MainTab.CATALOG ->
          CatalogScreen(
              onNavEvent = currentOnNavEvent,
              modifier = Modifier.padding(innerPadding),
          )
      // SCAN never becomes the active tab — handled via nav event above.
      MainTab.SCAN,
      MainTab.PLANNER ->
          TabPlaceholder(selectedTab.label, modifier = Modifier.padding(innerPadding))
      MainTab.PROFILE ->
          ProfileScreen(onNavEvent = currentOnNavEvent, modifier = Modifier.padding(innerPadding))
    }
  }
}

private enum class MainTab(val label: String, val icon: ImageVector) {
  CATALOG("Catalog", Icons.AutoMirrored.Filled.MenuBook),
  SCAN("Scan", Icons.Default.DocumentScanner),
  PLANNER("Planner", Icons.Default.CalendarMonth),
  PROFILE("Profile", Icons.Default.Person),
}

@Composable
private fun TabPlaceholder(label: String, modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(text = label, style = MaterialTheme.typography.headlineMedium)
  }
}
