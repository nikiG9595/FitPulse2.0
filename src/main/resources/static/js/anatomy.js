(() => {
    document.querySelectorAll('[data-anatomy-mode]').forEach(async panel => {
        const mode=panel.dataset.anatomyMode;
        const primary=new Set((panel.dataset.primary||'').split(',').map(v=>v.trim()).filter(Boolean));
        const secondary=new Set((panel.dataset.secondary||'').split(',').map(v=>v.trim()).filter(Boolean));
        const selected=panel.dataset.selected||'';
        function choose(name) {
            panel.querySelector('.anatomy-selection').textContent=name||'Всички упражнения';
            if(mode!=='library')return;
            const form=document.querySelector('form.module-filter'), filter=form?.querySelector('[name="muscle"]');
            if(filter){filter.value=name;form.requestSubmit();}
        }
        panel.querySelectorAll('[data-muscle-select]').forEach(button=>button.addEventListener('click',()=>choose(button.dataset.muscleSelect)));
        try {
            for(const host of panel.querySelectorAll('[data-src]')){
                const response=await fetch(host.dataset.src);if(!response.ok)throw new Error();
                const doc=new DOMParser().parseFromString(await response.text(),'image/svg+xml');
                if(doc.querySelector('parsererror'))throw new Error();
                const svg=document.importNode(doc.documentElement,true);host.replaceChildren(svg);
                const zones=[...svg.querySelectorAll('[data-zone]')];
                zones.forEach((zone,index)=>{
                    zone.setAttribute('tabindex',index===0?'0':'-1');
                    const active=mode==='detail'?primary.has(zone.dataset.zone):selected===zone.dataset.muscle||selected==='Цяло тяло'||(selected==='Кардио'&&['Крака','Седалище'].includes(zone.dataset.muscle));
                    zone.classList.toggle('is-primary',mode==='detail'&&active);zone.classList.toggle('is-secondary',mode==='detail'&&!active&&secondary.has(zone.dataset.zone));zone.classList.toggle('is-selected',mode==='library'&&active);zone.setAttribute('aria-pressed',String(active));
                    zone.addEventListener('click',()=>choose(zone.dataset.muscle));
                    zone.addEventListener('keydown',event=>{if(event.key==='Enter'||event.key===' '){event.preventDefault();choose(zone.dataset.muscle);}else if(['ArrowRight','ArrowDown','ArrowLeft','ArrowUp','Home','End'].includes(event.key)){event.preventDefault();const next=event.key==='Home'?0:event.key==='End'?zones.length-1:(index+(['ArrowRight','ArrowDown'].includes(event.key)?1:-1)+zones.length)%zones.length;zone.setAttribute('tabindex','-1');zones[next].setAttribute('tabindex','0');zones[next].focus();}});
                });
            }
        }catch(_){panel.querySelector('.anatomy-error').hidden=false;}
    });
})();
