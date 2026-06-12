-- Vitrin seed'i = prod içerik. Fiyatlar ve ürün adları açıkça KURGUSAL yer tutuculardır
-- (UNCONFIRMED — gerçek değerler netleşince yeni migration ile güncellenir).
-- Uzun HTML metinler dollar-quoted ($body$...$body$).

-- ── Koleksiyonlar ────────────────────────────────────────────────────────────

INSERT INTO collections (code, slug_tr, slug_en, name_tr, name_en, description_tr, description_en, hero_image_url, sort_order, active) VALUES
('CUTTING_TOOLS', 'kesim-araclari', 'cutting-tools', 'Kesim Araçları', 'Cutting Tools',
 'Tamamen kapalı bıçak sistemiyle çocukların güvenle kullanabileceği karton kesim makineleri. Yaratıcılığın ilk adımı burada başlar.',
 'Cardboard cutting machines kids can use safely, thanks to a fully enclosed blade system. Creativity starts here.',
 '/img/collection/kesim-araclari.jpg', 1, TRUE),
('ADD_ONS', 'eklentiler', 'add-ons', 'Eklentiler', 'Add-Ons',
 'Microline makinenizi büyüten bıçak modülleri ve aksesuarlar. Çocuğunuz büyüdükçe makinesi de onunla birlikte büyür.',
 'Blade modules and accessories that grow your Microline machine. As your child grows, their machine grows with them.',
 '/img/collection/eklentiler.jpg', 2, TRUE),
('PROJECT_KITS', 'proje-kitleri', 'project-kits', 'Proje Kitleri', 'Project Kits',
 'Önceden çizilmiş şablonlar ve adım adım yönergelerle eksiksiz karton proje setleri. Kutudan çıkar çıkmaz üretmeye başlayın.',
 'Complete cardboard project sets with pre-printed templates and step-by-step instructions. Start making right out of the box.',
 '/img/collection/proje-kitleri.jpg', 3, TRUE),
('BUNDLES', 'avantajli-setler', 'bundles', 'Avantajlı Setler', 'Money-Saving Bundles',
 'Makine, eklenti ve proje kitlerini bir arada sunan indirimli paketler. Ayrı ayrı almaktan her zaman daha avantajlı.',
 'Discounted packages that combine the machine, add-ons and project kits. Always a better deal than buying separately.',
 '/img/collection/avantajli-setler.jpg', 4, TRUE);

-- ── Amiral gemisi: Microline Kesim Makinesi (ML-C1) ─────────────────────────

INSERT INTO products (sku, slug_tr, slug_en, name_tr, name_en, tagline_tr, tagline_en,
                      description_tr, description_en, safety_notes_tr, safety_notes_en,
                      specs_json, price_amount, currency, compare_at_price, collection_id,
                      hero_image_url, featured, active, sort_order, meta_description_tr, meta_description_en)
