package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.UserPreferencesDataStore

class LoginViewModelFactory(
    private val prefs: UserPreferencesDataStore   // 1. Guardamos el DataStore aquí
) : ViewModelProvider.Factory {

    // 2. Sobreescribimos create(): Android llama aquí cuando necesita el ViewModel
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        // 3. Si el ViewModel que quiere crear es LoginViewModel...
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {

            // 4. Lo construimos pasando prefs al constructor
            return LoginViewModel(prefs) as T
        }

        // 5. Si pide otro ViewModel, error
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
