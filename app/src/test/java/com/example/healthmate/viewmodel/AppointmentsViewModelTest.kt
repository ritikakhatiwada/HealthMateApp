package com.example.healthmate.viewmodel

import com.example.healthmate.domain.repository.AppointmentRepository
import com.example.healthmate.domain.repository.AuthRepository
import com.example.healthmate.model.Appointment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AppointmentsViewModelTest {

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
    fun load_appointments_success_test() = runTest {
        val apptRepo = mock<AppointmentRepository>()
        val authRepo = mock<AuthRepository>()
        val viewModel = AppointmentsViewModel(apptRepo, authRepo)
        
        val mockAppointments = listOf(
            Appointment(id = "1", patientName = "Test", doctorName = "Dr. Smith", date = "2025-10-10")
        )

        whenever(authRepo.getCurrentUserId()).thenReturn("user123")
        whenever(apptRepo.getUserAppointments("user123")).thenReturn(mockAppointments)

        viewModel.loadAppointments()
        
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.appointmentsState.value
        assertTrue(state is AppointmentsViewModel.AppointmentsState.Success)
        assertEquals(1, (state as AppointmentsViewModel.AppointmentsState.Success).appointments.size)
        assertEquals("Dr. Smith", state.appointments[0].doctorName)
    }
}
