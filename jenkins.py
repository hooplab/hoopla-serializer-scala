import subprocess
import sys
import sh
import time
import requests
import os
import logging

logging.basicConfig(level=logging.INFO)

logging.info("Running tests with coverage...")
proc = subprocess.Popen(["sbt", "clean", "coverage", "test"], shell=False)
proc.communicate()

if (not proc.returncode == 0):
    raise Exception("Some tests failed!")

#Assumption: the keyfile has been added to the repo at the CI server.
logging.info("Submitting test coverage to coveralls.io...")
proc = subprocess.Popen(["sbt", "coveralls"], shell=False)
proc.communicate()
