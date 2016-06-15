import subprocess
import sys
# Define a function for running commands and capturing stdout line by line
# (Modified from Vartec's solution because it wasn't printing all lines)
def runProcess(exe):
    p = subprocess.Popen(exe, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    return iter(p.stdout.readline, b'')


def run(cmd):

    # Run the command and capture the output line by line.
    for line in runProcess(cmd.split()):
        # Eliminate leading and trailing whitespace
        line.strip()
        sys.stdout.write(line.decode('UTF-8'))