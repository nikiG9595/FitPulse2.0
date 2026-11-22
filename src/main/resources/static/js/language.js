(() => {
    const button = document.querySelector('[data-language-toggle]');
    if (!button) return;

    const draftKey = 'fitpulse-language-draft';
    const page = new URL(location.href);
    const languageMarker = /[?&]__fitpulseLanguage=(?:bg|en)$/;
    if (languageMarker.test(page.search)) {
        page.search = page.search.replace(languageMarker, '');
        history.replaceState(history.state, '', page.href);
    }
    const supported = ['text', 'email', 'number', 'search', 'tel', 'url', 'date',
        'datetime-local', 'time', 'textarea', 'select-one', 'select-multiple', 'checkbox', 'radio'];
    const fields = () => [...document.forms].flatMap((form, formIndex) =>
        [...form.elements].filter(field => field.name && field.name !== 'termsAccepted'
            && supported.includes(field.type)).map((field, index) => ({
            field,
            key: JSON.stringify([formIndex, form.getAttribute('action'), index, field.name, field.type])
        })));

    function dirty(field) {
        if (field.type === 'checkbox' || field.type === 'radio') return field.checked !== field.defaultChecked;
        if (field instanceof HTMLSelectElement) {
            const hasDefault = [...field.options].some(option => option.defaultSelected);
            return [...field.options].some((option, index) =>
                option.selected !== (option.defaultSelected || (!field.multiple && !hasDefault && index === 0)));
        }
        return field.value !== field.defaultValue;
    }

    try {
        const draft = JSON.parse(sessionStorage.getItem(draftKey));
        sessionStorage.removeItem(draftKey);
        if (draft?.url === page.href && Date.now() - draft.time < 30000) {
            for (const {field, key} of fields()) {
                if (!Object.hasOwn(draft.values, key)) continue;
                const value = draft.values[key];
                if (field.type === 'checkbox' || field.type === 'radio') field.checked = value;
                else if (field instanceof HTMLSelectElement) {
                    for (const option of field.options) option.selected = value.includes(option.value);
                } else field.value = value;
            }
        }
    } catch {
        // Language switching remains available when browser storage is disabled.
    }

    button.hidden = false;
    button.addEventListener('click', () => {
        if ([...document.querySelectorAll('input[type="password"], input[type="file"]')].some(field => field.value)) {
            alert(button.dataset.languageBlocked);
            return;
        }

        const values = Object.fromEntries(fields().filter(({field}) => dirty(field)
            || field.form?.querySelector('[aria-invalid="true"], .is-invalid, .alert-danger, .field-error'))
            .map(({field, key}) => {
            if (field.type === 'checkbox' || field.type === 'radio') return [key, field.checked];
            if (field instanceof HTMLSelectElement) return [key, [...field.selectedOptions].map(option => option.value)];
            return [key, field.value];
        }));
        try {
            if (Object.keys(values).length) {
                sessionStorage.setItem(draftKey, JSON.stringify({url: page.href, time: Date.now(), values}));
            }
        } catch {
            alert(button.dataset.languageBlocked);
            return;
        }

        const next = document.documentElement.lang === 'en' ? 'bg' : 'en';
        const secure = location.protocol === 'https:' ? '; Secure' : '';
        document.cookie = `fitpulse-language=${next}; Path=${button.dataset.languagePath}; Max-Age=31536000; SameSite=Lax${secure}`;
        // A GET with a different query also refreshes pages reached by POST or with an anchor.
        const destination = new URL(page.href);
        destination.search += (page.search ? '&' : '?') + '__fitpulseLanguage=' + next;
        location.replace(destination.href);
    });
})();
