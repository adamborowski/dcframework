var Finder = require('./file-finder');
var finder = new Finder();
finder.getFiles('/Users/aborowski/Documents/dcframework-reports', loadJson);
function loadJson(files) {
    for (var path of files) {
        console.log(path);
        var config = require(path)
        var endParam = config.args.endParameter
        console.log(endParam);
    }
}