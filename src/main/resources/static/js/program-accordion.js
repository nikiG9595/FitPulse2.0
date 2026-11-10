(() => {
    document.querySelectorAll('.session-grid').forEach(grid => {
        const sessions = [...grid.querySelectorAll(':scope > details.session-card')];
        sessions.forEach(session => session.addEventListener('toggle', () => {
            if (session.open) sessions.forEach(other => { if (other !== session) other.open = false; });
        }));
    });
})();
