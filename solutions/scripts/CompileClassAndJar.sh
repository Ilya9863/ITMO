#!/bin/bash
dependence="../../shared/java-advanced-2023/artifacts/info.kgeorgiy.java.advanced.implementor.jar"
file=" ../java-solutions/info.kgeorgiy.ja.kurkin.implementor/*.java"
dir="../java-solutions/info/kgeorgiy/ja/kurkin/implementor"
javac -d $dir -cp $dependence $file -d .
jar -cvfm Implimentor.jar MANIFEST.MF $dir
