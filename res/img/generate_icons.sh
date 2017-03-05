#!/bin/bash

#    ExamCode - generate_icons.sh
#    Copyright (c) 2017 Henning Kerstan und Roman Ortmann GbR.
#
#    Licensed under the Apache License, Version 2.0 (the "License");
#    you may not use this file except in compliance with the License.
#    You may obtain a copy of the License at

#       http://www.apache.org/licenses/LICENSE-2.0

#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.

rm -rf 16x16
mkdir -p 16x16

rm -rf 32x32
mkdir -p 32x32

rm -rf 48x48
mkdir -p 48x48

rm -rf 128x128
mkdir -p 128x128

rm -rf 256x256
mkdir -p 256x256

cd originals

for f in *.png 
do
    echo "Processing $f"
    convert -resize 16x16 "$f" "../16x16/$f"
    convert -resize 32x32 "$f" "../32x32/$f"
    convert -resize 48x48 "$f" "../48x48/$f"
    convert -resize 128x128 "$f" "../128x128/$f"
    convert -resize 256x256 "$f" "../256x256/$f"
done

