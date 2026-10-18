package com.example

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.data.KeyboardPreferences
import com.example.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = KeyboardPreferences(this)

        setContent {
            MyApplicationTheme {
                SplitKeyboardApp(
                    prefs = prefs,
                    onOpenSettings = {
                        startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                    },
                    onOpenPicker = {
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                        imm?.showInputMethodPicker()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitKeyboardApp(
    prefs: KeyboardPreferences,
    onOpenSettings: () -> Unit,
    onOpenPicker: () -> Unit
) {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(checkKeyboardEnabled(context)) }
    var isSelected by remember { mutableStateOf(checkKeyboardSelected(context)) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showArchitectureDialog by remember { mutableStateOf(false) }

    // Recheck status when returning to app
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isEnabled = checkKeyboardEnabled(context)
                isSelected = checkKeyboardSelected(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Keyboard,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Split Keyboard for Tablet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Dual Thumb Panels • Touch-Through Center",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showArchitectureDialog = true },
                        modifier = Modifier.testTag("tech_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Architecture Specs"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Status and Quick Setup Banner
            SetupStatusCard(
                isEnabled = isEnabled,
                isSelected = isSelected,
                onOpenSettings = onOpenSettings,
                onOpenPicker = onOpenPicker,
                onRefresh = {
                    isEnabled = checkKeyboardEnabled(context)
                    isSelected = checkKeyboardSelected(context)
                }
            )

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Touch-Through Playground") },
                    icon = { Icon(Icons.Default.TouchApp, contentDescription = null) },
                    modifier = Modifier.testTag("tab_playground")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Geometry & Ergonomics") },
                    icon = { Icon(Icons.Default.Tune, contentDescription = null) },
                    modifier = Modifier.testTag("tab_geometry")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Setup & Help") },
                    icon = { Icon(Icons.Default.HelpOutline, contentDescription = null) },
                    modifier = Modifier.testTag("tab_setup")
                )
            }

            when (selectedTab) {
                0 -> TouchThroughPlaygroundTab()
                1 -> GeometrySettingsTab(prefs)
                2 -> SetupGuideTab(onOpenSettings, onOpenPicker)
            }
        }
    }

    if (showArchitectureDialog) {
        ArchitectureExplainerDialog(onDismiss = { showArchitectureDialog = false })
    }
}

@Composable
fun SetupStatusCard(
    isEnabled: Boolean,
    isSelected: Boolean,
    onOpenSettings: () -> Unit,
    onOpenPicker: () -> Unit,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled && isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusIcon = if (isEnabled && isSelected) Icons.Default.CheckCircle else Icons.Default.Warning
                    val statusColor = if (isEnabled && isSelected) Color(0xFF10B981) else Color(0xFFF59E0B)
                    Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEnabled && isSelected) {
                            "Split Keyboard is Active"
                        } else if (!isEnabled) {
                            "Step 1 Required: Enable Keyboard in System Settings"
                        } else {
                            "Step 2 Required: Select as Current Keyboard"
                        },
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isEnabled && isSelected) {
                        "Ready to type with left/right thumb panels while scrolling and tapping the center."
                    } else if (!isEnabled) {
                        "Split Keyboard must be turned ON under Android Settings > System > Languages & Input."
                    } else {
                        "Split Keyboard is enabled. Switch your active input method to start typing."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isEnabled) {
                    Button(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("enable_keyboard_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Text("Enable")
                    }
                } else if (!isSelected) {
                    Button(
                        onClick = onOpenPicker,
                        modifier = Modifier.testTag("switch_keyboard_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Text("Switch IME")
                    }
                } else {
                    OutlinedButton(
                        onClick = onOpenPicker,
                        modifier = Modifier.testTag("change_keyboard_button")
                    ) {
                        Text("Change IME")
                    }
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.testTag("refresh_status_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh status")
                }
            }
        }
    }
}

