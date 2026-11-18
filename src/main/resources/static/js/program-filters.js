(() => {
    const panel=document.getElementById('program-filters');
    if(!panel)return;
    const cards=[...document.querySelectorAll('.program-library .library-card')];
    const groups=[...panel.querySelectorAll('[data-filter]')];
    const goal=document.getElementById('program-goal');
    const reset=document.getElementById('clear-program-filters');
    function update(writeUrl=true){
        const filters={goal:goal.value};
        groups.forEach(group=>filters[group.dataset.filter]=group.querySelector('[aria-pressed="true"]')?.dataset.value || '');
        let count=0;
        cards.forEach(card=>{card.hidden=Object.entries(filters).some(([key,value])=>value && card.dataset[key]!==value);if(!card.hidden)count++;});
        document.getElementById('program-count').textContent=count;
        document.getElementById('program-empty').hidden=count!==0;
        reset.hidden=!Object.values(filters).some(Boolean);
        if(writeUrl){const url=new URL(window.location.href);url.searchParams.delete('q');Object.entries(filters).forEach(([key,value])=>{if(value)url.searchParams.set(key,value);else url.searchParams.delete(key);});history.replaceState(null,'',url);}
    }
    groups.forEach(group=>group.querySelectorAll('button').forEach(button=>button.addEventListener('click',()=>{
        group.querySelectorAll('button').forEach(item=>item.setAttribute('aria-pressed',String(item===button)));update();
    })));
    goal.addEventListener('change',()=>update());
    reset.addEventListener('click',()=>{goal.value='';groups.forEach(group=>group.querySelectorAll('button').forEach(button=>button.setAttribute('aria-pressed',String(button.dataset.value===''))));update();});
    update(false);
})();
