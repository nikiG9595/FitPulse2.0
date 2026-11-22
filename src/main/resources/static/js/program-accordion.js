(() => {
    document.querySelectorAll('.session-grid').forEach(grid => {
        const sessions = [...grid.querySelectorAll(':scope > details.session-card')];
        sessions.forEach(session => session.addEventListener('toggle', () => {
            if (session.open) sessions.forEach(other => { if (other !== session) other.open = false; });
        }));
    });

    if (CSS.supports('selector(details::details-content)') &&
            CSS.supports('interpolate-size', 'allow-keywords')) return;

    const reducedMotion = matchMedia('(prefers-reduced-motion: reduce)');
    document.querySelectorAll('.program-exercise').forEach(exercise => {
        const summary = exercise.querySelector(':scope > summary');
        const content = exercise.querySelector('.program-exercise-content');
        let animation;
        let expanding;
        summary.addEventListener('click', event => {
            if (reducedMotion.matches || !content.animate) return;
            event.preventDefault();
            expanding = animation ? !expanding : !exercise.open;
            const start = exercise.open ? content.getBoundingClientRect().height : 0;
            animation?.cancel();
            exercise.open = true;
            animation = content.animate(
                {height: [`${start}px`, expanding ? `${content.getBoundingClientRect().height}px` : '0px']},
                {duration: 230, easing: 'ease', fill: 'both'}
            );
            animation.onfinish = () => {
                exercise.open = expanding;
                animation.cancel();
                animation = null;
            };
        });
    });
})();
