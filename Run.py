#!/usr/bin/env python

import os

# Removing old log files 0 - 999
os.system('rm Model*.log')

# Tuples to be run. Each is of the form [[Models], [Traffic Densities],
# [Execution durations], [Speed Limits]].
ops = [[4],
       [100, 200, 400, 800],
       [100, 500, 1500, 2500],
       [15, 25, 35]]

# Limits of the speed adjustment curves
# [[Means], [Standard Deviations], [[maxRed, minRed, minInc, maxInc]]]
limits = [[0],
          [8, 10, 12],
          [[5, 15, 5, 15], [0,15, 0, 15], [0, 20, 0, 20]]]

for mean in limits[0]:
    for std in limits[1]:
        for lims in limits[2]:
            for m in ops[0]:
                for e in ops[1]:
                    for d in ops[2]:
                        for s in ops[3]:
                            for r in range(20):
                                os.system('java -ea -server -Xmx1000M -jar aim4-root/aim4.jar -headless -r ' + str(r) + ' -m ' + str(m) + ' -e ' + str(e) + ' -d ' + str(d) + ' -s ' + str(s) + ' -f "Model_' + str(m) + '_' + str(mean) + '_' + str(std) + '.log"' + ' -U ' + str(mean) + ' -D ' + str(std) + ' -N ' + str(lims[0]) + ' -M ' + str(lims[1]) + ' -Z ' + str(lims[2]) + ' -X ' + str(lims[3]) + ' -L 0 -H 9999')
