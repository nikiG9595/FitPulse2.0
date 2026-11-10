(() => {
    const format = new Intl.NumberFormat('bg-BG', {maximumFractionDigits: 0});
    const calories = v => 4 * v.protein + 9 * v.fat + 4 * v.carbs;
    const zero = () => ({protein: 0, fat: 0, carbs: 0});
    function show(container, prefix, values) {
        for (const [key, label] of [['protein','Б'], ['fat','М'], ['carbs','В']]) {
            const element = container.querySelector(`[data-${prefix}-${key}]`);
            if (element) element.textContent = `${label} ${format.format(values[key])} г`;
        }
        container.querySelector(`[data-${prefix}-calories]`).textContent = `${format.format(calories(values))} ккал`;
    }
    function update(day) {
        let valid = true;
        day.querySelectorAll('.grams-input').forEach(input => {
            const good = input.checkValidity() && Number.isFinite(input.valueAsNumber);
            input.form.querySelector('button[type="submit"]').disabled = !good;
            if (!good) {
                valid = false;
                input.closest('.nutrition-meal').querySelector('.unsaved-note').textContent = 'Въведи количество от 1 до 2000 г с до два десетични знака.';
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
        input.closest('.nutrition-meal').querySelector('.unsaved-note').textContent = 'Преизчислено на екрана. Натисни „Запази“ до промененото количество.';
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
        button.disabled = true; button.textContent = 'Запис…'; note.hidden = false;
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
                    if(element)element.textContent=(key==='calories'?'':({protein:'Б ',fat:'М ',carbs:'В '})[key])+format.format(totals[key])+(key==='calories'?' ккал':' г');
                }
            };
            if(saved.meal&&saved.day){paintSaved(form.closest('.nutrition-meal'),'meal',saved.meal);paintSaved(form.closest('.interactive-day'),'day',saved.day);}
            note.textContent = form.closest('.nutrition-meal').querySelector('[data-dirty="true"]')
                ? 'Записано. Има и други незапазени количества в това хранене.' : 'Количеството е запазено в личния ти режим.';
        } catch (_) {
            input.value=input.dataset.savedValue;delete input.dataset.dirty;update(input.closest('.interactive-day'));
            note.textContent = 'Количеството не е запазено. Провери стойността (1–2000 г), връзката и дали още си в профила си, след което опитай отново.';
        } finally {
            form.dataset.saving = 'false'; button.disabled = !input.checkValidity(); button.textContent = 'Запази';
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
            feedback.textContent = 'Запис…';
            try {
                const response = await fetch(form.action, {method:'POST',body:payload,headers:{Accept:'application/json'},credentials:'same-origin'});
                if (!response.ok || !response.headers.get('content-type')?.includes('application/json')) throw new Error();
                const saved = await response.json();
                if (!saved.saved || typeof saved.checked !== 'boolean') throw new Error();
                input.checked = saved.checked; input.dataset.saved = String(saved.checked);
                row.classList.toggle('purchased',saved.checked);feedback.textContent = 'Отметката е запазена.';
            } catch (_) {
                input.checked = previous; row.classList.toggle('purchased',previous);
                feedback.textContent = 'Отметката не е запазена. Опитайте отново.';
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
        const lines = ['Списък за пазаруване — FitPulse', ...Array.from(document.querySelectorAll('[data-shopping-text]'), row => `${row.classList.contains('purchased') ? '[✓]' : '[ ]'} ${row.dataset.shoppingText}`)];
        const text = lines.join('\n');
        const status = document.getElementById('shopping-feedback');
        try {
            if (navigator.clipboard?.writeText) await navigator.clipboard.writeText(text);
            else {const area = document.createElement('textarea'); area.value = text; document.body.append(area); area.select(); const done = document.execCommand('copy'); area.remove(); if (!done) throw new Error();}
            status.textContent = 'Списъкът е копиран.';
        } catch (_) {
            const area = document.createElement('textarea'); area.className = 'form-control'; area.value = text; area.setAttribute('aria-label', 'Списък за ръчно копиране'); status.replaceChildren(document.createTextNode('Маркирай и копирай списъка: '), area); area.select();
        }
    });
})();
