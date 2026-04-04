package com.bluelampcreative.chompsquad.feature.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.bluelampcreative.chompsquad.domain.model.RecipeListItem
import com.bluelampcreative.chompsquad.ui.navigation.NavEvent
import com.bluelampcreative.chompsquad.ui.theme.ChompSpacing
import org.koin.androidx.compose.koinViewModel

@Suppress(
    "ModifierMissing",
    "ComposeModifierMissing",
) // Root tab composable — no modifier parameter needed
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onNavEvent: (NavEvent) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CatalogViewModel = koinViewModel(),
) {
  val viewState by viewModel.viewState.collectAsStateWithLifecycle()
  val currentOnNavEvent by rememberUpdatedState(onNavEvent)
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(Unit) { viewModel.navEvents.collect { currentOnNavEvent(it) } }

  val syncError = viewState.syncError
  LaunchedEffect(syncError) { if (syncError != null) snackbarHostState.showSnackbar(syncError) }

  Scaffold(
      modifier = modifier,
      snackbarHost = { SnackbarHost(snackbarHostState) },
  ) { innerPadding ->
    CatalogContent(
        viewState = viewState,
        onEvent = { viewModel.handleEvent(it) },
        modifier = Modifier.padding(innerPadding),
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CatalogContent(
    viewState: CatalogViewState,
    onEvent: (CatalogUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxSize()) {
    SearchBar(
        modifier = Modifier.fillMaxWidth(),
        inputField = {
          SearchBarDefaults.InputField(
              query = viewState.searchQuery,
              onQueryChange = { onEvent(CatalogUiEvent.OnSearchQueryChanged(it)) },
              onSearch = { onEvent(CatalogUiEvent.OnSearchExpandedChange(false)) },
              expanded = viewState.isSearchExpanded,
              onExpandedChange = { onEvent(CatalogUiEvent.OnSearchExpandedChange(it)) },
              placeholder = { Text("Search recipes…") },
              leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
              trailingIcon = {
                Row {
                  if (viewState.searchQuery.isNotBlank()) {
                    IconButton(onClick = { onEvent(CatalogUiEvent.OnSearchQueryChanged("")) }) {
                      Icon(Icons.Default.Close, contentDescription = "Clear search")
                    }
                  }
                  IconButton(onClick = { onEvent(CatalogUiEvent.OnToggleView) }) {
                    Icon(
                        imageVector =
                            if (viewState.isGridView) Icons.Default.ViewList
                            else Icons.Default.GridView,
                        contentDescription =
                            if (viewState.isGridView) "Switch to list view"
                            else "Switch to grid view",
                    )
                  }
                }
              },
          )
        },
        expanded = viewState.isSearchExpanded,
        onExpandedChange = { onEvent(CatalogUiEvent.OnSearchExpandedChange(it)) },
    ) {
      // Content shown when the user is actively searching.
      CatalogListSection(viewState = viewState, onEvent = onEvent, modifier = Modifier.weight(1f))
    }

    // Content shown when search is not expanded (normal browse state).
    if (!viewState.isSearchExpanded) {
      CatalogListSection(viewState = viewState, onEvent = onEvent, modifier = Modifier.weight(1f))
    }
  }
}

@Composable
private fun CatalogListSection(
    viewState: CatalogViewState,
    onEvent: (CatalogUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) {
    // ── Tag filter chips ──────────────────────────────────────────────────────
    if (viewState.availableTags.isNotEmpty()) {
      FilterChipRow(
          tags = viewState.availableTags,
          selectedTag = viewState.selectedTag,
          onTagSelect = { onEvent(CatalogUiEvent.OnTagSelected(it)) },
      )
    }

    // ── Recipe list / grid ────────────────────────────────────────────────────
    when {
      viewState.isLoading && viewState.recipes.isEmpty() -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      }
      viewState.recipes.isEmpty() -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text(
              text = "No recipes yet — scan or enter one to get started",
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(ChompSpacing.lg),
          )
        }
      }
      viewState.isGridView -> {
        PullToRefreshBox(
            isRefreshing = viewState.isLoading,
            onRefresh = { onEvent(CatalogUiEvent.OnRefresh) },
            modifier = Modifier.fillMaxSize(),
        ) {
          LazyVerticalGrid(
              columns = GridCells.Adaptive(minSize = 160.dp),
              contentPadding = PaddingValues(ChompSpacing.md),
              horizontalArrangement = Arrangement.spacedBy(ChompSpacing.sm),
              verticalArrangement = Arrangement.spacedBy(ChompSpacing.sm),
              modifier = Modifier.fillMaxSize(),
          ) {
            items(viewState.recipes, key = { it.id }) { recipe ->
              RecipeCard(
                  recipe = recipe,
                  isGridView = true,
                  onClick = { onEvent(CatalogUiEvent.OnRecipeTapped(recipe.id)) },
                  onImageLoadFail = {
                    if (
                        !recipe.heroImageId.isNullOrBlank() && !recipe.heroBlobPath.isNullOrBlank()
                    ) {
                      onEvent(
                          CatalogUiEvent.OnImageRefreshNeeded(
                              recipe.heroImageId,
                              recipe.heroBlobPath,
                          )
                      )
                    }
                  },
              )
            }
          }
        }
      }
      else -> {
        PullToRefreshBox(
            isRefreshing = viewState.isLoading,
            onRefresh = { onEvent(CatalogUiEvent.OnRefresh) },
            modifier = Modifier.fillMaxSize(),
        ) {
          LazyColumn(
              contentPadding = PaddingValues(ChompSpacing.md),
              verticalArrangement = Arrangement.spacedBy(ChompSpacing.sm),
              modifier = Modifier.fillMaxSize(),
          ) {
            items(viewState.recipes, key = { it.id }) { recipe ->
              RecipeCard(
                  recipe = recipe,
                  isGridView = false,
                  onClick = { onEvent(CatalogUiEvent.OnRecipeTapped(recipe.id)) },
                  onImageLoadFail = {
                    if (
                        !recipe.heroImageId.isNullOrBlank() && !recipe.heroBlobPath.isNullOrBlank()
                    ) {
                      onEvent(
                          CatalogUiEvent.OnImageRefreshNeeded(
                              recipe.heroImageId,
                              recipe.heroBlobPath,
                          )
                      )
                    }
                  },
              )
            }
          }
        }
      }
    }
  }
}

