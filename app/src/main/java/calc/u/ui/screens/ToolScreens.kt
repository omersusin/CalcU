package calc.u.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import calc.u.core.Currency
import calc.u.core.Finance
import calc.u.core.Geometry
import calc.u.core.HealthDate
import calc.u.core.Units
import calc.u.core.Engine

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConvertersScreen() {
    var input by remember { mutableStateOf("1") }
    var cat by remember { mutableStateOf("length") }
    val v = input.toDoubleOrNull() ?: 0.0
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(input, { input = it }, label = { Text("Value") })
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("length","mass","volume","temp","area","speed","pressure","energy","power","data").forEach {
                FilterChip(it == cat, { cat = it }, { Text(it) })
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val map: Map<String, Units.UnitDef>? = when (cat) {
                "length" -> Units.length; "mass" -> Units.mass; "volume" -> Units.volume
                "area" -> Units.area; "speed" -> Units.speed; "pressure" -> Units.pressure
                "energy" -> Units.energy; "power" -> Units.power; "data" -> Units.data
                else -> null
            }
            if (cat == "temp") {
                item {
                    Text("C=${Units.convertTemp(v,"C","C")} F=${Units.convertTemp(v,"C","F")} K=${Units.convertTemp(v,"C","K")}")
                    Text("Enter Celsius above; ft+in: ${Units.ftInToCm(5.0, 9.0)} cm for 5ft 9in")
                    Text("Bases: BIN=${Units.fromBase(v,2)} OCT=${Units.fromBase(v,8)} HEX=${Units.fromBase(v,16)} ROMAN=${try { Units.toRoman(v.toInt()) } catch(e:Exception){""}}")
                }
            } else if (map != null) {
                val base = map.values.first()
                map.forEach { (name, def) ->
                    val out = try { Units.convert(v, base, def) } catch(e:Exception){0.0}
                    item { Text("$v ${map.keys.first()} = $out $name") }
                }
            }
        }
    }
}

@Composable
fun FinanceScreen() {
    var bill by remember { mutableStateOf("100") }
    var tipP by remember { mutableStateOf("15") }
    var split by remember { mutableStateOf("2") }
    var principal by remember { mutableStateOf("10000") }
    var rate by remember { mutableStateOf("5") }
    var months by remember { mutableStateOf("24") }
    val (tipAmt, grand, per) = Finance.tip(bill.toDoubleOrNull()?:0.0, tipP.toDoubleOrNull()?:0.0, split.toIntOrNull()?:1)
    val emi = Finance.emi(principal.toDoubleOrNull()?:0.0, rate.toDoubleOrNull()?:0.0, months.toIntOrNull()?:0)
    val (si, siTotal) = Finance.simple(principal.toDoubleOrNull()?:0.0, rate.toDoubleOrNull()?:0.0, 2.0)
    val ci = Finance.compound(principal.toDoubleOrNull()?:0.0, rate.toDoubleOrNull()?:0.0, 2.0)
    LazyColumn(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text("Tip & Split", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(bill, { bill = it }, label = { Text("Bill") })
            OutlinedTextField(tipP, { tipP = it }, label = { Text("Tip %") })
            OutlinedTextField(split, { split = it }, label = { Text("Split") })
            Text("Tip=$tipAmt Total=$grand Per-person=$per")
            Divider()
            Text("Loan EMI", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(principal, { principal = it }, label = { Text("Principal") })
            OutlinedTextField(rate, { rate = it }, label = { Text("Annual %") })
            OutlinedTextField(months, { months = it }, label = { Text("Months") })
            Text("EMI=$emi")
            Text("Simple 2y: interest=$si total=$siTotal | Compound 2y: $ci")
            Divider()
            Text("Tax incl/excl: net=${Finance.withTax(110.0,10.0,true)} gross=${Finance.withTax(100.0,10.0,false)}")
        }
    }
}

@Composable
fun MathScreen() {
    var a by remember { mutableStateOf("12") }
    var b by remember { mutableStateOf("18") }
    var quad by remember { mutableStateOf("1,-3,2") }
    val av = a.toLongOrNull() ?: 0L; val bv = b.toLongOrNull() ?: 0L
    val parts = quad.split(",").mapNotNull { it.toDoubleOrNull() }
    val roots = if (parts.size == 3) Engine.solveQuadratic(parts[0], parts[1], parts[2]) else emptyList()
    LazyColumn(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text("GCD=${Engine.gcd(av,bv)} LCM=${try{Engine.lcm(av,bv)}catch(e:Exception){0}} Prime(a)=${Engine.isPrime(av)} nCr=${try{Engine.nCr(av,bv)}catch(e:Exception){0}} nPr=${try{Engine.nPr(av,bv)}catch(e:Exception){0}}")
            OutlinedTextField(a, { a = it }, label = { Text("a") })
            OutlinedTextField(b, { b = it }, label = { Text("b") })
            OutlinedTextField(quad, { quad = it }, label = { Text("quad a,b,c") })
            Text("Quadratic roots: $roots")
            Text("Fraction(0.3333)=${Engine.toFraction(0.3333)} Circle r=5 area=${Geometry.circleArea(5.0)} Sphere vol=${Geometry.sphereVolume(3.0)}")
            Text("BMI(70kg,175cm)=${HealthDate.bmi(70.0,175.0)} Ohm(V=12,R=4)=${HealthDate.ohm(12.0,null,4.0)}")
            Text("Currencies: ${Currency.codes.take(12).joinToString()} ... ${Currency.codes.size} total. 100 USD->EUR ≈ ${Currency.convert(100.0, 1.0, 0.92)}")
        }
    }
}
