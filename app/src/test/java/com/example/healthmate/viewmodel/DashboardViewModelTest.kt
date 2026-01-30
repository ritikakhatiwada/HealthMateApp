package com.example.healthmate.viewmodel

import com.example.healthmate.domain.repository.UserRepository
import com.example.healthmate.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun load_user_data_success_test() = runTest {
        val repo = mock<UserRepository>()
        val viewModel = DashboardViewModel(repo)
        val mockUser = User(id = "123", name = "Test User", email = "test@test.com")

        whenever(repo.getCurrentUserId()).thenReturn("123")
        whenever(repo.getUserById("123")).thenReturn(mockUser)

        viewModel.loadUserData()
        
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.userState.value
        assertTrue(state is DashboardViewModel.DashboardState.Success)
        assertTrue((state as DashboardViewModel.DashboardState.Success).user.name == "Test User")
    }
}
