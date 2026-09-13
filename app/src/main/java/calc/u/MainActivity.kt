package calc.u

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShortText
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import calc.u.data.SettingsRepository
import calc.u.ui.CalcViewModel
import calc.u.ui.JumpToCalcFab
import calc.u.ui.screens.AnalyzeScreen
import calc.u.ui.screens.CalculatorScreen
import calc.u.ui.screens.ConvertersScreen
import calc.u.ui.screens.FinanceScreen
import calc.u.ui.screens.GraphScreen
import calc.u.ui.screens.MathScreen
import calc.u.ui.screens.SettingsScreen
import calc.u.ui.screens.StepsScreen
import calc.u.ui.screens.TextCalcScreen
import calc.u.ui.screens.TextDataScreen
import calc.u.ui.screens.TimeLabScreen
import calc.u.ui.screens.ElectroScreen
import calc.u.ui.screens.EverydayScreen
import calc.u.ui.screens.GeometryScreen
import calc.u.ui.screens.HealthScreen
import calc.u.ui.screens.QrScanScreen
import calc.u.ui.screens.ProgrammerScreen
import calc.u.ui.screens.RulerScreen
import calc.u.ui.screens.SensorScreen
import calc.u.ui.screens.ToolsHub
import calc.u.ui.screens.TourScreen
import calc.u.ui.theme.CalcUTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Dest(val route: String, val label: String, val icon: ImageVector)

private val MainDests = listOf(
    Dest("calc", "Calculator", Icons.Filled.Calculate),
    Dest("graph", "Graph", Icons.Filled.ShowChart)
)

private val ToolDests = listOf(
    Dest("convert", "Convert", Icons.Filled.SwapHoriz),
    Dest("finance", "Finance", Icons.Filled.AttachMoney),
    Dest("math", "Math", Icons.Filled.GridOn),
    Dest("steps", "Steps", Icons.Filled.Timeline),
    Dest("geometry", "Geometry", Icons.Filled.Category),
    Dest("programmer", "Programmer", Icons.Filled.Code),
    Dest("time", "Time Lab", Icons.Filled.Timer),
    Dest("electro", "Electro", Icons.Filled.Build),
    Dest("textdata", "Text+Data", Icons.Filled.ShortText),
    Dest("textcalc", "Text Calc", Icons.Filled.Description),
    Dest("qrscan", "QR Scan", Icons.Filled.QrCode),
    Dest("everyday", "Everyday", Icons.Filled.Widgets),
    Dest("ruler", "Ruler", Icons.Filled.Straighten),
    Dest("health", "Health", Icons.Filled.Favorite),
    Dest("sensors", "Sensors", Icons.Filled.Explore),
    Dest("analyze", "Analyze", Icons.Filled.BarChart),
    Dest("tools", "Tools", Icons.Filled.Apps)
)

private data class DrawerGroup(val title: String, val routes: List<String>)

private val DrawerGroups = listOf(
    DrawerGroup("Calculate", listOf("programmer")),
    DrawerGroup("Convert", listOf("convert")),
    DrawerGroup("Finance", listOf("finance")),
    DrawerGroup("Math", listOf("math", "steps", "geometry")),
    DrawerGroup("Time", listOf("time")),
    DrawerGroup("Electro+Network", listOf("electro")),
    DrawerGroup("Text+Data", listOf("textdata", "textcalc", "qrscan")),
    DrawerGroup("Everyday", listOf("everyday", "ruler", "health")),
    DrawerGroup("System", listOf("sensors", "analyze", "tools"))
)

private val SettingsDest = Dest("settings", "Settings", Icons.Filled.Settings)

private val AllDests = MainDests + ToolDests + SettingsDest

private val AllRoutes = AllDests.map { it.route }.toSet()

