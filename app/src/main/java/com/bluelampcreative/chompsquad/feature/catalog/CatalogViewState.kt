package com.bluelampcreative.chompsquad.feature.catalog

import com.bluelampcreative.chompsquad.core.ViewAction
import com.bluelampcreative.chompsquad.domain.model.RecipeListItem

data class CatalogViewState(
    /** True while the initial server sync is in-flight and Room has no cached data yet. */
    val isLoading: Boolean = true,
    val recipes: List<RecipeListItem> = emptyList(),
    /** All tags from the unfiltered local recipe cache — drives the FilterChip row. */
    val availableTags: List<String> = emptyList(),
    val searchQuery: String = "",
    val selectedTag: String? = null,
    val isGridView: Boolean = false,
    /** True while the SearchBar is expanded (user actively searching). */
    val isSearchExpanded: Boolean = false,
    /** Non-null when a background sync fails; shown as a transient snackbar. */
    val syncError: String? = null,
)

sealed interface CatalogAction : ViewAction {
  data class RecipesUpdated(val recipes: List<RecipeListItem>) : CatalogAction

  data class TagsUpdated(val tags: List<String>) : CatalogAction

  data class SearchQueryChanged(val query: String) : CatalogAction

  data class TagSelected(val tag: String?) : CatalogAction

  data object ViewToggled : CatalogAction

  data class SearchExpandedChanged(val expanded: Boolean) : CatalogAction

  data object SyncStarted : CatalogAction

  data object SyncCompleted : CatalogAction

  data class SyncFailed(val message: String) : CatalogAction
}

sealed interface CatalogUiEvent {
  data class OnSearchQueryChanged(val query: String) : CatalogUiEvent

  data class OnTagSelected(val tag: String?) : CatalogUiEvent

  data object OnToggleView : CatalogUiEvent

  data class OnSearchExpandedChange(val expanded: Boolean) : CatalogUiEvent

  data class OnRecipeTapped(val id: String) : CatalogUiEvent

  data object OnRefresh : CatalogUiEvent

  data class OnImageRefreshNeeded(val imageId: String, val blobPath: String) : CatalogUiEvent
}
