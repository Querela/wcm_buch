import os, sys

FILE_PREFIX = 'data_'
count = batch = numfiles = bufsize = 0
outfolder = "/home/maimiyll/CMPrak/splitted_data"
buf = []
MB = pow(2,20)
max_buf = (10*MB)

def write_file(FILE_PREFIX,numfiles,buf):
  outfile = '%s/%s%s' % (outfolder,FILE_PREFIX, numfiles)
  F = open (outfile, 'w')
  F.write('\n')
  F.write(''.join(buf))
  F.close()
  # sys.stderr.write('Wrote file named %s\n' % outfile)

while True:
  line = sys.stdin.readline()
  # to assign that next file will start with '<http://'
  if line.startswith('<http://'):
    # count : number of items
    count += 1
    batch += 1
    # buf keeps on appending until 5MB is reached
    # then write to file
    if bufsize > max_buf:
      batch = 0
      write_file(FILE_PREFIX,numfiles,buf)
      numfiles += 1
      buf = []
      bufsize = 0
  buf.append(line)
  bufsize += len(line)
  if not line: break
  # write the rest (<5MB) in last file
write_file(FILE_PREFIX,numfiles,buf)
print(count, 'found')
