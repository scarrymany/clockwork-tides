#!/usr/bin/env python3
"""Exact recipe tutorial diagrams, generated from shipped JSON. No gameplay files are changed.
Requires Pillow, numpy and CairoSVG. Third-party artwork is used only inside explanatory diagrams.
"""
import collections,html,io,json,pathlib,subprocess,zipfile
from PIL import Image
ROOT=pathlib.Path(__file__).resolve().parent.parent
OUT=ROOT/'docs/recipes';OUT.mkdir(exist_ok=True)
CREATE=ROOT/'dependency-cache/create-1.21.1-6.0.10.jar'
MINECRAFT=ROOT/'.gradle-home/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar'
NAMES={
 'create:andesite_alloy':('Andesite Alloy','Андезитовый сплав'),
 'create:cogwheel':('Cogwheel','Шестерня'),
 'minecraft:string':('String','Нить'),
 'minecraft:fishing_rod':('Fishing Rod','Удочка'),
 'create:copper_sheet':('Copper Sheet','Медный лист'),
 'create:polished_rose_quartz':('Polished Rose Quartz','Полированный розовый кварц'),
 'create:brass_sheet':('Brass Sheet','Латунный лист'),
 'create:precision_mechanism':('Precision Mechanism','Механизм точности'),
 'clockwork_tides:andesite_rod':('Andesite Fishing Rod','Андезитовая удочка'),
 'clockwork_tides:copper_rod':('Copper Fishing Rod','Медная удочка'),
 'clockwork_tides:brass_rod':('Brass Fishing Rod','Латунная удочка')}

def icon(item):
 ns,name=item.split(':')
 if ns=='clockwork_tides':return Image.open(ROOT/f'src/main/resources/assets/{ns}/textures/item/{name}.png').convert('RGBA')
 if item=='create:cogwheel':return render_cogwheel()
 with zipfile.ZipFile(CREATE if ns=='create' else MINECRAFT) as z:
  return Image.open(io.BytesIO(z.read(f'assets/{ns}/textures/item/{name}.png'))).convert('RGBA')

def pixel_svg(im,x,y,size):
 # Explicit pixel runs keep every pixel sharp in browsers and in PNG exports.
 w,h=im.size;out=[f'<g transform="translate({x} {y}) scale({size/w})" shape-rendering="crispEdges">']
 for yy in range(h):
  xx=0
  while xx<w:
   color=im.getpixel((xx,yy));end=xx+1
   while end<w and im.getpixel((end,yy))==color:end+=1
   if color[3]:
    out.append(f'<rect x="{xx}" y="{yy}" width="{end-xx}" height="1" fill="#{color[0]:02x}{color[1]:02x}{color[2]:02x}" opacity="{color[3]/255:.4f}"/>')
   xx=end
 return ''.join(out)+'</g>'

