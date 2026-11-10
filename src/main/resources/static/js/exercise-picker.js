(() => {
    document.querySelectorAll('[data-exercise-picker]').forEach((input, index) => {
        const list = document.createElement('datalist');
        list.id = `exercise-options-${index}`;
        input.setAttribute('list', list.id);
        input.setAttribute('autocomplete', 'off');
        input.after(list);
        const id = input.form.querySelector('[name="exerciseId"]');
        let choices = [], timer, request;
        async function search() {
            request?.abort(); request = new AbortController();
            try {
                const url = new URL(input.dataset.searchUrl, window.location.origin);
                url.searchParams.set('q', input.value);
                const response = await fetch(url, {signal: request.signal});
                if (!response.ok) return;
                choices = await response.json();
                list.replaceChildren(...choices.map(exercise => {
                    const option = document.createElement('option');
                    option.value = exercise.name;
                    option.label = `${exercise.muscle} · ${exercise.equipment}`;
                    return option;
                }));
                select();
            } catch (_) { /* A free-text entry remains available during connection loss. */ }
        }
        function select() {
            const match = choices.find(e => e.name === input.value);
            if (id) id.value = match ? match.id : '';
        }
        input.addEventListener('input', () => {
            select(); clearTimeout(timer); timer = setTimeout(search, 180);
        });
        input.addEventListener('change', select);
        input.addEventListener('focus', search);
    });
})();
