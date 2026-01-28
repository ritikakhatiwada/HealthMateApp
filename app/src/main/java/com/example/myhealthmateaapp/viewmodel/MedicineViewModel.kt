package com.example.myhealthmateaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myhealthmateaapp.model.Medicine
import com.example.myhealthmateaapp.repository.MedicineRepo

class MedicineViewModel(
    private val repository: MedicineRepo
) : ViewModel() {

    suspend fun addMedicine(medicine: Medicine) {
        repository.addMedicine(medicine)
    }

    suspend fun getAllMedicines(): List<Medicine> {
        return repository.getAllMedicines()
    }

    class Factory(private val repository: MedicineRepo) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MedicineViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MedicineViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
