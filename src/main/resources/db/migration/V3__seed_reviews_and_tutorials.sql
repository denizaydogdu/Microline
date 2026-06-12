-- Yorum ve eğitim içeriği seed'i (Phase 5). Vitrin sitesinde seed = prod içerik.
-- NOT: Plan V3'ü yasal sayfalara ayırmıştı; Flyway out-of-order kapalı olduğundan
-- bu migration V3 olarak uygulanır, yasal sayfalar Phase 7'de V4 olur.
-- Yorumlar tek dilli orijinaldir (locale kolonu); product_id NULL = genel marka yorumu.
-- created_at açıkça verilir: sayfalar created_at DESC sıralar, sıra deterministik kalmalı.

-- ── Yorumlar: 7 TR + 3 EN ────────────────────────────────────────────────────

INSERT INTO reviews (product_id, author_name, author_title_tr, author_title_en,
                     rating, body, locale, featured, approved, source, created_at)
VALUES
((SELECT id FROM products WHERE sku = 'ML-C1'), 'Ayşe K.',
 'Sınıf Öğretmeni', 'Primary School Teacher', 5,
 'Üçüncü sınıf öğrencilerimle dört aydır kullanıyoruz. Bıçak tamamen kapalı olduğu için içim çok rahat; çocuklar kendi maketlerini kesip birleştirirken ince motor becerileri gözle görülür şekilde gelişti. Artık dönem sonu sergimizi tamamen karton projelerle hazırlıyoruz.',
 'tr', TRUE, TRUE, 'site', TIMESTAMPTZ '2026-05-28 10:00:00+03'),

((SELECT id FROM products WHERE sku = 'ML-C1'), 'Mehmet Y.',
 'İki Çocuk Babası', 'Father of Two', 5,
 'Kargo kutularını artık çöpe atmıyoruz. 7 ve 10 yaşındaki oğullarım hafta sonları kale, garaj, ne bulurlarsa onu yapıyor. En sevdiğim kısmı, bıçağın parmaklara hiçbir şekilde temas etmemesi — başlarında nöbet tutmama gerek kalmıyor.',
 'tr', FALSE, TRUE, 'site', TIMESTAMPTZ '2026-05-14 18:30:00+03'),

(NULL, 'Zeynep A.',
 'Maker Atölyesi Eğitmeni', 'Maker Workshop Instructor', 5,
 'Atölyemde 6-12 yaş grubuyla çalışıyorum ve Microline tüm kesim işini çocuklara devretmemi sağladı. Makasla ve maketçi bıçağıyla yapamadığımız detaylı kesimleri artık çocuklar kendileri yapıyor. Velilerin ilk sorusu hep aynı: bu gerçekten güvenli mi? Evet, gerçekten güvenli.',
 'tr', TRUE, TRUE, 'instagram', TIMESTAMPTZ '2026-04-30 14:15:00+03'),

((SELECT id FROM products WHERE sku = 'ML-C1'), 'Elif D.',
 'Okul Öncesi Öğretmeni', 'Preschool Teacher', 5,
 'Sınıfımızdaki sanat köşesinin yeni gözdesi. Kalın mukavvayı bile rahatça kesiyor ve gürültüsü çok az. Çocuklar kendi şekillerini kesmek için sıraya giriyor.',
 'tr', FALSE, TRUE, 'site', TIMESTAMPTZ '2026-04-18 09:45:00+03'),

((SELECT id FROM products WHERE sku = 'ML-C1'), 'Murat S.',
 'Maket Meraklısı Baba', 'Model-Making Dad', 4,
 'Kızımla birlikte bilye parkuru yaptık; makine beklediğimden sağlam çıktı. Tek eksiği hazır şablon çeşidinin henüz az olması, yine de kendi çizimlerimizle gayet iyi idare ediyoruz.',
 'tr', FALSE, TRUE, 'site', TIMESTAMPTZ '2026-04-02 20:10:00+03'),

(NULL, 'Fatma T.',
 'Anne', 'Mother', 5,
 'Tablet süresini azaltmak için almıştık, evin en çok kullanılan oyuncağı oldu. Kızım her hafta yeni bir proje planlıyor; evimiz karton şehre döndü ama hiç şikâyetçi değilim.',
 'tr', FALSE, TRUE, 'instagram', TIMESTAMPTZ '2026-03-21 16:00:00+03'),

