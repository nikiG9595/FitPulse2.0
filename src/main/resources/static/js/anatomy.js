(() => {
    const normalize=value=>(value||'').normalize('NFC').trim();
    const groupZones={'Бицепс':['BICEPS'],'Трицепс':['TRICEPS'],'Гърди':['CHEST'],'Гръб':['TRAPEZIUS','RHOMBOIDS','LATS','BACK_LOWER'],'Рамене':['SHOULDERS_FRONT','SHOULDERS_SIDE','SHOULDERS_REAR'],'Крака':['QUADS','HAMSTRINGS','ADDUCTORS','ABDUCTORS','CALVES'],'Седалище':['GLUTES','ABDUCTORS'],'Корем':['CORE','OBLIQUES'],'Кардио':['QUADS','HAMSTRINGS','CALVES','GLUTES']};
    document.querySelectorAll('[data-anatomy-mode]').forEach(async panel=>{
        const mode=panel.getAttribute('data-anatomy-mode');
        const primary=new Set((panel.getAttribute('data-primary')||'').split(',').map(normalize));
        const secondary=new Set((panel.getAttribute('data-secondary')||'').split(',').map(normalize));
        let selected=normalize(panel.getAttribute('data-selected')), subgroup=panel.getAttribute('data-subgroup')||'';
        const options=new Map([...panel.querySelectorAll('[data-subgroup-zone]')].map(button=>[button.getAttribute('data-subgroup-select'),button.getAttribute('data-subgroup-zone')]));
        const allZones=[];
        const keyOf=zone=>normalize(zone.getAttribute('data-zone')).toUpperCase();
        const pathsOf=zone=>zone.matches('path')?[zone]:[...zone.querySelectorAll('path,polygon,ellipse')];
        function color(zone,value){zone.style.setProperty('--muscle-fill',value);pathsOf(zone).forEach(path=>{path.style.setProperty('fill',value,'important');path.setAttribute('fill',value);});}
        function paint(){
            const chosen=subgroup&&options.has(subgroup)?new Set([options.get(subgroup)]):new Set(groupZones[selected]||[]);
            // Every matching path is painted: LEFT, RIGHT and compound contours on both figures.
            allZones.forEach(zone=>{
                const key=keyOf(zone),active=mode==='detail'?primary.has(key):!!selected&&(selected==='Цяло тяло'||chosen.has(key));
                const assist=mode==='detail'&&!active&&secondary.has(key);
                zone.classList.toggle('is-primary',mode==='detail'&&active);zone.classList.toggle('is-selected',mode==='library'&&active);zone.classList.toggle('is-secondary',assist);zone.setAttribute('aria-pressed',String(active));
                const neutral=key.includes('SHOULDER')||key==='GLUTES'?'#647286':['BICEPS','HAMSTRINGS','OBLIQUES','TRAPEZIUS'].includes(key)?'#465366':'#566477';
                color(zone,active?'#ff681f':assist?'#ffd3ac':neutral);
            });
            panel.querySelectorAll('[data-muscle-select]').forEach(button=>button.setAttribute('aria-pressed',String(normalize(button.getAttribute('data-muscle-select'))===selected)));
            panel.querySelectorAll('[data-subgroup-select]').forEach(button=>button.setAttribute('aria-pressed',String(button.getAttribute('data-subgroup-select')===subgroup)));
        }
        function submit(){
            const form=document.querySelector('form.module-filter');if(!form)return;
            form.querySelector('[name="muscle"]').value=selected;
            const sub=form.querySelector('[name="subgroup"]');if(sub)sub.value=subgroup;
            form.querySelector('[name="page"]')?.remove();form.requestSubmit();
        }
        function choose(name){if(mode!=='library')return;selected=normalize(name);subgroup='';paint();panel.querySelector('.anatomy-selection').textContent=selected||'Всички упражнения';submit();}
        panel.querySelectorAll('[data-muscle-select]').forEach(button=>button.addEventListener('click',()=>choose(button.getAttribute('data-muscle-select'))));
        panel.querySelectorAll('[data-subgroup-select]').forEach(button=>button.addEventListener('click',()=>{subgroup=button.getAttribute('data-subgroup-select');if(subgroup&&!options.has(subgroup))subgroup='';paint();submit();}));
        document.querySelector('form.module-filter [name="muscle"]')?.addEventListener('change',event=>{selected=event.target.value;subgroup='';const hidden=document.querySelector('form.module-filter [name="subgroup"]');if(hidden)hidden.value='';document.querySelector('form.module-filter [name="page"]')?.remove();panel.querySelector('.anatomy-subgroups')?.setAttribute('hidden','');paint();});
        panel.querySelector('.anatomy-scroll')?.addEventListener('click',event=>{
            const results=document.getElementById('exercise-results');if(!results)return;event.preventDefault();
            results.scrollIntoView({behavior:matchMedia('(prefers-reduced-motion: reduce)').matches?'instant':'smooth',block:'start'});results.focus({preventScroll:true});
        });
        for(const host of panel.querySelectorAll('[data-src]')){
            try{
                const response=await fetch(host.getAttribute('data-src'),{cache:'no-cache',credentials:'same-origin'});if(!response.ok)throw new Error();
                const doc=new DOMParser().parseFromString(await response.text(),'image/svg+xml');if(doc.querySelector('parsererror'))throw new Error();
                const svg=document.importNode(doc.documentElement,true);host.replaceChildren(svg);
                const zones=[...svg.querySelectorAll('[data-zone]')];allZones.push(...zones);
                zones.forEach((zone,index)=>{
                    zone.setAttribute('tabindex',index===0?'0':'-1');
                    const chooseZone=()=>choose(zone.getAttribute('data-muscle'));
                    zone.addEventListener('click',chooseZone);
                    const highlight=()=>allZones.filter(other=>keyOf(other)===keyOf(zone)).forEach(other=>color(other,'#ff995d'));
                    zone.addEventListener('pointerenter',highlight);zone.addEventListener('pointerleave',paint);zone.addEventListener('focus',highlight);zone.addEventListener('blur',paint);
                    zone.addEventListener('keydown',event=>{
                        if(event.key==='Enter'||event.key===' '){event.preventDefault();chooseZone();}
                        else if(['ArrowRight','ArrowDown','ArrowLeft','ArrowUp','Home','End'].includes(event.key)){event.preventDefault();const next=event.key==='Home'?0:event.key==='End'?zones.length-1:(index+(['ArrowRight','ArrowDown'].includes(event.key)?1:-1)+zones.length)%zones.length;zone.setAttribute('tabindex','-1');zones[next].setAttribute('tabindex','0');zones[next].focus();}
                    });
                });paint();
            }catch(_){panel.querySelector('.anatomy-error').hidden=false;}
        }
    });
})();