VALUES (
 'ML-C1', 'microline-kesim-makinesi', 'microline-cutting-machine',
 'Microline Kesim Makinesi', 'Microline Cutting Machine',
 'Kapalı bıçak, sınırsız hayal gücü — 4 yaş ve üzeri için tamamen güvenli karton kesimi.',
 'Enclosed blade, unlimited imagination — completely safe cardboard cutting for ages 4 and up.',
 $body$<p>Microline, çocukların kartonu <strong>makasa ya da maket bıçağına hiç dokunmadan</strong> kesmesini sağlayan, çocuk güvenli bir kesim makinesidir. Bıçak tamamen kapalı bir modülün içindedir: parmaklar hiçbir açıdan bıçağa ulaşamaz, ama karton 3 mm kalınlığa kadar zahmetsizce kesilir.</p>
<p>İki hız kademesi sayesinde yeni başlayanlar yavaş ve kontrollü, deneyimli küçük üreticiler ise hızlı kesim yapabilir. <strong>Otomatik durdurma</strong> sistemi, makine zorlandığında ya da kapak açıldığında bıçağı anında durdurur.</p>
<ul>
<li>Tamamen kapalı, çocuk güvenli bıçak modülü</li>
<li>İki hız kademesi: öğrenme ve hızlı üretim</li>
<li>Zorlanmada ve kapak açılmasında otomatik durdurma</li>
<li>3 mm'ye kadar oluklu karton, kraft ve mukavva</li>
<li>4 yaş ve üzeri için tasarlandı</li>
</ul>
<p>Kargo kutularını oyuncaklara, maketlere ve sahne dekorlarına dönüştürün — Microline ile geri dönüşüm, ailecek yapılan bir oyuna dönüşür.</p>$body$,
 $body$<p>Microline is a kid-safe cutting machine that lets children cut cardboard <strong>without ever touching scissors or a utility knife</strong>. The blade lives inside a fully enclosed module: fingers cannot reach it from any angle, yet cardboard up to 3 mm thick cuts effortlessly.</p>
<p>Two speed settings let beginners cut slowly and in control, while experienced little makers can speed things up. The <strong>auto-stop</strong> system halts the blade instantly when the machine is strained or the cover is opened.</p>
<ul>
<li>Fully enclosed, kid-safe blade module</li>
<li>Two speeds: learning mode and fast production</li>
<li>Auto-stop on strain and on cover opening</li>
<li>Cuts corrugated cardboard, kraft and board up to 3 mm</li>
<li>Designed for ages 4 and up</li>
</ul>
<p>Turn shipping boxes into toys, models and stage props — with Microline, recycling becomes a game the whole family plays.</p>$body$,
 $body$<p>4 yaş ve üzeri içindir. Bıçak modülü tamamen kapalıdır; yine de ilk kullanımı bir yetişkin eşliğinde yapmanızı öneririz. Yalnızca karton ve kâğıt türevi malzemeler için tasarlanmıştır — plastik, kumaş veya ahşap kesmeyin. Bıçak modülünü yalnızca yetişkinler değiştirmelidir.</p>$body$,
 $body$<p>For ages 4 and up. The blade module is fully enclosed; we still recommend adult supervision for first use. Designed only for cardboard and paper-based materials — do not cut plastic, fabric or wood. Blade modules must be replaced by adults only.</p>$body$,
 '{"maxThicknessMm": 3, "speeds": 2, "age": "4+", "weightKg": 1.2, "power": "USB-C (5V/2A)", "autoStop": true, "materials": ["oluklu karton", "kraft", "mukavva"]}',
 4990.00, 'TRY', 5990.00,
 (SELECT id FROM collections WHERE code = 'CUTTING_TOOLS'),
 '/img/product/microline-1.jpg', TRUE, TRUE, 1,
 'Çocuk güvenli Microline karton kesim makinesi: kapalı bıçak, iki hız, otomatik durdurma. 4 yaş ve üzeri.',
 'Kid-safe Microline cardboard cutting machine: enclosed blade, two speeds, auto-stop. Ages 4 and up.');

INSERT INTO product_variants (product_id, sku, name_tr, name_en, price_delta, active, sort_order) VALUES
((SELECT id FROM products WHERE sku = 'ML-C1'), 'ML-C1-STD', 'Standart Paket', 'Standard Pack', 0.00, TRUE, 1),
((SELECT id FROM products WHERE sku = 'ML-C1'), 'ML-C1-PRO', 'Pro Paket (yedek bıçak modülü + taşıma çantası)', 'Pro Pack (spare blade module + carry case)', 500.00, TRUE, 2);

INSERT INTO product_images (product_id, url, alt_tr, alt_en, sort_order) VALUES
((SELECT id FROM products WHERE sku = 'ML-C1'), '/img/product/microline-1.jpg',
 'Microline Kesim Makinesi önden görünüm', 'Microline Cutting Machine, front view', 1),
((SELECT id FROM products WHERE sku = 'ML-C1'), '/img/product/microline-2.jpg',
 'Bir çocuk Microline ile karton keserken', 'A child cutting cardboard with Microline', 2),
((SELECT id FROM products WHERE sku = 'ML-C1'), '/img/product/microline-3.jpg',
 'Kapalı bıçak modülünün yakın çekimi', 'Close-up of the enclosed blade module', 3);

-- ── Eklenti: ProKesim Bıçak Modülü (ML-A1) ──────────────────────────────────

