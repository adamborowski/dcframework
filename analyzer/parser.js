var Finder = require('./file-finder');
var finder = new Finder();
finder.getFiles('/Users/aborowski/Documents/dcframework-reports/node1', loadJson);
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

    var counter=0;
    console.log('case\t problem size\t num nodes\t tx\t randomThreshold\t o1\t o2\t num threads\t  computation time');
    cases.sort((a, b)=>a.problemSize - b.problemSize);
    for (var caseName in cases) {
        var _case = cases[caseName];
        _case.args = _case.runs[0].args;
        var args = _case.args;
        _case.problemSize = args.endParameter;
        _case.result = _case.runs[0].result;
        _case.numNodes = Object.keys(_case.runs[0].nodeStatistics).length;
        _case.computationTime = mean(_case.runs[0].nodeStatistics[0].computationTime, _case.runs[1].nodeStatistics[0].computationTime);
        console.log(`${counter++}\t ${_case.problemSize}\t ${_case.numNodes}\t ${args.maxThreshold}\t ${args.randomThreshold}\t ${args.optimizeShortReturn}\t ${args.optimizeInitialDistribution}\t ${args.numThreads}\t ${_case.computationTime}`)
    }

    var casesByProblemSize = {};

    for(var caseName in cases){
        var _case = cases[caseName];
        var problemSize = _case.problemSize;
        if(!casesByProblemSize.hasOwnProperty(problemSize)){
            casesByProblemSize[problemSize]=[];
        }
        casesByProblemSize[problemSize].push(_case);


    }
    // console.log(casesByProblemSize);


    // console.log(runsByParams)
}