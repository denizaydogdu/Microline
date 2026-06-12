-- Higgsfield ile üretilen eğitim kapak görselleri (static/img/tutorial/)
UPDATE tutorial_posts SET cover_image_url = '/img/tutorial/kutu-ev.jpg'
WHERE slug_tr = 'ilk-kesiminiz-basit-kutu-ev';

UPDATE tutorial_posts SET cover_image_url = '/img/tutorial/robot-maske.jpg'
WHERE slug_tr = 'karton-robot-maskesi';

UPDATE tutorial_posts SET cover_image_url = '/img/tutorial/kelebek.jpg'
WHERE slug_tr = 'hareketli-kelebek';

UPDATE tutorial_posts SET cover_image_url = '/img/tutorial/kalin-karton.jpg'
WHERE slug_tr = 'kalin-kartonu-kolayca-kesme-teknikleri';

UPDATE tutorial_posts SET cover_image_url = '/img/tutorial/bilye-parkuru.jpg'
WHERE slug_tr = 'marble-run-bilye-parkuru';
