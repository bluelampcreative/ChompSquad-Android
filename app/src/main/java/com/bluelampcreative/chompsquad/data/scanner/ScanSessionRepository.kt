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
 * [ingredientEdits] carries the working ingredient list while the ingredient editor (task 2.5) is
 * open. Null means no edit is in progress. [ScanResultViewModel] collects non-null emissions to
 * reflect changes back on the review screen.
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

  override fun clear() {
    pendingImages = emptyList()
    scanResult = null
    _ingredientEdits.value = null
  }
}
