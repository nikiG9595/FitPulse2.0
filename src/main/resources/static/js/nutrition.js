(() => {
    const messages = window.fitPulseMessages;
    const format = new Intl.NumberFormat(document.documentElement.lang, {maximumFractionDigits: 0});
    const calories = v => 4 * v.protein + 9 * v.fat + 4 * v.carbs;
    const zero = () => ({protein: 0, fat: 0, carbs: 0});
    function show(container, prefix, values) {
        for (const [key, label] of [['protein',messages['ui.p.2']], ['fat',messages['ui.f.2']], ['carbs',messages['ui.c.2']]]) {
            const element = container.querySelector(`[data-${prefix}-${key}]`);
            if (element) element.textContent = `${label} ${format.format(values[key])}${messages['ui.g']}`;
        }
        container.querySelector(`[data-${prefix}-calories]`).textContent = `${format.format(calories(values))}${messages['ui.kcal']}`;
    }
    function update(day) {
        let valid = true;
        day.querySelectorAll('.grams-input').forEach(input => {
            const good = input.checkValidity() && Number.isFinite(input.valueAsNumber);
            input.form.querySelector('button[type="submit"]').disabled = !good;
            if (!good) {
                valid = false;
                input.closest('.nutrition-meal').querySelector('.unsaved-note').textContent = messages['ui.enter.an.amount.from.1.to.2000.2'];
            }
        });
        if (!valid) return;
        const daily = zero();
        day.querySelectorAll('.nutrition-meal').forEach(meal => {
            const sum = zero();
            meal.querySelectorAll('.interactive-ingredient').forEach(row => {
                const input = row.querySelector('.grams-input');
                const value = input.valueAsNumber;
                const valid = input.checkValidity() && Number.isFinite(value);
                row.querySelector('button[type="submit"]').disabled = !valid;
                if (valid) for (const key of ['protein','fat','carbs']) sum[key] += value * Number(row.dataset[key]) / 100;
            });
            show(meal, 'meal', sum);
            for (const key of ['protein','fat','carbs']) daily[key] += sum[key];
        });
        show(day, 'day', daily);
    }
    document.querySelectorAll('.grams-input').forEach(input => {input.dataset.savedValue=input.value;input.addEventListener('input', () => {
        input.dataset.dirty = 'true';
        const day = input.closest('.interactive-day');
        input.closest('.nutrition-meal').querySelector('.unsaved-note').hidden = false;
        input.closest('.nutrition-meal').querySelector('.unsaved-note').textContent = messages['ui.values.were.recalculated.on.screen.select.save'];
        update(day);
    });});
    document.querySelectorAll('.interactive-ingredient form').forEach(form => form.addEventListener('submit', async event => {
        event.preventDefault();
        if (form.dataset.saving === 'true' || !form.reportValidity()) return;
        form.dataset.saving = 'true';
        const input = form.querySelector('.grams-input');
        const sentValue = input.value;
        const button = form.querySelector('button[type="submit"]');
        const note = form.closest('.nutrition-meal').querySelector('.unsaved-note');
        const payload = new FormData(form);
        button.disabled = true; button.textContent = messages['ui.saving']; note.hidden = false;
        try {
            const response = await fetch(form.action, {method: 'POST', body: payload, headers: {'Accept': 'application/json'}, credentials: 'same-origin'});
            if (!response.ok || !response.headers.get('content-type')?.includes('application/json')) throw new Error();
            const saved = await response.json();
            if (!saved.saved) throw new Error();
            input.dataset.savedValue=String(saved.grams);
            if (input.value === sentValue) {input.value=String(saved.grams);delete input.dataset.dirty;}
            const paintSaved=(container,prefix,totals)=>{
                for(const key of ['calories','protein','fat','carbs']){
                    const element=container.querySelector(`[data-${prefix}-${key}]`);
                    if(element)element.textContent=(key==='calories'?'':({protein:messages['ui.p'],fat:messages['ui.f'],carbs:messages['ui.c']})[key])+format.format(totals[key])+(key==='calories'?messages['ui.kcal']:messages['ui.g']);
                }
            };
            if(saved.meal&&saved.day){paintSaved(form.closest('.nutrition-meal'),'meal',saved.meal);paintSaved(form.closest('.interactive-day'),'day',saved.day);}
            note.textContent = form.closest('.nutrition-meal').querySelector('[data-dirty="true"]')
                ? messages['ui.saved.this.meal.still.has.other.unsaved'] : messages['ui.the.amount.was.saved.in.your.personal'];
        } catch (_) {
            input.value=input.dataset.savedValue;delete input.dataset.dirty;update(input.closest('.interactive-day'));
            note.textContent = messages['ui.the.amount.was.not.saved.check.the'];
        } finally {
            form.dataset.saving = 'false'; button.disabled = !input.checkValidity(); button.textContent = messages['action.save'];
        }
    }));
    // The same DOM remains open after gram saves; substitutions return to the exact meal anchor.
    const shoppingWrites=new Set();
    document.querySelectorAll('[data-shopping-check] input[type="checkbox"]').forEach(input => {
        input.dataset.saved = String(input.checked);
        input.form.addEventListener('submit', event => event.preventDefault());
        input.addEventListener('change', () => {
            const write=(async()=>{
            const previous = input.dataset.saved === 'true';
            const form = input.form, row = input.closest('.shopping-item');
            const payload = new FormData(form); // Includes Spring's CSRF field, before disabling the checkbox.
            const feedback = document.getElementById('shopping-feedback');
            input.disabled = true; row.setAttribute('aria-busy','true');
            row.classList.toggle('purchased',input.checked);
            feedback.textContent = messages['ui.saving'];
            try {
                const response = await fetch(form.action, {method:'POST',body:payload,headers:{Accept:'application/json'},credentials:'same-origin'});
                if (!response.ok || !response.headers.get('content-type')?.includes('application/json')) throw new Error();
                const saved = await response.json();
                if (!saved.saved || typeof saved.checked !== 'boolean') throw new Error();
                input.checked = saved.checked; input.dataset.saved = String(saved.checked);
                row.classList.toggle('purchased',saved.checked);feedback.textContent = messages['ui.checkmark.saved'];
            } catch (_) {
                input.checked = previous; row.classList.toggle('purchased',previous);
                feedback.textContent = messages['ui.the.checkmark.was.not.saved.try.again'];
            } finally {input.disabled = false; row.removeAttribute('aria-busy');}
            })();shoppingWrites.add(write);write.finally(()=>shoppingWrites.delete(write));
        });
    });
    document.querySelector('[data-shopping-reset]')?.addEventListener('submit',async event=>{
        if(!shoppingWrites.size)return;event.preventDefault();const form=event.currentTarget;
        form.querySelector('button[type="submit"]').disabled=true;
        await Promise.allSettled([...shoppingWrites]);HTMLFormElement.prototype.submit.call(form);
    });
    document.querySelectorAll('.interactive-menu a.ingredient-swap,.interactive-menu .dish-actions a').forEach(link=>link.addEventListener('click',()=>{
        try{sessionStorage.setItem('fitpulse-meal-return:'+location.pathname,link.closest('.nutrition-meal').id);}catch(_){}
    }));
    window.addEventListener('pageshow',event=>{if(event.persisted&&document.querySelector('.interactive-menu .grams-input')){
        try{const open=document.querySelector('.nutrition-meal[open]');if(open)sessionStorage.setItem('fitpulse-meal-return:'+location.pathname,open.id);}catch(_){}location.reload();
    }});
    document.getElementById('print-shopping')?.addEventListener('click', () => window.print());
    document.getElementById('copy-shopping')?.addEventListener('click', async () => {
        const lines = [messages['ui.shopping.list.fitpulse'], ...Array.from(document.querySelectorAll('[data-shopping-text]'), row => `${row.classList.contains('purchased') ? '[✓]' : '[ ]'} ${row.dataset.shoppingText}`)];
        const text = lines.join('\n');
        const status = document.getElementById('shopping-feedback');
        try {
            if (navigator.clipboard?.writeText) await navigator.clipboard.writeText(text);
            else {const area = document.createElement('textarea'); area.value = text; document.body.append(area); area.select(); const done = document.execCommand('copy'); area.remove(); if (!done) throw new Error();}
            status.textContent = messages['ui.the.list.was.copied'];
        } catch (_) {
            const area = document.createElement('textarea'); area.className = 'form-control'; area.value = text; area.setAttribute('aria-label', messages['ui.list.for.manual.copying']); status.replaceChildren(document.createTextNode(messages['ui.select.and.copy.the.list']), area); area.select();
        }
    });
})();