((SELECT id FROM products WHERE sku = 'ML-C1'), 'Can B.',
 'STEM Atölye Koordinatörü', 'STEM Workshop Coordinator', 5,
 'Okulumuzun STEM atölyesi için altı adet aldık. Kurulum dakikalar sürüyor ve otomatik durdurma özelliği kalabalık sınıfta büyük güvence. Toplu alım sürecinde ekip de çok ilgiliydi.',
 'tr', FALSE, TRUE, 'site', TIMESTAMPTZ '2026-03-05 11:20:00+03'),

((SELECT id FROM products WHERE sku = 'ML-C1'), 'Selin M.',
 'Anne', 'Parent', 5,
 'My eight-year-old turned a pile of delivery boxes into a working marble run in a single weekend. The blade is fully enclosed, so I can let her cut on her own — that alone is worth the price.',
 'en', TRUE, TRUE, 'site', TIMESTAMPTZ '2026-05-20 13:00:00+03'),

(NULL, 'David O.',
 'Maker Baba', 'Maker Dad', 5,
 'We have been building cardboard robots every Sunday since this arrived. It cuts thick board cleanly and stops the moment it leaves the surface. Brilliant little machine.',
 'en', FALSE, TRUE, 'instagram', TIMESTAMPTZ '2026-04-25 19:40:00+03'),

((SELECT id FROM products WHERE sku = 'ML-C1'), 'Emre H.',
 'Görsel Sanatlar Öğretmeni', 'Art Teacher', 5,
 'I use it in my art classes with students aged nine to twelve. They handle the whole cutting process themselves while I focus on the designs. Clean cuts, no exposed blades, no cut fingers.',
 'en', FALSE, TRUE, 'site', TIMESTAMPTZ '2026-03-30 08:50:00+03');

-- ── Eğitimler: 5 yayınlanmış post (zorluk ve tarih çeşitli) ──────────────────

INSERT INTO tutorial_posts (slug_tr, slug_en, title_tr, title_en, excerpt_tr, excerpt_en,
                            body_tr, body_en, difficulty, published, published_at, created_at)
VALUES
('ilk-kesiminiz-basit-kutu-ev', 'your-first-cut-simple-box-house',
 'İlk Kesiminiz: Basit Kutu Ev', 'Your First Cut: Simple Box House',
 'Microline ile ilk projeniz için en iyi başlangıç: tek bir ayakkabı kutusundan 20 dakikada minik bir ev.',
 'The perfect first project with your Microline: a tiny house from a single shoe box in 20 minutes.',
 $body$<p>Microline kutudan yeni çıktıysa, ilk proje olarak basit kutu evden daha iyisi yoktur. Tek malzemeniz bir ayakkabı kutusu; tek araç gereciniz Microline ve bir kurşun kalem. Bu projede düz çizgi kesmeyi, köşe dönmeyi ve parçaları geçmeli birleştirmeyi öğreneceksiniz.</p>
<h2>1. Adım: Çizin</h2>
<p>Kutunun uzun yüzüne bir kapı (yaklaşık 6×10 cm) ve iki pencere çizin. Çizgileri kalın ve net çizin; çocuklar makineyle çizgiyi takip ederken kalın çizgi işi çok kolaylaştırır.</p>
<h2>2. Adım: Kesin</h2>
<p>Microline'ı çizginin başına yerleştirin ve yavaşça itin. Köşelere geldiğinizde makineyi durdurup kutuyu çevirin — makineyi döndürmek yerine kartonu döndürmek her zaman daha temiz sonuç verir. Kapının üç kenarını kesip dördüncüyü katlarsanız açılır kapanır gerçek bir kapınız olur.</p>
<h2>3. Adım: Çatıyı ekleyin</h2>
<p>Kutunun kapağını ortadan katlayıp üçgen çatı yapın ve iki kenarından gövdeye geçirin. Tebrikler — ilk Microline projeniz hazır! Evi boyamak, kapı koluna düğme yapıştırmak ve bahçe eklemek tamamen size kalmış.</p>$body$,
 $body$<p>If your Microline is fresh out of the box, there is no better first project than the simple box house. All you need is a shoe box, your Microline and a pencil. In this project you will learn to cut straight lines, turn corners and join pieces with slot connections.</p>
<h2>Step 1: Draw</h2>
<p>Draw a door (about 6×10 cm) and two windows on the long side of the box. Make the lines thick and clear; a bold line makes it much easier for kids to follow with the machine.</p>
<h2>Step 2: Cut</h2>
<p>Place the Microline at the start of the line and push it gently. At corners, stop the machine and rotate the box — turning the cardboard instead of the machine always gives a cleaner result. Cut three sides of the door and fold the fourth to get a real working door.</p>
<h2>Step 3: Add the roof</h2>
<p>Fold the box lid down the middle to form a triangular roof and slot it onto the body. Congratulations — your first Microline project is done! Painting the house, adding a button doorknob or building a garden is entirely up to you.</p>$body$,
 'BEGINNER', TRUE, TIMESTAMPTZ '2026-01-15 09:00:00+03', TIMESTAMPTZ '2026-01-15 09:00:00+03'),

