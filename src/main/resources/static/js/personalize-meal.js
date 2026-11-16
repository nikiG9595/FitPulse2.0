(() => {
    const form = document.getElementById('meal-personalization');
    if (!form) return;
    const result = document.getElementById('personalization-result');
    const add = document.getElementById('add-personal-meal');
    const token = form.querySelector('[name="previewToken"]');
    const stale = document.getElementById('personalization-stale');
    let saving = false;
    const invalidate = () => {
        if (!result) return;
        result.hidden = true;
        if (add) add.hidden = true;
        if (token) token.value = '';
        stale.hidden = false;
    };
    form.querySelectorAll('[name="sex"],[name="age"],[name="height"],[name="weight"],[name="activity"],[name="eligible"]').forEach(input => {
        input.addEventListener('input', invalidate);
        input.addEventListener('change', invalidate);
    });
    form.addEventListener('submit', event => {
        if (saving) { event.preventDefault(); return; }
        if (event.submitter?.value !== 'confirm') return;
        if (!token?.value || !result || result.hidden) { event.preventDefault(); invalidate(); return; }
        saving = true;
        add.setAttribute('aria-busy', 'true');
        // Keep the submitting button enabled so its action=confirm is sent with the form.
    });
    // A restored page must not reuse an old draft or a prior successful save.
    window.addEventListener('pageshow', event => {
        if (event.persisted) { saving = false; invalidate(); }
    });
})();
