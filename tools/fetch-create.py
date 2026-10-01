#!/usr/bin/env python3
"""Download only the pinned, official Create release after verifying its SHA-256."""
import hashlib
import pathlib
import sys
import urllib.request

URL = 'https://cdn.modrinth.com/data/LNytGWDc/versions/UjX6dr61/create-1.21.1-6.0.10.jar'
EXPECTED = 'ef87fe5709f1ba1f5b8bb20a2925b5afb4669e178fd6d8bf10c167759eefe37a'
DEST = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else 'mods') / 'create-1.21.1-6.0.10.jar'

def main():
    DEST.parent.mkdir(parents=True, exist_ok=True)
    if DEST.exists():
        if hashlib.sha256(DEST.read_bytes()).hexdigest() == EXPECTED:
            print(f'Already verified: {DEST}')
            return
        raise SystemExit(f'Refusing to overwrite unexpected existing file: {DEST}')
    request = urllib.request.Request(URL, headers={'User-Agent':'ClockworkTides/1.0.0 (dependency installer)'})
    with urllib.request.urlopen(request, timeout=120) as response:
        content = response.read()
    if hashlib.sha256(content).hexdigest() != EXPECTED:
        raise SystemExit('Checksum mismatch; nothing installed. Download the pinned release from Modrinth manually.')
    temporary = DEST.with_suffix('.jar.part')
    temporary.write_bytes(content)
    temporary.replace(DEST)
    print(f'Installed and verified: {DEST}')

if __name__ == '__main__':
    main()
