package calc.u

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
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
import calc.u.ui.theme.CalcUTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Dest(val route: String, val label: String, val icon: ImageVector)

private val Dests = listOf(
    Dest("calc", "Calculator", Icons.Filled.Calculate),
    Dest("graph", "Graph", Icons.Filled.ShowChart),
    Dest("convert", "Convert", Icons.Filled.SwapHoriz),
    Dest("finance", "Finance", Icons.Filled.AttachMoney),
    Dest("math", "Math", Icons.Filled.GridOn),
    Dest("settings", "Settings", Icons.Filled.Settings)
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var settingsRepo: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val theme by settingsRepo.theme.collectAsStateWithLifecycle(initialValue = "system")
            CalcUTheme(theme = theme) {
                val nav = rememberNavController()
                val drawer = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                var route by remember { mutableStateOf("calc") }
                fun go(r: String) {
                    route = r
                    nav.navigate(r) { launchSingleTop = true; popUpTo("calc") }
                }
                ModalNavigationDrawer(
                    drawerState = drawer,
                    drawerContent = {
                        ModalDrawerSheet {
                            Text(
                                "CalcU",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(all = 16.dp).semantics { heading() }
                            )
                            Dests.forEach { d ->
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
                ) {
                    BoxWithConstraints(Modifier.fillMaxSize()) {
                        val wide = maxWidth >= 840.dp
                        Scaffold(
                            topBar = {
                                @OptIn(ExperimentalMaterial3Api::class)
                                TopAppBar(
                                    title = { Text("CalcU") },
                                    navigationIcon = {
                                        IconButton(
                                            onClick = { scope.launch { drawer.open() } }
                                        ) { Icon(Icons.Filled.Menu, contentDescription = "Open navigation") }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                                    )
                                )
                            },
                            bottomBar = {
                                if (!wide) {
                                    NavigationBar {
                                        Dests.forEach { d ->
                                            NavigationBarItem(
                                                selected = route == d.route,
                                                onClick = { go(d.route) },
                                                icon = { Icon(d.icon, contentDescription = null) },
                                                label = { Text(d.label) }
                                            )
                                        }
                                    }
                                }
                            }
                        ) { pad ->
                            androidx.compose.foundation.layout.Row(Modifier.padding(pad).fillMaxSize()) {
                                if (wide) {
                                    NavigationRail {
                                        Dests.forEach { d ->
                                            NavigationRailItem(
                                                selected = route == d.route,
                                                onClick = { go(d.route) },
                                                icon = { Icon(d.icon, contentDescription = null) },
                                                label = { Text(d.label) }
                                            )
                                        }
                                    }
                                }
                                NavHost(
                                    navController = nav,
                                    startDestination = "calc",
                                    modifier = Modifier.weight(1f).widthIn(max = 840.dp)
                                ) {
                                    composable("calc") { Centered { CalculatorScreen() } }
                                    composable("graph") { Centered { GraphScreen() } }
                                    composable("convert") { Centered { ConvertersScreen() } }
                                    composable("finance") { Centered { FinanceScreen() } }
                                    composable("math") { Centered { MathScreen() } }
                                    composable("settings") { Centered { SettingsScreen() } }
                                }
                            }
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
