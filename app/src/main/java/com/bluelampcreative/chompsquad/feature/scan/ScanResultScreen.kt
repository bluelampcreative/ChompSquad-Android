package com.bluelampcreative.chompsquad.feature.scan

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.bluelampcreative.chompsquad.domain.model.Ingredient
import com.bluelampcreative.chompsquad.domain.model.Step
import com.bluelampcreative.chompsquad.ui.navigation.NavEvent
import com.bluelampcreative.chompsquad.ui.theme.ChompSpacing
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Suppress(
    "ModifierMissing",
    "ComposeModifierMissing",
) // Root navigation composable — no modifier parameter needed
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultScreen(
    onNavEvent: (NavEvent) -> Unit,
    viewModel: ScanResultViewModel = koinViewModel(),
) {
  val viewState by viewModel.viewState.collectAsStateWithLifecycle()
  val currentOnNavEvent by rememberUpdatedState(onNavEvent)
  val haptic = LocalHapticFeedback.current
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(Unit) { viewModel.navEvents.collect { currentOnNavEvent(it) } }

  // Haptic + brief delay before navigating away so the spring animation is visible.
  LaunchedEffect(viewState.saveSuccess) {
    if (viewState.saveSuccess) {
      haptic.performHapticFeedback(HapticFeedbackType.Confirm)
      delay(SAVE_SUCCESS_DELAY_MS)
      viewModel.handleEvent(ScanResultUiEvent.OnNavigateAfterSave)
    }
  }

  // Show save errors as a transient snackbar.
  val saveError = viewState.saveError
  LaunchedEffect(saveError) {
    if (saveError != null) {
      snackbarHostState.showSnackbar(saveError)
    }
  }

  val fabScale by
      animateFloatAsState(
          targetValue = if (viewState.saveSuccess) 1.25f else 1f,
          animationSpec =
              spring(
                  dampingRatio = Spring.DampingRatioLowBouncy,
                  stiffness = Spring.StiffnessMediumLow,
              ),
          label = "fabScale",
      )

  Scaffold(
      snackbarHost = { SnackbarHost(snackbarHostState) },
      topBar = {
        TopAppBar(
            title = { Text("Review Recipe") },
            navigationIcon = {
              IconButton(onClick = { viewModel.handleEvent(ScanResultUiEvent.OnClose) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
              }
            },
        )
      },
      floatingActionButton = {
        if (!viewState.isLoading) {
          ExtendedFloatingActionButton(
              onClick = {
                if (!viewState.isSaving && !viewState.saveSuccess) {
                  viewModel.handleEvent(ScanResultUiEvent.OnSave)
                }
              },
              icon = {
                if (viewState.isSaving) {
                  CircularProgressIndicator(
                      modifier = Modifier.size(24.dp),
                      color = MaterialTheme.colorScheme.onPrimary,
                      strokeWidth = 2.dp,
                  )
                } else {
                  Icon(Icons.Default.Check, contentDescription = null)
                }
              },
              text = { Text(if (viewState.saveSuccess) "Saved!" else "Save") },
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.scale(fabScale),
          )
        }
      },
  ) { innerPadding ->
    if (viewState.isLoading) {
      Box(
          modifier = Modifier.fillMaxSize().padding(innerPadding),
          contentAlignment = Alignment.Center,
      ) {
        CircularProgressIndicator()
      }
    } else {
      ScanResultContent(
          viewState = viewState,
          onEvent = { viewModel.handleEvent(it) },
          modifier = Modifier.padding(innerPadding),
      )
    }
  }
}

private const val SAVE_SUCCESS_DELAY_MS = 800L

@Composable
internal fun ScanResultContent(
    viewState: ScanResultViewState,
    onEvent: (ScanResultUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
  LazyColumn(
      modifier = modifier.fillMaxSize().padding(horizontal = ChompSpacing.md),
      verticalArrangement = Arrangement.spacedBy(ChompSpacing.sm),
  ) {
    viewState.heroImageUrl?.let { url ->
      item { HeroImage(url = url, modifier = Modifier.padding(vertical = ChompSpacing.sm)) }
    }

    item { SectionHeader("Basic Info") }
    item {
      ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(ChompSpacing.md),
            verticalArrangement = Arrangement.spacedBy(ChompSpacing.sm),
        ) {
          RecipeTextField(
              value = viewState.title,
              onValueChange = { onEvent(ScanResultUiEvent.OnTitleChanged(it)) },
              label = "Title",
          )
          Row(horizontalArrangement = Arrangement.spacedBy(ChompSpacing.sm)) {
            RecipeTextField(
                value = viewState.yieldAmount,
                onValueChange = { onEvent(ScanResultUiEvent.OnYieldAmountChanged(it)) },
                label = "Yield",
                modifier = Modifier.weight(1f),
                // Text keyboard supports fractions/mixed numbers (e.g. "1/2", "2 1/4").
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            )
            RecipeTextField(
                value = viewState.yieldUnit,
                onValueChange = { onEvent(ScanResultUiEvent.OnYieldUnitChanged(it)) },
                label = "Unit",
                modifier = Modifier.weight(1f),
            )
          }
          RecipeTextField(
              value = viewState.source,
              onValueChange = { onEvent(ScanResultUiEvent.OnSourceChanged(it)) },
              label = "Source",
          )
          RecipeTextField(
              value = viewState.tags,
              onValueChange = { onEvent(ScanResultUiEvent.OnTagsChanged(it)) },
              label = "Tags (comma-separated)",
          )
        }
      }
    }

    item { SectionHeader("Time (minutes)") }
    item {
      ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(ChompSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(ChompSpacing.sm),
        ) {
          RecipeTextField(
              value = viewState.prepTime,
              onValueChange = { onEvent(ScanResultUiEvent.OnPrepTimeChanged(it)) },
              label = "Prep",
              modifier = Modifier.weight(1f),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          )
          RecipeTextField(
              value = viewState.cookTime,
              onValueChange = { onEvent(ScanResultUiEvent.OnCookTimeChanged(it)) },
              label = "Cook",
              modifier = Modifier.weight(1f),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          )
          RecipeTextField(
              value = viewState.totalTime,
              onValueChange = { onEvent(ScanResultUiEvent.OnTotalTimeChanged(it)) },
              label = "Total",
              modifier = Modifier.weight(1f),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          )
        }
      }
    }

    if (viewState.ingredients.isNotEmpty()) {
      item { SectionHeader("Ingredients") }
      item {
        IngredientsSection(
            ingredients = viewState.ingredients,
            onEditClick = { onEvent(ScanResultUiEvent.OnEditIngredients) },
        )
      }
    }

    if (viewState.steps.isNotEmpty()) {
      item { SectionHeader("Steps") }
      item {
        StepsSection(
            steps = viewState.steps,
            onEditClick = { onEvent(ScanResultUiEvent.OnEditSteps) },
        )
      }
    }

    // Bottom padding so FAB doesn't obscure the last item.
    item { Spacer(modifier = Modifier.height(ChompSpacing.xxl)) }
  }
}

