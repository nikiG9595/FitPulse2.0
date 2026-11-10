(() => {
    const root=document.getElementById('live-session');if(!root)return;
    const prefix=`fitpulse-session-${root.dataset.sessionId}-`;
    const forms=[...root.querySelectorAll('.session-set-row')], timers=new Map(), chains=new Map();
    const error=document.getElementById('session-error');
    const clearCache=()=>{try{Object.keys(sessionStorage).filter(k=>k.startsWith(prefix)).forEach(k=>sessionStorage.removeItem(k));}catch(_){}};
    if(root.dataset.status!=='DRAFT'){clearCache();return;}
    function values(form){return {repetitions:form.elements.repetitions.value,weight:form.elements.weight.value,completed:form.elements.completed.checked};}
    function cache(form){try{sessionStorage.setItem(prefix+form.dataset.rowId,JSON.stringify(values(form)));}catch(_){}}
    function required(form){form.elements.repetitions.required=form.elements.completed.checked;form.elements.weight.required=form.elements.completed.checked;}
    async function send(form){
        required(form);if(!form.checkValidity()){form.querySelector('[data-save-status]').textContent='Попълни валидни повторения и тежест за изпълнената серия.';return false;}
        const sent=JSON.stringify(values(form));const label=form.querySelector('[data-save-status]');label.textContent='Запис…';
        try{
            const response=await fetch(form.action,{method:'POST',body:new FormData(form),headers:{Accept:'application/json'},credentials:'same-origin'});
            if(!response.ok||!response.headers.get('content-type')?.includes('application/json'))throw new Error();
            const result=await response.json();if(!result.saved)throw new Error();
            if(JSON.stringify(values(form))===sent){form.dataset.dirty='false';try{sessionStorage.removeItem(prefix+form.dataset.rowId);}catch(_){}}
            label.textContent='Запазено';return true;
        }catch(_){label.textContent='Не е запазено. Данните остават тук; провери връзката и опитай отново.';return false;}
    }
    function save(form){const previous=chains.get(form)||Promise.resolve(true);const next=previous.catch(()=>false).then(()=>send(form));chains.set(form,next);return next;}
    forms.forEach(form=>{
        try{const cached=JSON.parse(sessionStorage.getItem(prefix+form.dataset.rowId));if(cached){form.elements.repetitions.value=cached.repetitions;form.elements.weight.value=cached.weight;form.elements.completed.checked=cached.completed;form.dataset.dirty='true';}}catch(_){}
        required(form);
        form.addEventListener('input',()=>{required(form);form.dataset.dirty='true';cache(form);clearTimeout(timers.get(form));timers.set(form,setTimeout(()=>save(form),400));});
        form.addEventListener('change',()=>{required(form);form.dataset.dirty='true';cache(form);clearTimeout(timers.get(form));timers.set(form,setTimeout(()=>save(form),150));});
        form.addEventListener('submit',event=>{event.preventDefault();clearTimeout(timers.get(form));save(form);});
        if(form.dataset.dirty==='true')save(form);
    });
    async function flush(){timers.forEach(clearTimeout);const result=await Promise.all(forms.map(async form=>{await(chains.get(form)||Promise.resolve());return form.dataset.dirty==='true'?save(form):true;}));return result.every(Boolean);}
    async function exit(event,form,href){event.preventDefault();error.hidden=true;const button=form?.querySelector('button');if(button)button.disabled=true;
        if(await flush()){clearCache();if(form)HTMLFormElement.prototype.submit.call(form);else window.location.href=href;}
        else{error.hidden=false;error.textContent='Има незапазени или невалидни серии. Поправи ги преди приключване или излизане.';if(button)button.disabled=false;}
    }
    root.querySelector('[data-session-finish]')?.addEventListener('submit',event=>{if(!forms.some(form=>form.elements.completed.checked)){event.preventDefault();error.hidden=false;error.textContent='Отбележи поне една реално изпълнена серия преди завършване.';return;}exit(event,event.currentTarget);});
    root.querySelector('[data-session-cancel]')?.addEventListener('submit',event=>{event.preventDefault();timers.forEach(clearTimeout);clearCache();HTMLFormElement.prototype.submit.call(event.currentTarget);});
    root.querySelector('[data-session-pause]')?.addEventListener('click',event=>exit(event,null,event.currentTarget.href));
    let state={serverNow:Number(root.dataset.serverNow),deadline:root.dataset.deadline?Number(root.dataset.deadline):null,remaining:Number(root.dataset.remaining)};
    let offset=state.serverNow-Date.now(), wasPositive=true, durationChanged=false;
    const clock=document.getElementById('rest-clock'), timerForm=document.getElementById('rest-timer-form'), timerStatus=document.getElementById('timer-status');
    function paint(){const remaining=state.deadline==null?state.remaining:Math.max(0,Math.ceil((state.deadline-Date.now()-offset)/1000));clock.textContent=`${String(Math.floor(remaining/60)).padStart(2,'0')}:${String(remaining%60).padStart(2,'0')}`;if(remaining===0&&wasPositive)timerStatus.textContent='Почивката приключи.';wasPositive=remaining>0;}
    async function timer(action){
        if(!timerForm.reportValidity()||!Number.isInteger(timerForm.elements.seconds.valueAsNumber)||timerForm.elements.seconds.valueAsNumber<5||timerForm.elements.seconds.valueAsNumber>900){timerStatus.textContent='Въведи цяло число от 5 до 900 секунди.';return false;}
        try{const body=new FormData(timerForm);body.set('action',action);const response=await fetch(timerForm.action,{method:'POST',body,headers:{Accept:'application/json'}});if(!response.ok||!response.headers.get('content-type')?.includes('application/json'))throw new Error();state=await response.json();offset=state.serverNow-Date.now();if(action==='reset')durationChanged=false;timerStatus.textContent=action==='pause'?'Таймерът е на пауза.':action==='reset'?'Таймерът е нулиран.':'Почивката започна.';paint();return true;}
        catch(_){timerStatus.textContent='Таймерът не е запазен. Провери връзката и опитай отново.';return false;}
    }
    timerForm.elements.seconds.addEventListener('input',()=>durationChanged=true);
    root.querySelectorAll('[data-timer-action]').forEach(button=>button.addEventListener('click',async()=>{if(button.dataset.timerAction==='start'&&durationChanged&&!(await timer('reset')))return;await timer(button.dataset.timerAction);}));
    root.querySelectorAll('[data-start-rest]').forEach(button=>button.addEventListener('click',async()=>{timerForm.elements.seconds.value=button.dataset.startRest;if(await timer('reset'))await timer('start');}));
    paint();setInterval(paint,1000);
})();
