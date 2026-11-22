(() => {
    const root = document.documentElement;
    const storageKey = 'fitpulse-theme';
    let toggle;

    function applyTheme(value) {
        const theme = value === 'light' ? 'light' : 'dark';
        const changed = root.dataset.bsTheme !== theme;
        root.dataset.bsTheme = theme;
        toggle?.setAttribute('aria-pressed', String(theme === 'light'));
        if (toggle) {
            toggle.title = theme === 'light' ? toggle.dataset.enableDark : toggle.dataset.enableLight;
            if (changed) document.dispatchEvent(new Event('fitpulse:themechange'));
        }
    }

    let savedTheme;
    try {
        savedTheme = localStorage.getItem(storageKey);
    } catch {
        // The theme can still be changed for this page when storage is unavailable.
    }
    applyTheme(savedTheme);

    document.addEventListener('DOMContentLoaded', () => {
        toggle = document.querySelector('[data-theme-toggle]');
        if (!toggle) return;

        applyTheme(root.dataset.bsTheme);
        toggle.hidden = false;
        let transitionTimer;
        toggle.addEventListener('click', () => {
            const nextTheme = root.dataset.bsTheme === 'dark' ? 'light' : 'dark';
            root.classList.add('theme-changing');
            clearTimeout(transitionTimer);
            transitionTimer = setTimeout(() => root.classList.remove('theme-changing'), 200);
            applyTheme(nextTheme);
            try {
                localStorage.setItem(storageKey, nextTheme);
            } catch {
                // Keep the applied theme for this page.
            }
        });
    });

    window.addEventListener('storage', event => {
        if (event.key === storageKey || event.key === null) applyTheme(event.newValue);
    });
})();