('karton-robot-maskesi', 'cardboard-robot-mask',
 'Karton Robot Maskesi', 'Cardboard Robot Mask',
 'Bir kargo kutusundan kafaya tam oturan, anteni ve hareketli çenesi olan robot maskesi yapın.',
 'Build a robot mask with an antenna and a moving jaw from a single shipping box.',
 $body$<p>Robot maskesi, evdeki kostüm krizlerinin bir numaralı çözümüdür: bir kargo kutusu, 30 dakika ve bolca hayal gücü. Kafaya oturan silindir gövdeyi, vizör deliğini ve anteni adım adım yapacağız.</p>
<h2>1. Adım: Gövdeyi ölçün</h2>
<p>Kutuyu açıp düzleştirin ve çocuğunuzun baş çevresini ölçün. Baş çevresi + 4 cm uzunluğunda, 25 cm yüksekliğinde bir şerit kesin. Bu şerit silindir olarak kapanıp maskenin gövdesini oluşturacak.</p>
<h2>2. Adım: Vizör ve havalandırma</h2>
<p>Şerit düzken göz hizasına 12×4 cm bir vizör kesin. Vizörün altına iki sıra kısa yatay çizgi keserseniz hem havalandırma olur hem de robot görünümü güçlenir. Kesimleri düz zeminde yapın; Microline'ın otomatik durdurması kartondan kalkar kalkmaz devreye girer.</p>
<h2>3. Adım: Anten ve birleştirme</h2>
<p>Artan kartondan 2 cm genişliğinde bir şerit kıvırıp tepeye geçme yuvasıyla sabitleyin — yapıştırıcıya gerek yok. Gövdeyi silindir yapıp kenarlardan geçmeli kilitle kapatın. Alüminyum folyo kaplama ve şişe kapağı düğmeler maskeyi bir üst seviyeye taşır.</p>$body$,
 $body$<p>The robot mask is the number-one fix for costume emergencies: one shipping box, 30 minutes and plenty of imagination. We will build the head cylinder, the visor opening and the antenna step by step.</p>
<h2>Step 1: Measure the body</h2>
<p>Open the box flat and measure your child's head circumference. Cut a strip that is the circumference + 4 cm long and 25 cm tall. This strip will close into a cylinder and form the body of the mask.</p>
<h2>Step 2: Visor and vents</h2>
<p>While the strip is still flat, cut a 12×4 cm visor at eye level. Two rows of short horizontal cuts below the visor add ventilation and boost the robot look. Cut on a flat surface; the Microline auto-stop kicks in the moment it lifts off the cardboard.</p>
<h2>Step 3: Antenna and assembly</h2>
<p>Curl a 2 cm strip of leftover cardboard and fix it on top with a slot joint — no glue needed. Close the body into a cylinder with interlocking tabs on the edges. Aluminium foil cladding and bottle-cap buttons take the mask to the next level.</p>$body$,
 'BEGINNER', TRUE, TIMESTAMPTZ '2026-02-12 09:00:00+03', TIMESTAMPTZ '2026-02-12 09:00:00+03'),

