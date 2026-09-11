package calc.u

import android.os.Bundle
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timeline
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import calc.u.data.SettingsRepository
import calc.u.ui.screens.CalculatorScreen
import calc.u.ui.screens.ConvertersScreen
import calc.u.ui.screens.FinanceScreen
import calc.u.ui.screens.GraphScreen
import calc.u.ui.screens.MathScreen
import calc.u.ui.screens.SettingsScreen
import calc.u.ui.screens.StepsScreen
import calc.u.ui.theme.CalcUTheme
import com.microsoft.fluentui.theme.FluentTheme
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
    Dest("steps", "Steps", Icons.Filled.Timeline)
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
            CalcUTheme(theme = theme) {
                FluentTheme {
                    val nav = rememberNavController()
                    val drawer = rememberDrawerState(DrawerValue.Closed)
                    val scope = rememberCoroutineScope()
                    val startRoute = if (intent?.getStringExtra("dest") == "graph") "graph" else "calc"
                    var route by remember { mutableStateOf(startRoute) }
                    fun go(r: String) {
                        route = r
                        nav.navigate(r) { launchSingleTop = true; popUpTo("calc") }
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
                                ToolDests.forEach { d ->
                                    NavigationDrawerItem(
                                        label = { Text(d.label) },
                                        icon = { Icon(d.icon, contentDescription = null) },
                                        selected = route == d.route,
                                        onClick = { go(d.route); scope.launch { drawer.close() } },
                                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                    )
                                }
                                Spacer(Modifier.weight(1f))
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
                                        composable("finance") { Centered { FinanceScreen() } }
                                        composable("math") { Centered { MathScreen() } }
                                        composable("steps") { Centered { StepsScreen() } }
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
