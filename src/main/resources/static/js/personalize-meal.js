(() => {
    const messages = window.fitPulseMessages;
    const form = document.getElementById('meal-personalization');
    if (!form) return;
    const result = document.getElementById('personalization-result');
    const fields = document.getElementById('personalization-fields');
    const add = document.getElementById('add-personal-meal');
    const token = form.querySelector('[name="previewToken"]');
    const stale = document.getElementById('personalization-stale');
    const weight = form.querySelector('[name="weight"]');
    const editWeight = document.getElementById('change-weight');
    const saveWeight = document.getElementById('save-weight');
    const weightStatus = document.getElementById('weight-status');
    editWeight?.addEventListener('click', () => {
        weight.readOnly = false; weight.focus(); weight.select();
        editWeight.hidden = true; saveWeight.hidden = false;
    });
    saveWeight?.addEventListener('click', async event => {
        event.preventDefault();
        if (!weight.reportValidity()) return;
        saveWeight.disabled = true; weightStatus.textContent = '';
        try {
            const response = await fetch(saveWeight.formAction, {method:'POST', body:new FormData(form), headers:{Accept:'application/json'}});
            if(!response.ok) throw new Error();
            await response.json();
            weight.readOnly = true; saveWeight.hidden = true;
            if(editWeight) editWeight.hidden = false;
            weightStatus.textContent = messages['ui.weight.saved'];
            invalidate();
        } catch { weightStatus.textContent = messages['ui.could.not.save.your.weight.try.again']; }
        finally { saveWeight.disabled = false; }
    });
    let saving = false;
    const invalidate = () => {
        if (!result) return;
        result.hidden = true;
        fields.hidden = false;
        if (add) add.hidden = true;
        if (token) token.value = '';
        stale.hidden = false;
    };
    form.querySelectorAll('[name="sex"],[name="birthDate"],[name="height"],[name="weight"],[name="activity"],[name="eligible"]').forEach(input => {
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
