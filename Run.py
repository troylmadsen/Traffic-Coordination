#!/usr/bin/env python

import os

# Tuples to be run. Each is of the form [duration, model].
ops = [[100, 3],
       [200, 3],
       [400, 3]]

for o in ops:
    for r in range(20):
        os.system('java -ea -server -Xmx1000M -jar aim4.jar -headless -e ' + str(o[0]) + ' -r ' + str(r) + ' -m ' + str(o[1]) + ' -f "Model' + str(o[1]) + '.log"')
