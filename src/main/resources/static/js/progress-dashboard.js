(() => {
    const messages = window.fitPulseMessages;
    const dashboard = document.getElementById('progress-dashboard');
    if (!dashboard) return;
    const period = document.getElementById('progress-period');
    const bodyType = document.getElementById('body-type');
    const error = document.getElementById('progress-error');
    const number = new Intl.NumberFormat(document.documentElement.lang, {maximumFractionDigits: 2});
    const date = value => new Date(value + 'T12:00:00').toLocaleDateString(document.documentElement.lang);
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
            empty.textContent = messages['ui.the.chart.could.not.load.your.entries'];
            return;
        }
        const theme = getComputedStyle(document.documentElement);
        const chartColors = {
            accent: theme.getPropertyValue('--fp-accent').trim(),
            fill: theme.getPropertyValue('--fp-chart-fill').trim(),
            text: theme.getPropertyValue('--fp-muted').trim(),
            grid: theme.getPropertyValue('--fp-chart-grid').trim()
        };
        charts[name] = new Chart(canvas, {
            type: 'line', data: {labels: points.map(p => date(p.date)), datasets: [{label, data: points.map(p => Number(p.value)), borderColor: chartColors.accent, backgroundColor: chartColors.fill, fill: true, tension: .18, pointRadius: points.length > 100 ? 1 : 4, pointHoverRadius: 7}]},
            options: {responsive: true, maintainAspectRatio: false, animation: window.matchMedia('(prefers-reduced-motion: reduce)').matches ? false : {duration: 250}, interaction: {mode: 'index', intersect: false}, plugins: {legend: {display: false}, tooltip: {callbacks: {label: context => `${label}: ${number.format(context.parsed.y)} ${unit}`}}}, scales: {x: {title: {display: true, text: messages['ui.date'], color: chartColors.text}, ticks: {color: chartColors.text, maxTicksLimit: 8}, grid: {color: chartColors.grid}}, y: {title: {display: true, text: unit, color: chartColors.text}, ticks: {color: chartColors.text}, grid: {color: chartColors.grid}}}}
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
        graph('body', data.body[bodyType.value] || [], messages['ui.centimeters'], bodyType.selectedOptions[0].textContent);
    }
    function renderCharts() {
        if (!data) return;
        graph('weight', data.weights, messages['ui.kilograms'], messages['ui.weight']);
        body();
    }
    async function load() {
        histories.forEach(history=>history.reset());
        request?.abort(); request = new AbortController();
        const activeRequest=request;
        try {
            const url = new URL(dashboard.dataset.url, window.location.origin); url.searchParams.set('period', period.value);
            const response = await fetch(url, {cache: 'no-store', signal: request.signal, headers: {'Accept': 'application/json'}});
            if (!response.ok || !response.headers.get('content-type')?.includes('application/json')) throw new Error();
            const nextData = await response.json();
            if(activeRequest!==request || activeRequest.signal.aborted)return;
            data = nextData; error.hidden = true;
            text('current-weight', data.current == null ? '—' : number.format(data.current) + messages['ui.kg']);
            text('weight-date', data.lastDate ? date(data.lastDate) : messages['ui.no.measurement']);
            text('weight-change', data.change == null ? '—' : (Number(data.change) > 0 ? '+' : '') + number.format(data.change) + messages['ui.kg']);
            renderCharts();
            const metrics = document.getElementById('weight-metrics'); metrics.replaceChildren();
            if (data.weights.length) {
                const values = data.weights.map(p => Number(p.value));
                metrics.append(metric(messages['ui.first.in.period'], number.format(values[0]) + messages['ui.kg']), metric(messages['ui.last.in.period'], number.format(values.at(-1)) + messages['ui.kg']), metric(messages['ui.minimum'], number.format(Math.min(...values)) + messages['ui.kg']), metric(messages['ui.maximum'], number.format(Math.max(...values)) + messages['ui.kg']));
            }
        } catch (e) {
            if (e.name !== 'AbortError') {error.hidden = false; error.textContent = messages['ui.the.charts.could.not.load.try.again'];}
        }
    }
    const histories=[...document.querySelectorAll('[data-history]')].map(section=>{
        const rows=[...section.querySelectorAll('.history-row')];
        const more=section.querySelector('[data-show-more]');
        const empty=section.querySelector('[data-history-empty]');
        let limit=5;
        function render(){
            const from=dashboard.dataset['from'+period.value] || '';
            const matching=rows.filter(row=>row.dataset.recordedAt>=from && row.dataset.recordedAt<=dashboard.dataset.today);
            rows.forEach(row=>row.hidden=true);
            matching.slice(0,limit).forEach(row=>row.hidden=false);
            more.hidden=matching.length<=limit;
            empty.hidden=rows.length===0 || matching.length!==0;
        }
        more.addEventListener('click',()=>{limit+=5;render();});
        return {reset(){limit=5;render();}};
    });
    document.addEventListener('fitpulse:themechange', renderCharts);
    period.addEventListener('change', load); bodyType.addEventListener('change', body); load();
})();
