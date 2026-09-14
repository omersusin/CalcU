// text-extra pack v1 — pure string tools, no host APIs (ES5 only).
function slugify(s) {
    var t = (s || "").toLowerCase();
    t = t.replace(/[^a-z0-9]+/g, "-").replace(/^-+|-+$/g, "");
    return t;
}
function reverseLines(s) {
    return (s || "").split("\n").map(function (l) {
        return l.split("").reverse().join("");
    }).join("\n");
}
function wordFreq(s) {
    var m = {};
    var words = (s || "").toLowerCase().match(/[a-z0-9']+/g) || [];
    words.forEach(function (w) { m[w] = (m[w] || 0) + 1; });
    return Object.keys(m).sort(function (a, b) { return m[b] - m[a]; }).slice(0, 10).map(function (w) {
        return w + ": " + m[w];
    }).join("\n");
}
