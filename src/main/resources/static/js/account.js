document.querySelectorAll('input[type="password"]').forEach(input => {
    const wrapper = document.createElement('div');
    wrapper.className = 'password-field';
    input.before(wrapper);
    wrapper.append(input);

    const button = document.createElement('button');
    button.type = 'button';
    button.className = 'password-toggle';
    const show = document.querySelector('meta[name="password-show"]').content;
    const hide = document.querySelector('meta[name="password-hide"]').content;
    const update = visible => {
        button.setAttribute('aria-label', visible ? hide : show);
        button.title = visible ? hide : show;
        button.classList.toggle('password-visible', visible);
    };
    button.innerHTML = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" aria-hidden="true"><path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6-10-6-10-6Z"/><circle cx="12" cy="12" r="2.5"/><path class="eye-slash" d="m4 3 16 18"/></svg>';
    update(false);
    button.addEventListener('click', () => {
        const visible = input.type === 'password';
        input.type = visible ? 'text' : 'password';
        update(visible);
    });
    wrapper.append(button);
});
const avatarMenu = document.querySelector('.nav-avatar-menu');
if (avatarMenu) {
    document.addEventListener('click', event => {
        if (!avatarMenu.contains(event.target)) avatarMenu.open = false;
    });
    avatarMenu.addEventListener('keydown', event => {
        if (event.key === 'Escape' && avatarMenu.open) {
            avatarMenu.open = false;
            avatarMenu.querySelector('summary').focus();
        }
    });
}
