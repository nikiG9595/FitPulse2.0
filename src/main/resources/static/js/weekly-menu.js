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
        if(window.location.hash.startsWith('#menu-day-')){const selected=document.getElementById(window.location.hash.slice(1));if(selected){days.forEach(d=>d.open=d===selected);requestAnimationFrame(()=>selected.scrollIntoView({block:'start'}));}}
    });
})();