INSERT INTO products (sku, slug_tr, slug_en, name_tr, name_en, tagline_tr, tagline_en,
                      description_tr, description_en, safety_notes_tr, safety_notes_en,
                      specs_json, price_amount, currency, compare_at_price, collection_id,
                      hero_image_url, featured, active, sort_order, meta_description_tr, meta_description_en)
VALUES (
 'ML-A1', 'prokesim-bicak-modulu', 'prokesim-blade-module',
 'ProKesim Bıçak Modülü', 'ProKesim Blade Module',
 'Büyüyen üreticiler için: 8 yaş ve üzerine daha derin ve daha hızlı kesim.',
 'For growing makers: deeper, faster cuts for ages 8 and up.',
 $body$<p>ProKesim, Microline Kesim Makinesi'ne takılan, <strong>ileri yaş grubuna yönelik</strong> bıçak modülüdür. Standart modülle aynı kapalı güvenlik gövdesini kullanır; daha keskin geometrisi sayesinde kalın mukavvada bile tek geçişte temiz kenar bırakır.</p>
<p>Takmak saniyeler sürer: eski modülü çıkarın, ProKesim'i yerine oturtun. Makine modülü otomatik tanır ve hız ayarlarını buna göre düzenler.</p>$body$,
 $body$<p>ProKesim is a blade module for the Microline Cutting Machine aimed at <strong>older makers</strong>. It uses the same enclosed safety housing as the standard module; its sharper geometry leaves a clean edge even in thick board in a single pass.</p>
<p>Installation takes seconds: pop out the old module, click ProKesim into place. The machine recognises the module automatically and adjusts its speed settings.</p>$body$,
 $body$<p>8 yaş ve üzeri içindir. Modül değişimi yalnızca yetişkinler tarafından yapılmalıdır. Yalnızca Microline Kesim Makinesi ile kullanın.</p>$body$,
 $body$<p>For ages 8 and up. Module replacement must be done by adults only. Use only with the Microline Cutting Machine.</p>$body$,
 '{"maxThicknessMm": 3, "age": "8+", "compatibleWith": ["ML-C1"], "weightKg": 0.1}',
 990.00, 'TRY', NULL,
 (SELECT id FROM collections WHERE code = 'ADD_ONS'),
 '/img/product/prokesim-1.jpg', FALSE, TRUE, 1,
 'ProKesim bıçak modülü: Microline için 8 yaş ve üzerine daha derin, daha hızlı kesim eklentisi.',
 'ProKesim blade module: a deeper, faster cutting add-on for Microline, ages 8 and up.');

-- ── Proje kiti: Başlangıç Proje Kiti (ML-K1) ────────────────────────────────

INSERT INTO products (sku, slug_tr, slug_en, name_tr, name_en, tagline_tr, tagline_en,
                      description_tr, description_en, safety_notes_tr, safety_notes_en,
                      specs_json, price_amount, currency, compare_at_price, collection_id,
                      hero_image_url, featured, active, sort_order, meta_description_tr, meta_description_en)
VALUES (
 'ML-K1', 'baslangic-proje-kiti', 'starter-project-kit',
 'Başlangıç Proje Kiti', 'Starter Project Kit',
 'İlk gün üç proje: ev, robot ve uçak — şablonlar çizili, yönergeler hazır.',
 'Three projects on day one: a house, a robot and a plane — templates printed, instructions ready.',
 $body$<p>Başlangıç Proje Kiti, Microline ile ilk adımı atmanın en kolay yolu. Kutudan <strong>önceden çizilmiş 3 proje şablonu</strong> (ev, robot, uçak), adım adım resimli yönerge kitapçığı ve renkli yapışkan şeritler çıkar.</p>
<p>Her şablon, makinenin yavaş hız kademesiyle kesilecek şekilde tasarlandı — küçük eller ilk denemede başarıya ulaşır, özgüven kazanır.</p>$body$,
 $body$<p>The Starter Project Kit is the easiest first step with Microline. Inside the box: <strong>3 pre-printed project templates</strong> (a house, a robot, a plane), a step-by-step illustrated instruction booklet and coloured adhesive strips.</p>
<p>Every template is designed to be cut on the machine's slow speed setting — small hands succeed on the first try and gain confidence.</p>$body$,
 $body$<p>4 yaş ve üzeri içindir. Kit içeriği kesim makinesi içermez; Microline Kesim Makinesi ile birlikte kullanılır. Küçük parçalar içerir.</p>$body$,
 $body$<p>For ages 4 and up. The kit does not include a cutting machine; it is used together with the Microline Cutting Machine. Contains small parts.</p>$body$,
 '{"projects": 3, "age": "4+", "templates": ["ev", "robot", "uçak"], "requires": "ML-C1"}',
 690.00, 'TRY', NULL,
 (SELECT id FROM collections WHERE code = 'PROJECT_KITS'),
 '/img/product/baslangic-kiti-1.jpg', FALSE, TRUE, 1,
 'Başlangıç Proje Kiti: Microline için önceden çizilmiş 3 karton proje şablonu ve resimli yönergeler.',
 'Starter Project Kit: 3 pre-printed cardboard project templates and illustrated instructions for Microline.');

