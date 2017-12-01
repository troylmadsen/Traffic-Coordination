#!/usr/bin/env python

import os

# Removing old log files
os.system('rm Model?.log')

# Tuples to be run. Each is of the form [[Models], [Traffic Densities],
# [Execution durations], [Speed Limits]].
ops = [[1, 2, 3, 4],
       [100, 200, 400, 800],
       [100, 500, 1500, 2500],
       [15, 25, 35]]

for m in ops[0]:
    for e in ops[1]:
        for d in ops[2]:
            for s in ops[3]:
                for r in range(20):
                    os.system('java -ea -server -Xmx1000M -jar aim4-root/aim4.jar -headless -r ' + str(r) + ' -m ' + str(m) + ' -e ' + str(e) + ' -d ' + str(d) + ' -s ' + str(s) + ' -f "Model' + str(m) + '.log"' + ' -U 0 -D 10 -N 0 -M 9999 -Z 0 -X 9999 -L 0 -H 9999')
