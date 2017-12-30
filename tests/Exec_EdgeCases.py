#!/usr/bin/env python

import os


# This file executes all tests considered to be 'edge cases'.

# Mean value edges
mu = [-5, 0, 5]

# Standard deviation edges
std = [1, 5]

# Minimum value edges
minimum = [1, 4]

# Maximum value edges
maximum = [5, 9]

# Place all files to execute into a list
edge_files = set()
# Iterate over all mean edges
for U in mu:
    # Iterate over all standard deviation edges
    for D in std:
        # Iterate over all minimum edges
        for N in minimum:
            # Iterate over all maximum edges
            for X in maximum:
                params = str(U) + '_' + str(D) + '_' + str(N) + '_' + str(X)

                for filename in os.listdir(os.getcwd()):
                    root, ext = os.path.splitext(filename)
                    if root.endswith(params) and ext == '.py':
                        edge_files.add(filename)

# Execute all gathered files
for filename in edge_files:
    print('\n\n\n!!!!! Starting ' + filename)
    os.system('./' + filename)
    os.system('mv * completed/')
