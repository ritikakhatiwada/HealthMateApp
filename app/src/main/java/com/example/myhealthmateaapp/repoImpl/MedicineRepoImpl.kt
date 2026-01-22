package com.example.myhealthmateaapp.repoImpl

import com.example.myhealthmateaapp.model.Medicine
import com.example.myhealthmateaapp.repository.MedicineRepo

class MedicineRepoImpl : MedicineRepo {

        // Temporary in-memory list (later replace with Room / Firebase)
        private val medicineList = mutableListOf<Medicine>()

        override suspend fun addMedicine(medicine: Medicine) {
            medicineList.add(medicine)
        }

        override suspend fun getAllMedicines(): List<Medicine> {
            return medicineList
        }
}