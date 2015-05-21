#!/bin/bash
i=0

getFiles() {
   Rep="$1"
   for item in $Rep/*
   do [ -f "$item" ] && FILE[$i]="$item"
      i=$(($i+1))
      [ -d "$item" ] && getFiles "$item"
   done
}


fixEncoding(){
   for i in ${FILE[@]}
   do
	encoding=$(egrep -woi "iso-8859-1|utf-8" -m 1 $i)
	if [ $i != *"utf"* ] || [ $i != *"iso"* ] ;
	then
		mv $i ${i%.*}_${encoding}.html
	fi
   done
}


[ -d "${1:-cri}" ] && getFiles ${1:-cri}
fixEncoding