('hareketli-kelebek', 'moving-butterfly',
 'Hareketli Kelebek', 'Moving Butterfly',
 'Kanat çırpan karton kelebek: basit bir krank mekanizmasıyla ilk hareketli projenize geçin.',
 'A cardboard butterfly that flaps its wings — your first moving project with a simple crank mechanism.',
 $body$<p>Şimdiye kadar kestiğiniz her şey yerinde duruyorsa, sırada hareket var. Bu projede iki kanat, bir gövde ve bir krank kolu keseceğiz; kolu çevirince kelebek kanat çırpacak. Mekanizma basit ama sonuç büyüleyici.</p>
<h2>1. Adım: Parçaları kesin</h2>
<p>Kalıbı kartona çizin: iki simetrik kanat, 4×20 cm gövde şeridi ve 1,5 cm genişliğinde üç kısa şerit (krank için). Kanat kıvrımları gibi detaylı hatlarda Microline'ı yavaş itmek temiz kavis kesmenin sırrıdır.</p>
<h2>2. Adım: Krankı kurun</h2>
<p>Gövde şeridini U biçiminde katlayın ve iki yan duvara karşılıklı delik açın. Kısa şeritleri Z biçiminde birleştirip mile dönüştürün, milin iki ucunu deliklerden geçirin. Mile bağlanan dikey çubuklar kanatların altına geçmeli yuva ile takılır.</p>
<h2>3. Adım: Deneyin ve ayarlayın</h2>
<p>Kolu yavaşça çevirin: kanatlar sırayla yükselip alçalmalı. Kanatlar takılıyorsa delikleri 1-2 mm genişletin. Mekanizma rahat dönünce kanatları boyayın — krep kağıdı kaplama kanatlara şeffaf bir uçuş efekti verir.</p>$body$,
 $body$<p>If everything you have cut so far stands still, it is time for motion. In this project we will cut two wings, a body and a crank arm; turn the handle and the butterfly flaps. The mechanism is simple, the result is mesmerising.</p>
<h2>Step 1: Cut the parts</h2>
<p>Trace the pattern onto cardboard: two symmetrical wings, a 4×20 cm body strip and three short 1.5 cm strips for the crank. On detailed contours like wing curves, pushing the Microline slowly is the secret to clean arcs.</p>
<h2>Step 2: Build the crank</h2>
<p>Fold the body strip into a U shape and punch matching holes in the two side walls. Join the short strips into a Z-shaped axle and pass its ends through the holes. Vertical rods attached to the axle connect to the underside of the wings with slot joints.</p>
<h2>Step 3: Test and tune</h2>
<p>Turn the handle slowly: the wings should rise and fall in turn. If a wing sticks, widen the holes by 1–2 mm. Once the mechanism spins freely, decorate the wings — crepe-paper covering gives them a translucent flight effect.</p>$body$,
 'INTERMEDIATE', TRUE, TIMESTAMPTZ '2026-03-10 09:00:00+03', TIMESTAMPTZ '2026-03-10 09:00:00+03'),

('kalin-kartonu-kolayca-kesme-teknikleri', 'how-to-easily-cut-thick-cardboard',
 'Kalın Kartonu Kolayca Kesme Teknikleri', 'How to Easily Cut Thick Cardboard',
 'Çift oluklu mukavva mı? Doğru hız, doğru yön ve birkaç püf noktasıyla Microline kalın kartonda da temiz keser.',
 'Double-wall board? With the right speed, the right direction and a few tricks, Microline cuts thick cardboard cleanly too.',
 $body$<p>Microline 3 mm'ye kadar kartonu keser; ama kalın ve çift oluklu mukavvada teknik fark yaratır. Bu rehberde projelerimizde her gün kullandığımız üç tekniği paylaşıyoruz: oluk yönünü okumak, hız kademesini düşürmek ve çift paso kesim.</p>
<h2>Oluk yönünü okuyun</h2>
<p>Mukavvanın içindeki dalgalı katman (oluk) tek yönde uzanır. Oluğa paralel kesimler her zaman daha az direnç gösterir. Projenizi kartona yerleştirirken uzun kesimleri oluk yönüne denk getirin; makine zorlanmaz, kenarlar ezilmez.</p>
<h2>Hızı düşürün, iki pasoda kesin</h2>
<p>Kalın malzemede düşük hız kademesi + iki geçiş, tek geçişten daima daha temizdir. İlk pasoda çizgiyi yarı derinliğe kadar açın, ikinci pasoda aynı izden geçin. Çocuklara da öğretmesi kolay bir kural: "Kalın karton, iki tur."</p>
<h2>Kenar ezilmesini önleyin</h2>
<p>Kesime başlamadan kartonun altına ikinci bir düz karton koyun; esneyen zemin, ezik kenarların bir numaralı sebebidir. Kesim bitiminde kenarda tüylenme kaldıysa, kenarı tırnakla değil cetvel kenarıyla bastırarak düzeltin. Bu üç alışkanlık, bilye parkuru gibi yük taşıyan projelerde farkı açık biçimde gösterir.</p>$body$,
 $body$<p>Microline cuts cardboard up to 3 mm — but on thick, double-wall board, technique makes the difference. In this guide we share the three techniques we use daily: reading the flute direction, lowering the speed setting and double-pass cutting.</p>
<h2>Read the flute direction</h2>
<p>The wavy layer inside corrugated board (the flute) runs in one direction. Cuts parallel to the flute always meet less resistance. When laying out your project, align long cuts with the flute direction; the machine will not strain and the edges will not crush.</p>
<h2>Slow down and cut in two passes</h2>
<p>On thick material, a low speed setting plus two passes always beats a single pass. Open the line to half depth on the first pass, then follow the same groove on the second. An easy rule to teach kids too: "Thick board, two rounds."</p>
<h2>Prevent crushed edges</h2>
<p>Put a second flat piece of cardboard under your workpiece before cutting; a flexing surface is the number-one cause of crushed edges. If the edge is fuzzy after the cut, press it flat with the edge of a ruler rather than a fingernail. These three habits clearly show their worth in load-bearing projects like marble runs.</p>$body$,
 'INTERMEDIATE', TRUE, TIMESTAMPTZ '2026-04-18 09:00:00+03', TIMESTAMPTZ '2026-04-18 09:00:00+03'),

