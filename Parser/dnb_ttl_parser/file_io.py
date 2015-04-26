import os
import csv

## walk all files under path
def get_infiles(inPath):
    infiles = []
    for root, dirs, files in os.walk(inPath):
        files = [f for f in files if not f[0] == '.']
        for filename in files:
            infile = os.path.join(root, filename)
            infiles.append(infile)
    return infiles

def read_text(infile):
    with open(infile,'r',encoding='utf8') as f:
        text = f.read()
        return text

def write_text(outfile,data):
    with open(outfile,'a',encoding='utf8') as f: # a for append
        f.write(data)
