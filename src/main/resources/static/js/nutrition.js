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
    document.querySelectorAll('.grams-input').forEach(input => input.addEventListener('input', () => {
        input.dataset.dirty = 'true';
        const day = input.closest('.interactive-day');
        input.closest('.nutrition-meal').querySelector('.unsaved-note').hidden = false;
        input.closest('.nutrition-meal').querySelector('.unsaved-note').textContent = 'Преизчислено на екрана. Натисни „Запази“ до промененото количество.';
        update(day);
    }));
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
            if (input.value === sentValue) delete input.dataset.dirty;
            note.textContent = form.closest('.nutrition-meal').querySelector('[data-dirty="true"]')
                ? 'Записано. Има и други незапазени количества в това хранене.' : 'Количеството е запазено в личния ти режим.';
        } catch (_) {
            note.textContent = 'Количеството не е запазено. Провери стойността (1–2000 г), връзката и дали още си в профила си, след което опитай отново.';
        } finally {
            form.dataset.saving = 'false'; button.disabled = !input.checkValidity(); button.textContent = 'Запази';
        }
    }));
    // A server redirect can land on a day other than the first one.
    if (window.location.hash.startsWith('#menu-day-')) {
        const day = document.getElementById(window.location.hash.slice(1));
        if (day) {day.open = true; requestAnimationFrame(() => day.scrollIntoView({block: 'start'}));}
    }
    document.querySelectorAll('[data-shopping-check] input[type="checkbox"]').forEach(input => input.addEventListener('change', () => input.form.requestSubmit()));
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
