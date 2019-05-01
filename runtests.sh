#!/bin/sh

DIR=RES`date +%Y%m%d%Hh%M`
mkdir $DIR

# =======================================================
BDD=
mv lib/libbuddy.so lib/libbuddy.so.discarded
mv lib/libbuddy.jnilib lib/libbuddy.jniilb.discarded
mv lib/buddy.dll lib/buddy.dll.discarded

sh testAFMU-NOP.sh
mv AFMU-NOP.tsv $DIR/AFMU-NOP$BDD.tsv

sh testSA-NOP.sh
mv SA-NOP.tsv $DIR/SA-NOP$BDD.tsv

mv lib/javabdd-1.0b2.jar .
sh testTS-NOP.sh
mv TS-NOP.tsv $DIR/TS-NOP$BDD.tsv
mv javabdd-1.0b2.jar lib

sh testAFMU-RDFS.sh
mv AFMU-RDFS.tsv $DIR/AFMU-RDFS$BDD.tsv

sh testAFMU-UCQ.sh
mv AFMU-UCQ.tsv $DIR/AFMU-UCQ$BDD.tsv

mv lib/javabdd-1.0b2.jar .
sh testTS-RDFS.sh
mv TS-RDFS.tsv $DIR/TS-RDFS$BDD.tsv
mv javabdd-1.0b2.jar lib

mv lib/javabdd-1.0b2.jar .
mv lib/lmusolver.jar lib/lmusolver.jar.web
mv lib/lmusolver.jar.pg lib/lmusolver.jar
sh testTS-UCQ.sh
mv TS-UCQ.tsv $DIR/TS-UCQ$BDD.tsv
mv lib/lmusolver.jar lib/lmusolver.jar.pg
mv lib/lmusolver.jar.web lib/lmusolver.jar
mv javabdd-1.0b2.jar lib

# =======================================================
BDD=-NATIV
mv lib/libbuddy.so.discarded lib/libbuddy.so
mv lib/libbuddy.jniilb.discarded lib/libbuddy.jnilib
mv lib/buddy.dll.discarded lib/buddy.dll

sh testAFMU-NOP.sh
mv AFMU-NOP.tsv $DIR/AFMU-NOP$BDD.tsv

sh testSA-NOP.sh
mv SA-NOP.tsv $DIR/SA-NOP$BDD.tsv

mv lib/javabdd-1.0b2.jar .
sh testTS-NOP.sh
mv TS-NOP.tsv $DIR/TS-NOP$BDD.tsv
mv javabdd-1.0b2.jar lib

sh testAFMU-RDFS.sh
mv AFMU-RDFS.tsv $DIR/AFMU-RDFS$BDD.tsv

sh testAFMU-UCQ.sh
mv AFMU-UCQ.tsv $DIR/AFMU-UCQ$BDD.tsv

mv lib/javabdd-1.0b2.jar .
sh testTS-RDFS.sh
mv TS-RDFS.tsv $DIR/TS-RDFS$BDD.tsv
mv javabdd-1.0b2.jar lib

mv lib/javabdd-1.0b2.jar .
sh testTS-UCQ.sh
mv TS-UCQ.tsv $DIR/TS-UCQ$BDD.tsv
mv javabdd-1.0b2.jar lib