@Suppress("UnstableCollections")
@Composable
private fun FilterChipRow(
    tags: List<String>,
    selectedTag: String?,
    onTagSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
  LazyRow(
      modifier = modifier.fillMaxWidth(),
      contentPadding = PaddingValues(horizontal = ChompSpacing.md),
      horizontalArrangement = Arrangement.spacedBy(ChompSpacing.xs),
  ) {
    item {
      FilterChip(
          selected = selectedTag == null,
          onClick = { onTagSelect(null) },
          label = { Text("All") },
          colors =
              FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                  selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
              ),
      )
    }
    items(tags) { tag ->
      FilterChip(
          selected = selectedTag == tag,
          onClick = { onTagSelect(if (selectedTag == tag) null else tag) },
          label = { Text(tag) },
          colors =
              FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                  selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
              ),
      )
    }
  }
}

@Composable
private fun RecipeCard(
    recipe: RecipeListItem,
    isGridView: Boolean,
    onClick: () -> Unit,
    onImageLoadFail: () -> Unit,
    modifier: Modifier = Modifier,
) {
  if (isGridView) {
    ElevatedCard(modifier = modifier.fillMaxWidth().clickable(onClick = onClick)) {
      AsyncImage(
          model = recipe.heroImageUrl,
          contentDescription = recipe.title,
          contentScale = ContentScale.Crop,
          onError = { onImageLoadFail() },
          modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(MaterialTheme.shapes.medium),
      )
      Column(modifier = Modifier.padding(ChompSpacing.sm)) {
        Text(
            text = recipe.title,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        recipe.totalTime?.let { minutes ->
          Text(
              text = "$minutes min",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  } else {
    ElevatedCard(modifier = modifier.fillMaxWidth().clickable(onClick = onClick)) {
      Row(
          modifier = Modifier.padding(ChompSpacing.sm),
          horizontalArrangement = Arrangement.spacedBy(ChompSpacing.sm),
          verticalAlignment = Alignment.CenterVertically,
      ) {
        AsyncImage(
            model = recipe.heroImageUrl,
            contentDescription = recipe.title,
            contentScale = ContentScale.Crop,
            onError = { onImageLoadFail() },
            modifier = Modifier.size(72.dp).clip(MaterialTheme.shapes.medium),
        )
        Column(modifier = Modifier.weight(1f)) {
          Text(
              text = recipe.title,
              style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
          )
          recipe.totalTime?.let { minutes ->
            Text(
                text = "$minutes min",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          if (recipe.tags.isNotEmpty()) {
            Text(
                text = recipe.tags.take(3).joinToString(" · "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }
    }
  }
}
