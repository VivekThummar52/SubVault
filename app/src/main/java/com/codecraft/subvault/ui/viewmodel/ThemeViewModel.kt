package com.codecraft.subvault.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codecraft.subvault.domain.repository.PreferenceRepository
import com.codecraft.subvault.ui.theme.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    val theme: StateFlow<AppTheme> = preferenceRepository.getUserPreferences()
        .map { it.appTheme }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                AppTheme.SYSTEM
            } else {
                AppTheme.LIGHT
            }
        )

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            preferenceRepository.saveAppTheme(theme)
        }
    }
}
