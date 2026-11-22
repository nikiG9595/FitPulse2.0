(() => {
    const dialog = document.querySelector('[data-media-dialog]');
    if (!dialog) return;
    const banner = document.querySelector('[data-media-banner]');
    const checkbox = dialog.querySelector('[data-media-checkbox]');
    const storageKey = 'fitpulse-media-choice-v1';
    let choice;

    function apply(value) {
        choice = value === 'allow' || value === 'deny' ? value : null;
        const allowed = choice === 'allow';
        banner.hidden = choice !== null;
        document.documentElement.dataset.externalMedia = allowed ? 'allowed' : 'blocked';
        document.querySelectorAll('[data-external-src]').forEach(media => {
            if (allowed && !media.hasAttribute('src')) {
                if (media.dataset.mediaFallback !== undefined) media.nextElementSibling.hidden = true;
                media.hidden = false;
                media.setAttribute('src', media.dataset.externalSrc);
            } else if (!allowed) {
                media.removeAttribute('src');
                if (media.dataset.mediaFallback !== undefined) media.nextElementSibling.hidden = false;
            }
        });
    }

    function save(value) {
        try { localStorage.setItem(storageKey, value); } catch { /* The choice applies on this page. */ }
        apply(value);
        if (dialog.open) dialog.close();
    }

    let saved;
    try { saved = localStorage.getItem(storageKey); } catch { /* External media stays blocked by default. */ }
    apply(saved);
    document.querySelectorAll('[data-media-choice]').forEach(button => {
        button.addEventListener('click', () => save(button.dataset.mediaChoice));
    });
    document.querySelectorAll('[data-media-settings]').forEach(button => {
        button.hidden = false;
        button.addEventListener('click', () => {
            checkbox.checked = choice === 'allow';
            dialog.showModal();
        });
    });
    dialog.querySelector('[data-media-save]').addEventListener('click', () => save(checkbox.checked ? 'allow' : 'deny'));
    window.addEventListener('storage', event => {
        if (event.key === storageKey || event.key === null) apply(event.newValue);
    });
})();
