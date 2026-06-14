package com.example.data.db

import androidx.room.*
import com.example.data.model.ChargeLog
import com.example.data.model.ChargingStation
import kotlinx.coroutines.flow.Flow

@Dao
interface StationDao {
    @Query("SELECT * FROM charging_stations ORDER BY name ASC")
    fun getAllStations(): Flow<List<ChargingStation>>

    @Query("SELECT * FROM charging_stations WHERE city = :city ORDER BY name ASC")
    fun getStationsByCity(city: String): Flow<List<ChargingStation>>

    @Query("SELECT * FROM charging_stations WHERE id = :id LIMIT 1")
    fun getStationByIdFlow(id: Int): Flow<ChargingStation?>

    @Query("SELECT * FROM charging_stations WHERE id = :id LIMIT 1")
    suspend fun getStationById(id: Int): ChargingStation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStations(stations: List<ChargingStation>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStation(station: ChargingStation)

    @Update
    suspend fun updateStation(station: ChargingStation)

    @Query("UPDATE charging_stations SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavorite(id: Int, isFav: Boolean)

    @Query("UPDATE charging_stations SET status = :status, pricePerKwh = :price, lastUpdated = :now WHERE id = :id")
    suspend fun updateRealtimeStatus(id: Int, status: String, price: Double, now: Long = System.currentTimeMillis())

    // Charge Log queries
    @Query("SELECT * FROM charge_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ChargeLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ChargeLog)

    @Query("DELETE FROM charge_logs")
    suspend fun clearLogs()
}
