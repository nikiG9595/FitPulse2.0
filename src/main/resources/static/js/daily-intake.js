(() => {
    const host = document.querySelector('[data-intake-url]');
    if (!host) return;
    const mobile = matchMedia('(max-width: 767px)');
    const content = host.querySelector('[data-intake-content]');
    const status = host.querySelector('[data-intake-status]');
    const retry = host.querySelector('[data-intake-retry]');
    let busy = false;

    function showError(visible) {
        status.hidden = !visible;
        retry.hidden = !visible;
    }

    async function loadCard(openPortions = []) {
        const response = await fetch(host.dataset.intakeUrl, { cache: 'no-store' });
        if (!response.ok || response.redirected) throw new Error('Cannot load daily intake');
        content.innerHTML = await response.text();
        openPortions.forEach(id => {
            const details = document.getElementById(id);
            if (details) details.open = true;
        });
    }

    async function refresh(form, control) {
        if (!mobile.matches || busy) return;
        busy = true;
        showError(false);
        const openPortions = [...content.querySelectorAll('details[open]')].map(details => details.id);
        const originalControls = [...content.querySelectorAll('input[type="checkbox"], select')];
        const data = form ? new URLSearchParams(new FormData(form)) : null;
        const oldPlan = control?.selectedOptions
            ? [...control.options].find(option => option.defaultSelected)?.value
            : null;
        originalControls.forEach(item => { item.disabled = true; });
        host.setAttribute('aria-busy', 'true');
        try {
            let failed = false;
            if (form) {
                try {
                    const response = await fetch(form.action, { method: 'POST', body: data, cache: 'no-store' });
                    failed = response.status !== 204 || response.redirected;
                } catch {
                    failed = true;
                }
            }
            await loadCard(control?.tagName === 'SELECT' ? [] : openPortions);
            showError(failed);
        } catch {
            if (control?.type === 'checkbox') control.checked = control.defaultChecked;
            if (oldPlan) control.value = oldPlan;
            showError(true);
        } finally {
            originalControls.forEach(item => { item.disabled = false; });
            busy = false;
            host.removeAttribute('aria-busy');
            if (control) document.getElementById(control.id)?.focus({ preventScroll: true });
        }
    }

    content.addEventListener('change', event => {
        const control = event.target;
        if (control.form && control.matches('input[type="checkbox"], select')) refresh(control.form, control);
    });
    retry.addEventListener('click', () => refresh());
    mobile.addEventListener('change', () => refresh());
    document.addEventListener('visibilitychange', () => {
        if (!document.hidden) refresh();
    });
    refresh();
})();
