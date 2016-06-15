import sys
from colored import fg, attr

import  modules.run
from modules.registers import *

node_index = sys.argv[1]
if node_index != '0':
    master_url = 'tcp://' + sys.argv[2] + ':61616'
    output_base = "/dev/null"
else:
    output_base = sys.argv[2]
problem_size = [1000, 10000, 10000]
tx = [1000, 4000, 16000]
# ti = 0.5*tx
tr = [0.5, 0.8]

op1 = [False, True]
op2 = [False, True]
num_threads = [1, 2, 4, 8]
# num nodes done manually

manager = OverflowManager([
    CyclicRegister(problem_size),
    CyclicRegister(tx),
    CyclicRegister(tr),
    CyclicRegister(op1),
    CyclicRegister(op2),
    CyclicRegister(num_threads)])


def get_cmd_arguments(p):
    a = []
    a.extend(['-i', str(node_index)])
    if node_index == '0':
        a.extend(['-s', '0', '-e', str(p['problem_size'])])
        a.extend(['-tx', str(p['tx'])])
        a.extend(['-ti', str(p['tx'] / 2)])
        a.extend(['-tr', str(p['tr'])])
        if p['op1']:
            a.append('-o1')
        if p['op2']:
            a.append('-o2')
        a.extend(['-n', str(p['num_threads'])])
    else:
        a.insert(0, '-u ' + master_url)
    cmd = ' '.join(a)
    return cmd


col1 = fg('yellow')
res = attr('reset')


def do_test(p, indices):
    arguments = get_cmd_arguments(p)
    for i in range(0, 4):
        file_name = output_base+'/node.' + node_index + '.' + '-'.join(indices) + '.run.' + str(i) + '.json'
        args = '-r ' + file_name + ' ' + arguments
        cmd = './run.sh -r ' + file_name + ' ' + arguments+' -l info'
        print(col1 + cmd + res)
        modules.run.run(cmd)


while True:
    values = manager.getArray()
    parameters = {
        'problem_size': values[0],
        'tx': values[1],
        'tr': values[2],
        'op1': values[3],
        'op2': values[4],
        'num_threads': values[5]
    }

    do_test(parameters, manager.getIndices())

    manager.next()
    if manager.isOverflowOccured():
        break
