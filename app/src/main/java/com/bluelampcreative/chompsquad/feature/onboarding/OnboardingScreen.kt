package com.bluelampcreative.chompsquad.feature.onboarding

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bluelampcreative.chompsquad.ui.theme.ChompSquadTheme
import com.bluelampcreative.chompsquad.ui.theme.brandGolden
import com.bluelampcreative.chompsquad.ui.theme.brandGoldenDark
import com.bluelampcreative.chompsquad.ui.theme.brandGreen

// ── Data ──────────────────────────────────────────────────────────────────────

private data class OnboardingPageData(
    val icon: ImageVector,
    val illustrationColor: Color,
    val headline: String,
    val body: String,
)

private val pages =
    listOf(
        OnboardingPageData(
            icon = Icons.Default.CameraAlt,
            illustrationColor = brandGreen,
            headline = "Scan any recipe\nin seconds",
            body =
                "Point your camera at any recipe — cookbook, card, or screen — and let ChompSquad do the rest.",
        ),
        OnboardingPageData(
            icon = Icons.AutoMirrored.Default.MenuBook,
            illustrationColor = brandGreen,
            headline = "Build your perfect\ncookbook",
            body =
                "Every recipe you scan is saved, organized, and searchable in your personal collection.",
        ),
        OnboardingPageData(
            icon = Icons.Default.Restaurant,
            illustrationColor = brandGreen,
            headline = "Cook with\nconfidence",
            body = "Your entire recipe collection — always at your fingertips, online or off.",
        ),
    )

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun OnboardingScreen(
    onNavigateToSignUp: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val pagerState = rememberPagerState { pages.size }

  Column(
      modifier =
          modifier
              .fillMaxSize()
              .background(MaterialTheme.colorScheme.background)
              .statusBarsPadding()
              .navigationBarsPadding(),
  ) {
    // Skip button — invisible on the last page but always occupies space so
    // the pager and CTAs below don't shift when it disappears.
    val isLastPage = pagerState.currentPage >= pages.lastIndex
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
      TextButton(
          onClick = onNavigateToSignIn,
          enabled = !isLastPage,
          modifier = Modifier.alpha(if (isLastPage) 0f else 1f),
      ) {
        Text("Skip")
      }
    }

    // Pager — takes remaining space above the CTA zone
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.weight(1f),
    ) { pageIndex ->
      OnboardingPageContent(
          page = pages[pageIndex],
          isCurrentPage = pagerState.currentPage == pageIndex,
      )
    }

    // Page indicators
    PageIndicatorRow(
        pageCount = pages.size,
        currentPage = pagerState.currentPage,
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
    )

    // CTAs — always visible
    OnboardingCtas(
        onNavigateToSignUp = onNavigateToSignUp,
        onNavigateToSignIn = onNavigateToSignIn,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp),
    )
  }
}

// ── Page content ──────────────────────────────────────────────────────────────

@Composable
private fun OnboardingPageContent(
    page: OnboardingPageData,
    isCurrentPage: Boolean,
    modifier: Modifier = Modifier,
) {
  // Spring-scale entrance animation fires each time this page becomes current.
  var entered by remember(isCurrentPage) { mutableStateOf(false) }
  LaunchedEffect(isCurrentPage) { if (isCurrentPage) entered = true }

  val illustrationScale by
      animateFloatAsState(
          targetValue = if (entered) 1f else 0.88f,
          animationSpec =
              spring(
                  dampingRatio = Spring.DampingRatioMediumBouncy,
                  stiffness = Spring.StiffnessLow,
              ),
          label = "illustration-scale",
      )

  Column(
      modifier = modifier.fillMaxSize().padding(horizontal = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
  ) {
    // Icon placeholder — replace with brand illustration in task 3.9.
    Box(
        modifier =
            Modifier.size(260.dp)
                .scale(illustrationScale)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(page.illustrationColor),
        contentAlignment = Alignment.Center,
    ) {
      Icon(
          imageVector = page.icon,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(120.dp),
      )
    }

    Spacer(modifier = Modifier.height(40.dp))

    Text(
        text = page.headline,
        style = MaterialTheme.typography.headlineLarge,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground,
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = page.body,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

// ── Page indicators ───────────────────────────────────────────────────────────

@Composable
private fun PageIndicatorRow(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
  Row(
      modifier = modifier,
      horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    repeat(pageCount) { index ->
      val isSelected = index == currentPage
      Box(
          modifier =
              Modifier.size(if (isSelected) 10.dp else 7.dp)
                  .clip(CircleShape)
                  .background(
                      if (isSelected) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.outlineVariant
                  ),
      )
    }
  }
}

// ── CTAs ──────────────────────────────────────────────────────────────────────

@Composable
private fun OnboardingCtas(
    onNavigateToSignUp: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
  Column(
      modifier = modifier,
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Button(
        onClick = onNavigateToSignUp,
        modifier = Modifier.fillMaxWidth(),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = brandGolden,
                contentColor = brandGoldenDark,
            ),
    ) {
      Text("Create account")
    }

    TextButton(
        onClick = onNavigateToSignIn,
        modifier = Modifier.fillMaxWidth(),
    ) {
      Text(
          text = "Sign in",
          color = MaterialTheme.colorScheme.primary,
      )
    }
  }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OnboardingScreenPreview() {
  ChompSquadTheme {
    OnboardingScreen(
        onNavigateToSignUp = {},
        onNavigateToSignIn = {},
    )
  }
}
