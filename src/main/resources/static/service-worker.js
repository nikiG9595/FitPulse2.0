const scope = new URL(self.registration.scope);
const cachePrefix = `fitpulse-static-${scope.pathname}-`;
const cacheName = `${cachePrefix}v1`;
const assets = new Map(Object.entries({
    'css/bootstrap-5.3.3.min.css': 'text/css',
    'css/style.css': 'text/css',
    'css/theme.css': 'text/css',
    'css/fitpulse-2.css': 'text/css',
    'css/mobile-app.css': 'text/css',
    'js/mobile-app.js': /^(?:text|application)\/javascript$/,
    'images/pwa/fitness-hero.jpg': 'image/jpeg',
    'images/pwa/icon-192.png': 'image/png',
    'images/pwa/icon-512.png': 'image/png',
    'images/pwa/icon-maskable-512.png': 'image/png',
    'images/pwa/apple-touch-icon.png': 'image/png'
}).map(([path, type]) => [new URL(path, scope).href, type]));

self.addEventListener('activate', event => {
    event.waitUntil(caches.keys().then(keys => Promise.all(keys
        .filter(key => key.startsWith(cachePrefix) && key !== cacheName)
        .map(key => caches.delete(key)))));
});

self.addEventListener('fetch', event => {
    const request = event.request;
    const type = assets.get(request.url);
    if (request.method !== 'GET' || request.mode === 'navigate' || !type ||
        request.headers.has('Authorization') || request.headers.has('Range')) return;
    event.respondWith(loadAsset(request.url, type));
});

async function loadAsset(url, type) {
    // Only anonymous, non-redirected responses with the expected static MIME type enter the cache.
    try {
        const response = await fetch(url, { credentials: 'omit', redirect: 'error' });
        const mime = (response.headers.get('Content-Type') || '').split(';')[0].trim();
        if (response.ok && (typeof type === 'string' ? type === mime : type.test(mime))) {
            try {
                const cache = await caches.open(cacheName);
                await cache.put(url, response.clone());
            } catch {
                // A storage failure must not prevent loading the original response.
            }
        }
        return response;
    } catch {
        return (await caches.match(url, { cacheName })) || Response.error();
    }
}
