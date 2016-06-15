var Finder = require('./file-finder');
var finder = new Finder();
finder.getFiles('/Users/aborowski/Documents/dcframework-reports', loadJson);
var fileReg = /(.*)\.run\.(\d)\.json/;

function mean(a, b) {
    return (a + b) / 2;
}

function loadJson(files) {
    var cases = {};

    for (var path of files) {

        var testPart = path.match(fileReg)[1];

        if (!cases.hasOwnProperty(testPart)) {
            cases[testPart] = {runs: []};
        }

        var config = require(path)
        cases[testPart].runs.push(config);



    }

    for (var caseName in cases) {
        var _case = cases[caseName];
        _case.args = _case.runs[0].args;
        _case.result = _case.runs[0].result;
        _case.computationTime = mean(_case.runs[0].nodeStatistics[0].computationTime, _case.runs[1].nodeStatistics[0].computationTime);
    }

    var casesByProblemSize = {};
    for(var caseName in cases){
        var _case = cases[caseName];
        var problemSize = _case.args.endParameter;
        if(!casesByProblemSize.hasOwnProperty(problemSize)){
            casesByProblemSize[problemSize]=[];
        }
        casesByProblemSize[problemSize].push(_case);
    }
    console.log(casesByProblemSize);

    // console.log(runsByParams)
}