@Composable
fun TouchThroughPlaygroundTab() {
    var typedText by remember { mutableStateOf("") }
    var centerTapCount by remember { mutableIntStateOf(0) }
    var centerScrollDistance by remember { mutableIntStateOf(0) }
    var switchA by remember { mutableStateOf(false) }
    var switchB by remember { mutableStateOf(true) }
    val eventLogs = remember { mutableStateListOf<String>() }
    val listState = rememberLazyListState()

    fun logEvent(msg: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        eventLogs.add(0, "[$time] $msg")
        if (eventLogs.size > 20) {
            eventLogs.removeLast()
        }
    }

    LaunchedEffect(typedText) {
        if (typedText.isNotEmpty()) {
            logEvent("Typing update: length ${typedText.length}")
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemScrollOffset }
            .collect { offset ->
                if (offset > 0) {
                    centerScrollDistance += 1
                }
            }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .testTag("playground_scrollable_container"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Typing Input Field
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "1. Active Text Input (Focus to summon keyboard)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        if (typedText.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    typedText = ""
                                    logEvent("Cleared text field")
                                },
                                modifier = Modifier.testTag("clear_text_button")
                            ) {
                                Text("Clear")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = typedText,
                        onValueChange = { typedText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_input_field"),
                        placeholder = { Text("Tap here to open Split Keyboard and type with your thumbs...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0284C7)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        item {
            // Live Proof Metrics Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TOUCH-THROUGH TELEMETRY (CENTER GAP VERIFICATION)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = Color(0xFF38BDF8)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MetricItem(label = "Center Taps", value = centerTapCount.toString(), color = Color(0xFF10B981))
                        MetricItem(label = "Center Scrolls", value = centerScrollDistance.toString(), color = Color(0xFF38BDF8))
                        MetricItem(label = "Chars Typed", value = typedText.length.toString(), color = Color(0xFFA855F7))
                    }
                }
            }
        }

        item {
            // Center Interaction Zone Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Center Screen Touch-Through Zone",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "While the split keyboard is open, the space between the thumb panels is fully interactive. Test the controls below without closing the keyboard:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                centerTapCount++
                                logEvent("Tapped: 'Counter +1' (Total: $centerTapCount)")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("center_tap_counter_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                        ) {
                            Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Center Tap (+1)")
                        }

                        Button(
                            onClick = {
                                centerTapCount++
                                logEvent("Tapped: 'Quick Action B'")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("center_action_b_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                        ) {
                            Text("Center Action B")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = switchA,
                                onCheckedChange = {
                                    switchA = it
                                    centerTapCount++
                                    logEvent("Toggled Switch Alpha: $it")
                                },
                                modifier = Modifier.testTag("center_switch_alpha"),
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF0284C7))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Center Toggle Alpha", style = MaterialTheme.typography.bodyMedium)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = switchB,
                                onCheckedChange = {
                                    switchB = it
                                    centerTapCount++
                                    logEvent("Toggled Switch Beta: $it")
                                },
                                modifier = Modifier.testTag("center_switch_beta"),
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF0284C7))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Center Toggle Beta", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        // Feed of scrollable cards in the center to test scrolling while typing
        item {
            Text(
                text = "Scrollable Content Feed (Swipe up/down through center while keyboard is up):",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        itemsIndexed(
            listOf(
                "Tablet Article: Native IME Touchable Insets in Android 14+",
                "Code Editor Pane: fun onComputeInsets(outInsets: Insets)",
                "Web Browser Tab: Interactive Search Results & Links",
                "Productivity Tool: Multi-column Table Grid with Active Cells",
                "Drawing Canvas / Note: Center Scribble and Selection Area",
                "Terminal Console: Logs and Background Process Output"
            )
        ) { index, title ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        centerTapCount++
                        logEvent("Tapped Card #${index + 1}: $title")
                    }
                    .testTag("test_card_$index"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Item #${index + 1}: $title", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Tap or scroll this item through the empty center gap.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            // Live Event Log Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EVENT AUDIT TRAIL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = Color(0xFF38BDF8)
                        )
                        if (eventLogs.isNotEmpty()) {
                            TextButton(onClick = { eventLogs.clear() }) {
                                Text("Clear Log", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (eventLogs.isEmpty()) {
                        Text(
                            text = "Interact with keys or center controls to view live touch dispatch events.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )
                    } else {
                        eventLogs.take(8).forEach { log ->
                            Text(
                                text = log,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                ),
                                color = Color(0xFFE2E8F0)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
    }
}

@Composable
fun GeometrySettingsTab(prefs: KeyboardPreferences) {
    var panelWidth by remember { mutableFloatStateOf(prefs.panelWidthDp) }
    var panelHeight by remember { mutableFloatStateOf(prefs.panelHeightDp) }
    var bottomMargin by remember { mutableFloatStateOf(prefs.panelBottomMarginDp) }
    var panelAlpha by remember { mutableFloatStateOf(prefs.panelAlpha) }
    var haptic by remember { mutableStateOf(prefs.hapticFeedback) }
    var sound by remember { mutableStateOf(prefs.soundFeedback) }
    var showNumbers by remember { mutableStateOf(prefs.showNumberRow) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Ergonomic Split Geometry",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Adjust the position and size of the left and right panels to match your hand size and thumb reach on your tablet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            // Tablet Screen Visual Diagram
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "TABLET LANDSCAPE SCHEMATIC",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = Color(0xFF38BDF8)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        // Left Panel Representation
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .width((panelWidth / 4).dp)
                                .height((panelHeight / 3.5).dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0284C7))
                                .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Left QWERTY", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }

                        // Center Gap Representation
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "Touch-Through Center",
                                color = Color(0xFF34D399),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Right Panel Representation
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .width((panelWidth / 4).dp)
                                .height((panelHeight / 3.5).dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0284C7))
                                .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Right QWERTY", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Panel Width
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Panel Width", fontWeight = FontWeight.Bold)
                        Text("${panelWidth.toInt()} dp", color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = panelWidth,
                        onValueChange = {
                            panelWidth = it
                            prefs.panelWidthDp = it
                        },
                        valueRange = 200f..380f,
                        modifier = Modifier.testTag("panel_width_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Panel Height
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Panel Height", fontWeight = FontWeight.Bold)
                        Text("${panelHeight.toInt()} dp", color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = panelHeight,
                        onValueChange = {
                            panelHeight = it
                            prefs.panelHeightDp = it
                        },
                        valueRange = 180f..360f,
                        modifier = Modifier.testTag("panel_height_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Bottom Margin (Thumb elevation)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Bottom Elevation (Resting Height)", fontWeight = FontWeight.Bold)
                        Text("${bottomMargin.toInt()} dp", color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = bottomMargin,
                        onValueChange = {
                            bottomMargin = it
                            prefs.panelBottomMarginDp = it
                        },
                        valueRange = 0f..180f,
                        modifier = Modifier.testTag("bottom_margin_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Panel Opacity
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Panel Opacity / Glass", fontWeight = FontWeight.Bold)
                        Text("${(panelAlpha * 100).toInt()}%", color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = panelAlpha,
                        onValueChange = {
                            panelAlpha = it
                            prefs.panelAlpha = it
                        },
                        valueRange = 0.6f..1.0f,
                        modifier = Modifier.testTag("panel_alpha_slider")
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Haptic Feedback (Vibration)", fontWeight = FontWeight.Bold)
                            Text("Subtle mechanical click on key tap", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = haptic,
                            onCheckedChange = {
                                haptic = it
                                prefs.hapticFeedback = it
                            },
                            modifier = Modifier.testTag("haptic_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Key Click Audio", fontWeight = FontWeight.Bold)
                            Text("Soft audio tick sound effect", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = sound,
                            onCheckedChange = {
                                sound = it
                                prefs.soundFeedback = it
                            },
                            modifier = Modifier.testTag("sound_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Show Dedicated Number Row", fontWeight = FontWeight.Bold)
                            Text("Adds 1-5 to left panel and 6-0 to right panel", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = showNumbers,
                            onCheckedChange = {
                                showNumbers = it
                                prefs.showNumberRow = it
                            },
                            modifier = Modifier.testTag("show_numbers_switch")
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    prefs.resetGeometry()
                    panelWidth = prefs.panelWidthDp
                    panelHeight = prefs.panelHeightDp
                    bottomMargin = prefs.panelBottomMarginDp
                    panelAlpha = prefs.panelAlpha
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_geometry_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset Geometry to Defaults")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SetupGuideTab(
    onOpenSettings: () -> Unit,
    onOpenPicker: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Tablet Quick Start Guide",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Three simple steps to start using the split floating keyboard on your OnePlus tablet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            SetupStepCard(
                stepNumber = "1",
                title = "Install & Open App",
                description = "Download and tap the APK to install. Grant the standard keyboard activation permission.",
                actionLabel = "Already Installed",
                onAction = {},
                isCompleted = true
            )
        }

        item {
            val context = LocalContext.current
            val isEnabled = checkKeyboardEnabled(context)
            SetupStepCard(
                stepNumber = "2",
                title = "Enable Split Keyboard in System Settings",
                description = "Android requires all custom keyboards to be enabled once in Settings > System > Languages & Input > On-screen keyboard. Tap below to jump straight to the setting.",
                actionLabel = if (isEnabled) "Enabled (Done)" else "Open Android Keyboard Settings",
                onAction = onOpenSettings,
                isCompleted = isEnabled
            )
        }

        item {
            val context = LocalContext.current
            val isSelected = checkKeyboardSelected(context)
            SetupStepCard(
                stepNumber = "3",
                title = "Switch Active Keyboard",
                description = "Select 'Split Keyboard (Tablet)' as your active input method. You can switch back to Gboard or any other keyboard at any time using the keyboard switcher icon.",
                actionLabel = if (isSelected) "Active (Done)" else "Switch Keyboard Now",
                onAction = onOpenPicker,
                isCompleted = isSelected
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tips for Tablet Thumb Typing",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Drag either panel up or down by touching the top header ('LEFT' or 'RIGHT') to align with your thumb height.")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Both panels contain a Space bar, so either thumb can tap space freely.")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Double tap Shift (⇧) to engage Caps Lock.")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Tap the '⌄' button on the right panel to hide or dismiss the keyboard.")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Use the empty center of your tablet screen to scroll websites, tap links, or click editor buttons.")
                }
            }
        }
    }
}

@Composable
fun SetupStepCard(
    stepNumber: String,
    title: String,
    description: String,
    actionLabel: String,
    onAction: () -> Unit,
    isCompleted: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isCompleted) Color(0xFF10B981) else Color(0xFF0284C7)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    } else {
                        Text(text = stepNumber, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onAction,
                enabled = !isCompleted,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
            ) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
fun ArchitectureExplainerDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFF0284C7))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Technical Architecture", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        },
        text = {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Text(
                    text = "Why InputMethodService with TOUCHABLE_INSETS_REGION is the correct solution:",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "1. Native OS Insets: Android's InputMethodService.Insets provides TOUCHABLE_INSETS_REGION. This informs Android WindowManager that only the Left and Right panel bounding boxes receive touches. Touches falling in the center bypass the keyboard window completely and are dispatched to the underlying app by the OS kernel.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "2. No Focus Lost: Because it is a native InputMethodService, the underlying app (browser, code editor, notes) retains active text focus and cursor state. Keystrokes are committed via InputConnection.commitText().",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "3. Why Floating Overlays (SYSTEM_ALERT_WINDOW) fail: Standalone overlay windows cannot obtain an InputConnection to target apps. They would require invasive AccessibilityService permissions, fight for window focus, cannot reliably trigger keyboard actions (Search/Enter), and can be blocked by Android security restrictions.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
            ) {
                Text("Got It")
            }
        }
    )
}

fun checkKeyboardEnabled(context: Context): Boolean {
    return try {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        val enabledList = imm?.enabledInputMethodList ?: emptyList()
        enabledList.any { it.packageName == context.packageName }
    } catch (e: Exception) {
        false
    }
}

fun checkKeyboardSelected(context: Context): Boolean {
    return try {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val currentInfo = imm?.currentInputMethodInfo
            currentInfo?.packageName == context.packageName
        } else {
            val defaultMethod = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.DEFAULT_INPUT_METHOD
            ) ?: ""
            defaultMethod.contains(context.packageName)
        }
    } catch (e: Exception) {
        false
    }
}
