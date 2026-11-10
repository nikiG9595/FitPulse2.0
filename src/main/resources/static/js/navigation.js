(() => {
    const toggle = document.getElementById('fitpulse-menu-toggle'), menu = document.getElementById('fitpulse-menu');
    if (!toggle || !menu) return;
    const member = menu.closest('.fit-nav').classList.contains('nav-member');
    const wide = window.matchMedia('(min-width: 768px)');
    const expanded = () => !member && wide.matches;
    const layout = menu.closest('.nav-layout'), slogan = layout.querySelector('.nav-slogan'), identity = layout.querySelector('.nav-identity');
    function align() {
        const width = (expanded() ? menu : toggle).getBoundingClientRect().width;
        layout.classList.toggle('slogan-inline', 2 * Math.max(identity.getBoundingClientRect().width, width) + slogan.getBoundingClientRect().width + 32 <= layout.clientWidth);
    }
    const observer = new ResizeObserver(align); [layout,identity,menu,toggle,slogan].forEach(e => observer.observe(e));
    document.fonts.ready.then(align); align();
    const items = () => [...menu.querySelectorAll('a,button')].filter(e => e.getClientRects().length && !e.disabled);
    function open(value, focus) {
        menu.classList.toggle('show', value); toggle.setAttribute('aria-expanded', String(value));
        toggle.setAttribute('aria-label', value ? 'Затвори менюто' : 'Отвори менюто');
        if (value && focus) {const list=items(); (focus==='last'?list.at(-1):list[0])?.focus();}
    }
    if (member) {toggle.setAttribute('aria-haspopup','menu');menu.querySelector('ul').setAttribute('role','menu');menu.querySelectorAll('li').forEach(e=>e.setAttribute('role','none'));menu.querySelectorAll('a,button').forEach(e=>e.setAttribute('role','menuitem'));}
    toggle.addEventListener('click',()=>open(!menu.classList.contains('show')));
    toggle.addEventListener('keydown',event=>{if(['ArrowDown','ArrowUp'].includes(event.key)){event.preventDefault();open(true,event.key==='ArrowUp'?'last':'first');}});
    menu.addEventListener('keydown',event=>{
        const list=items(), index=list.indexOf(document.activeElement);
        if(['ArrowDown','ArrowUp','Home','End'].includes(event.key)){
            event.preventDefault();const next=event.key==='Home'?0:event.key==='End'?list.length-1:(index+(event.key==='ArrowDown'?1:-1)+list.length)%list.length;list[next]?.focus();
        }
    });
    document.addEventListener('keydown',event=>{if(event.key==='Escape'&&menu.classList.contains('show')){event.preventDefault();open(false);toggle.focus();}});
    document.addEventListener('click',event=>{if(!menu.contains(event.target)&&!toggle.contains(event.target))open(false);});
    layout.addEventListener('focusout',()=>setTimeout(()=>{if(!layout.contains(document.activeElement))open(false);},0));
    menu.addEventListener('click',event=>{if(event.target.closest('a,button')&&!expanded())open(false);});
    wide.addEventListener('change',()=>{open(false);align();});
    const path=window.location.pathname.replace(/\/$/,''), profile=menu.querySelector('[data-profile-link]');
    const root=profile?new URL(profile.href).pathname.replace(/\/profile\/?$/,''):'';
    const personal=profile&&['/profile','/dashboard','/journal','/calendar','/progress','/training-sessions'].some(part=>path===root+part||path.startsWith(root+part+'/'));
    menu.querySelectorAll('a.nav-link').forEach(link=>{const target=new URL(link.href).pathname.replace(/\/$/,'');const active=personal?link===profile:path===target||path.startsWith(target+'/')||(target===root+'/programs'&&path.startsWith(root+'/exercises'));if(active)link.setAttribute('aria-current','page');});
})();
