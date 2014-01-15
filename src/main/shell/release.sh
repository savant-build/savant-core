#!/bin/bash
mkdir -p ~/.savant/cache/org/savantbuild/savant-core/0.1.0-\{integration\}/
cp build/jars/*.jar ~/.savant/cache/org/savantbuild/savant-core/0.1.0-\{integration\}/
cp src/main/resources/amd.xml ~/.savant/cache/org/savantbuild/savant-core/0.1.0-\{integration\}/savant-core-0.1.0-\{integration\}.jar.amd
cd ~/.savant/cache/org/savantbuild/savant-core/0.1.0-\{integration\}/
md5sum savant-core-0.1.0-\{integration\}.jar > savant-core-0.1.0-\{integration\}.jar.md5
md5sum savant-core-0.1.0-\{integration\}.jar.amd > savant-core-0.1.0-\{integration\}.jar.amd.md5
md5sum savant-core-0.1.0-\{integration\}-src.jar > savant-core-0.1.0-\{integration\}-src.jar.md5
