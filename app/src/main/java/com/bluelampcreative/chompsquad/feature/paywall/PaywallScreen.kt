package com.bluelampcreative.chompsquad.feature.paywall

import androidx.activity.ComponentActivity
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluelampcreative.chompsquad.ui.navigation.NavEvent
import com.bluelampcreative.chompsquad.ui.theme.ChompSpacing
import com.bluelampcreative.chompsquad.ui.theme.ChompSquadTheme
import com.bluelampcreative.chompsquad.ui.theme.brandGolden
import com.bluelampcreative.chompsquad.ui.theme.brandGreen
import com.bluelampcreative.chompsquad.ui.theme.brandGreenDark
import com.bluelampcreative.chompsquad.ui.theme.brandGreenLight
import org.koin.androidx.compose.koinViewModel

private val proBenefits =
    listOf(
        "Unlimited recipe scans",
        "Ad-free experience",
        "AI hero photo generation",
        "Priority support",
    )

@Composable
fun PaywallScreen(
    onNavEvent: (NavEvent) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaywallViewModel = koinViewModel(),
) {
  val currentOnNavEvent by rememberUpdatedState(onNavEvent)
  LaunchedEffect(Unit) { viewModel.navEvents.collect { currentOnNavEvent(it) } }
  val viewState by viewModel.viewState.collectAsStateWithLifecycle()

  PaywallScreen(
      onHandleEvent = viewModel::handleEvent,
      viewState = viewState,
      modifier = modifier,
  )
}

@Composable
fun PaywallScreen(
    onHandleEvent: (PaywallUiEvent) -> Unit,
    viewState: PaywallViewState,
    modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val activity = context as? ComponentActivity

  Box(modifier = modifier.fillMaxSize()) {
    Box(
        modifier =
            Modifier.fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(brandGreenDark, brandGreen, brandGreenLight),
                        endY = 600f,
                    )
                )
    ) {
      Column(
          modifier =
              Modifier.fillMaxSize()
                  .statusBarsPadding()
                  .navigationBarsPadding()
                  .verticalScroll(rememberScrollState())
                  .padding(horizontal = ChompSpacing.md),
          horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        // Close button
        Box(modifier = Modifier.fillMaxWidth()) {
          IconButton(
              onClick = { onHandleEvent(PaywallUiEvent.OnClose) },
              modifier = Modifier.align(Alignment.TopEnd),
          ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
            )
          }
        }

        Spacer(Modifier.height(ChompSpacing.md))

        // Hero headline
        Text(
            text = "Chomp Squad Pro",
            style =
                MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                ),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(ChompSpacing.sm))

        Text(
            text = "Unlock the full ChompSquad experience",
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White.copy(alpha = 0.85f),
                ),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(ChompSpacing.xl))

        // Billing period toggle
        PeriodToggle(
            selectedPeriod = viewState.selectedPeriod,
            onSelect = { onHandleEvent(PaywallUiEvent.OnSelectPeriod(it)) },
        )

        Spacer(Modifier.height(ChompSpacing.lg))

        // Benefits card
        BenefitsCard()

        Spacer(Modifier.height(ChompSpacing.lg))

        // Price display
        PriceDisplay(viewState = viewState)

        Spacer(Modifier.height(ChompSpacing.lg))

        // Purchase CTA
        PurchaseButton(
            viewState = viewState,
            activityAvailable = activity != null,
            onClick = { activity?.let { onHandleEvent(PaywallUiEvent.OnPurchase(it)) } },
        )

        Spacer(Modifier.height(ChompSpacing.sm))

        TextButton(onClick = { onHandleEvent(PaywallUiEvent.OnClose) }) {
          Text(
              text = "Maybe later",
              color = Color.White.copy(alpha = 0.7f),
          )
        }

        Spacer(Modifier.height(ChompSpacing.lg))

        // Legal footer
        Text(
            text = "Terms of Service  ·  Privacy Policy",
            style =
                MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.5f)),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(ChompSpacing.md))
      }
    }

    // Error dialog
    if (viewState.errorMessage != null) {
      AlertDialog(
          onDismissRequest = { onHandleEvent(PaywallUiEvent.OnDismissError) },
          title = { Text("Purchase failed") },
          text = { Text(viewState.errorMessage) },
          confirmButton = {
            TextButton(onClick = { onHandleEvent(PaywallUiEvent.OnDismissError) }) { Text("OK") }
          },
      )
    }
  }
}

