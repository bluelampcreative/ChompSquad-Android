package com.bluelampcreative.chompsquad.feature.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
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
fun IngredientEditorScreen(
    onNavEvent: (NavEvent) -> Unit,
    viewModel: IngredientEditorViewModel = koinViewModel(),
) {
  val viewState by viewModel.viewState.collectAsStateWithLifecycle()
  val currentOnNavEvent by rememberUpdatedState(onNavEvent)

  LaunchedEffect(Unit) { viewModel.navEvents.collect { currentOnNavEvent(it) } }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Edit Ingredients") },
            navigationIcon = {
              IconButton(onClick = { viewModel.handleEvent(IngredientEditorUiEvent.OnClose) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
              }
            },
        )
      },
      floatingActionButton = {
        ExtendedFloatingActionButton(
            onClick = { viewModel.handleEvent(IngredientEditorUiEvent.OnDone) },
            icon = { Icon(Icons.Default.Check, contentDescription = null) },
            text = { Text("Done") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        )
      },
  ) { innerPadding ->
    IngredientEditorContent(
        viewState = viewState,
        onEvent = { viewModel.handleEvent(it) },
        modifier = Modifier.padding(innerPadding),
    )
  }
}

@Suppress("UnstableCollections")
@Composable
private fun IngredientEditorContent(
    viewState: IngredientEditorViewState,
    onEvent: (IngredientEditorUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
  val lazyListState = rememberLazyListState()
  val reorderState =
      rememberReorderableLazyListState(lazyListState) { from, to ->
        onEvent(IngredientEditorUiEvent.OnMove(from.index, to.index))
      }

  LazyColumn(
      state = lazyListState,
      modifier = modifier.fillMaxSize().padding(horizontal = ChompSpacing.md),
      verticalArrangement = Arrangement.spacedBy(ChompSpacing.sm),
  ) {
    itemsIndexed(viewState.ingredients, key = { _, ingredient -> ingredient.id }) {
        index,
        ingredient ->
      ReorderableItem(reorderState, key = ingredient.id) {
        // draggableHandle() is a ReorderableItemScope member extension — must be called
        // here so the scope is in scope, then passed as a plain Modifier to IngredientRow.
        IngredientRow(
            ingredient = ingredient,
            onEvent = onEvent,
            modifier = Modifier.padding(vertical = ChompSpacing.xs / 2),
            dragHandleModifier =
                Modifier.draggableHandle().size(24.dp).padding(end = ChompSpacing.xs),
        )
      }
      if (index < viewState.ingredients.lastIndex) {
        HorizontalDivider()
      }
    }

    item {
      TextButton(
          onClick = { onEvent(IngredientEditorUiEvent.OnAddIngredient) },
          modifier = Modifier.fillMaxWidth(),
      ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Text("Add Ingredient", modifier = Modifier.padding(start = ChompSpacing.xs))
      }
    }

    // Bottom padding so FAB doesn't obscure the last item.
    item { Spacer(modifier = Modifier.height(ChompSpacing.xxl)) }
  }
}

@Composable
private fun IngredientRow(
    ingredient: EditableIngredient,
    onEvent: (IngredientEditorUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    dragHandleModifier: Modifier = Modifier,
) {
  ElevatedCard(
      modifier = modifier.fillMaxWidth().heightIn(min = 48.dp),
  ) {
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

      Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(ChompSpacing.xs),
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(ChompSpacing.xs)) {
          IngredientTextField(
              value = ingredient.quantity,
              onValueChange = {
                onEvent(IngredientEditorUiEvent.OnQuantityChanged(ingredient.id, it))
              },
              label = "Qty",
              modifier = Modifier.weight(1f),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
          )
          IngredientTextField(
              value = ingredient.unit,
              onValueChange = { onEvent(IngredientEditorUiEvent.OnUnitChanged(ingredient.id, it)) },
              label = "Unit",
              modifier = Modifier.weight(1f),
          )
        }
        IngredientTextField(
            value = ingredient.name,
            onValueChange = { onEvent(IngredientEditorUiEvent.OnNameChanged(ingredient.id, it)) },
            label = "Name",
            modifier = Modifier.fillMaxWidth(),
        )
        IngredientTextField(
            value = ingredient.prepNote,
            onValueChange = {
              onEvent(IngredientEditorUiEvent.OnPrepNoteChanged(ingredient.id, it))
            },
            label = "Prep note",
            modifier = Modifier.fillMaxWidth(),
        )
      }

      IconButton(
          onClick = { onEvent(IngredientEditorUiEvent.OnRemoveIngredient(ingredient.id)) },
      ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Remove ingredient",
            tint = MaterialTheme.colorScheme.error,
        )
      }
    }
  }
}

@Composable
private fun IngredientTextField(
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
      modifier = modifier,
      singleLine = true,
      keyboardOptions = keyboardOptions,
      shape = MaterialTheme.shapes.medium,
  )
}
