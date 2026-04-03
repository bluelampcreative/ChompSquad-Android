package com.bluelampcreative.chompsquad.data.scanner

import android.net.Uri
import com.bluelampcreative.chompsquad.domain.model.Ingredient
import com.bluelampcreative.chompsquad.domain.model.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Singleton

/**
 * In-memory store that bridges the Camera capture screen (which sets the images to scan) and the
 * Scan Submission screen (which sets the resulting [Recipe] after the upload succeeds).
 *
 * [ingredientEdits] carries the ingredient list for the ingredient editor (task 2.5). Null means
 * the editor has never been opened, or was dismissed via [clearIngredientEdits]. A non-null value
 * means either the editor is open with the current list, or the user confirmed edits and the result
 * is ready to be reflected. [ScanResultViewModel] collects non-null emissions to keep the review
 * screen in sync.
 *
 * Scoped as a singleton so all screens in the scan flow share the same instance without persisting
 * anything to disk. Call [clear] when the scan flow is fully complete.
 */
interface ScanSessionRepository {
  val ingredientEdits: StateFlow<List<Ingredient>?>

  fun setPendingImages(uris: List<Uri>)

  fun getPendingImages(): List<Uri>

  fun setScanResult(recipe: Recipe)

  fun getScanResult(): Recipe?

  fun setIngredientEdits(ingredients: List<Ingredient>)

  /** Resets [ingredientEdits] to null. Call when the editor is dismissed without saving. */
  fun clearIngredientEdits()

  fun clear()
}

@Singleton(binds = [ScanSessionRepository::class])
class DefaultScanSessionRepository : ScanSessionRepository {
  @Volatile private var pendingImages: List<Uri> = emptyList()
  @Volatile private var scanResult: Recipe? = null

  private val _ingredientEdits = MutableStateFlow<List<Ingredient>?>(null)
  override val ingredientEdits: StateFlow<List<Ingredient>?> = _ingredientEdits

  override fun setPendingImages(uris: List<Uri>) {
    pendingImages = uris.toList()
  }

  override fun getPendingImages(): List<Uri> = pendingImages.toList()

  override fun setScanResult(recipe: Recipe) {
    scanResult = recipe
  }

  override fun getScanResult(): Recipe? = scanResult

  override fun setIngredientEdits(ingredients: List<Ingredient>) {
    _ingredientEdits.value = ingredients.toList()
  }

  override fun clearIngredientEdits() {
    _ingredientEdits.value = null
  }

  override fun clear() {
    pendingImages = emptyList()
    scanResult = null
    _ingredientEdits.value = null
  }
}