private fun nextTheme(current: String): String = when (current) {
    "light" -> "dark"
    "dark" -> "system"
    else -> "light"
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var settingsRepo: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val theme by settingsRepo.theme.collectAsStateWithLifecycle(initialValue = "system")
            val themeMode by settingsRepo.themeMode.collectAsStateWithLifecycle(initialValue = "system")
            val themeAmoled by settingsRepo.amoled.collectAsStateWithLifecycle(initialValue = false)
            val dynamicColor by settingsRepo.dynamicColor.collectAsStateWithLifecycle(initialValue = true)
            val customSeed by settingsRepo.customSeedArgb.collectAsStateWithLifecycle(initialValue = null)
            val keepOn by settingsRepo.keepScreenOn.collectAsStateWithLifecycle(initialValue = false)
            LaunchedEffect(keepOn) {
                if (keepOn) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            CalcUTheme(theme = theme, mode = themeMode, amoled = themeAmoled, dynamic = dynamicColor, customSeedArgb = customSeed) {
                val tourSeen by settingsRepo.tourSeen.collectAsStateWithLifecycle(initialValue = true)
                if (!tourSeen) {
                    val tourScope = rememberCoroutineScope()
                    TourScreen(onDone = { tourScope.launch { settingsRepo.setTourSeen() } })
                } else {
                val nav = rememberNavController()
                    val drawer = rememberDrawerState(DrawerValue.Closed)
                    val scope = rememberCoroutineScope()
                    val startRoute = intent?.getStringExtra("dest")?.takeIf { it in AllRoutes } ?: "calc"
                    var route by remember { mutableStateOf(startRoute) }
                    var overflowOpen by remember { mutableStateOf(false) }
                    var showAbout by remember { mutableStateOf(false) }
                    val vibrationOn by settingsRepo.vibration.collectAsStateWithLifecycle(initialValue = true)
                    val fractionsOn by settingsRepo.fractions.collectAsStateWithLifecycle(initialValue = true)
                    fun go(r: String) {
                        val safe = r.takeIf { it in AllRoutes } ?: return
                        route = safe
                        runCatching { nav.navigate(safe) { launchSingleTop = true; popUpTo("calc") } }
                    }
                    BoxWithConstraints(Modifier.fillMaxSize()) {
                        val expanded = maxWidth >= 1008.dp
                        val rail = maxWidth >= 600.dp && !expanded
                        val pane: @Composable () -> Unit = {
                            val destByRoute = ToolDests.associateBy { it.route }
                            Column(Modifier.fillMaxSize().padding(vertical = 12.dp)) {
                                Text(
                                    "CalcU",
                                    style = MaterialTheme.typography.headlineSmall,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                        .semantics { heading() }
                                )
                                (MainDests).forEach { d ->
                                    NavigationDrawerItem(
                                        label = { Text(d.label) },
                                        icon = { Icon(d.icon, contentDescription = null) },
                                        selected = route == d.route,
                                        onClick = { go(d.route); scope.launch { drawer.close() } },
                                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                    )
                                }
                                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                                LazyColumn(Modifier.weight(1f)) {
                                    DrawerGroups.forEach { group ->
                                        item(key = "drawer-group-${group.title}") {
                                            Text(
                                                group.title,
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier
                                                    .padding(horizontal = 28.dp, vertical = 4.dp)
                                                    .semantics { heading() }
                                            )
                                        }
                                        items(group.routes, key = { "drawer-$it" }) { r ->
                                            val d = destByRoute[r]
                                            if (d != null) {
                                                NavigationDrawerItem(
                                                    label = { Text(d.label) },
                                                    icon = { Icon(d.icon, contentDescription = null) },
                                                    selected = route == d.route,
                                                    onClick = { go(d.route); scope.launch { drawer.close() } },
                                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                                )
                                            }
                                        }
                                    }
                                }
                                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                                NavigationDrawerItem(
                                    label = { Text(SettingsDest.label) },
                                    icon = { Icon(SettingsDest.icon, contentDescription = null) },
                                    selected = route == SettingsDest.route,
                                    onClick = { go(SettingsDest.route); scope.launch { drawer.close() } },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                )
                            }
                        }
                        val content: @Composable () -> Unit = {
                            Scaffold(
                                topBar = {
                                    @OptIn(ExperimentalMaterial3Api::class)
                                    TopAppBar(
                                        title = { Text(AllDests.firstOrNull { it.route == route }?.label ?: "CalcU") },
                                        navigationIcon = {
                                            if (!expanded) {
                                                IconButton(
                                                    onClick = { scope.launch { drawer.open() } }
                                                ) { Icon(Icons.Filled.Menu, contentDescription = "Open navigation") }
                                            }
                                        },
                                        actions = {
                                            IconButton(onClick = { go("tools") }) {
                                                Icon(Icons.Filled.Search, contentDescription = "Search tools")
                                            }
                                            IconButton(onClick = { overflowOpen = true }) {
                                                Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                                            }
                                            DropdownMenu(
                                                expanded = overflowOpen,
                                                onDismissRequest = { overflowOpen = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Vibration") },
                                                    leadingIcon = {
                                                        if (vibrationOn) Icon(Icons.Filled.Check, contentDescription = null)
                                                    },
                                                    onClick = {
                                                        scope.launch { settingsRepo.setVibration(!vibrationOn) }
                                                        overflowOpen = false
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Fractions") },
                                                    leadingIcon = {
                                                        if (fractionsOn) Icon(Icons.Filled.Check, contentDescription = null)
                                                    },
                                                    onClick = {
                                                        scope.launch { settingsRepo.setFractions(!fractionsOn) }
                                                        overflowOpen = false
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Theme: $theme") },
                                                    onClick = {
                                                        scope.launch { settingsRepo.setTheme(nextTheme(theme)) }
                                                        overflowOpen = false
                                                    }
                                                )
                                                HorizontalDivider()
                                                DropdownMenuItem(
                                                    text = { Text("Settings") },
                                                    onClick = { overflowOpen = false; go("settings") }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("About") },
                                                    onClick = { overflowOpen = false; showAbout = true }
                                                )
                                            }
                                        },
                                        colors = TopAppBarDefaults.topAppBarColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                        )
                                    )
                                },
                                floatingActionButton = {
                                    if (route != "calc" && !expanded) {
                                        JumpToCalcFab(onJump = { go("calc") })
                                    }
                                }
                            ) { pad ->
                                if (showAbout) {
                                    AboutDialog(onClose = { showAbout = false })
                                }
                                Row(Modifier.padding(pad).fillMaxSize()) {
                                    if (rail) {
                                        NavigationRail(
                                            modifier = Modifier.verticalScroll(rememberScrollState())
                                        ) {
                                            (MainDests + ToolDests).forEach { d ->
                                                NavigationRailItem(
                                                    selected = route == d.route,
                                                    onClick = { go(d.route) },
                                                    icon = { Icon(d.icon, contentDescription = null) },
                                                    label = { Text(d.label) }
                                                )
                                            }
                                            NavigationRailItem(
                                                selected = route == SettingsDest.route,
                                                onClick = { go(SettingsDest.route) },
                                                icon = { Icon(SettingsDest.icon, contentDescription = null) },
                                                label = { Text(SettingsDest.label) }
                                            )
                                        }
                                    }
                                    Box(Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                                    NavHost(
                                        navController = nav,
                                        startDestination = startRoute,
                                        modifier = Modifier.widthIn(max = 840.dp).fillMaxSize()
                                    ) {
                                        composable("calc") { Centered { CalculatorScreen() } }
                                        composable("graph") { Centered { GraphScreen() } }
                                        composable("convert") { Centered { ConvertersScreen() } }
                                        composable("finance") { Centered { FinanceScreen(onNavigate = { if (it == "back") nav.popBackStack() else go(it) }) } }
                                        composable("math") { Centered { MathScreen(onNavigate = { if (it == "back") nav.popBackStack() else go(it) }) } }
                                        composable("steps") { Centered { StepsScreen(onNavigate = { if (it == "back") nav.popBackStack() else go(it) }) } }
                                        composable("geometry") { Centered { GeometryScreen() } }
                                        composable("health") { Centered { HealthScreen(onNavigate = { if (it == "back") nav.popBackStack() else go(it) }) } }
                                        composable("programmer") { Centered { ProgrammerScreen() } }
                                        composable("time") { Centered { TimeLabScreen() } }
                                        composable("electro") { Centered { ElectroScreen() } }
                                        composable("textdata") { Centered { TextDataScreen() } }
                                        composable("textcalc") { Centered { TextCalcScreen() } }
                                        composable("everyday") { Centered { EverydayScreen() } }
                                        composable("sensors") { Centered { SensorScreen() } }
                                        composable("ruler") { Centered { RulerScreen() } }
                                        composable("qrscan") { Centered { QrScanScreen() } }
                                        composable("analyze") {
                                            val vm: CalcViewModel = hiltViewModel()
                                            val activity by vm.activity.collectAsStateWithLifecycle()
                                            val st by vm.uiState.collectAsStateWithLifecycle()
                                            Centered {
                                                AnalyzeScreen(
                                                    activity = activity,
                                                    historyCount = st.history.size
                                                )
                                            }
                                        }
                                        composable("tools") { Centered { ToolsHub(onOpen = { go(it) }) } }
                                        composable("settings") { Centered { SettingsScreen() } }
                                    }
                                    }
                                }
                            }
                        }
                        if (expanded) {
                            PermanentNavigationDrawer(
                                drawerContent = { PermanentDrawerSheet { pane() } },
                                content = content
                            )
                        } else {
                            ModalNavigationDrawer(
                                drawerState = drawer,
                                drawerContent = { ModalDrawerSheet { pane() } },
                                content = content
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutDialog(onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = { TextButton(onClick = onClose) { Text("Close") } },
        title = { Text("CalcU") },
        text = { Text("Free forever: no ads, no tracking, local only.") }
    )
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) { content() }
}
