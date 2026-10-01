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
COLORS={'andesite':'#a8b5a4','copper':'#67c4b6','brass':'#ecc66b'}

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

def text(x,y,s,size=20,color='#edf3f3',weight='normal'):
 return f'<text x="{x}" y="{y}" fill="{color}" font-size="{size}" font-weight="{weight}" font-family="DejaVu Sans, sans-serif">{html.escape(s)}</text>'

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
manifest=[]
for num,kind in enumerate(['andesite','copper','brass'],1):
 rid=f'{kind}_rod';recipe=json.loads((ROOT/f'src/main/resources/data/clockwork_tides/recipe/{rid}.json').read_text());accent=COLORS[kind]
 result=recipe['result'];output=result['id'];title,ru=NAMES[output];keys=recipe['key'];rows=recipe['pattern'];counts=collections.Counter(''.join(rows));counts.pop(' ',None)
 cells=[[keys[c]['item'] if c!=' ' else None for c in row] for row in rows]
 desc='; '.join(f'Row {i+1}: '+', '.join(NAMES[c][0] if c else 'empty' for c in row) for i,row in enumerate(cells))
 svg=[f'<svg xmlns="http://www.w3.org/2000/svg" width="1320" height="720" viewBox="0 0 1320 720" role="img" aria-labelledby="title desc"><title id="title">{html.escape(title)} crafting recipe / {html.escape(ru)}</title><desc id="desc">{html.escape(desc)}. Output: {result["count"]} {html.escape(title)}. Upgrades do not preserve enchantments or names.</desc>',
 '<rect width="1320" height="720" rx="24" fill="#121d24"/>',f'<rect x="0" y="0" width="10" height="720" rx="5" fill="{accent}"/>',
 text(56,55,'CLOCKWORK TIDES  /  CRAFTING RECIPES',14,'#93a8b2','bold'),text(56,101,title.upper(),32,'#f3f5f2','bold'),text(690,99,ru,24,accent),
 f'<rect x="1200" y="38" width="64" height="64" rx="14" fill="{accent}"/>',text(1217,82,f'{num:02}',28,'#121d24','bold'),
 '<path d="M56 132H1264" stroke="#31434c"/>',text(56,173,'CRAFTING TABLE / ВЕРСТАК',15,'#93a8b2','bold'),text(708,173,'INGREDIENTS / ИНГРЕДИЕНТЫ',15,'#93a8b2','bold')]
 for yy,row in enumerate(rows):
  for xx,ch in enumerate(row):
   x=56+112*xx;y=195+112*yy
   svg.append(f'<rect x="{x}" y="{y}" width="104" height="104" rx="8" fill="#1d303a" stroke="#3d535d" stroke-width="2"/>')
   if ch==' ':
    svg.append(f'<path d="M{x+46} {y+52}h12 M{x+52} {y+46}v12" stroke="#30454f" stroke-width="2"/>')
   else:
    item=keys[ch]['item'];svg.append(f'<g><title>{html.escape(item)}</title>'+pixel_svg(ICONS[item],x+16,y+16,72)+'</g>');svg.append(text(x+8,y+19,ch,12,accent,'bold'))
 svg += [f'<path d="M413 358h32m-10-10 10 10-10 10" fill="none" stroke="{accent}" stroke-width="4" stroke-linecap="round" stroke-linejoin="round"/>',f'<rect x="476" y="266" width="160" height="160" rx="16" fill="#223239" stroke="{accent}" stroke-width="2"/>',pixel_svg(ICONS[output],492,282,128),text(489,245,'RESULT / РЕЗУЛЬТАТ',13,'#93a8b2','bold'),text(495,461,f'×{result["count"]} ROD / УДОЧКА',16,accent,'bold')]
 for i,(key,count) in enumerate(counts.items()):
  item=keys[key]['item'];en,russian=NAMES[item];y=200+i*88
  svg.append(f'<rect x="708" y="{y}" width="556" height="78" rx="10" fill="#192b34"/>');svg.append(pixel_svg(ICONS[item],716,y+7,64));svg.append(text(796,y+30,en,20,'#eff4ef','bold'));svg.append(text(796,y+54,russian,16,'#9cafb7'));svg.append(text(1201,y+46,f'×{count}',25,accent,'bold'))
 svg += [text(56,560,'Empty slots stay empty • Пустые ячейки оставьте пустыми',16,'#93a8b2'),'<rect x="56" y="594" width="1208" height="82" rx="12" fill="#2b2c28"/>',text(78,625,'CRAFT BEFORE ENCHANTING — upgrades consume the old rod and reset its name / enchantments.',17,'#e4cea1','bold'),text(78,652,'Сначала крафт, потом чары: улучшение расходует старую удочку и сбрасывает имя и зачарования.',17,'#c2bda9'),text(56,702,'Exact recipe JSON • Minecraft 1.21.1 / Create 6.0.10 • Item artwork © Mojang Studios / The Create Team',12,'#708994'),'</svg>']
 path=OUT/f'{kind}-rod-recipe.svg';path.write_text(''.join(svg))
 import cairosvg
 cairosvg.svg2png(url=str(path),write_to=str(path.with_suffix('.png')))
 manifest.append({'recipe':rid,'source':f'src/main/resources/data/clockwork_tides/recipe/{rid}.json','cells':cells,'ingredients':{keys[k]['item']:v for k,v in counts.items()},'result':result})
(OUT/'recipe-diagrams.json').write_text(json.dumps(manifest,indent=2,ensure_ascii=False)+'\n')
print('Created 3 SVG + PNG diagrams directly from recipe JSON')
