package com.techquantum.promptvault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PromptViewModelFactory(
    private val repository: PromptRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PromptViewModel::class.java)) {
            return PromptViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
