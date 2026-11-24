INSERT INTO gym_photos (gym_id, image_url, alt_text, sort_order, cover_photo)
SELECT gym.id, '/images/gyms/rea-fitness-ruse-01-optimized.jpg', gym.name, 0,
       NOT EXISTS (
           SELECT 1 FROM gym_photos cover
           WHERE cover.gym_id = gym.id AND cover.cover_photo = TRUE
       )
FROM gyms gym
WHERE gym.slug = 'rhea-sport-beauty-ruse'
  AND NOT EXISTS (
      SELECT 1 FROM gym_photos photo
      WHERE photo.gym_id = gym.id AND photo.image_url = '/images/gyms/rea-fitness-ruse-01-optimized.jpg'
  );
