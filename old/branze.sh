#!/bin/sh
wget "http://www.zumi.pl/lista-firm/$1-$2" -q 0 -O t1.html
grep "<li itemprop=\"itemListElement\">" t1.html | sed 's/[ \t]*<li itemprop=\"itemListElement\"><a href=\"/http:\/\/zumi.pl/' | sed 's/">.*//' > adresy.html
#mamy adresy stron z ulicami
for line in $(cat adresy.html) 
do
 time=$(grep -m1 -ao '[0-5]' /dev/urandom | sed s/0/10/ | head -n1)

 echo "Pobieram adres: $line..." 1>&2

 wget $line -q 0 -O - | grep "<a itemprop=\"\|<span itemprop=\"" | sed 's/[ \t]*<a itemprop="name" href="/U-R-L http:\/\/www.zumi.pl/' | sed 's/#homePage">/\nN-A-M-E /' | sed 's/<a itemprop=\"url".*//' | sed 's/[ \t]*<a itemprop.*">/A-D-R-R-E-S-S /' | sed 's/<\/a><br \/>//' | sed 's/<\/a>//'| sed 's/[ \t]*<span itemprop="telephone">/T-E-L /' | sed 's/&nbsp;/ /' | sed 's/<\/span>/\n/' #>> $3.raw

 echo "$time - sekund oczekiwania..." 1>&2

 sleep $time

done
#dos2unix $3.raw
#sh convert.sh $3.raw $2 > $3
