package com.lifeos.app.ui.common

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.lifeos.app.LifeOSApplication

/**
 * Single generic factory used by every screen ViewModel — avoids pulling in
 * Hilt/Dagger for an app with a handful of ViewModels.
 */
class AppViewModelFactory(private val app: LifeOSApplication) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return modelClass.getConstructor(LifeOSApplication::class.java).newInstance(app) as T
    }
}

fun lifeOSApp(application: Application): LifeOSApplication = application as LifeOSApplication
