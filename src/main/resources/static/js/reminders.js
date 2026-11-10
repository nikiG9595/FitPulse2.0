(() => {
    const banner = document.getElementById('workout-reminders');
    if (!banner) return;
    let busy = false;
    async function refresh() {
        if (busy || document.hidden) return;
        busy = true;
        try {
            const response = await fetch(banner.dataset.url, {headers: {'Accept': 'application/json'}, cache: 'no-store'});
            if (!response.ok || !response.headers.get('content-type')?.includes('application/json')) {
                banner.hidden = true;
                return;
            }
            const reminders = await response.json();
            banner.hidden = reminders.length === 0;
            banner.querySelector('span').textContent = reminders.map(r => {
                // Calendar timestamps are Sofia local wall time; keep them in that timezone.
                const [date, time] = r.startsAt.split('T');
                return `${r.title} · ${date.split('-').reverse().join('.')} ${time.slice(0, 5)}`;
            }).join(' | ');
        } catch (_) {
            // Preserve the page if the connection is temporarily unavailable.
        } finally { busy = false; }
    }
    refresh();
    setInterval(refresh, 60000);
    document.addEventListener('visibilitychange', refresh);
})();
