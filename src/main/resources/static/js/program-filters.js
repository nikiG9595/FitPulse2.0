(() => {
    const panel=document.getElementById('program-filters');
    if(!panel)return;
    const cards=[...document.querySelectorAll('.program-library .library-card')];
    const groups=[...panel.querySelectorAll('[data-filter]')];
    const goal=document.getElementById('program-goal');
    const reset=document.getElementById('clear-program-filters');
    const goalShortcuts=[...panel.querySelectorAll('[data-program-goal]')];
    const mobileLevel=panel.querySelector('#mobile-program-level');
    const levels=panel.querySelectorAll('[data-filter="level"] button');
    function update(writeUrl=true){
        const filters={goal:goal.value};
        groups.forEach(group=>filters[group.dataset.filter]=group.querySelector('[aria-pressed="true"]')?.dataset.value || '');
        let count=0;
        cards.forEach(card=>{card.hidden=Object.entries(filters).some(([key,value])=>value && card.dataset[key]!==value);if(!card.hidden)count++;});
        document.getElementById('program-count').textContent=count;
        document.getElementById('program-empty').hidden=count!==0;
        reset.hidden=!Object.values(filters).some(Boolean);
        mobileLevel.value=filters.level;
        goalShortcuts.forEach(link=>{
            const selected=link.dataset.programGoal===goal.value;
            link.classList.toggle('is-selected', selected);
            if(selected)link.setAttribute('aria-current','true');
            else link.removeAttribute('aria-current');
        });
        if(writeUrl){const url=new URL(window.location.href);url.searchParams.delete('q');Object.entries(filters).forEach(([key,value])=>{if(value)url.searchParams.set(key,value);else url.searchParams.delete(key);});history.replaceState(null,'',url);}
    }
    groups.forEach(group=>group.querySelectorAll('button').forEach(button=>button.addEventListener('click',()=>{
        group.querySelectorAll('button').forEach(item=>item.setAttribute('aria-pressed',String(item===button)));update();
    })));
    goal.addEventListener('change',()=>update());
    mobileLevel.addEventListener('change',()=>{
        levels.forEach(button=>button.setAttribute('aria-pressed',String(button.dataset.value===mobileLevel.value)));
        update();
    });
    goalShortcuts.forEach(link=>link.addEventListener('click', event=>{
        if(event.ctrlKey || event.metaKey || event.shiftKey || event.altKey)return;
        event.preventDefault();
        goal.value=link.dataset.programGoal;
        update();
    }));
    reset.addEventListener('click',()=>{goal.value='';groups.forEach(group=>group.querySelectorAll('button').forEach(button=>button.setAttribute('aria-pressed',String(button.dataset.value===''))));update();});
    update(false);
})();
