package org.nexlibre.orbita

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import org.nexlibre.orbita.core.LauncherState

/** HOME and ordinary launcher entry can use distinct tasks; both share one process-level kernel. */
class OrbitaApplication : Application(), ViewModelStoreOwner {
    override val viewModelStore = ViewModelStore()
    val launcherState: LauncherState by lazy {
        ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this))[LauncherState::class.java]
    }
}
