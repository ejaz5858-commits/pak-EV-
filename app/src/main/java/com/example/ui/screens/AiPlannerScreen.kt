package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.EvStation
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.EVViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiPlannerScreen(
    viewModel: EVViewModel,
    modifier: Modifier = Modifier
) {
    val evModel by viewModel.evModel.collectAsStateWithLifecycle()
    val batteryValue by viewModel.batteryPercent.collectAsStateWithLifecycle()
    val destinationRoute by viewModel.destinationRoute.collectAsStateWithLifecycle()
    val aiOutput by viewModel.aiOutput.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val aiError by viewModel.aiErrorMessage.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()

    // Premium Cars list popular in Pakistan
    val popularVehicles = listOf(
        "BYD Atto 3", "Changan Deepal S07", "BYD Seal", "MG ZS EV", "Audi e-tron", "Tesla Model 3", "MG4 EV"
    )

    // Popular road trips in Pakistan
    val popularPaths = listOf(
        "Lahore to Islamabad via M-2",
        "Islamabad to Peshawar via M-1",
        "Karachi to Hyderabad via M-9",
        "Lahore to Multan via M-3",
        "Islamabad to Murree (Hill Climb)"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Header card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.AutoAwesome,
                        contentDescription = "AI",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "AI Smart Route Advisor",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Get calculated charging stops, energy budget calculations, and motorway range forecasts.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Configuration Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                
                // Vehicle Input
                Column {
                    Text(
                        text = "Your EV Vehicle Profile",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = evModel,
                        onValueChange = { viewModel.evModel.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_vehicle_input"),
                        leadingIcon = { Icon(Icons.Outlined.EvStation, contentDescription = "Car") },
                        placeholder = { Text("E.g., Changan Deepal S07") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // popular cars row recommendation
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(popularVehicles) { car ->
                            val isSelected = evModel.lowercase() == car.lowercase()
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.evModel.value = car }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(car, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Battery capacity select
                Column {
                    Text(
                        text = "Current Battery Level: $batteryValue%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = (batteryValue.toFloatOrNull() ?: 50f) / 100f,
                        onValueChange = { percent ->
                            viewModel.batteryPercent.value = (percent * 100).toInt().toString()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_battery_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // Planned Route Destination
                Column {
                    Text(
                        text = "Planned Destination Route",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = destinationRoute,
                        onValueChange = { viewModel.destinationRoute.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_route_input"),
                        leadingIcon = { Icon(Icons.Default.PinDrop, contentDescription = "Route") },
                        placeholder = { Text("E.g., Lahore to Islamabad via M-2") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // popular paths row recommendation
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(popularPaths) { path ->
                            val isSelected = destinationRoute.lowercase() == path.lowercase()
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        color = if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.secondary
                                        else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.destinationRoute.value = path }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(path, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Generate trigger button
                Button(
                    onClick = { viewModel.generateAiRoadTripPlan() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("trigger_ai_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isAiLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Querying Gemini Advisor...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Consult AI")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Calculate Smart Stops & Itinerary", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        // Loading Ticker Banner
        if (isAiLoading) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "rocket")
                    val rocketOffset by infiniteTransition.animateFloat(
                        initialValue = -16f,
                        targetValue = 16f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "rocket"
                    )

                    Row(
                        modifier = Modifier.offset(x = rocketOffset.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = "Computing",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.EvStation,
                            contentDescription = "EV Charging stops Finder",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Analyzing Motorway Range drops & matching connector plugs...",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = Alignment.CenterHorizontally.run { TextAlign.Center }
                    )
                }
            }
        }

        // Error message callout (e.g. api not loaded check)
        aiError?.let { errText ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = "Warning", tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = errText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Advisory Output Log Results Rendering
        aiOutput?.let { text ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 80.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.TaskAlt,
                                contentDescription = "Active Plan",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI Trip Roadmap Calculated",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Compact reset planner report
                        IconButton(onClick = { viewModel.aiOutput.value = null }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset")
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Structured rendered markdown results text
                    Text(
                        text = text,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("ai_result_advisory")
                    )
                }
            }
        }
    }
}
