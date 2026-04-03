package com.bluelampcreative.chompsquad.feature.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluelampcreative.chompsquad.ui.navigation.NavEvent
import com.bluelampcreative.chompsquad.ui.theme.ChompSpacing
import org.koin.androidx.compose.koinViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Suppress(
    "ModifierMissing",
    "ComposeModifierMissing",
) // Root navigation composable — no modifier parameter needed
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepEditorScreen(
    onNavEvent: (NavEvent) -> Unit,
    viewModel: StepEditorViewModel = koinViewModel(),
) {
  val viewState by viewModel.viewState.collectAsStateWithLifecycle()
  val currentOnNavEvent by rememberUpdatedState(onNavEvent)

  LaunchedEffect(Unit) { viewModel.navEvents.collect { currentOnNavEvent(it) } }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Edit Steps") },
            navigationIcon = {
              IconButton(onClick = { viewModel.handleEvent(StepEditorUiEvent.OnClose) }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
              }
            },
        )
      },
      floatingActionButton = {
        ExtendedFloatingActionButton(
            onClick = { viewModel.handleEvent(StepEditorUiEvent.OnDone) },
            icon = { Icon(Icons.Default.Check, contentDescription = null) },
            text = { Text("Done") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        )
      },
  ) { innerPadding ->
    StepEditorContent(
        viewState = viewState,
        onEvent = { viewModel.handleEvent(it) },
        modifier = Modifier.padding(innerPadding),
    )
  }
}

@Suppress("UnstableCollections")
@Composable
private fun StepEditorContent(
    viewState: StepEditorViewState,
    onEvent: (StepEditorUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
  val lazyListState = rememberLazyListState()
  val reorderState =
      rememberReorderableLazyListState(lazyListState) { from, to ->
        onEvent(StepEditorUiEvent.OnMove(from.index, to.index))
      }

  LazyColumn(
      state = lazyListState,
      modifier = modifier.fillMaxSize().padding(horizontal = ChompSpacing.md),
      verticalArrangement = Arrangement.spacedBy(ChompSpacing.sm),
  ) {
    itemsIndexed(viewState.steps, key = { _, step -> step.id }) { index, step ->
      ReorderableItem(reorderState, key = step.id) {
        StepRow(
            step = step,
            stepNumber = index + 1,
            onEvent = onEvent,
            modifier = Modifier.padding(vertical = ChompSpacing.xs / 2),
            dragHandleModifier =
                Modifier.draggableHandle().size(24.dp).padding(end = ChompSpacing.xs),
        )
      }
      if (index < viewState.steps.lastIndex) {
        HorizontalDivider()
      }
    }

    item {
      TextButton(
          onClick = { onEvent(StepEditorUiEvent.OnAddStep) },
          modifier = Modifier.fillMaxWidth(),
      ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Text("Add Step", modifier = Modifier.padding(start = ChompSpacing.xs))
      }
    }

    // Bottom padding so FAB doesn't obscure the last item.
    item { Spacer(modifier = Modifier.height(ChompSpacing.xxl)) }
  }
}

@Composable
private fun StepRow(
    step: EditableStep,
    stepNumber: Int,
    onEvent: (StepEditorUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    dragHandleModifier: Modifier = Modifier,
) {
  ElevatedCard(modifier = modifier.fillMaxWidth().heightIn(min = 48.dp)) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .padding(horizontal = ChompSpacing.sm, vertical = ChompSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ChompSpacing.xs),
    ) {
      Icon(
          imageVector = Icons.Default.Menu,
          contentDescription = "Drag to reorder",
          modifier = dragHandleModifier,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      Text(
          text = "$stepNumber.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.width(28.dp),
      )

      OutlinedTextField(
          value = step.instruction,
          onValueChange = { onEvent(StepEditorUiEvent.OnInstructionChanged(step.id, it)) },
          label = { Text("Instruction") },
          modifier = Modifier.weight(1f),
          shape = MaterialTheme.shapes.medium,
      )

      IconButton(onClick = { onEvent(StepEditorUiEvent.OnRemoveStep(step.id)) }) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Remove step",
            tint = MaterialTheme.colorScheme.error,
        )
      }
    }
  }
}