def render_cogwheel():
 import math,numpy as np
 with zipfile.ZipFile(CREATE) as z:
  model=json.loads(z.read('assets/create/models/block/cogwheel.json'))
  textures={k:np.array(Image.open(io.BytesIO(z.read('assets/'+v.replace(':','/textures/')+'.png'))).convert('RGBA')) for k,v in model['textures'].items() if k!='particle'}
 def rot(axis,degrees):
  t=math.radians(degrees);c,s=math.cos(t),math.sin(t)
  return np.array({'x':[[1,0,0],[0,c,-s],[0,s,c]],'y':[[c,0,s],[0,1,0],[-s,0,c]]}[axis])
 R=rot('x',30)@rot('y',225);size=64;out=np.zeros((size,size,4),np.uint8);depth=np.full((size,size),-1e9)
 for el in model['elements']:
  x0,y0,z0=el['from'];x1,y1,z1=el['to']
  faces={'north':[(x1,y1,z0),(x1,y0,z0),(x0,y0,z0),(x0,y1,z0)],'south':[(x0,y1,z1),(x0,y0,z1),(x1,y0,z1),(x1,y1,z1)],'east':[(x1,y1,z1),(x1,y0,z1),(x1,y0,z0),(x1,y1,z0)],'west':[(x0,y1,z0),(x0,y0,z0),(x0,y0,z1),(x0,y1,z1)],'up':[(x0,y1,z0),(x0,y1,z1),(x1,y1,z1),(x1,y1,z0)],'down':[(x0,y0,z1),(x0,y0,z0),(x1,y0,z0),(x1,y0,z1)]}
  for name,f in el['faces'].items():
   v=np.array(faces[name],float)
   if 'rotation' in el:
    q=el['rotation'];o=np.array(q['origin']);v=(v-o)@rot(q['axis'],q['angle']).T+o
   v=(v-8)@R.T;v[:,:2]*=2;v[:,0]+=32;v[:,1]=32-v[:,1]
   u0,w0,u1,w1=f['uv'];uv=np.array([(u0,w0),(u0,w1),(u1,w1),(u1,w0)]);uv=np.roll(uv,f.get('rotation',0)//90,axis=0)
   tex=textures[f['texture'][1:]];h,w=tex.shape[:2];shade={'up':1,'down':.55,'north':.75,'south':.8,'east':.65,'west':.88}[name]
   for ids in [(0,1,2),(0,2,3)]:
    p=v[list(ids)];t=uv[list(ids)];a,b,c=p[:,:2];den=(b[1]-c[1])*(a[0]-c[0])+(c[0]-b[0])*(a[1]-c[1])
    if abs(den)<1e-5:continue
    for yy in range(max(0,int(p[:,1].min())),min(size,int(p[:,1].max())+1)):
     for xx in range(max(0,int(p[:,0].min())),min(size,int(p[:,0].max())+1)):
      X,Y=xx+.5,yy+.5;aa=((b[1]-c[1])*(X-c[0])+(c[0]-b[0])*(Y-c[1]))/den;bb=((c[1]-a[1])*(X-c[0])+(a[0]-c[0])*(Y-c[1]))/den;cc=1-aa-bb
      if min(aa,bb,cc)<-1e-6:continue
      bc=np.array([aa,bb,cc]);zz=bc@p[:,2]
      if zz<=depth[yy,xx]:continue
      u,vv=bc@t;col=tex[min(h-1,max(0,int(vv*h/16))),min(w-1,max(0,int(u*w/16)))].copy()
      if not col[3]:continue
      col[:3]=(col[:3]*shade).astype(np.uint8);out[yy,xx]=col;depth[yy,xx]=zz
 return Image.fromarray(out)

ICONS={k:icon(k) for k in NAMES}
with zipfile.ZipFile(MINECRAFT) as z:
 panel=Image.open(io.BytesIO(z.read('assets/minecraft/textures/gui/container/crafting_table.png'))).convert('RGBA')
# Keep the vanilla workbench recipe region and its original bottom frame.
background=Image.new('RGBA',(176,84))
background.paste(panel.crop((0,0,176,80)),(0,0))
background.paste(panel.crop((0,162,176,166)),(0,80))
manifest=[]
for kind in ['andesite','copper','brass']:
 rid=f'{kind}_rod';recipe=json.loads((ROOT/f'src/main/resources/data/clockwork_tides/recipe/{rid}.json').read_text())
 result=recipe['result'];output=result['id'];title,ru=NAMES[output];keys=recipe['key'];rows=recipe['pattern'];counts=collections.Counter(''.join(rows));counts.pop(' ',None)
 cells=[[keys[c]['item'] if c!=' ' else None for c in row] for row in rows]
 desc='; '.join(f'Row {i+1}: '+', '.join(NAMES[c][0] if c else 'empty' for c in row) for i,row in enumerate(cells))
 svg=[f'<svg xmlns="http://www.w3.org/2000/svg" width="704" height="336" viewBox="0 0 176 84" role="img" aria-labelledby="title desc"><title id="title">{html.escape(title)} crafting recipe / {html.escape(ru)}</title><desc id="desc">{html.escape(desc)}. Output: {result["count"]} {html.escape(title)}.</desc>',pixel_svg(background,0,0,176)]
 for yy,row in enumerate(rows):
  for xx,ch in enumerate(row):
   if ch!=' ':
    item=keys[ch]['item'];svg.append(f'<g><title>{html.escape(item)}</title>'+pixel_svg(ICONS[item],30+18*xx,17+18*yy,16)+'</g>')
 svg.append(pixel_svg(ICONS[output],124,35,16));svg.append('</svg>')
 path=OUT/f'{kind}-rod-recipe.svg';path.write_text(''.join(svg))
 import cairosvg
 cairosvg.svg2png(url=str(path),write_to=str(path.with_suffix('.png')))
 manifest.append({'recipe':rid,'source':f'src/main/resources/data/clockwork_tides/recipe/{rid}.json','cells':cells,'ingredients':{keys[k]['item']:v for k,v in counts.items()},'result':result})
(OUT/'recipe-diagrams.json').write_text(json.dumps(manifest,indent=2,ensure_ascii=False)+'\n')
print('Created 3 vanilla-workbench SVG + PNG diagrams directly from recipe JSON')
