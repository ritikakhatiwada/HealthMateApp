package com.example.myhealthmateaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhealthmateaapp.model.User
import com.example.myhealthmateaapp.repository.UserRepo
import com.example.myhealthmateaapp.repository.UserRepoImpl
import kotlinx.coroutines.launch

class UserViewModel(
    private val repo: UserRepo = UserRepoImpl()
) : ViewModel() {

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repo.login(email, password)
            if (result.isSuccess) onSuccess()
            else onError(result.exceptionOrNull()?.localizedMessage ?: "Login failed")
        }
    }

    fun signup(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repo.signup(email, password)
            if (result.isSuccess) onSuccess()
            else onError(result.exceptionOrNull()?.localizedMessage ?: "Signup failed")
        }
    }

    fun resetPassword(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repo.resetPassword(email)
            if (result.isSuccess) onSuccess()
            else onError(result.exceptionOrNull()?.localizedMessage ?: "Password reset failed")
        }
    }

    fun saveUser(
        uid: String,
        name: String,
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        repo.saveUser(uid, name, email, onSuccess, onError)
    }

    fun fetchUser(
        uid: String,
        onResult: (User?) -> Unit
    ) {
        repo.fetchUser(uid, onResult)
    }
}
