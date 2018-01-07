#!/usr/bin/env python

import os

# Tuples to be run. Each is of the form [[Models], [Traffic Densities],
# [Execution durations], [Speed Limits]].
ops = [[4],
       [100, 200, 400, 800],
       [100, 500, 1500, 2500],
       [15, 25, 35]]

for m in ops[0]:
    for e in ops[1]:
        for d in ops[2]:
            for s in ops[3]:
                for r in range(20):
                    os.system('java -ea -server -Xmx1000M -jar ![0] -headless -r ' + str(r) + ' -m ' + str(m) + ' -e ' + str(e) + ' -d ' + str(d) + ' -s ' + str(s) + ' -f "![1]' + 'Model_' + str(m) + '_' + '![2]' + '.log"' + ' -U ' + '![3]' + ' -D ' + '![4]' + ' -N ' + '![5]' + ' -M ' + '![6]' + ' -Z ' + '![7]' + ' -X ' + '![8]' + ' -L 0 -H 9999')
