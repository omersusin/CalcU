// num-extra pack v1 — pure number tools, no host APIs (ES5 only).
function toRoman(s) {
    var n = parseInt((s || "").trim(), 10);
    if (isNaN(n) || n < 1 || n > 3999) return "1-3999 only";
    var table = [[1000, "M"], [900, "CM"], [500, "D"], [400, "CD"], [100, "C"], [90, "XC"], [50, "L"], [40, "XL"], [10, "X"], [9, "IX"], [5, "V"], [4, "IV"], [1, "I"]];
    var out = "";
    for (var i = 0; i < table.length; i++) {
        while (n >= table[i][0]) { out += table[i][1]; n -= table[i][0]; }
    }
    return out;
}
function fromRoman(s) {
    var t = (s || "").toUpperCase().replace(/[^MDCLXVI]/g, "");
    if (!t) return "empty";
    var map = {M: 1000, D: 500, C: 100, L: 50, X: 10, V: 5, I: 1};
    var total = 0, prev = 0;
    for (var i = t.length - 1; i >= 0; i--) {
        var v = map[t.charAt(i)];
        if (v < prev) total -= v; else { total += v; prev = v; }
    }
    return String(total);
}
function ordinal(s) {
    var n = parseInt((s || "").trim(), 10);
    if (isNaN(n)) return "not a number";
    var a = Math.abs(n) % 100, b = Math.abs(n) % 10;
    var suf = (a >= 11 && a <= 13) ? "th" : (b === 1 ? "st" : (b === 2 ? "nd" : (b === 3 ? "rd" : "th")));
    return String(n) + suf;
}
function factors(s) {
    var n = parseInt((s || "").trim(), 10);
    if (isNaN(n) || n < 1 || n > 1000000000) return "1-1000000000 only";
    var out = [];
    for (var i = 1; i * i <= n; i++) {
        if (n % i === 0) { out.push(i); if (i * i !== n) out.push(n / i); }
    }
    out.sort(function (a, b) { return a - b; });
    return out.join(", ");
}
