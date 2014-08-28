#!/bin/bash
name="savant-core"
version="0.2.0-{integration}"
cp src/main/resources/amd.xml ~/.savant/cache/org/savantbuild/${name}/${version}/${name}-${version}.jar.amd
cd ~/.savant/cache/org/savantbuild/${name}/${version}/
md5sum ${name}-${version}.jar > ${name}-${version}.jar.md5
md5sum ${name}-${version}.jar.amd > ${name}-${version}.jar.amd.md5
md5sum ${name}-${version}-src.jar > ${name}-${version}-src.jar.md5
