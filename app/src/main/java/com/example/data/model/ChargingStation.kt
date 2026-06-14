package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "charging_stations")
data class ChargingStation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val operator: String,
    val city: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val connectorTypes: String, // Comma-separated list (e.g. "CCS2 (120kW DC), Type 2 (22kW AC)")
    val pricePerKwh: Double,    // Price in PKR
    val status: String,         // "Available", "Occupied", "Out of Service"
    val isFavorite: Boolean = false,
    val isFastCharging: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "charge_logs")
data class ChargeLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val stationId: Int,
    val stationName: String,
    val connectorUsed: String,
    val energyLoaded: Double,  // in kWh
    val totalCost: Double,     // in PKR
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