('marble-run-bilye-parkuru', 'marble-run-challenge',
 'Marble Run: Bilye Parkuru', 'Marble Run Challenge',
 'Rampalar, dönemeçler ve bir huni: duvara kurulan karton bilye parkuruyla fizik, tasarım ve sabır bir arada.',
 'Ramps, bends and a funnel: physics, design and patience meet in this wall-mounted cardboard marble run.',
 $body$<p>Bilye parkuru, karton projelerinin kralıdır: kesim hassasiyeti, eğim hesabı ve bolca deneme-yanılma ister. Bu projede üç rampa, bir dönemeç ve bir huniden oluşan, duvara veya büyük bir mukavva levhaya kurulan parkur yapacağız. Süre olarak bir hafta sonunu ayırın — buna değecek.</p>
<h2>1. Adım: Rayları kesin</h2>
<p>Her ray için 30×6 cm şeritler kesin ve iki uzun kenarını 1,5 cm içeriden katlayarak U kanal oluşturun. Katlama çizgisini Microline ile yarı derinlikte çizerseniz (kesmeden) kıvrım cetvelle çizilmiş gibi düzgün olur. En az altı ray hazırlayın; yedek raylar deneme aşamasında hayat kurtarır.</p>
<h2>2. Adım: Eğimi ayarlayın</h2>
<p>Rayları zemine tutturmadan önce kitap destekleriyle deneyin. İyi bir başlangıç eğimi her 30 cm'de 3-4 cm düşüştür: bilye ne sürünmeli ne fırlamalı. Dönemeçte dış duvarı 2 cm yükseltin, yoksa bilye virajı dışarıdan terk eder.</p>
<h2>3. Adım: Huni ve final</h2>
<p>Geniş bir karton daireden pasta dilimi çıkarıp kalanını koni yapın: huniniz hazır. Huniyi parkurun başına yerleştirin ki bilye nereden bırakılırsa bırakılsın raya girsin. Son raya küçük bir zil veya metal kapak koyun — bitiş sesi, parkurun en tatmin edici parçasıdır.</p>
<h2>Meydan okuma</h2>
<p>Parkur çalışınca süre tutun: bilyenin yolculuğunu 10 saniyenin üzerine çıkarabilir misiniz? Daha uzun yol, daha yavaş eğim, daha çok dönemeç... Tasarımı geliştirmenin sınırı yok.</p>$body$,
 $body$<p>The marble run is the king of cardboard projects: it demands cutting precision, slope calculation and plenty of trial and error. In this project we will build a run with three ramps, a bend and a funnel, mounted on a wall or a large cardboard sheet. Set aside a weekend — it is worth it.</p>
<h2>Step 1: Cut the rails</h2>
<p>Cut 30×6 cm strips for each rail and fold both long edges 1.5 cm in to form a U channel. If you score the fold line at half depth with the Microline (without cutting through), the crease comes out ruler-straight. Prepare at least six rails; spares save lives during testing.</p>
<h2>Step 2: Tune the slope</h2>
<p>Test the rails with book props before fixing anything down. A good starting slope is a 3–4 cm drop every 30 cm: the marble should neither crawl nor fly off. Raise the outer wall of the bend by 2 cm, or the marble will leave the curve on the outside.</p>
<h2>Step 3: Funnel and finish</h2>
<p>Cut a pie slice out of a wide cardboard circle and roll the rest into a cone: your funnel is ready. Place it at the top of the run so the marble finds the rail no matter where it is dropped. Put a small bell or a metal lid at the end of the last rail — the finishing sound is the most satisfying part of the build.</p>
<h2>The challenge</h2>
<p>Once the run works, start timing: can you push the marble's journey past 10 seconds? A longer path, a gentler slope, more bends... There is no limit to improving the design.</p>$body$,
 'ADVANCED', TRUE, TIMESTAMPTZ '2026-05-22 09:00:00+03', TIMESTAMPTZ '2026-05-22 09:00:00+03');
