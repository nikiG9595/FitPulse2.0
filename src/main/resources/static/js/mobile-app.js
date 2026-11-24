(() => {
    const workerUrl = document.currentScript.dataset.workerUrl;
    if (window.isSecureContext && 'serviceWorker' in navigator) {
        navigator.serviceWorker.register(workerUrl).catch(error => {
            console.warn('FitPulse service worker:', error);
        });
    }

    const mobile = matchMedia('(max-width: 767px)');
    const standalone = matchMedia('(display-mode: standalone)');
    const isStandalone = () => standalone.matches || navigator.standalone === true;
    const installButtons = [...document.querySelectorAll('[data-install-fitpulse]')];
    const installDialog = document.getElementById('fp-install-dialog');
    if (installDialog && installButtons.length) {
        const menuItem = document.querySelector('.fp-install-menu-item');
        const closeButton = installDialog.querySelector('[data-install-close]');
        let installPrompt;
        let installTrigger;
        let installed = false;

        function updateInstallButtons() {
            const visible = mobile.matches && !installed && !isStandalone();
            installButtons.forEach(button => { button.hidden = !visible; });
            if (menuItem) menuItem.hidden = !visible;
        }

        window.addEventListener('beforeinstallprompt', event => {
            if (!mobile.matches || isStandalone()) return;
            event.preventDefault();
            installPrompt = event;
        });
        window.addEventListener('appinstalled', () => {
            installed = true;
            installPrompt = null;
            updateInstallButtons();
            if (installDialog.open) closeInstructions();
        });

        function restoreFocus() {
            const target = installTrigger?.getClientRects().length
                ? installTrigger
                : document.getElementById('fitpulse-menu-toggle');
            target?.focus();
        }

        function closeInstructions() {
            if (typeof installDialog.close === 'function') installDialog.close();
            else {
                installDialog.removeAttribute('open');
                restoreFocus();
            }
        }

        function showInstructions() {
            const ios = /iPhone|iPad|iPod/.test(navigator.userAgent) ||
                (/Macintosh/.test(navigator.userAgent) && navigator.maxTouchPoints > 1);
            installDialog.querySelector('[data-install-ios]').hidden = !ios;
            installDialog.querySelector('[data-install-browser]').hidden = ios;
            if (typeof installDialog.showModal === 'function') installDialog.showModal();
            else installDialog.setAttribute('open', '');
            closeButton.focus();
        }

        installButtons.forEach(button => button.addEventListener('click', async () => {
            if (installed || isStandalone()) return;
            installTrigger = button;
            if (installPrompt) {
                const prompt = installPrompt;
                installPrompt = null;
                try {
                    await prompt.prompt();
                    const choice = await prompt.userChoice;
                    if (choice?.outcome === 'accepted') {
                        installed = true;
                        updateInstallButtons();
                    }
                    restoreFocus();
                    return;
                } catch {
                    // A browser can withdraw an installation prompt before it is used.
                }
            }
            showInstructions();
        }));
        closeButton.addEventListener('click', closeInstructions);
        installDialog.addEventListener('close', restoreFocus);
        mobile.addEventListener('change', () => {
            updateInstallButtons();
            if (!mobile.matches && installDialog.open) closeInstructions();
        });
        standalone.addEventListener('change', updateInstallButtons);
        updateInstallButtons();
    }

    const tabs = [...document.querySelectorAll('[data-mobile-tab]')];
    if (!tabs.length) return;
    const preferences = document.querySelector('.fp-installed-preferences');
    const controls = [...document.querySelectorAll('.fit-nav .nav-actions :is([data-theme-toggle], [data-language-toggle])')]
        .map(button => {
            const originalPosition = document.createComment('');
            button.before(originalPosition);
            return { button, originalPosition };
        });
    function updateNavigation() {
        const installed = mobile.matches && isStandalone();
        document.documentElement.classList.toggle('fp-installed-mobile', installed);
        if (!preferences) return;
        preferences.hidden = !installed;
        controls.forEach(({ button, originalPosition }) => {
            if (installed) {
                preferences.append(button);
                button.setAttribute('role', 'menuitem');
            } else {
                originalPosition.after(button);
                button.removeAttribute('role');
            }
        });
    }
    mobile.addEventListener('change', updateNavigation);
    standalone.addEventListener('change', updateNavigation);
    updateNavigation();
    const path = location.pathname.replace(/\/+$/, '');
    const root = new URL(tabs[0].href).pathname.replace(/\/+$/, '');
    const current = path.slice(root.length);
    const sections = {
        training: /^\/(?:programs|exercises|training-sessions)(?:\/|$)/,
        nutrition: /^\/meal-plans(?:\/|$)/,
        profile: /^\/(?:profile|progress|journal|calendar|dashboard)(?:\/|$)/
    };
    const active = current ? tabs.find(tab => sections[tab.dataset.mobileTab]?.test(current)) : tabs[0];
    active?.setAttribute('aria-current', 'page');

    const progress = document.querySelector('[data-progress-url]');
    if (!progress) return;
    async function loadProgress() {
        if (!mobile.matches) return;
        mobile.removeEventListener('change', loadProgress);
        const status = progress.querySelector('[data-progress-status]');
        try {
            const response = await fetch(progress.dataset.progressUrl, {
                cache: 'no-store',
                headers: { Accept: 'application/json' }
            });
            if (!response.ok || response.redirected) return;
            const data = await response.json();
            const locale = document.documentElement.lang;
            const formatter = new Intl.NumberFormat(locale, { maximumFractionDigits: 2 });
            const weights = progress.querySelector('[data-progress-weight]');
            const workouts = progress.querySelector('[data-progress-workouts]');
            const measurements = progress.querySelector('[data-progress-measurements]');
            const measurementCount = Object.values(data.body || {}).reduce((sum, values) => sum + values.length, 0);
            if (Number.isFinite(data.current)) {
                const number = new Intl.NumberFormat(locale, { style: 'unit', unit: 'kilogram', maximumFractionDigits: 2 });
                weights.querySelector('dd').textContent = number.format(data.current);
                weights.hidden = false;
            }
            if (data.completed > 0) {
                workouts.querySelector('dd').textContent = formatter.format(data.completed);
                workouts.hidden = false;
            }
            if (measurementCount > 0) {
                measurements.querySelector('dd').textContent = formatter.format(measurementCount);
                measurements.hidden = false;
            }
            const hasData = [weights, workouts, measurements].some(item => !item.hidden);
            progress.querySelector('[data-progress-values]').hidden = !hasData;
            progress.querySelector('[data-progress-empty]').hidden = hasData;
            status.hidden = true;
        } catch {
            // The progress link remains available when the summary cannot be retrieved.
        }
    }
    mobile.addEventListener('change', loadProgress);
    loadProgress();
})();
