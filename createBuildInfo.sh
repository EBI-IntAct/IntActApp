#!/usr/bin/env bash


VERSION=$(head -n 1 /Users/eragueneau/IdeaProjects/IntActApp/src/main/resources/buildInfo.txt)
echo "$((VERSION + 1))"> /Users/eragueneau/IdeaProjects/IntActApp/src/main/resources/buildInfo.txt
date +"%d/%m/%Y - %H:%M">> /Users/eragueneau/IdeaProjects/IntActApp/src/main/resources/buildInfo.txt
