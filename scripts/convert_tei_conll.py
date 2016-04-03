# Script for converting XML-TEI to CONLL format
# Author: Barbara McGillivray
# Date: 14/02/2016
# Version 1.0

# Import modules:

import re
import os
from os.path import isfile, join#, splitext
import sys
import glob

reload(sys)  
sys.setdefaultencoding('utf8')

# Read data:
# Output of MorphAdorner
directory = 'C:\\files\\'


files = os.listdir(directory)
for file in files:
    if file.endswith("Dury_Hartlib_1628.xml"):
        print "file:"+str(file)
        input = open(directory + file)
        file_name = file.replace(".xml", "")
        out = open(directory +"conll\\" + file_name + ".conll", 'w')

        # parse XML:
        
        for line in input:
            # find words:
            word_matcher = re.match(r' +?<w .*lem=\"(.*?)\" pos=\"(.*?)\" .*xml:id=\"(.*?)\".*>(.+?)<\/w>', line)
            if word_matcher:
                id = word_matcher.group(3)
                form = word_matcher.group(4)
                form = form.encode('UTF-8')
                lemma = word_matcher.group(1)
                lemma = lemma.encode('UTF-8')
                pos = word_matcher.group(2)
                # print output:
                out.write(str(id) + "\t" + str(form) + "\t" + str(lemma) + "\t" + str(pos) + "\n")
        
            # find sentence endings:
            sentence_matcher = re.match(r' +?<pc .*unit=\"sentence\".*?>.*<\/pc>', line)
            if sentence_matcher:
                print "yes"
                out.write("\n")
            
            
        input.close()
        out.close()
