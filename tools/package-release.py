#!/usr/bin/env python3
"""Package only original addon content; dependencies remain official pinned downloads."""
import hashlib, json, pathlib, zipfile
root=pathlib.Path(__file__).resolve().parent.parent
dist=root/'dist';dist.mkdir(exist_ok=True)
manifest=json.loads((root/'dependency-manifest.json').read_text())
create=manifest['create']
jar=root/'build/libs/clockwork-tides-1.0.0.jar'
index={'formatVersion':1,'game':'minecraft','versionId':'1.0.0','name':'Clockwork Tides','summary':'Create fishing rods and open-water salvage','dependencies':{'minecraft':'1.21.1','neoforge':'21.1.219'},'files':[{'path':'mods/'+create['filename'],'hashes':{'sha1':create['sha1'],'sha512':create['sha512']},'env':{'client':'required','server':'required'},'downloads':[create['url']],'fileSize':create['size_bytes']}]}
pack=dist/'clockwork-tides-1.0.0.mrpack'
with zipfile.ZipFile(pack,'w',zipfile.ZIP_DEFLATED) as z:
 z.writestr('modrinth.index.json',json.dumps(index,indent=2)+'\n')
 z.write(jar,'overrides/mods/'+jar.name)
 z.write(root/'LICENSE','overrides/clockwork-tides-LICENSE.txt')
archive=dist/'clockwork-tides-1.0.0-neoforge-1.21.1.zip'
with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED) as z:
 z.write(jar,'mods/'+jar.name)
 z.write(pack,pack.name)
 for f in ['README.md','LICENSE','THIRD-PARTY-NOTICES.md','CHANGELOG.md','dependency-manifest.json','docs/INSTALL-RU.md','docs/RECIPES.md','docs/MECHANICS.md','docs/TESTING.md','tools/fetch-create.py','tools/fetch-create.ps1']:
  z.write(root/f,f)
 z.writestr('START-HERE.txt','Clockwork Tides 1.0.0\n\nEasiest: import the included .mrpack in Prism Launcher or Modrinth App. The launcher fetches pinned NeoForge/Create from official sources. A licensed Minecraft account is required.\n\nManual: install NeoForge 21.1.219 for Minecraft 1.21.1; copy our jar from mods/; download official Create 6.0.10 using the supplied checksum-verifying script or the link in README.\n\nCreate is not rehosted inside this ZIP. It is downloaded unchanged from official Modrinth CDN.\nRussian instructions: docs/INSTALL-RU.md\n')
files=[jar,root/'build/libs/clockwork-tides-1.0.0-sources.jar',pack,archive,root/'docs/media/clockwork-tides-60sec.mp4']
(dist/'SHA256SUMS.txt').write_text(''.join(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+p.name+'\n' for p in files))
for p in [pack,archive]:
 with zipfile.ZipFile(p) as z: assert z.testzip() is None
 print(p.name,p.stat().st_size)