@Composable
private fun PeriodToggle(selectedPeriod: BillingPeriod, onSelect: (BillingPeriod) -> Unit) {
  val periods = listOf(BillingPeriod.Monthly, BillingPeriod.Annual)

  SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
    periods.forEachIndexed { index, period ->
      SegmentedButton(
          selected = selectedPeriod == period,
          onClick = { onSelect(period) },
          shape = SegmentedButtonDefaults.itemShape(index = index, count = periods.size),
          colors =
              SegmentedButtonDefaults.colors(
                  activeContainerColor = brandGolden,
                  activeContentColor = Color.Black,
                  inactiveContainerColor = Color.White.copy(alpha = 0.15f),
                  inactiveContentColor = Color.White,
              ),
          label = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                  text = if (period == BillingPeriod.Monthly) "Monthly" else "Annual",
                  fontWeight = FontWeight.SemiBold,
              )
              if (period == BillingPeriod.Annual) {
                Text(
                    text = "Save 20%",
                    style = MaterialTheme.typography.labelSmall,
                )
              }
            }
          },
      )
    }
  }
}

@Composable
private fun BenefitsCard() {
  ElevatedCard(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
  ) {
    Column(modifier = Modifier.padding(ChompSpacing.lg)) {
      Text(
          text = "Everything in Pro",
          style =
              MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = brandGreenDark,
              ),
      )
      Spacer(Modifier.height(ChompSpacing.md))
      proBenefits.forEach { benefit ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ChompSpacing.sm),
            modifier = Modifier.padding(vertical = ChompSpacing.xs),
        ) {
          Surface(
              shape = RoundedCornerShape(50),
              color = brandGreen,
              modifier = Modifier.size(24.dp),
          ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.padding(4.dp),
            )
          }
          Text(
              text = benefit,
              style = MaterialTheme.typography.bodyMedium,
          )
        }
      }
    }
  }
}

@Composable
private fun PriceDisplay(viewState: PaywallViewState) {
  val selectedPackage =
      when (viewState.selectedPeriod) {
        BillingPeriod.Monthly -> viewState.monthlyPackage
        BillingPeriod.Annual -> viewState.annualPackage
      }

  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    when {
      viewState.isLoadingOfferings -> CircularProgressIndicator(color = Color.White)
      selectedPackage == null ->
          Text(
              text = "Pricing unavailable",
              style =
                  MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.7f)),
          )
      else -> {
        Text(
            text = selectedPackage.formattedPrice,
            style =
                MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = brandGolden,
                ),
        )
        Text(
            text =
                when (viewState.selectedPeriod) {
                  BillingPeriod.Monthly -> "per month"
                  BillingPeriod.Annual -> "per year, billed annually"
                },
            style =
                MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.8f)),
        )
      }
    }
  }
}

@Composable
private fun PurchaseButton(
    viewState: PaywallViewState,
    activityAvailable: Boolean,
    onClick: () -> Unit,
) {
  val selectedPackage =
      when (viewState.selectedPeriod) {
        BillingPeriod.Monthly -> viewState.monthlyPackage
        BillingPeriod.Annual -> viewState.annualPackage
      }
  val enabled =
      activityAvailable &&
          !viewState.isLoadingOfferings &&
          !viewState.isPurchasing &&
          selectedPackage != null

  Button(
      onClick = onClick,
      enabled = enabled,
      modifier = Modifier.fillMaxWidth().height(56.dp),
      shape = RoundedCornerShape(12.dp),
      colors =
          ButtonDefaults.buttonColors(
              containerColor = brandGolden,
              contentColor = Color.Black,
          ),
  ) {
    if (viewState.isPurchasing) {
      CircularProgressIndicator(
          modifier = Modifier.size(20.dp),
          color = Color.Black,
          strokeWidth = 2.dp,
      )
    } else {
      Text(
          text =
              if (selectedPackage != null) {
                "Get Pro · ${selectedPackage.formattedPrice}"
              } else {
                "Get Pro"
              },
          fontWeight = FontWeight.Bold,
          style = MaterialTheme.typography.titleMedium,
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun PaywallScreenPreview() {
  ChompSquadTheme {
    PaywallScreen(
        onHandleEvent = {},
        viewState =
            PaywallViewState(
                isLoadingOfferings = false,
                monthlyPackage = null,
                annualPackage = null,
                selectedPeriod = BillingPeriod.Annual,
            ),
    )
  }
}
