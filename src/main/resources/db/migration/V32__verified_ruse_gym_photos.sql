-- Attach verified photos to existing Ruse gyms and keep any administrator-selected cover.
INSERT INTO gym_photos (gym_id, image_url, alt_text, sort_order, cover_photo)
SELECT gym.id, source.image_url, gym.name, source.sort_order,
       source.sort_order = 0 AND NOT EXISTS (
           SELECT 1 FROM gym_photos existing_cover
           WHERE existing_cover.gym_id = gym.id AND existing_cover.cover_photo = TRUE
       )
FROM (
    SELECT 'adeo-sport-spa-ruse' AS gym_slug, '/images/gyms/adeo-sport-spa-ruse-01.webp' AS image_url, 0 AS sort_order
    UNION ALL
    SELECT 'adeo-sport-spa-ruse', '/images/gyms/adeo-sport-spa-ruse-02.webp', 1
    UNION ALL
    SELECT 'adeo-sport-spa-ruse', '/images/gyms/adeo-sport-spa-ruse-03.webp', 2
    UNION ALL
    SELECT 'atlet-fitness-ruse', '/images/gyms/atlet-fitness-ruse-01.webp', 0
    UNION ALL
    SELECT 'atlet-fitness-ruse', '/images/gyms/atlet-fitness-ruse-02.webp', 1
    UNION ALL
    SELECT 'atlet-fitness-ruse', '/images/gyms/atlet-fitness-ruse-03.webp', 2
    UNION ALL
    SELECT 'big-dogs-gym-ruse', '/images/gyms/big-dogs-gym-ruse-01.webp', 0
    UNION ALL
    SELECT 'big-dogs-gym-ruse', '/images/gyms/big-dogs-gym-ruse-02.webp', 1
    UNION ALL
    SELECT 'cosmo-gym-ruse', '/images/gyms/cosmo-gym-ruse-01.webp', 0
    UNION ALL
    SELECT 'cosmo-gym-ruse', '/images/gyms/cosmo-gym-ruse-02.webp', 1
    UNION ALL
    SELECT 'ego-fitness-spa-ruse', '/images/gyms/ego-fitness-spa-ruse-01.webp', 0
    UNION ALL
    SELECT 'ego-fitness-spa-ruse', '/images/gyms/ego-fitness-spa-ruse-02.webp', 1
    UNION ALL
    SELECT 'fitness-flex-ruse', '/images/gyms/fitness-flex-ruse-01.webp', 0
    UNION ALL
    SELECT 'fitness-flex-ruse', '/images/gyms/fitness-flex-ruse-02.webp', 1
    UNION ALL
    SELECT 'fitness-sila-borisova-ruse', '/images/gyms/fitness-sila-borisova-ruse-01.webp', 0
    UNION ALL
    SELECT 'fitness-sila-borisova-ruse', '/images/gyms/fitness-sila-borisova-ruse-02.webp', 1
    UNION ALL
    SELECT 'fitness-sila-vazrazhdane-ruse', '/images/gyms/fitness-sila-vazrazhdane-ruse-01.webp', 0
    UNION ALL
    SELECT 'fitness-sila-vazrazhdane-ruse', '/images/gyms/fitness-sila-vazrazhdane-ruse-02.webp', 1
    UNION ALL
    SELECT 'fitness-titan-ruse', '/images/gyms/fitness-titan-ruse-01.webp', 0
    UNION ALL
    SELECT 'fitness-titan-ruse', '/images/gyms/fitness-titan-ruse-02.webp', 1
    UNION ALL
    SELECT 'fitness-titan-ruse', '/images/gyms/fitness-titan-ruse-03.webp', 2
    UNION ALL
    SELECT 'fitness-tri-set-ruse', '/images/gyms/fitness-tri-set-ruse-01.webp', 0
    UNION ALL
    SELECT 'fitness-tri-set-ruse', '/images/gyms/fitness-tri-set-ruse-02.webp', 1
    UNION ALL
    SELECT 'millennia-24-7-ruse', '/images/gyms/millennia-24-7-ruse-01.webp', 0
    UNION ALL
    SELECT 'millennia-24-7-ruse', '/images/gyms/millennia-24-7-ruse-02.webp', 1
    UNION ALL
    SELECT 'millennia-24-7-ruse', '/images/gyms/millennia-24-7-ruse-03.webp', 2
    UNION ALL
    SELECT 'move-gym-ruse', '/images/gyms/move-gym-ruse-01.webp', 0
    UNION ALL
    SELECT 'move-gym-ruse', '/images/gyms/move-gym-ruse-02.webp', 1
    UNION ALL
    SELECT 'onyx-sport-center-ruse', '/images/gyms/onyx-sport-center-ruse-01.webp', 0
    UNION ALL
    SELECT 'onyx-sport-center-ruse', '/images/gyms/onyx-sport-center-ruse-02.webp', 1
    UNION ALL
    SELECT 'onyx-sport-center-ruse', '/images/gyms/onyx-sport-center-ruse-03.webp', 2
) source
JOIN gyms gym ON gym.slug = source.gym_slug
JOIN cities city ON city.id = gym.city_id AND city.slug = 'ruse'
WHERE NOT EXISTS (
    SELECT 1 FROM gym_photos existing_photo
    WHERE existing_photo.gym_id = gym.id AND existing_photo.image_url = source.image_url
);
