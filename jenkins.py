import subprocess
import sys
import sh
import time
import requests
import os
import logging

logging.basicConfig(level=logging.INFO)

logging.info("Running tests...")
proc = subprocess.Popen(["sbt", "test"], shell=False)
proc.communicate()

if (not proc.returncode == 0):
    raise Exception("sbt assembly failed!")
