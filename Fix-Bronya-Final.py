import re, glob
files = glob.glob("AL-Game/data/static_data/items/**/*.xml", recursive=True)
fixed=0
for path in files:
    if "armor" not in path.lower():
        continue
    txt=open(path,'r',encoding='utf-8',errors='ignore').read()
    orig=txt
    def repl_item(m):
        global fixed
        full=m.group(0)
        lvl_m=re.search(r'level="(\d+)"', full)
        if not lvl_m: return full
        lvl=int(lvl_m.group(1))
        if lvl>10 or lvl==0: return full
        if 'weapon_type=' in full: return full
        def fix_pdef(pm):
            global fixed
            val=int(pm.group(2))
            expected=lvl*3+12
            if val>expected*1.8:
                fixed+=1
                return f'{pm.group(1)}" value="{expected}"'
            return pm.group(0)
        full=re.sub(r'(PHYSICAL_DEFENSE)" value="(\d+)"', fix_pdef, full)
        def fix_mres(pm):
            global fixed
            val=int(pm.group(2))
            expected=lvl*4+10
            if val>expected*2.5:
                fixed+=1
                return f'{pm.group(1)}" value="{expected}"'
            return pm.group(0)
        full=re.sub(r'(MAGICAL_RESIST)" value="(\d+)"', fix_mres, full)
        return full
    txt=re.sub(r'<item_template[^>]*>.*?</item_template>', repl_item, txt, flags=re.S)
    if txt!=orig:
        open(path+'.bak','w',encoding='utf-8').write(orig)
        open(path,'w',encoding='utf-8').write(txt)
        print(f"[OK] {path}")

print(f"Total armor fixed: {fixed}")