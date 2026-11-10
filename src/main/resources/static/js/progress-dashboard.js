(() => {
    const dashboard = document.getElementById('progress-dashboard');
    if (!dashboard) return;
    const period = document.getElementById('progress-period');
    const bodyType = document.getElementById('body-type');
    const liftType = document.getElementById('lift-type');
    const error = document.getElementById('progress-error');
    const number = new Intl.NumberFormat('bg-BG', {maximumFractionDigits: 2});
    const date = value => new Date(value + 'T12:00:00').toLocaleDateString('bg-BG');
    const charts = {};
    let data, request;
    function text(id, value) { document.getElementById(id).textContent = value; }
    function graph(name, points, unit, label) {
        charts[name]?.destroy(); delete charts[name];
        const canvas = document.getElementById(name + '-chart');
        const empty = document.getElementById(name + '-empty');
        canvas.hidden = !points.length;
        empty.hidden = !!points.length;
        if (!points.length) return;
        if (!window.Chart) {
            canvas.hidden = true; empty.hidden = false;
            empty.textContent = 'Графиката не може да се зареди. Записите ти са запазени и са достъпни в историята.';
            return;
        }
        charts[name] = new Chart(canvas, {
            type: 'line', data: {labels: points.map(p => date(p.date)), datasets: [{label, data: points.map(p => Number(p.value)), borderColor: '#ff763f', backgroundColor: '#ff763f12', fill: true, tension: .18, pointRadius: points.length > 100 ? 1 : 4, pointHoverRadius: 7}]},
            options: {responsive: true, maintainAspectRatio: false, animation: window.matchMedia('(prefers-reduced-motion: reduce)').matches ? false : {duration: 250}, interaction: {mode: 'index', intersect: false}, plugins: {legend: {display: false}, tooltip: {callbacks: {label: context => `${label}: ${number.format(context.parsed.y)} ${unit}`}}}, scales: {x: {title: {display: true, text: 'Дата', color: '#aab1bf'}, ticks: {color: '#aab1bf', maxTicksLimit: 8}, grid: {color: '#ffffff05'}}, y: {title: {display: true, text: unit, color: '#aab1bf'}, ticks: {color: '#aab1bf'}, grid: {color: '#ffffff0b'}}}}
        });
    }
    function metric(label, value) {
        const span = document.createElement('span');
        const strong = document.createElement('strong');
        strong.textContent = value;
        span.append(document.createTextNode(label + ': '), strong);
        return span;
    }
    function body() {
        if (!data) return;
        graph('body', data.body[bodyType.value] || [], 'Сантиметри', bodyType.selectedOptions[0].textContent);
    }
    function lift() {
        if (!data) return;
        const entry = data.lifts.find(l => l.key === liftType.value);
        graph('lift', entry?.points || [], 'Килограми', 'Максимална работна тежест');
        const metrics = document.getElementById('lift-metrics'); metrics.replaceChildren();
        if (entry) metrics.append(metric('Последна за периода', entry.last == null ? 'Няма запис' : number.format(entry.last) + ' кг'), metric('Личен рекорд за всички записи', number.format(entry.best) + ' кг'));
        document.getElementById('lift-history').replaceChildren(...(entry?.points || []).slice().reverse().map(point => {
            const row = document.createElement('li'); row.textContent = `${date(point.date)} · ${number.format(point.value)} кг`; return row;
        }));
    }
    async function load() {
        request?.abort(); request = new AbortController();
        try {
            const url = new URL(dashboard.dataset.url, window.location.origin); url.searchParams.set('period', period.value);
            const response = await fetch(url, {cache: 'no-store', signal: request.signal, headers: {'Accept': 'application/json'}});
            if (!response.ok || !response.headers.get('content-type')?.includes('application/json')) throw new Error();
            data = await response.json(); error.hidden = true;
            text('current-weight', data.current == null ? '—' : number.format(data.current) + ' кг');
            text('weight-date', data.lastDate ? date(data.lastDate) : 'Няма измерване');
            text('weight-change', data.change == null ? '—' : (Number(data.change) > 0 ? '+' : '') + number.format(data.change) + ' кг');
            text('completed-workouts', data.completed); text('personal-records', data.records);
            graph('weight', data.weights, 'Килограми', 'Тегло');
            const metrics = document.getElementById('weight-metrics'); metrics.replaceChildren();
            if (data.weights.length) {
                const values = data.weights.map(p => Number(p.value));
                metrics.append(metric('Начално за периода', number.format(values[0]) + ' кг'), metric('Последно за периода', number.format(values.at(-1)) + ' кг'), metric('Минимум', number.format(Math.min(...values)) + ' кг'), metric('Максимум', number.format(Math.max(...values)) + ' кг'));
            }
            const previous = liftType.value;
            liftType.replaceChildren(...data.lifts.map(l => {const option = document.createElement('option'); option.value = l.key; option.textContent = l.name; return option;}));
            if (data.lifts.some(l => l.key === previous)) liftType.value = previous;
            if (!data.lifts.length) {const option = document.createElement('option'); option.textContent = 'Няма записани упражнения'; liftType.append(option);}
            body(); lift();
        } catch (e) {
            if (e.name !== 'AbortError') {error.hidden = false; error.textContent = 'Не успяхме да заредим графиките. Опитай отново или използвай историята по-долу.';}
        }
    }
    period.addEventListener('change', load); bodyType.addEventListener('change', body); liftType.addEventListener('change', lift); load();
})();
