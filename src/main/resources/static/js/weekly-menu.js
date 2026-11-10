(() => {
    const reduce=window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    document.querySelectorAll('.compact-week').forEach(week=>{
        const days=[...week.querySelectorAll('.compact-day')];
        days.forEach(day=>day.addEventListener('toggle',()=>{
            if(!day.open)return;
            days.forEach(other=>{if(other!==day)other.open=false;});
            if(!reduce)day.querySelector('.weekly-day-content')?.animate([{opacity:0,transform:'translateY(-5px)'},{opacity:1,transform:'translateY(0)'}],{duration:180,easing:'ease-out'});
        }));
        week.querySelectorAll('.compact-meal').forEach(meal=>meal.addEventListener('toggle',()=>{if(meal.open&&!reduce)meal.querySelector('.compact-meal-content')?.animate([{opacity:0},{opacity:1}],{duration:160});}));
        let target=window.location.hash.slice(1);
        try{const remembered=sessionStorage.getItem('fitpulse-meal-return:'+location.pathname);sessionStorage.removeItem('fitpulse-meal-return:'+location.pathname);if(!target&&remembered)target=remembered;}catch(_){}
        if(/^menu-(day|meal)-\d+(?:-\d+)?$/.test(target)){
            const selected=document.getElementById(target);
            if(selected&&week.contains(selected)){
                const day=selected.matches('.compact-day')?selected:selected.closest('.compact-day');
                days.forEach(other=>other.open=other===day);
                if(selected.matches('.nutrition-meal'))selected.open=true;
                requestAnimationFrame(()=>selected.scrollIntoView({block:'start'}));
            }
        }
    });
})();