@Composable
private fun HeroImage(url: String, modifier: Modifier = Modifier) {
  ElevatedCard(modifier = modifier.fillMaxWidth().height(200.dp)) {
    AsyncImage(
        model = url,
        contentDescription = "Recipe image",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize(),
    )
  }
}

@Composable
private fun SectionHeader(label: String) {
  Text(
      text = label,
      style = MaterialTheme.typography.labelLarge,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(start = ChompSpacing.xs, top = ChompSpacing.sm),
  )
}

@Composable
private fun RecipeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      label = { Text(label) },
      modifier = modifier.fillMaxWidth(),
      singleLine = true,
      keyboardOptions = keyboardOptions,
      shape = MaterialTheme.shapes.medium,
  )
}

@Suppress("UnstableCollections")
@Composable
private fun IngredientsSection(ingredients: List<Ingredient>, onEditClick: () -> Unit) {
  ElevatedCard(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(ChompSpacing.md)) {
      ingredients.forEachIndexed { index, ingredient ->
        IngredientRow(ingredient)
        if (index < ingredients.lastIndex) {
          HorizontalDivider(modifier = Modifier.padding(vertical = ChompSpacing.xs))
        }
      }
      Spacer(modifier = Modifier.height(ChompSpacing.sm))
      Button(onClick = onEditClick, modifier = Modifier.fillMaxWidth()) { Text("Edit Ingredients") }
    }
  }
}

@Suppress("UnstableCollections")
@Composable
private fun StepsSection(steps: List<Step>, onEditClick: () -> Unit) {
  ElevatedCard(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(ChompSpacing.md)) {
      steps.forEachIndexed { index, step ->
        StepRow(step)
        if (index < steps.lastIndex) {
          HorizontalDivider(modifier = Modifier.padding(vertical = ChompSpacing.xs))
        }
      }
      Spacer(modifier = Modifier.height(ChompSpacing.sm))
      Button(onClick = onEditClick, modifier = Modifier.fillMaxWidth()) { Text("Edit Steps") }
    }
  }
}

@Composable
private fun IngredientRow(ingredient: Ingredient) {
  val text = buildString {
    if (!ingredient.quantity.isNullOrBlank()) append("${ingredient.quantity} ")
    if (!ingredient.unit.isNullOrBlank()) append("${ingredient.unit} ")
    append(ingredient.name)
    if (!ingredient.prepNote.isNullOrBlank()) append(", ${ingredient.prepNote}")
  }
  Text(text = text, style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun StepRow(step: Step) {
  Row(verticalAlignment = Alignment.Top) {
    Text(
        text = "${step.position}.",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.width(28.dp),
    )
    Text(text = step.instruction, style = MaterialTheme.typography.bodyMedium)
  }
}
