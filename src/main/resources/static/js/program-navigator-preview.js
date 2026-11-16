// Reuse the navigator's SVG and anatomy styles without interactive muscle controls.
document.querySelectorAll('[data-navigator-preview]').forEach(async host => {
    try {
        const response = await fetch(host.dataset.navigatorPreview, {credentials: 'same-origin'});
        if (!response.ok) return;
        const documentSvg = new DOMParser().parseFromString(await response.text(), 'image/svg+xml');
        if (documentSvg.querySelector('parsererror') || documentSvg.documentElement.localName !== 'svg') return;
        const svg = document.importNode(documentSvg.documentElement, true);
        svg.setAttribute('aria-hidden', 'true');
        svg.setAttribute('focusable', 'false');
        svg.querySelectorAll('[tabindex]').forEach(element => element.removeAttribute('tabindex'));
        host.replaceChildren(svg);
    } catch (_) {
        // The existing SVG image remains visible if the styled preview cannot load.
    }
});
