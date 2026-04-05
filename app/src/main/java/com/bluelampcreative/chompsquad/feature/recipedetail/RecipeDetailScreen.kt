package com.bluelampcreative.chompsquad.feature.recipedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.bluelampcreative.chompsquad.domain.model.Ingredient
import com.bluelampcreative.chompsquad.domain.model.Recipe
import com.bluelampcreative.chompsquad.domain.model.RecipeImage
import com.bluelampcreative.chompsquad.domain.model.Step
import com.bluelampcreative.chompsquad.ui.navigation.NavEvent
import com.bluelampcreative.chompsquad.ui.theme.ChompSpacing
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Suppress(
    "ModifierMissing",
    "ComposeModifierMissing",
) // Full-screen navigation destination — no modifier parameter needed
@Composable
fun RecipeDetailScreen(
    recipeId: String,
    onNavEvent: (NavEvent) -> Unit,
    viewModel: RecipeDetailViewModel = koinViewModel { parametersOf(recipeId) },
) {
  val viewState by viewModel.viewState.collectAsStateWithLifecycle()
  val currentOnNavEvent by rememberUpdatedState(onNavEvent)
  var showMoreMenu by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) { viewModel.navEvents.collect { currentOnNavEvent(it) } }

  Box(modifier = Modifier.fillMaxSize()) {
    when {
      viewState.isLoading && viewState.recipe == null -> {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
      }
      viewState.error != null && viewState.recipe == null -> {
        Text(
            text = viewState.error ?: "Something went wrong",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.align(Alignment.Center).padding(ChompSpacing.lg),
        )
      }
      viewState.recipe != null -> {
        RecipeDetailContent(
            recipe = viewState.recipe!!,
            onEvent = { viewModel.handleEvent(it) },
        )
      }
    }

    // Floating back button — always visible so the user can escape from error/loading states too.
    Surface(
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 4.dp,
        modifier =
            Modifier.align(Alignment.TopStart)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(ChompSpacing.sm),
    ) {
      IconButton(onClick = { viewModel.handleEvent(RecipeDetailUiEvent.OnBack) }) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
        )
      }
    }

    // Floating action buttons (top-right) — only rendered once the recipe is available.
    val recipe = viewState.recipe
    if (recipe != null) {
      Row(
          modifier =
              Modifier.align(Alignment.TopEnd)
                  .windowInsetsPadding(WindowInsets.statusBars)
                  .padding(ChompSpacing.sm),
          horizontalArrangement = Arrangement.spacedBy(ChompSpacing.xs),
      ) {
        Surface(shape = CircleShape, color = Color.White, shadowElevation = 4.dp) {
          IconButton(onClick = { /* Favorite toggle — implemented in task D4 */ }) {
            Icon(
                imageVector =
                    if (recipe.isFavorited) Icons.Default.Favorite
                    else Icons.Default.FavoriteBorder,
                contentDescription = if (recipe.isFavorited) "Unfavorite" else "Favorite",
                tint =
                    if (recipe.isFavorited) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface,
            )
          }
        }
        Surface(shape = CircleShape, color = Color.White, shadowElevation = 4.dp) {
          IconButton(onClick = { /* Add to Cookbook — implemented in task D3 */ }) {
            Icon(imageVector = Icons.Default.Bookmark, contentDescription = "Add to Cookbook")
          }
        }
        Surface(shape = CircleShape, color = Color.White, shadowElevation = 4.dp) {
          Box {
            IconButton(onClick = { showMoreMenu = true }) {
              Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More options")
            }
            DropdownMenu(
                expanded = showMoreMenu,
                onDismissRequest = { showMoreMenu = false },
            ) {
              // Placeholder — edit / delete / share actions added in later tasks.
            }
          }
        }
      }
    }
  }
}

@Composable
private fun RecipeDetailContent(
    recipe: Recipe,
    onEvent: (RecipeDetailUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
  LazyColumn(modifier = modifier.fillMaxSize()) {
    // ── Hero image pager (edge-to-edge, no horizontal padding) ────────────────
    item {
      ImagePagerSection(
          images = recipe.images,
          onImageRefresh = { imageId, blobPath ->
            onEvent(RecipeDetailUiEvent.OnImageRefreshNeeded(imageId, blobPath))
          },
      )
    }

    // ── Text content ──────────────────────────────────────────────────────────
    item {
      Column(
          modifier = Modifier.padding(horizontal = ChompSpacing.md),
          verticalArrangement = Arrangement.spacedBy(ChompSpacing.md),
      ) {
        Spacer(modifier = Modifier.height(ChompSpacing.sm))

        Text(text = recipe.title, style = MaterialTheme.typography.headlineLarge)

        if (!recipe.source.isNullOrBlank()) {
          AttributionRow(source = recipe.source)
        }

        if (recipe.cookTime != null || recipe.totalTime != null || recipe.yieldAmount != null) {
          StatsRow(recipe = recipe)
        }

        if (recipe.tags.isNotEmpty()) {
          TagsSection(tags = recipe.tags)
        }

        if (recipe.ingredients.isNotEmpty()) {
          IngredientsSection(ingredients = recipe.ingredients)
        }

        if (recipe.steps.isNotEmpty()) {
          StepsSection(steps = recipe.steps)
        }

        // Bottom padding for navigation bar inset.
        Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars))
      }
    }
  }
}

