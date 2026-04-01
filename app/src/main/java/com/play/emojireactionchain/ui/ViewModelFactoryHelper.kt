package com.play.emojireactionchain.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

fun <VM : ViewModel> gameViewModelFactory(creator: () -> VM): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val viewModel = creator()
            if (modelClass.isAssignableFrom(viewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return viewModel as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

