#!/bin/sh
#IFS=$'\n';
cat $1 | perl -p -e 's/\t*//' | perl -p -e 's/\n(\n)/\1/' | perl -p -e 's/[\t ]*U-R-L (.*)\n/'$1'|||P|=HIPERŁĄCZE("\1"/' | perl -p -e 's/N-A-M-E (.*)\n/;"\1")|/' | perl -p -e 's/A-D-R-R-E-S-S (.*)\n/\1|/' | perl -p -e's/(.*)[\n]/\1|\n/' | perl -p -e 's/T-E-L//' | grep  '.\{5,\}'