-- ── Avantajlı set: Microline + 3 Eklenti Seti (ML-B1) ───────────────────────

INSERT INTO products (sku, slug_tr, slug_en, name_tr, name_en, tagline_tr, tagline_en,
                      description_tr, description_en, safety_notes_tr, safety_notes_en,
                      specs_json, price_amount, currency, compare_at_price, collection_id,
                      hero_image_url, featured, active, sort_order, meta_description_tr, meta_description_en)
VALUES (
 'ML-B1', 'microline-3-eklenti-seti', 'microline-3-add-on-bundle',
 'Microline + 3 Eklenti Seti', 'Microline + 3 Add-On Bundle',
 'Hepsi bir arada: makine, ProKesim modülü, Başlangıç Kiti ve yedek bıçak — ayrı almaktan %13 avantajlı.',
 'All in one: the machine, the ProKesim module, the Starter Kit and a spare blade — 13% cheaper than buying separately.',
 $body$<p>Microline + 3 Eklenti Seti, atölyenizi tek seferde kurar: <strong>Microline Kesim Makinesi</strong>, ileri yaş için <strong>ProKesim Bıçak Modülü</strong>, ilk projeler için <strong>Başlangıç Proje Kiti</strong> ve bir yedek standart bıçak modülü aynı kutuda.</p>
<p>Parçaları ayrı ayrı almaya göre bütçenizde anlamlı bir fark bırakır; kardeşler için de ideal bir başlangıçtır — biri yavaş kademede öğrenirken diğeri ProKesim ile üretir.</p>$body$,
 $body$<p>The Microline + 3 Add-On Bundle sets up your workshop in one go: the <strong>Microline Cutting Machine</strong>, the <strong>ProKesim Blade Module</strong> for older kids, the <strong>Starter Project Kit</strong> for first projects and a spare standard blade module, all in one box.</p>
<p>It leaves a meaningful difference in your budget compared to buying the parts separately, and it is an ideal start for siblings — one learns on the slow setting while the other makes with ProKesim.</p>$body$,
 $body$<p>Makine 4 yaş ve üzeri, ProKesim modülü 8 yaş ve üzeri içindir. Modül değişimini yalnızca yetişkinler yapmalıdır. Küçük parçalar içerir.</p>$body$,
 $body$<p>The machine is for ages 4 and up; the ProKesim module for ages 8 and up. Module replacement must be done by adults only. Contains small parts.</p>$body$,
 '{"contents": ["ML-C1", "ML-A1", "ML-K1", "yedek bıçak modülü"], "age": "4+", "savingsPercent": 13}',
 6490.00, 'TRY', 7460.00,
 (SELECT id FROM collections WHERE code = 'BUNDLES'),
 '/img/product/set-1.jpg', FALSE, TRUE, 1,
 'Microline + 3 Eklenti Seti: makine, ProKesim modülü, Başlangıç Kiti ve yedek bıçak tek kutuda, indirimli.',
 'Microline + 3 Add-On Bundle: machine, ProKesim module, Starter Kit and spare blade in one discounted box.');
