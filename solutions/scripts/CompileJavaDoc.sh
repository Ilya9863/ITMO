#!/bin/bash
dependence="../../shared/java-advanced-2023/artifacts/info.kgeorgiy.java.advanced.implementor.jar"
file=" ../java-solutions/info.kgeorgiy.ja.kurkin.implementor/Implementor.java"
javadoc -d ../javadoc -private -cp $dependence $file

