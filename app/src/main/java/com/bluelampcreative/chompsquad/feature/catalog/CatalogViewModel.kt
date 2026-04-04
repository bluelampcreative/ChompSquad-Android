package com.bluelampcreative.chompsquad.feature.catalog

import androidx.lifecycle.viewModelScope
import com.bluelampcreative.chompsquad.core.CoreViewModel
import com.bluelampcreative.chompsquad.data.remote.RecipeRepository
import com.bluelampcreative.chompsquad.ui.navigation.NavEvent
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

private const val SEARCH_DEBOUNCE_MS = 300L
private const val LIST_PAGE_SIZE = 100

@OptIn(FlowPreview::class)
@KoinViewModel
class CatalogViewModel(
    private val recipeRepository: RecipeRepository,
) : CoreViewModel<CatalogViewState, CatalogAction, CatalogUiEvent>(CatalogViewState()) {

  // Separate flows drive the Room subscription so that search input is debounced while tag
  // changes take effect immediately.
  private val searchFlow = MutableStateFlow("")
  private val tagFlow = MutableStateFlow<String?>(null)

  init {
    // Unfiltered observation for the tag chip row — always reflects the full local library.
    viewModelScope.launch {
      recipeRepository
          .observeRecipes(tag = null, search = null)
          .map { recipes -> recipes.flatMap { it.tags }.distinct().sorted() }
          .distinctUntilChanged()
          .collect { tags -> state.dispatch(CatalogAction.TagsUpdated(tags)) }
    }

    // Filtered observation that updates whenever the search query (debounced) or tag changes.
    viewModelScope.launch {
      combine(searchFlow.debounce(SEARCH_DEBOUNCE_MS), tagFlow) { search, tag -> search to tag }
          .flatMapLatest { (search, tag) ->
            recipeRepository.observeRecipes(
                tag = tag,
                search = search.takeIf { it.isNotBlank() },
            )
          }
          .collect { recipes -> state.dispatch(CatalogAction.RecipesUpdated(recipes)) }
    }

    // Background sync — populates Room from the server on first open.
    viewModelScope.launch { sync() }
  }

  override fun reducer(
      state: CatalogViewState,
      action: CatalogAction,
  ): CatalogViewState =
      when (action) {
        is CatalogAction.RecipesUpdated -> state.copy(recipes = action.recipes, isLoading = false)
        is CatalogAction.TagsUpdated -> state.copy(availableTags = action.tags)
        is CatalogAction.SearchQueryChanged -> state.copy(searchQuery = action.query)
        is CatalogAction.TagSelected -> state.copy(selectedTag = action.tag)
        CatalogAction.ViewToggled -> state.copy(isGridView = !state.isGridView)
        CatalogAction.SyncStarted -> state.copy(syncError = null, isLoading = true)
        CatalogAction.SyncCompleted -> state.copy(isLoading = false)
        is CatalogAction.SyncFailed -> state.copy(syncError = action.message, isLoading = false)
      }

  override fun handleEvent(event: CatalogUiEvent) {
    when (event) {
      is CatalogUiEvent.OnSearchQueryChanged -> {
        state.dispatch(CatalogAction.SearchQueryChanged(event.query))
        searchFlow.value = event.query
      }
      is CatalogUiEvent.OnTagSelected -> {
        // Selecting a tag clears the search so both filters don't compete.
        if (event.tag != null) {
          state.dispatch(CatalogAction.SearchQueryChanged(""))
          searchFlow.value = ""
        }
        state.dispatch(CatalogAction.TagSelected(event.tag))
        tagFlow.value = event.tag
      }
      CatalogUiEvent.OnToggleView -> state.dispatch(CatalogAction.ViewToggled)
      is CatalogUiEvent.OnRecipeTapped -> navigate(NavEvent.NavigateToRecipeDetail(event.id))
      CatalogUiEvent.OnRefresh -> viewModelScope.launch { sync() }
      is CatalogUiEvent.OnImageRefreshNeeded ->
          viewModelScope.launch {
            if (event.blobPath.isNotBlank()) {
              recipeRepository.refreshHeroImageUrl(event.imageId, event.blobPath)
            }
          }
    }
  }

  private suspend fun sync() {
    state.dispatch(CatalogAction.SyncStarted)
    recipeRepository
        .syncRecipes(
            pageSize = LIST_PAGE_SIZE,
            tag = tagFlow.value,
            search = searchFlow.value.takeIf { it.isNotBlank() },
        )
        .fold(
            onSuccess = { state.dispatch(CatalogAction.SyncCompleted) },
            onFailure = { error ->
              state.dispatch(CatalogAction.SyncFailed(error.message ?: "Sync failed"))
            },
        )
  }
}
