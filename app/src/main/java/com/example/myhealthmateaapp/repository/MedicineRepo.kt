package com.example.myhealthmateaapp.repository

import com.example.myhealthmateaapp.model.Medicine

interface MedicineRepo {
        suspend fun addMedicine(medicine: Medicine)

        suspend fun getAllMedicines(): List<Medicine>
    }
