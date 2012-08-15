__author__ = 'TC'
import os,os.path
import zipfile

def zip_dir(dirname,zipfilename):
    fileList = []
    if os.path.isfile(dirname):
        fileList.append(dirname)
    else :
        for root, dirs, files in os.walk(dirname):
            for name in files:
                fileList.append(os.path.join(root, name))

    zf = zipfile.ZipFile(zipfilename, "w", zipfile.zlib.DEFLATED)
    for tar in fileList:
        arcname = tar[len(dirname):]
        #print arcname
        zf.write(tar,arcname)
    zf.close()


def unzip_file(zipfilename, unziptodir):
    if not os.path.exists(unziptodir): os.mkdir(unziptodir, 0777)
    zfobj = zipfile.ZipFile(zipfilename)
    for name in zfobj.namelist():
        name = name.replace('\\','/')

        if name.endswith('/'):
            os.mkdir(os.path.join(unziptodir, name))
        else:
            ext_filename = os.path.join(unziptodir, name)
            ext_dir= os.path.dirname(ext_filename)
            if not os.path.exists(ext_dir) : os.mkdir(ext_dir,0777)
            outfile = open(ext_filename, 'wb')
            outfile.write(zfobj.read(name))
            outfile.close()

#!/usr/bin/env Python
def printFile(fileName):
# attempt to open file for reading
    global file
    try:
        file = open(fileName, 'r')
    except IOError, e:
        print "*** file open error:", e

# display contents to the screen
    for eachLine in file:
        print eachLine,
    file.close()

def readFile(fileName):
# attempt to open file for reading
    gap = os.linesep
    global file
    try:
        file = open(fileName, 'r')
    except IOError, e:
        print "*** file open error:", e

    # display contents to the screen
    all = ''
    for eachLine in file:
        all += eachLine
        all += gap
    file.close()
    return all

def writeToFile(str, fileName):
    if os.path.exists(fileName):
        print "ERROR: '%s' already exists" % fileName

    file = open(fileName, "w")
    file.write(str)
    file.close()