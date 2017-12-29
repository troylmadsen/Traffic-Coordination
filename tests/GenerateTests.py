#!/usr/bin/env python

import os

# This file generates all combinations of test inputs for the desired
# value ranges in the current directory. Old tests are remove before
# writing new tests.


# Template test file to write to other files
# This assumes the template is in the same directory
template_path = os.getcwd() + '/Template.py'

# Read in the template for writing new tests
template = ''
with open(template_path, 'r') as template_file:
    template = template_file.read()


# Removing old test files
os.system('rm -rf Run*')


# Mean values to use for adjustment
mu = range(-5, 5 + 1)

# Standard deviations to use for adjustment
std = range(1, 5 + 1)

# Minimum value to adjust speed by
minimum = range(1, 4 + 1)

# Maximum value to adjust speed by
maximum = range(5, 9 + 1)

# Index of test number
index = 0

# Iterate over all means
for U in mu:
    # Iterate over all standard deviations
    for D in std:
        # Iterate over all minimums
        for N in minimum:
            # Iterate over all maximums
            for X in maximum:
                index += 1

                params = str(U) + '_' + str(D) + '_' + str(N) + '_' + str(X)

                filename = 'Run_' + str(index) + '_' + params + '.py'

                with open(filename, 'w+') as testfile:
                    content = template.replace('![0]', str(params))
                    content = content.replace('![1]', str(U))
                    content = content.replace('![2]', str(D))
                    content = content.replace('![3]', str(N))
                    content = content.replace('![4]', str(X))
                    content = content.replace('![5]', str(N))
                    content = content.replace('![6]', str(X))
                    testfile.write(content)

                os.system('chmod 700 ' + str(filename))
