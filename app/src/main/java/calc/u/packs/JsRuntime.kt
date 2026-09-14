package calc.u.packs

import com.squareup.duktape.Duktape

// Sandboxed JS evaluation for tool packs. The engine exposes NO host bridge
// (no Java objects, no file/network access from script) — scripts are pure
// functions of string input. Each call creates and closes its own engine.
// Known limit: no execution timeout (Duktape has no interrupt API), so only
// hash-pinned catalog packs are ever evaluated; caller runs this off the UI
// thread and surfaces failures as error text.
object JsRuntime {
    fun eval(script: String, function: String, input: String): Result<String> = runCatching {
        val duktape = Duktape.create()
        try {
            duktape.evaluate(script)
            duktape.evaluate(jsCall(function, input))?.toString() ?: ""
        } finally {
            runCatching { duktape.close() }
        }
    }
}