@Suppress("UnstableCollections")
@Composable
private fun ImagePagerSection(
    images: List<RecipeImage>,
    onImageRefresh: (imageId: String, blobPath: String) -> Unit,
    modifier: Modifier = Modifier,
) {
  val pageCount = images.size.coerceAtLeast(1)
  val pagerState = rememberPagerState(pageCount = { pageCount })

  Box(modifier = modifier.fillMaxWidth().aspectRatio(4f / 3f)) {
    if (images.isEmpty()) {
      val backgroundColor = MaterialTheme.colorScheme.primaryContainer
      Box(
          modifier = Modifier.fillMaxSize().background(backgroundColor),
          contentAlignment = Alignment.Center,
      ) {
        Icon(
            imageVector = Icons.Default.Restaurant,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(64.dp),
        )
      }
    } else {
      HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
        val image = images[page]
        val imagePlaceholder = ColorPainter(MaterialTheme.colorScheme.primaryContainer)
        AsyncImage(
            model = image.url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = imagePlaceholder,
            onError = {
              if (image.blobPath.isNotBlank()) {
                onImageRefresh(image.id, image.blobPath)
              }
            },
            modifier = Modifier.fillMaxSize(),
        )
      }
      if (images.size > 1) {
        PagerIndicator(
            pageCount = images.size,
            currentPage = pagerState.currentPage,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = ChompSpacing.sm),
        )
      }
    }
  }
}

@Composable
private fun PagerIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
  Row(
      modifier = modifier,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    repeat(pageCount) { index ->
      Box(
          modifier =
              Modifier.size(if (index == currentPage) 8.dp else 6.dp)
                  .clip(CircleShape)
                  .background(
                      if (index == currentPage) Color.White else Color.White.copy(alpha = 0.5f)
                  ),
      )
    }
  }
}

@Composable
private fun AttributionRow(
    source: String,
    modifier: Modifier = Modifier,
) {
  Row(
      modifier = modifier,
      horizontalArrangement = Arrangement.spacedBy(ChompSpacing.xs),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
        imageVector = Icons.Default.Link,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(16.dp),
    )
    Text(
        text = source,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun StatsRow(
    recipe: Recipe,
    modifier: Modifier = Modifier,
) {
  Row(
      modifier = modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(ChompSpacing.sm),
  ) {
    recipe.cookTime?.let { minutes ->
      StatCard(
          icon = Icons.Default.Timer,
          label = "Cook",
          value = "$minutes min",
          modifier = Modifier.weight(1f),
      )
    }
    recipe.totalTime?.let { minutes ->
      StatCard(
          icon = Icons.Default.Schedule,
          label = "Total",
          value = "$minutes min",
          modifier = Modifier.weight(1f),
      )
    }
    val yieldText =
        listOfNotNull(recipe.yieldAmount, recipe.yieldUnit).joinToString(" ").takeIf {
          it.isNotBlank()
        }
    if (yieldText != null) {
      StatCard(
          icon = Icons.Default.People,
          label = "Serves",
          value = yieldText,
          modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
  ElevatedCard(modifier = modifier) {
    Column(
        modifier = Modifier.padding(ChompSpacing.sm).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ChompSpacing.xs),
    ) {
      Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp),
      )
      Text(text = value, style = MaterialTheme.typography.labelLarge)
      Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Suppress("UnstableCollections")
@Composable
private fun TagsSection(
    tags: List<String>,
    modifier: Modifier = Modifier,
) {
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(ChompSpacing.xs)) {
    Text(text = "Tags", style = MaterialTheme.typography.titleMedium)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(ChompSpacing.xs)) {
      tags.forEach { tag ->
        SuggestionChip(
            onClick = {},
            label = { Text(tag) },
            colors =
                SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
        )
      }
    }
  }
}

@Suppress("UnstableCollections")
@Composable
private fun IngredientsSection(
    ingredients: List<Ingredient>,
    modifier: Modifier = Modifier,
) {
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(ChompSpacing.xs)) {
    Text(text = "Ingredients", style = MaterialTheme.typography.titleMedium)
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
      ingredients.forEachIndexed { index, ingredient ->
        Row(
            modifier = Modifier.padding(horizontal = ChompSpacing.md, vertical = ChompSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(ChompSpacing.sm),
            verticalAlignment = Alignment.Top,
        ) {
          Text(
              text = "•",
              color = MaterialTheme.colorScheme.primary,
              style = MaterialTheme.typography.bodyLarge,
          )
          Column(modifier = Modifier.weight(1f)) {
            Text(
                text = ingredient.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            )
            if (!ingredient.prepNote.isNullOrBlank()) {
              Text(
                  text = ingredient.prepNote,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
          val amount =
              listOfNotNull(ingredient.quantity, ingredient.unit).joinToString(" ").takeIf {
                it.isNotBlank()
              }
          if (amount != null) {
            Text(
                text = amount,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
        if (index < ingredients.lastIndex) {
          HorizontalDivider(modifier = Modifier.padding(horizontal = ChompSpacing.md))
        }
      }
    }
  }
}

@Suppress("UnstableCollections")
@Composable
private fun StepsSection(
    steps: List<Step>,
    modifier: Modifier = Modifier,
) {
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(ChompSpacing.xs)) {
    Text(text = "Instructions", style = MaterialTheme.typography.titleMedium)
    steps.forEach { step ->
      Row(
          horizontalArrangement = Arrangement.spacedBy(ChompSpacing.sm),
          verticalAlignment = Alignment.Top,
      ) {
        Text(
            text = "${step.position}.",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 2.dp),
        )
        Text(
            text = step.instruction,
            style = MaterialTheme.typography.bodyMedium,
        )
      }
    }
  }
}
