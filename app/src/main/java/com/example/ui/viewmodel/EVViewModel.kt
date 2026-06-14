package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiRequest
import com.example.data.api.GeminiRetrofitClient
import com.example.data.db.AppDatabase
import com.example.data.model.ChargeLog
import com.example.data.model.ChargingStation
import com.example.data.repository.StationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EVViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StationRepository
    private var simulationJob: Job? = null

    // Filter states
    val searchQuery = MutableStateFlow("")
    val selectedCity = MutableStateFlow("All")
    val selectedConnector = MutableStateFlow("All")
    val isFastChargingOnly = MutableStateFlow(false)

    // UI Status / Flags
    val isSimulating = MutableStateFlow(true) // Automatically start simulation by default
    val selectedStation = MutableStateFlow<ChargingStation?>(null)

    // AI Trip planner states
    val evModel = MutableStateFlow("BYD Atto 3")
    val batteryPercent = MutableStateFlow("40")
    val destinationRoute = MutableStateFlow("Lahore to Islamabad via M-2")
    val aiOutput = MutableStateFlow<String?>(null)
    val isAiLoading = MutableStateFlow(false)
    val aiErrorMessage = MutableStateFlow<String?>(null)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = StationRepository(db.stationDao())

        // Initial loading & pre-populating
        viewModelScope.launch(Dispatchers.IO) {
            repository.ensureDatabasePopulated()
            // Start simulation loop ticker
            startSimulationLoop()
        }
    }

    // Reactive streams
    val stations: StateFlow<List<ChargingStation>> = combine(
        repository.allStations,
        searchQuery,
        selectedCity,
        selectedConnector,
        isFastChargingOnly
    ) { stations, query, city, connector, fastOnly ->
        stations.filter { station ->
            val matchQuery = station.name.contains(query, ignoreCase = true) ||
                         station.address.contains(query, ignoreCase = true) ||
                         station.operator.contains(query, ignoreCase = true)
            val matchCity = city == "All" || station.city.contains(city, ignoreCase = true)
            val matchConnector = connector == "All" || station.connectorTypes.contains(connector, ignoreCase = true)
            val matchFast = !fastOnly || station.isFastCharging

            matchQuery && matchCity && matchConnector && matchFast
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // List of unique cities and connector types for selection filters
    val availableCities = listOf("All", "Lahore", "Karachi", "Islamabad", "Motorway M-2", "Rawalpindi", "Peshawar", "Quetta")
    val availableConnectors = listOf("All", "CCS2", "Type 2", "GB/T", "CHAdeMO")

    val chargeLogs: StateFlow<List<ChargeLog>> = repository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Periodically updates pricing and availability mock updates to show real-time changes
     */
    fun toggleSimulation(enable: Boolean) {
        isSimulating.value = enable
        if (enable) {
            startSimulationLoop()
        } else {
            simulationJob?.cancel()
        }
    }

    private fun startSimulationLoop() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(8000) // update statuses every 8 seconds
                if (isSimulating.value) {
                    repository.simulateRealtimeUpdates()
                    // If a station is selected, update its state dynamically in detail screen as well
                    val selected = selectedStation.value
                    if (selected != null) {
                        val freshList = repository.allStations.first()
                        val freshInstance = freshList.find { it.id == selected.id }
                        if (freshInstance != null) {
                            selectedStation.value = freshInstance
                        }
                    }
                }
            }
        }
    }

    fun selectStation(station: ChargingStation?) {
        selectedStation.value = station
    }

    fun toggleFavorite(stationId: Int, isFav: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleFavorite(stationId, isFav)
            // Sync selected station favorite flag
            if (selectedStation.value?.id == stationId) {
                selectedStation.value = selectedStation.value?.copy(isFavorite = isFav)
            }
        }
    }

    /**
     * Submit a simulated charge session to the user's local logs database
     */
    fun logChargeSession(station: ChargingStation, connectorUsed: String, kwhAdded: Double, rate: Double, duration: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val totalCost = kwhAdded * rate
            val log = ChargeLog(
                stationId = station.id,
                stationName = station.name,
                connectorUsed = connectorUsed,
                energyLoaded = kwhAdded,
                totalCost = totalCost,
                durationMinutes = duration
            )
            repository.addChargeLog(log)
        }
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllLogs()
        }
    }

    /**
     * Uses Gemini to generate local charging path suggestions in Pakistan.
     */
    fun generateAiRoadTripPlan() {
        val currentEv = evModel.value.trim()
        val currentBattery = batteryPercent.value.trim()
        val currentRoute = destinationRoute.value.trim()

        if (currentEv.isEmpty() || currentBattery.isEmpty() || currentRoute.isEmpty()) {
            aiErrorMessage.value = "Please fill in all inputs before consulting AI Adviser."
            return
        }

        isAiLoading.value = true
        aiErrorMessage.value = null
        aiOutput.value = null

        viewModelScope.launch(Dispatchers.IO) {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                withContext(Dispatchers.Main) {
                    isAiLoading.value = false
                    aiErrorMessage.value = "Gemini API Key is not configured. Please enter a valid key in the Secrets panel in AI Studio."
                    aiOutput.value = getFallbackTripPlan(currentEv, currentBattery, currentRoute)
                }
                return@launch
            }

            // Get current station catalog text to inject into prompt
            val stationList = repository.allStations.first()
            val stationCatalogText = stationList.joinToString("\n") { s ->
                "- [${s.city}] ${s.name} | Operators: ${s.operator} | Connectors: ${s.connectorTypes} | Price: Rs. ${s.pricePerKwh}/kWh | Status: ${s.status}"
            }

            val prompt = """
                You are a highly premium, intelligent EV Charging Adviser specializing in Pakistan's electric vehicle landscape.
                Create a professional travel itinerary and charging advisory for:
                - EV Model: $currentEv
                - Current Battery Level: $currentBattery%
                - Planned Destination/Route: $currentRoute

                Here is the real-time catalog of available EV Charging Stations indexed locally:
                $stationCatalogText

                Please format your response into these exact Markdown sections:
                1. 🚗 **EV Trip Feasibility**: Address if this trip is feasible. Analyze if the battery range fits the distance, highlighting specific Pakistan constraints (e.g. high-speed driving on M-2 Motorway uses more energy).
                2. 🔋 **Recommended Stops & Charging Strategy**: Suggest specific charging stations from the list (such as 'M-2 Bhera' or 'Centaurus') that best fit the route. Estimate how much charge they should add, and mention connector compatibility (e.g., CCS2, GB/T, etc.).
                3. 💰 **Estimated Budget**: Calculate estimated cost (in PKR) based on the charge required and station price rates.
                4. 💡 **Pakistan EV Travel Tips**: Professional tips for driving an EV in Pakistan (AC usage, peak times, and high-speed energy drop-off).

                Ensure the response is elegant, motivating, highly realistic, and uses precise Pakistani context. Avoid generic boilerplate advice. Keep sections clean and easy to scan.
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = prompt))
                    )
                )
            )

            try {
                val response = GeminiRetrofitClient.service.generateContent(apiKey, request)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                
                withContext(Dispatchers.Main) {
                    isAiLoading.value = false
                    if (responseText != null) {
                        aiOutput.value = responseText
                    } else {
                        aiErrorMessage.value = "Received empty response from Gemini API."
                        aiOutput.value = getFallbackTripPlan(currentEv, currentBattery, currentRoute)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isAiLoading.value = false
                    aiErrorMessage.value = "Network Error calling Gemini API: ${e.localizedMessage}. Displaying locally generated trip adviser log."
                    aiOutput.value = getFallbackTripPlan(currentEv, currentBattery, currentRoute)
                }
            }
        }
    }

    /**
     * An elegant, localized fallback adviser tool if no Gemini API Key is loaded,
     * ensuring the app remains 100% useful, professional and completely interactive.
     */
    private fun getFallbackTripPlan(ev: String, battery: String, route: String): String {
        val batVal = battery.toIntOrNull() ?: 50
        val needsStop = batVal < 80 && route.contains("M-2", ignoreCase = true)
        
        return """
            🚗 **EV Trip Feasibility ($route)**
            Traveling with a **$ev** starting at **$battery%** battery capacity is highly feasible but requires proper speed and HVAC management:
            - **M-2 Motorway Range Drop**: Standard highway speeds of 110-120 km/h in Pakistan reduce EV range by **25% to 30%** compared to urban city traffic due to high aerodynamic resistance.
            - **Climate Impact**: Peak summer AC use can add another **8-12%** load. It is highly recommended to cruise at **100 km/h** to conserve battery.

            🔋 **Recommended Stops & Charging Strategy**
            Based on our real-time database, here is your optimal stopping strategy:
            ${if (needsStop) """
            - **Stop 1: M-2 Bhera Service Area (Northbound/Southbound depending on direction)**
               - **Connector**: CCS2 Fast Charger (120 kW Super Fast)
               - **Recommendation**: Charge from **$battery%** to **80%** (approx. 25-35 mins). Bhera is the exact midpoint of M-2 (approx. 185 km from Lahore) and is vital to avoid range anxiety before climbing the Salt Range.
               - **Current Simulated Rate**: RS. 110 / kWh (high-speed charger premium).
               - **Status**: Checked Available.
            """ else """
            - **Option 1: Centaurus Mall Charging Hub (Islamabad)**
               - **Connector**: CCS2 & GB/T Double Ports (120 kW)
               - **Recommendation**: Charge upon arrival.
               - **Current Simulated Rate**: Rs. 89 / kWh.
            - **Option 2: Shell Recharge DHA (Lahore)**
               - **Connector**: CCS2 DC / Type 2 AC (22 kW)
               - **Current Simulated Rate**: Rs. 88 / kWh.
            """}
            
            💰 **Estimated Budget Summary**
            - **Energy required**: Approx. 35 - 45 kWh to complete path comfortably with safety reserve.
            - **Estimated Cost**: **Rs. 3,500 to Rs. 4,950 PKR** based on motorway fast-charging prices.

            💡 **Pakistan EV Travel Tips**
            1. **Connector Matching**: Chinese import EVs (e.g., BYD, Changan, MG) often use **GB/T** or require a **CCS2-to-GB/T** adapter. Make sure to check connector types!
            2. **Peak Tariff Hours**: Local distribution companies (like LESCO/K-Electric) have peak hours (typically 5 PM to 11 PM) where charger rates can be higher. Plan charging during daylight hours if possible.
            3. **Backup Stations**: Download offline station locator logs. The salt range descent can regenerate some battery power (regenerative braking), so use it to your advantage!
        """.trimIndent()
    }

    // Factory method for ViewModel
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EVViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return EVViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
