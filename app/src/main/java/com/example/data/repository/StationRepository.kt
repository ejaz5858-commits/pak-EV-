package com.example.data.repository

import com.example.data.db.StationDao
import com.example.data.model.ChargeLog
import com.example.data.model.ChargingStation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlin.random.Random

class StationRepository(private val stationDao: StationDao) {

    val allStations: Flow<List<ChargingStation>> = stationDao.getAllStations()
    val allLogs: Flow<List<ChargeLog>> = stationDao.getAllLogs()

    fun getStationsByCity(city: String): Flow<List<ChargingStation>> {
        return if (city == "All" || city.isEmpty()) {
            allStations
        } else {
            stationDao.getStationsByCity(city)
        }
    }

    fun getStationById(id: Int): Flow<ChargingStation?> = stationDao.getStationByIdFlow(id)

    suspend fun insertStation(station: ChargingStation) = stationDao.insertStation(station)

    suspend fun toggleFavorite(id: Int, isFav: Boolean) = stationDao.updateFavorite(id, isFav)

    suspend fun addChargeLog(log: ChargeLog) = stationDao.insertLog(log)

    suspend fun clearAllLogs() = stationDao.clearLogs()

    /**
     * Pre-populates the database with realistic Pakistan EV Charging Stations if it's currently empty.
     */
    suspend fun ensureDatabasePopulated() {
        val currentList = allStations.first()
        if (currentList.isEmpty()) {
            val initialStations = listOf(
                // Lahore
                ChargingStation(
                    name = "Shell Recharge - DHA Phase 5",
                    operator = "Shell Pakistan",
                    city = "Lahore",
                    address = "DHA Phase 5 Block CCA, near Bedian Road, Lahore",
                    latitude = 31.4705,
                    longitude = 74.4093,
                    connectorTypes = "CCS2 (120 kW DC Fast), Type 2 (22 kW AC)",
                    pricePerKwh = 88.0,
                    status = "Available",
                    isFastCharging = true
                ),
                ChargingStation(
                    name = "Dewan Motors BMW - Emporium Mall",
                    operator = "Dewan Motors",
                    city = "Lahore",
                    address = "Emporium Mall Parking Basement, Johar Town, Lahore",
                    latitude = 31.4682,
                    longitude = 74.2568,
                    connectorTypes = "CCS2 (50 kW DC Fast), Type 2 (22 kW AC)",
                    pricePerKwh = 95.0,
                    status = "Occupied",
                    isFastCharging = true
                ),
                ChargingStation(
                    name = "Total Parco - Gulberg Liberty",
                    operator = "Total Parco",
                    city = "Lahore",
                    address = "Liberty Roundabout, Gulberg III, Lahore",
                    latitude = 31.5125,
                    longitude = 74.3512,
                    connectorTypes = "Type 2 (22 kW AC)",
                    pricePerKwh = 72.0,
                    status = "Available",
                    isFastCharging = false
                ),

                // Karachi
                ChargingStation(
                    name = "KE Charger - Clifton Boat Basin",
                    operator = "K-Electric",
                    city = "Karachi",
                    address = "Boat Basin Retail strip, Block 5 Clifton, Karachi",
                    latitude = 24.8172,
                    longitude = 67.0315,
                    connectorTypes = "CCS2 (60 kW DC Fast), CHAdeMO (50 kW DC)",
                    pricePerKwh = 82.0,
                    status = "Available",
                    isFastCharging = true
                ),
                ChargingStation(
                    name = "Shell Recharge - DHA Phase 8 (Askari)",
                    operator = "Shell Pakistan",
                    city = "Karachi",
                    address = "Shell Askari Gas Station, Abdul Sattar Edhi Avenue, DHA Phase 8, Karachi",
                    latitude = 24.7892,
                    longitude = 67.0851,
                    connectorTypes = "CCS2 (120 kW Double Gun Fast DC)",
                    pricePerKwh = 98.0,
                    status = "Available",
                    isFastCharging = true
                ),
                ChargingStation(
                    name = "Dewan BMW Station - Shahrah-e-Faisal",
                    operator = "Dewan Motors",
                    city = "Karachi",
                    address = "Dewan Prestige Showroom, PECHS Block 6, Shahrah-e-Faisal, Karachi",
                    latitude = 24.8615,
                    longitude = 67.0722,
                    connectorTypes = "Type 2 (22 kW AC), GB/T AC (7 kW)",
                    pricePerKwh = 75.0,
                    status = "Out of Service",
                    isFastCharging = false
                ),

                // Islamabad
                ChargingStation(
                    name = "Centaurus Mall Charging Hub",
                    operator = "Tesla Industries Pak",
                    city = "Islamabad",
                    address = "Sector F-8, Jinnah Avenue, Underpass Parking, Islamabad",
                    latitude = 33.7077,
                    longitude = 73.0504,
                    connectorTypes = "CCS2 (120 kW DC Fast), GB/T (80 kW DC Fast)",
                    pricePerKwh = 89.0,
                    status = "Available",
                    isFastCharging = true
                ),
                ChargingStation(
                    name = "Shell Petrol Station - Sector G-6",
                    operator = "Shell Pakistan",
                    city = "Islamabad",
                    address = "Melody G-6/3, near Kurram Road, Islamabad",
                    latitude = 33.7285,
                    longitude = 73.0822,
                    connectorTypes = "Type 2 (22 kW AC)",
                    pricePerKwh = 74.0,
                    status = "Occupied",
                    isFastCharging = false
                ),

                // Motorway M-2
                ChargingStation(
                    name = "M-2 Bhera Service Area (Northbound)",
                    operator = "ABB & Dewan Power",
                    city = "Motorway M-2",
                    address = "Bhera Interchange, M-2 Motorway (Lahore-Islamabad direction)",
                    latitude = 32.4831,
                    longitude = 72.9152,
                    connectorTypes = "CCS2 (120 kW Super Fast DC), Type 2 (22 kW AC)",
                    pricePerKwh = 110.0,
                    status = "Available",
                    isFastCharging = true
                ),
                ChargingStation(
                    name = "M-2 Bhera Service Area (Southbound)",
                    operator = "Shell Recharge",
                    city = "Motorway M-2",
                    address = "Bhera Interchange, M-2 Motorway (Islamabad-Lahore direction)",
                    latitude = 32.4801,
                    longitude = 72.9122,
                    connectorTypes = "CCS2 (60 kW DC Fast), GB/T (60 kW DC)",
                    pricePerKwh = 105.0,
                    status = "Occupied",
                    isFastCharging = true
                ),
                ChargingStation(
                    name = "M-2 Kalar Kahar Service Area",
                    operator = "Total Parco",
                    city = "Motorway M-2",
                    address = "Kalar Kahar Interchange, M-2 Motorway, Punjab",
                    latitude = 32.7825,
                    longitude = 72.6953,
                    connectorTypes = "CCS2 (50 kW DC Fast), Type 2 (11 kW AC)",
                    pricePerKwh = 95.0,
                    status = "Available",
                    isFastCharging = true
                ),

                // Other Major Cities
                ChargingStation(
                    name = "Rawalpindi Saddar Station",
                    operator = "Total Parco",
                    city = "Rawalpindi",
                    address = "Saddar Metro Bus Station Commercial Strip, Rawalpindi",
                    latitude = 33.5935,
                    longitude = 73.0519,
                    connectorTypes = "CCS2 (60 kW DC), Type 2 (22 kW AC)",
                    pricePerKwh = 85.0,
                    status = "Available",
                    isFastCharging = true
                ),
                ChargingStation(
                    name = "Peshawar GT Road Fast Charger",
                    operator = "Dewae EV Network",
                    city = "Peshawar",
                    address = "Main GT Road near University Town entrance, Peshawar",
                    latitude = 34.0151,
                    longitude = 71.5249,
                    connectorTypes = "CCS2 (80 kW DC Fast), GB/T (40 kW DC)",
                    pricePerKwh = 89.0,
                    status = "Available",
                    isFastCharging = true
                ),
                ChargingStation(
                    name = "Quetta Cantt Charging Point",
                    operator = "Dewan Motors",
                    city = "Quetta",
                    address = "Serena Hotel Gate Area, Cantt, Quetta",
                    latitude = 30.1798,
                    longitude = 66.9986,
                    connectorTypes = "Type 2 (22 kW AC)",
                    pricePerKwh = 78.0,
                    status = "Available",
                    isFastCharging = false
                )
            )
            stationDao.insertStations(initialStations)
        }
    }

    /**
     * Randomly fluctuates pricing and changes statuses of 2 stations to simulate real-time updates.
     */
    suspend fun simulateRealtimeUpdates() {
        val currentList = allStations.first()
        if (currentList.isNotEmpty()) {
            // Select 2 random ones
            val indicesToUpdate = List(2) { Random.nextInt(currentList.size) }.distinct()
            val statuses = listOf("Available", "Occupied", "Out of Service")

            for (idx in indicesToUpdate) {
                val station = currentList[idx]
                val randomStatus = statuses[Random.nextInt(statuses.size)]

                // Prices fluctuate by +/- 1 to 5 PKR per kWh
                val priceChange = Random.nextInt(-4, 5) // -4 to +4
                val originalPrice = station.pricePerKwh
                var newPrice = originalPrice + priceChange

                // Keep price within a realistic 60 PKR to 125 PKR range
                if (newPrice < 60.0) newPrice = 60.0
                if (newPrice > 125.0) newPrice = 125.0

                stationDao.updateRealtimeStatus(station.id, randomStatus, newPrice)
            }
        }
    }
}
