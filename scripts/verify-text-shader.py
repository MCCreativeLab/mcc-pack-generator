#!/usr/bin/env python3
from pathlib import Path
from zipfile import ZipFile
import re,subprocess,argparse
parser=argparse.ArgumentParser(description="Validate MCC text shader against the exact Minecraft client shaders")
parser.add_argument('--client-jar', type=Path, required=True)
parser.add_argument('--vertex-shader', type=Path, help='Validate a generated pack shader, including all installed instructions')
parser.add_argument('--validator', default='glslangValidator')
parser.add_argument('--output', type=Path, default=Path('build/shader-validation'))
args=parser.parse_args()
jar=ZipFile(args.client_jar)
shader=(args.vertex_shader or (Path(__file__).resolve().parents[1]/'src/main/resources/resourcepack/shader/text.vsh')).read_text()
def includes(s):
 def read(m): return includes(jar.read('assets/minecraft/shaders/include/'+m[1]).decode())
 return re.sub(r'#include <minecraft:([^>]+)>',read,s)
root=args.output;root.mkdir(parents=True,exist_ok=True)
for label,defines in [('gui',['IS_GUI']),('gui-grayscale',['IS_GUI','IS_GRAYSCALE']),('world',[]),('see-through',['IS_SEE_THROUGH'])]:
 for ext,s in [('vert',shader),('frag',jar.read('assets/minecraft/shaders/core/text.fsh').decode())]:
  s=includes(s).replace('#version 330','#version 330\n'+'\n'.join('#define '+d for d in defines))
  # Test an actual generated instruction, not just the empty template.
  if not args.vertex_shader:
   s=s.replace('switch (int(id)) {','switch (int(id)) {\ncase 100:\n xOffset = int(guiSize.x * (-50.0/100))-10.0;\n yOffset = int(guiSize.y * (50.0/100))+5.0;\n pos.x -= guiSize.x * 0.5;\n pos.y += guiSize.y * 0.5;\n layer = 1;\n break;')
  (root/f'{label}.{ext}').write_text(s)
 p=subprocess.run([args.validator,'-l',str(root/f'{label}.vert'),str(root/f'{label}.frag')],text=True,capture_output=True)
 print(label,p.returncode,p.stdout,p.stderr)
 if p.returncode: raise SystemExit(p.returncode)
