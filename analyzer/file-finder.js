var FindFiles = require("node-find-files");
class Finder {
    getFiles(rootDir, callback) {
        var lines = [];
        var finder = new FindFiles({
            rootFolder: rootDir,
            filterFunction: function (a) {
                return a.endsWith('.json');
            }
        });

        finder.on("match", function (strPath, stat) {
            lines.push(strPath);
        })
        finder.on("complete", function () {
            callback(lines);
        })
        finder.on("patherror", function (err, strPath) {
            console.log("Error for Path " + strPath + " " + err)  // Note that an error in accessing a particular file does not stop the whole show
        })
        finder.on("error", function (err) {
            console.log("Global Error " + err);
        })
        finder.startSearch();
    }
}
module.exports = Finder;