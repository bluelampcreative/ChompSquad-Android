package com.bluelampcreative.chompsquad

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.bluelampcreative.chompsquad.di.DataModule
import com.bluelampcreative.chompsquad.di.NetworkModule
import com.bluelampcreative.chompsquad.di.PurchasesModule
import com.bluelampcreative.chompsquad.di.ScannerModule
import com.bluelampcreative.chompsquad.di.ViewModelModule
import io.ktor.client.HttpClient
import okio.Path.Companion.toOkioPath
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.annotation.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.logger.Level
import org.koin.core.qualifier.named
import org.koin.plugin.module.dsl.startKoin

@KoinApplication(
    modules =
        [
            ViewModelModule::class,
            DataModule::class,
            NetworkModule::class,
            PurchasesModule::class,
            ScannerModule::class,
        ]
)
private object KoinConfig

class ChompSquadApplication : Application(), SingletonImageLoader.Factory, KoinComponent {

  // Lazy — resolved after startKoin() completes in onCreate().
  private val imageHttpClient: HttpClient by inject(qualifier = named("coil"))

  override fun newImageLoader(context: Context): ImageLoader =
      ImageLoader.Builder(context)
          .components { add(KtorNetworkFetcherFactory(imageHttpClient)) }
          .crossfade(true)
          .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve("image_cache").toOkioPath())
                .maxSizeBytes(IMAGE_DISK_CACHE_BYTES)
                .build()
          }
          .logger(if (BuildConfig.DEBUG) DebugLogger() else null)
          .build()

  override fun onCreate() {
    super.onCreate()
    startKoin<KoinConfig> {
      androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)
      androidContext(this@ChompSquadApplication)
    }
  }
}

private const val IMAGE_DISK_CACHE_BYTES = 100L * 1024 * 1024 // 100 MB
