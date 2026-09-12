package calc.u

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShortText
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.DrawerValue
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
import calc.u.ui.screens.TextDataScreen
import calc.u.ui.screens.TimeLabScreen
import calc.u.ui.screens.ElectroScreen
import calc.u.ui.screens.EverydayScreen
import calc.u.ui.screens.QrScanScreen
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
    Dest("time", "Time Lab", Icons.Filled.Timer),
    Dest("electro", "Electro", Icons.Filled.Build),
    Dest("textdata", "Text+Data", Icons.Filled.ShortText),
    Dest("everyday", "Everyday", Icons.Filled.Apps),
    Dest("qrscan", "QR Scan", Icons.Filled.QrCode),
    Dest("sensors", "Sensors", Icons.Filled.Explore),
    Dest("ruler", "Ruler", Icons.Filled.Straighten),
    Dest("analyze", "Analyze", Icons.Filled.BarChart),
    Dest("tools", "Tools", Icons.Filled.Apps)
)

private val SettingsDest = Dest("settings", "Settings", Icons.Filled.Settings)

private val AllDests = MainDests + ToolDests + SettingsDest

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var settingsRepo: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val theme by settingsRepo.theme.collectAsStateWithLifecycle(initialValue = "system")
            val keepOn by settingsRepo.keepScreenOn.collectAsStateWithLifecycle(initialValue = false)
            LaunchedEffect(keepOn) {
                if (keepOn) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            CalcUTheme(theme = theme) {
                val tourSeen by settingsRepo.tourSeen.collectAsStateWithLifecycle(initialValue = true)
                if (!tourSeen) {
                    val tourScope = rememberCoroutineScope()
                    TourScreen(onDone = { tourScope.launch { settingsRepo.setTourSeen() } })
                } else {
                val nav = rememberNavController()
                    val drawer = rememberDrawerState(DrawerValue.Closed)
                    val scope = rememberCoroutineScope()
                    val startRoute = intent?.getStringExtra("dest")?.takeIf {
                        it in setOf("graph", "time", "electro", "textdata", "everyday", "sensors", "tools", "qrscan", "ruler", "analyze")
                    } ?: "calc"
                    var route by remember { mutableStateOf(startRoute) }
                    fun go(r: String) {
                        val safe = r.takeIf { it in setOf("calc", "graph", "convert", "finance", "math", "steps", "time", "electro", "textdata", "everyday", "sensors", "tools", "qrscan", "ruler", "analyze", "settings") } ?: return
                        route = safe
                        runCatching { nav.navigate(safe) { launchSingleTop = true; popUpTo("calc") } }
                    }
                    BoxWithConstraints(Modifier.fillMaxSize()) {
                        val expanded = maxWidth >= 1008.dp
                        val rail = maxWidth >= 600.dp && !expanded
                        val pane: @Composable () -> Unit = {
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
                                    items(ToolDests, key = { it.route }) { d ->
                                        NavigationDrawerItem(
                                            label = { Text(d.label) },
                                            icon = { Icon(d.icon, contentDescription = null) },
                                            selected = route == d.route,
                                            onClick = { go(d.route); scope.launch { drawer.close() } },
                                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                        )
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
                                        colors = TopAppBarDefaults.topAppBarColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                                        )
                                    )
                                },
                                floatingActionButton = {
                                    if (route != "calc" && !expanded) {
                                        JumpToCalcFab(onJump = { go("calc") })
                                    }
                                }
                            ) { pad ->
                                Row(Modifier.padding(pad).fillMaxSize()) {
                                    if (rail) {
                                        NavigationRail {
                                            (MainDests + ToolDests).forEach { d ->
                                                NavigationRailItem(
                                                    selected = route == d.route,
                                                    onClick = { go(d.route) },
                                                    icon = { Icon(d.icon, contentDescription = null) },
                                                    label = { Text(d.label) }
                                                )
                                            }
                                            Spacer(Modifier.weight(1f))
                                            NavigationRailItem(
                                                selected = route == SettingsDest.route,
                                                onClick = { go(SettingsDest.route) },
                                                icon = { Icon(SettingsDest.icon, contentDescription = null) },
                                                label = { Text(SettingsDest.label) }
                                            )
                                        }
                                    }
                                    NavHost(
                                        navController = nav,
                                        startDestination = startRoute,
                                        modifier = Modifier.weight(1f).widthIn(max = 840.dp)
                                    ) {
                                        composable("calc") { Centered { CalculatorScreen() } }
                                        composable("graph") { Centered { GraphScreen() } }
                                        composable("convert") { Centered { ConvertersScreen() } }
                                        composable("finance") { Centered { FinanceScreen(onNavigate = { if (it == "back") nav.popBackStack() else go(it) }) } }
                                        composable("math") { Centered { MathScreen(onNavigate = { if (it == "back") nav.popBackStack() else go(it) }) } }
                                        composable("steps") { Centered { StepsScreen(onNavigate = { if (it == "back") nav.popBackStack() else go(it) }) } }
                                        composable("time") { Centered { TimeLabScreen() } }
                                        composable("electro") { Centered { ElectroScreen() } }
                                        composable("textdata") { Centered { TextDataScreen() } }
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
private fun Centered(content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Box(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) { content() }
}
