import json
import random
import sys
import shutil
import os,sys


filename = '/home_structure.json'
fullpath = (os.path.dirname(os.path.realpath(__file__)) + os.sep) + filename

if __name__ == '__main__':
    with open(fullpath, "r") as f:
    	shutil.copyfileobj(f, sys.stdout)