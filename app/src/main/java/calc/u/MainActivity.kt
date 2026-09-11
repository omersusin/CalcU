package calc.u

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import calc.u.ui.screens.CalculatorScreen
import calc.u.ui.screens.ConvertersScreen
import calc.u.ui.screens.FinanceScreen
import calc.u.ui.screens.GraphScreen
import calc.u.ui.screens.MathScreen
import calc.u.ui.theme.CalcUTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalcUTheme {
                val nav = rememberNavController()
                val drawer = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                var route by remember { mutableStateOf("calc") }
                val items = listOf("calc" to "Calculator", "graph" to "Graph", "convert" to "Converters", "finance" to "Finance", "math" to "Math+Geometry+Health")
                ModalNavigationDrawer(
                    drawerState = drawer,
                    drawerContent = {
                        ModalDrawerSheet {
                            Text("CalcU — Fluent", modifier = Modifier.padding(all = 16.dp))
                            items.forEach { (r, t) ->
                                NavigationDrawerItem(label = { Text(t) }, selected = r == route, onClick = { route = r; nav.navigate(r); scope.launch { drawer.close() } })
                            }
                        }
                    }
                ) {
                    Scaffold(topBar = {
                        @OptIn(ExperimentalMaterial3Api::class)
                        TopAppBar(title = { Text("CalcU") }, navigationIcon = {
                            IconButton({ scope.launch { drawer.open() } }) { Icon(Icons.Filled.Menu, null) }
                        })
                    }) { pad ->
                        NavHost(nav, startDestination = "calc", Modifier.padding(pad)) {
                            composable("calc") { CalculatorScreen() }
                            composable("graph") { GraphScreen() }
                            composable("convert") { ConvertersScreen() }
                            composable("finance") { FinanceScreen() }
                            composable("math") { MathScreen() }
                        }
                    }
                }
            }
        }
    }
}
