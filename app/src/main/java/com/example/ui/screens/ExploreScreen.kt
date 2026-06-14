package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ChargingStation
import com.example.ui.viewmodel.EVViewModel
import kotlin.math.sqrt

// Define map node data classes
data class CityLocationNode(
    val name: String,
    val relativeX: Float, // 0 to 1
    val relativeY: Float, // 0 to 1
    val displayLabel: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: EVViewModel,
    modifier: Modifier = Modifier
) {
    val stations by viewModel.stations.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activeCity by viewModel.selectedCity.collectAsStateWithLifecycle()
    val activeConnector by viewModel.selectedConnector.collectAsStateWithLifecycle()
    val isFastOnly by viewModel.isFastChargingOnly.collectAsStateWithLifecycle()
    val isSimulating by viewModel.isSimulating.collectAsStateWithLifecycle()
    val selectedStation by viewModel.selectedStation.collectAsStateWithLifecycle()

    var showSimulateDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Search Bar & Filter Toggle Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_station_input"),
                        placeholder = { Text("Search stations, roads or networks...", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Simulation Status ticker
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isSimulating) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                            val pulseAlpha by infiniteTransition.animateFloat(
                                initialValue = 0.2f,
                                targetValue = 1.0f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulse"
                            )

                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = if (isSimulating) MaterialTheme.colorScheme.primary else Color.Gray,
                                        shape = CircleShape
                                    )
                            ) {
                                if (isSimulating) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha),
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSimulating) "Tracking Real-time Availability & Price fluctuation" else "Simulation Paused",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSimulating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        TextButton(
                            onClick = { viewModel.toggleSimulation(!isSimulating) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                text = if (isSimulating) "Pause" else "Resume",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // Interactive Drawing Map of Pakistan
            InteractivePakistanMap(
                activeCity = activeCity,
                onCitySelected = { viewModel.selectedCity.value = it }
            )

            // Category Chips Filters Row
            FilterCategoryBar(
                viewModel = viewModel,
                activeCity = activeCity,
                activeConnector = activeConnector,
                isFastOnly = isFastOnly
            )

            // Results summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Catalog: ${stations.size} stations found",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                if (activeCity != "All" || activeConnector != "All" || isFastOnly || searchQuery.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            viewModel.selectedCity.value = "All"
                            viewModel.selectedConnector.value = "All"
                            viewModel.isFastChargingOnly.value = false
                            viewModel.searchQuery.value = ""
                        },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text("Reset Filters", fontSize = 12.sp)
                    }
                }
            }

            // Lazy Column of charging stations
            if (stations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = "No stations",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No charging stations found",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Try clearing search keywords or changing city bounds to All.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = 32.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(stations, key = { it.id }) { station ->
                        StationCard(
                            station = station,
                            onClick = { viewModel.selectStation(station) },
                            onFavToggle = { viewModel.toggleFavorite(station.id, !station.isFavorite) }
                        )
                    }
                }
            }
        }

        // Selected Station Detail Sheet/Drawer dialog (Modal bottom layout)
        selectedStation?.let { station ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        RoundedCornerShape(24.dp)
                    )
                    .testTag("station_detail_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = station.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${station.operator} • ${station.city}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row {
                            IconButton(onClick = { viewModel.toggleFavorite(station.id, !station.isFavorite) }) {
                                Icon(
                                    imageVector = if (station.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "Fav",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                            IconButton(onClick = { viewModel.selectStation(null) }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    // Address Info
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Address",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = station.address,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Connectors Info
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ElectricalServices,
                            contentDescription = "Plugs",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Plugs: ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = station.connectorTypes,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Real-Time Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pricing Box
                        Column {
                            Text(
                                text = "LATEST TARIFF",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.ElectricalServices,
                                    contentDescription = "Price",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Rs. ${station.pricePerKwh}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "/kWh",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Availability Tag
                        StatusBadge(status = station.status)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.selectStation(null) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Back to Map")
                        }

                        if (station.status != "Out of Service") {
                            Button(
                                onClick = { showSimulateDialog = true },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("simulate_charge_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.EvStation, contentDescription = "Charge")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Simulate Charge", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active Charge Simulation Overlay Dialog
        if (showSimulateDialog && selectedStation != null) {
            SimulateChargeDialog(
                station = selectedStation!!,
                onDismiss = { showSimulateDialog = false },
                onRecord = { connector, kwh, duration ->
                    viewModel.logChargeSession(selectedStation!!, connector, kwh, selectedStation!!.pricePerKwh, duration)
                    showSimulateDialog = false
                    viewModel.selectStation(null) // Dismiss details as well 
                }
            )
        }
    }
}

/**
 * High fidelity interactive vector drawing map of Pakistan.
 * Draws highways N-5, Motorway M-2, and major cities with coordinates.
 * Tapping a city highlights it and filters search.
 */
@Composable
fun InteractivePakistanMap(
    activeCity: String,
    onCitySelected: (String) -> Unit
) {
    // List of localized Pakistan cities and geographic coordinate nodes for vector scaling
    val cities = listOf(
        CityLocationNode("Karachi", 0.25f, 0.85f, "Karachi Hub"),
        CityLocationNode("Quetta", 0.16f, 0.60f, "Quetta Point"),
        CityLocationNode("Multan", 0.50f, 0.53f, "Multan Area"),
        CityLocationNode("Faisalabad", 0.64f, 0.43f, "Faisalabad"),
        CityLocationNode("Lahore", 0.77f, 0.41f, "Lahore Gate"),
        CityLocationNode("Motorway M-2", 0.65f, 0.31f, "Bhera (M-2)"),
        CityLocationNode("Rawalpindi", 0.63f, 0.22f, "Rawalpindi"),
        CityLocationNode("Islamabad", 0.64f, 0.17f, "Islamabad Cap"),
        CityLocationNode("Peshawar", 0.51f, 0.19f, "Peshawar GT")
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val contextColor = MaterialTheme.colorScheme.onBackground

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Map,
                        contentDescription = "Map",
                        tint = primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Interactive Pakistan Charger Hubs Map",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Tap a node to filter",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val width = size.width
                            val height = size.height
                            // Find closest city
                            var closestCity: CityLocationNode? = null
                            var minDistance = Float.MAX_VALUE
                            val clickRadiusPx = 36.dp.toPx() // Click tolerance radius

                            for (city in cities) {
                                val cityX = city.relativeX * width
                                val cityY = city.relativeY * height
                                val dist = sqrt((offset.x - cityX) * (offset.x - cityX) + (offset.y - cityY) * (offset.y - cityY))
                                if (dist < minDistance && dist < clickRadiusPx) {
                                    minDistance = dist
                                    closestCity = city
                                }
                            }

                            if (closestCity != null) {
                                onCitySelected(closestCity.name)
                            } else {
                                // Clear filter on tap empty
                                onCitySelected("All")
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Draw National Highway N-5 Path
                    val karachi = Offset(0.25f * w, 0.85f * h)
                    val multan = Offset(0.50f * w, 0.53f * h)
                    val lahore = Offset(0.77f * w, 0.41f * h)
                    val rawalpindi = Offset(0.63f * w, 0.22f * h)
                    val islamabad = Offset(0.64f * w, 0.17f * h)
                    val peshawar = Offset(0.51f * w, 0.19f * h)

                    // Draw N-5 road network (dashed blue line)
                    drawLine(
                        color = contextColor.copy(alpha = 0.15f),
                        start = karachi,
                        end = multan,
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = contextColor.copy(alpha = 0.15f),
                        start = multan,
                        end = lahore,
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )

                    // Draw M-2 Super Highway (Green Glowing Path Connecting Lahore -> Bhera -> Rawalpindi/Islamabad)
                    val bhera = Offset(0.65f * w, 0.31f * h)
                    drawLine(
                        color = primaryColor.copy(alpha = 0.6f),
                        start = lahore,
                        end = bhera,
                        strokeWidth = 5f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = primaryColor.copy(alpha = 0.6f),
                        start = bhera,
                        end = rawalpindi,
                        strokeWidth = 5f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = primaryColor.copy(alpha = 0.4f),
                        start = rawalpindi,
                        end = peshawar,
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )

                    // Draw Quetta Connection
                    val quetta = Offset(0.16f * w, 0.60f * h)
                    drawLine(
                        color = contextColor.copy(alpha = 0.1f),
                        start = karachi,
                        end = quetta,
                        strokeWidth = 2f
                    )

                    // Draw each city station hubs
                    for (city in cities) {
                        val cityX = city.relativeX * w
                        val cityY = city.relativeY * h
                        val isSelected = activeCity.lowercase() == city.name.lowercase()

                        // Draw pulse for selected
                        if (isSelected) {
                            drawCircle(
                                color = secondaryColor.copy(alpha = 0.35f),
                                radius = 22f,
                                center = Offset(cityX, cityY)
                            )
                            drawCircle(
                                color = primaryColor,
                                radius = 10f,
                                center = Offset(cityX, cityY)
                            )
                        } else {
                            drawCircle(
                                color = if (city.name == "Motorway M-2") secondaryColor else primaryColor.copy(alpha = 0.8f),
                                radius = 6f,
                                center = Offset(cityX, cityY)
                            )
                        }

                        // Text labels for city map nodes
                        // Simple custom drawings of text (just draw small dots/indicators or labels if needed)
                    }
                }

                // Add absolute overlays of text labels for clean layout without canvas text alignment hassle
                cities.forEach { city ->
                    val isSelected = activeCity.lowercase() == city.name.lowercase()
                    Box(
                        modifier = Modifier
                            .offset(
                                x = (city.relativeX * 300).dp, // Simple relative scaling approximation
                                y = (city.relativeY * 135).dp
                            )
                    ) {
                        Text(
                            text = if (city.name == "Motorway M-2") "M-2" else city.name,
                            fontSize = 8.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            color = if (isSelected) secondaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier
                                .background(
                                    color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Horizontally scrolling selection filter bar.
 */
@Composable
fun FilterCategoryBar(
    viewModel: EVViewModel,
    activeCity: String,
    activeConnector: String,
    isFastOnly: Boolean
) {
    Column {
        // City Scroll List
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            item {
                Text(
                    text = "City:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            items(viewModel.availableCities) { city ->
                val isSelected = city == activeCity
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectedCity.value = city },
                    label = { Text(city, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("filter_city_$city")
                )
            }
        }

        // Connector Plugs Scroll List
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            item {
                Text(
                    text = "Plug:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            items(viewModel.availableConnectors) { connector ->
                val isSelected = connector == activeConnector
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectedConnector.value = connector },
                    label = { Text(connector, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    modifier = Modifier.testTag("filter_plug_$connector")
                )
            }

            item {
                // Spacer
                Spacer(modifier = Modifier.width(8.dp))
                // Fast Charging toggle
                FilterChip(
                    selected = isFastOnly,
                    onClick = { viewModel.isFastChargingOnly.value = !isFastOnly },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = "Fast", modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Fast DC (DC Only)", fontSize = 11.sp)
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                        selectedLabelColor = Color.Black
                    ),
                    modifier = Modifier.testTag("filter_fast_only")
                )
            }
        }
    }
}

/**
 * Beautiful Station Card with Material 3 styling, status colors and price tags.
 */
@Composable
fun StationCard(
    station: ChargingStation,
    onClick: () -> Unit,
    onFavToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("station_card_${station.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Operator Initials Icon
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        color = when (station.operator) {
                            "Shell Pakistan" -> Color(0xFFFFCC00).copy(alpha = 0.2f)
                            "K-Electric" -> Color(0xFF00D1FF).copy(alpha = 0.2f)
                            "Dewan Motors" -> Color(0xFF047857).copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                val letterInitial = if (station.operator.isNotEmpty()) station.operator.take(2) else "EV"
                Text(
                    text = letterInitial.uppercase(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = when (station.operator) {
                        "Shell Pakistan" -> Color(0xFFD43F00)
                        "K-Electric" -> Color(0xFF008DDF)
                        "Dewan Motors" -> Color(0xFF047857)
                        else -> MaterialTheme.colorScheme.primary
                    }
                )

                // Small Speed indicator bolts
                if (station.isFastCharging) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 4.dp, y = 4.dp)
                            .size(16.dp)
                            .background(MaterialTheme.colorScheme.tertiary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = "Fast DC",
                            tint = Color.Black,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text Info Columns
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = station.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onFavToggle,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (station.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (station.isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = station.address,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )

                    // Small mini Status indicator
                    StatusIndicatorDot(status = station.status)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Connector types tags
                    Text(
                        text = station.connectorTypes.split(",").firstOrNull() ?: "",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )

                    // Tariff Price tags
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Rs. ${station.pricePerKwh}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "/kWh",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compact circular indicator for status.
 */
@Composable
fun StatusIndicatorDot(status: String) {
    val (color, text) = when (status) {
        "Available" -> Color(0xFF047857) to "Available"
        "Occupied" -> Color(0xFF475569) to "Occupied"
        else -> Color(0xFFDC2626) to "Out of Service"
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
    }
}

/**
 * Padded tag badge for details view of the selected charging unit.
 */
@Composable
fun StatusBadge(status: String) {
    val (color, label) = when (status) {
        "Available" -> Color(0xFF047857) to "Available"
        "Occupied" -> Color(0xFF475569) to "Occupied"
        else -> Color(0xFFDC2626) to "Out of Service"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = label.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Compact dialog designed to simulate plug-in charging at a station.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulateChargeDialog(
    station: ChargingStation,
    onDismiss: () -> Unit,
    onRecord: (connector: String, kwh: Double, duration: Int) -> Unit
) {
    val connectorsList = station.connectorTypes.split(",")
    var selectedConnector by remember { mutableStateOf(connectorsList.firstOrNull()?.trim() ?: "CCS2") }
    var kwhValue by remember { mutableStateOf("30") }
    var durationValue by remember { mutableStateOf("45") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("simulate_charge_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.EvStation,
                        contentDescription = "Ev Station",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Plug-In Simulation",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "at " + station.name,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Select Plug
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Choose Charger Connector:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Simple flow list of connectors
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(connectorsList) { conn ->
                            val cleanConn = conn.trim()
                            val isSelected = selectedConnector == cleanConn
                            ElevatedFilterChip(
                                selected = isSelected,
                                onClick = { selectedConnector = cleanConn },
                                label = { Text(cleanConn, fontSize = 10.sp) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Energy Load (kWh)
                OutlinedTextField(
                    value = kwhValue,
                    onValueChange = { kwhValue = it },
                    label = { Text("Power Loaded (kWh)", fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("simulate_input_kwh"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Duration (Minutes)
                OutlinedTextField(
                    value = durationValue,
                    onValueChange = { durationValue = it },
                    label = { Text("Charging Duration (minutes)", fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("simulate_input_duration"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Calculated total price calculation
                val kwhDouble = kwhValue.toDoubleOrNull() ?: 0.0
                val totalCostPkr = kwhDouble * station.pricePerKwh
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Calculated Bill Rate:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Rs. %,.2f PKR".format(totalCostPkr),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val k = kwhValue.toDoubleOrNull() ?: 30.0
                            val d = durationValue.toIntOrNull() ?: 45
                            onRecord(selectedConnector, k, d)
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("confirm_simulate_charge_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Start & Log Session", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
