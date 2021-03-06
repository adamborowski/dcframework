var Finder = require('./file-finder');
var finder = new Finder();
finder.getFiles('/Users/aborowski/Documents/dcframework-reports/node1_2_3_4_6_8', loadJson);
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
    var caseArray = [];
    for (var caseName in cases) {
        caseArray.push(cases[caseName]);
    }
    caseArray = caseArray.sort((a, b)=>Number(a.runs[0].args.endParameter) - Number(b.runs[0].args.endParameter));
    for (var _case of caseArray) {
        _case.args = _case.runs[0].args;
        var args = _case.args;
        _case.problemSize = args.endParameter;
        _case.result = _case.runs[0].result;
        _case.numNodes = Object.keys(_case.runs[0].nodeStatistics).length;
        if (_case.runs.length > 1) {
            _case.computationTime = mean(_case.runs[0].nodeStatistics[0].computationTime, _case.runs[1].nodeStatistics[0].computationTime);
        }
        else {
            _case.computationTime = _case.runs[0].nodeStatistics[0].computationTime;
        }
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

    // var SUPER_ID=271;
    //
    // var values = function (a) {
    //     return Object.keys(a).map(x=>a[x])
    // };
    //
    // var res = [caseArray[SUPER_ID]]
    // if (res.length > 1) {
    //     console.log('more precise!')
    //     console.log(res)
    // }
    // if (res.length == 0) {
    //     console.log('no results')
    // }
    // else {
    //     var run = res[0].runs[0];
    //     var statistics = values(run.nodeStatistics);
    //     console.log(statistics)
    //     console.log('compute time:', res[0].computationTime)
    //     console.log('task proceed:', statistics.map(a=>a.numTaskProcessed));
    //     console.log('sum sent to global:', statistics.reduce((res, a)=>res + a.numTaskSentToGlobal, 0))
    // }
}