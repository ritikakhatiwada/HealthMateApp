package com.example.myhealthmateaapp.viewmodel
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import com.example.myhealthmateaapp.model.User
    import com.example.myhealthmateaapp.repository.UserRepo
    import kotlinx.coroutines.launch

    abstract class kjUserViewModel(
        private val repo: UserRepo = UserViewModel()
    ) : ViewModel(), UserRepo {

        override fun saveUser(
            uid: String,
            name: String,
            email: String,
            onSuccess: () -> Unit,
            onError: (String) -> Unit
        ) {
            viewModelScope.launch {
                try {
                    val user = User(
                        uid = uid,
                        name = name,
                        email = email
                    )
                    repo.addUser(user)
                    onSuccess()
                } catch (e: Exception) {
                    onError(e.localizedMessage ?: "Failed to save user")
                }
            }
        }

        override fun fetchUser(
            uid: String,
            onResult: (User?) -> Unit
        ) {
            viewModelScope.launch {
                onResult(repo.getUser(uid))
            }
        }
    }
