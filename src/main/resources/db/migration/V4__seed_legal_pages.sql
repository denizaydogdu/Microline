-- Yasal sayfa seed'leri.
-- !!! TÜM METİNLER YER TUTUCUDUR — YAYIN ÖNCESİ TÜRK E-TİCARET AVUKATI İNCELEMESİ ŞARTTIR !!!
-- Köşeli parantezli alanlar ([ŞİRKET UNVANI] vb.) gerçek şirket bilgileriyle doldurulacaktır.

INSERT INTO legal_pages (code, slug_tr, slug_en, title_tr, title_en, body_tr, body_en, created_at, updated_at) VALUES

('MESAFELI_SATIS', 'mesafeli-satis-sozlesmesi', 'distance-sales-agreement',
 'Mesafeli Satış Sözleşmesi', 'Distance Sales Agreement',
$body$<!-- LAWYER REVIEW REQUIRED -->
<h2>1. Taraflar</h2>
<p><strong>Satıcı:</strong> [ŞİRKET UNVANI] (bundan sonra "Satıcı") — MERSİS No: [MERSİS NO], Adres: [AÇIK ADRES], Telefon: [TELEFON], KEP: [KEP ADRESİ], E-posta: info@microline.com.tr</p>
<p><strong>Alıcı:</strong> Sipariş talebi formunda bilgileri yer alan tüketici (bundan sonra "Alıcı").</p>
<h2>2. Konu</h2>
<p>İşbu sözleşmenin konusu, Alıcı'nın Satıcı'ya ait www.microline.com.tr internet sitesinden elektronik ortamda siparişini verdiği, nitelikleri ve satış fiyatı sitede ve Ön Bilgilendirme Formu'nda belirtilen ürünün satışı ve teslimi ile ilgili olarak 6502 sayılı Tüketicinin Korunması Hakkında Kanun ve Mesafeli Sözleşmeler Yönetmeliği hükümleri gereğince tarafların hak ve yükümlülüklerinin saptanmasıdır.</p>
<h2>3. Sözleşme Konusu Ürün, Ödeme ve Teslimat</h2>
<p>Ürünün cinsi, türü, miktarı, marka/modeli, satış bedeli (tüm vergiler dâhil) ve ödeme şekli sipariş onay sayfasında ve Alıcı'ya iletilen sipariş özetinde belirtildiği gibidir.</p>
<p>Teslimat, anlaşmalı kargo firması aracılığıyla Alıcı'nın bildirdiği adrese yapılır. Teslimat süresi her hâlükârda <strong>30 günü geçemez</strong>. Bu süre içinde teslim edilmeyen ürünler için Alıcı sözleşmeyi feshedebilir.</p>
<p>Ürünün kargoya verilmesinden Alıcı'ya teslimine kadar oluşabilecek kayıp ve hasarlardan Satıcı sorumludur.</p>
<h2>4. Cayma Hakkı</h2>
<p>Alıcı, ürünün kendisine veya gösterdiği adresteki kişiye tesliminden itibaren <strong>14 (on dört) gün</strong> içinde herhangi bir gerekçe göstermeksizin ve cezai şart ödemeksizin sözleşmeden cayma hakkına sahiptir. Cayma bildirimi yazılı olarak veya kalıcı veri saklayıcısı ile Satıcı'ya yöneltilir. Ayrıntılar İptal, İade ve Cayma Hakkı sayfasında düzenlenmiştir.</p>
<h2>5. Cayma Hakkının İstisnaları</h2>
<p>Mesafeli Sözleşmeler Yönetmeliği'nin 15. maddesinde sayılan hâllerde (Alıcı'nın istekleri doğrultusunda kişiselleştirilen ürünler vb.) cayma hakkı kullanılamaz.</p>
<h2>6. Genel Hükümler</h2>
<p>Alıcı, sipariş öncesinde Ön Bilgilendirme Formu'nu okuyup elektronik ortamda teyit ettiğini kabul eder. İşbu sözleşme ve sipariş kayıtları Satıcı tarafından <strong>3 (üç) yıl</strong> süreyle saklanır.</p>
<h2>7. Uyuşmazlıkların Çözümü</h2>
<p>İşbu sözleşmeden doğan uyuşmazlıklarda, Ticaret Bakanlığı'nca ilan edilen parasal sınırlar dâhilinde Alıcı'nın veya Satıcı'nın yerleşim yerindeki Tüketici Hakem Heyetleri ile Tüketici Mahkemeleri yetkilidir.</p>$body$,
$body$<!-- LAWYER REVIEW REQUIRED -->
<h2>1. Parties</h2>
<p><strong>Seller:</strong> [COMPANY TITLE] — MERSIS No: [MERSIS NO], Address: [ADDRESS], Phone: [PHONE], E-mail: info@microline.com.tr</p>
<p><strong>Buyer:</strong> The consumer whose details are provided in the order request form.</p>
<h2>2. Subject</h2>
<p>This agreement sets out the rights and obligations of the parties regarding the sale and delivery of the product ordered electronically by the Buyer on www.microline.com.tr, pursuant to Turkish Consumer Protection Law No. 6502 and the Regulation on Distance Contracts.</p>
<h2>3. Product, Payment and Delivery</h2>
<p>The type, quantity, sales price (including all taxes) and payment method of the product are as stated on the order confirmation page. Delivery shall in no case exceed <strong>30 days</strong>; otherwise the Buyer may terminate the contract. The Seller bears the risk of loss or damage until delivery.</p>
<h2>4. Right of Withdrawal</h2>
<p>The Buyer may withdraw from the contract within <strong>14 days</strong> of delivery without giving any reason and without penalty. Details are set out on the Cancellation, Return and Withdrawal page.</p>
<h2>5. Exceptions</h2>
<p>The right of withdrawal cannot be exercised in the cases listed in Article 15 of the Regulation on Distance Contracts (e.g. personalised products).</p>
<h2>6. General Provisions</h2>
<p>Order records are retained for <strong>3 years</strong>.</p>
<h2>7. Disputes</h2>
<p>Consumer Arbitration Committees and Consumer Courts at the Buyer's or Seller's place of residence are competent, within the monetary limits announced by the Turkish Ministry of Trade.</p>$body$,
 now(), now()),

('ON_BILGILENDIRME', 'on-bilgilendirme-formu', 'pre-information-form',
 'Ön Bilgilendirme Formu', 'Pre-Information Form',
$body$<!-- LAWYER REVIEW REQUIRED -->
<!-- NOT: Bu form ödeme öncesinde Alıcı'ya EN AZ 12 PUNTO büyüklüğünde gösterilmeli ve onayı alınmalıdır. -->
<h2>1. Satıcı Bilgileri</h2>
<p>Unvan: [ŞİRKET UNVANI]<br>MERSİS No: [MERSİS NO]<br>Adres: [AÇIK ADRES]<br>Telefon: [TELEFON]<br>KEP: [KEP ADRESİ]<br>E-posta: info@microline.com.tr</p>
<h2>2. Ürünün Temel Nitelikleri</h2>
<p>Sözleşme konusu ürünün temel özellikleri, marka/modeli ve teknik nitelikleri ürün sayfasında yer almaktadır.</p>
<h2>3. Fiyat</h2>
<p>Ürünün tüm vergiler (KDV) dâhil toplam satış fiyatı sipariş özetinde gösterilir. Kargo ücreti ve varsa ek masraflar ayrıca belirtilir ve Alıcı tarafından ödenir; aksi kampanya koşullarında açıkça yazılır.</p>
<h2>4. Ödeme ve Teslimat</h2>
<p>Ödeme ve teslimat yöntemleri sipariş akışında gösterilir. Teslimat süresi her hâlükârda 30 günü aşamaz.</p>
<h2>5. Cayma Hakkı</h2>
<p>Alıcı, teslimden itibaren 14 gün içinde herhangi bir gerekçe göstermeksizin ve cezai şart ödemeksizin cayma hakkına sahiptir. Cayma bildirimi [İLETİŞİM KANALI] üzerinden yapılabilir. İade gönderileri için anlaşmalı kargo firması: [İADE KARGO FİRMASI]. Bu firma ile yapılan iade gönderilerinde kargo ücreti Satıcı'ya aittir.</p>
<h2>6. Cayma Hakkının İstisnaları</h2>
<p>Mesafeli Sözleşmeler Yönetmeliği m.15'te sayılan ürünlerde cayma hakkı kullanılamaz.</p>
<h2>7. Şikâyet ve İtiraz Yolları</h2>
<p>Alıcı, şikâyet ve itirazlarını yerleşim yerindeki veya Satıcı'nın bulunduğu yerdeki Tüketici Hakem Heyeti'ne veya Tüketici Mahkemesi'ne yapabilir.</p>$body$,
$body$<!-- LAWYER REVIEW REQUIRED -->
<h2>1. Seller Details</h2>
<p>Title: [COMPANY TITLE]<br>MERSIS No: [MERSIS NO]<br>Address: [ADDRESS]<br>Phone: [PHONE]<br>E-mail: info@microline.com.tr</p>
<h2>2. Essential Characteristics of the Product</h2>
<p>The essential characteristics of the product are stated on the product page.</p>
<h2>3. Price</h2>
<p>The total sales price including all taxes (VAT) is shown in the order summary. Shipping costs are stated separately.</p>
<h2>4. Payment and Delivery</h2>
<p>Payment and delivery methods are shown during the order flow. Delivery shall not exceed 30 days.</p>
<h2>5. Right of Withdrawal</h2>
<p>The Buyer may withdraw within 14 days of delivery without reason or penalty. Designated return carrier: [RETURN CARRIER]; return shipping via this carrier is paid by the Seller.</p>
<h2>6. Exceptions</h2>
<p>Withdrawal cannot be exercised for products listed in Article 15 of the Regulation on Distance Contracts.</p>
<h2>7. Complaints</h2>
<p>Complaints may be submitted to Consumer Arbitration Committees or Consumer Courts.</p>$body$,
 now(), now()),

('KVKK', 'kvkk-aydinlatma-metni', 'kvkk-privacy-notice',
 'KVKK Aydınlatma Metni', 'KVKK Privacy Notice',
$body$<!-- LAWYER REVIEW REQUIRED -->
<h2>1. Veri Sorumlusu</h2>
<p>6698 sayılı Kişisel Verilerin Korunması Kanunu ("KVKK") uyarınca kişisel verileriniz, veri sorumlusu sıfatıyla [ŞİRKET UNVANI] tarafından aşağıda açıklanan kapsamda işlenmektedir.</p>
<h2>2. İşlenen Kişisel Veriler ve İşleme Amaçları</h2>
<p>Sipariş talebi, iletişim ve bülten formları aracılığıyla ad-soyad, e-posta, telefon ve mesaj içeriği; siparişin oluşturulması, sizinle iletişime geçilmesi, talep ve şikâyetlerin yönetilmesi ve açık rızanız bulunması hâlinde ticari elektronik ileti gönderilmesi amaçlarıyla işlenir.</p>
<h2>3. Hukuki Sebep</h2>
<p>Kişisel verileriniz; KVKK m.5/2(c) "sözleşmenin kurulması veya ifası", m.5/2(ç) "hukuki yükümlülüğün yerine getirilmesi" ve bülten aboneliğinde m.5/1 "açık rıza" hukuki sebeplerine dayanılarak işlenir.</p>
<h2>4. Aktarım</h2>
<p>Verileriniz, teslimatın sağlanması amacıyla kargo firmalarına ve yasal yükümlülükler kapsamında yetkili kamu kurumlarına aktarılabilir. Yurt dışına aktarım yapılması hâlinde KVKK m.9 uyarınca açık rızanız alınır.</p>
<h2>5. Toplama Yöntemi</h2>
<p>Verileriniz, internet sitemizdeki formlar ve çerezler aracılığıyla elektronik ortamda toplanır.</p>
<h2>6. KVKK m.11 Kapsamındaki Haklarınız</h2>
<p>Kişisel verinizin işlenip işlenmediğini öğrenme, işlenmişse bilgi talep etme, amacına uygun kullanılıp kullanılmadığını öğrenme, eksik/yanlış işlenmişse düzeltilmesini isteme, silinmesini veya yok edilmesini isteme, otomatik sistemlerle analiz sonucu aleyhinize çıkan sonuca itiraz etme ve zarara uğramanız hâlinde tazminat talep etme haklarına sahipsiniz.</p>
<h2>7. Başvuru</h2>
<p>Taleplerinizi [BAŞVURU KANALI — yazılı / KEP / e-posta] üzerinden iletebilirsiniz. Başvurular en geç 30 gün içinde sonuçlandırılır.</p>$body$,
$body$<!-- LAWYER REVIEW REQUIRED -->
<h2>1. Data Controller</h2>
<p>Pursuant to Turkish Personal Data Protection Law No. 6698 ("KVKK"), your personal data is processed by [COMPANY TITLE] as data controller.</p>
<h2>2. Data Processed and Purposes</h2>
<p>Name, e-mail, phone and message content collected via order, contact and newsletter forms are processed to handle your order request, respond to enquiries and — with your explicit consent — send commercial electronic messages.</p>
<h2>3. Legal Basis</h2>
<p>Processing relies on KVKK art. 5/2(c) (performance of a contract), art. 5/2(ç) (legal obligation) and, for the newsletter, art. 5/1 (explicit consent).</p>
<h2>4. Transfers</h2>
<p>Data may be shared with cargo companies for delivery and with competent authorities where legally required. Cross-border transfers require explicit consent under KVKK art. 9.</p>
<h2>5. Collection Method</h2>
<p>Data is collected electronically via website forms and cookies.</p>
<h2>6. Your Rights (KVKK art. 11)</h2>
<p>You may learn whether your data is processed, request information, correction, deletion or destruction, object to results of automated analysis, and claim compensation for damages.</p>
<h2>7. Applications</h2>
<p>Requests may be submitted via [APPLICATION CHANNEL] and are concluded within 30 days.</p>$body$,
 now(), now()),

('GIZLILIK', 'gizlilik-politikasi', 'privacy-policy',
 'Gizlilik Politikası', 'Privacy Policy',
$body$<!-- LAWYER REVIEW REQUIRED -->
<h2>Gizliliğiniz Bizim İçin Önemli</h2>
<p>Microline olarak kişisel verilerinizi yalnızca siparişlerinizi yönetmek, sorularınızı yanıtlamak ve onay verdiyseniz bülten göndermek için kullanırız. Verilerinizi üçüncü kişilere satmayız.</p>
<h2>Hangi Verileri Topluyoruz?</h2>
<p>Form aracılığıyla paylaştığınız ad-soyad, e-posta, telefon ve mesaj içerikleri ile sitenin çalışması için gerekli çerez verileri.</p>
<h2>Saklama Süresi</h2>
<p>Sipariş ve sözleşme kayıtları yasal saklama süreleri (asgari 3 yıl) boyunca; bülten aboneliği verileri abonelikten çıkana kadar saklanır.</p>
<h2>Güvenlik</h2>
<p>Verileriniz şifreli bağlantı (HTTPS) üzerinden iletilir ve erişimi sınırlandırılmış sistemlerde saklanır.</p>
<p>Ayrıntılı bilgi için <a href="/tr/kvkk-aydinlatma-metni">KVKK Aydınlatma Metni</a>'ni ve <a href="/tr/cerez-politikasi">Çerez Politikası</a>'nı inceleyebilirsiniz.</p>$body$,
$body$<!-- LAWYER REVIEW REQUIRED -->
<h2>Your Privacy Matters</h2>
<p>Microline uses your personal data only to manage your orders, answer your questions and — if you opted in — send the newsletter. We never sell your data.</p>
<h2>What We Collect</h2>
<p>Name, e-mail, phone and message content you share via forms, plus cookies necessary for the site to function.</p>
<h2>Retention</h2>
<p>Order and contract records are kept for the statutory period (minimum 3 years); newsletter data until you unsubscribe.</p>
<h2>Security</h2>
<p>Data is transmitted over HTTPS and stored in access-restricted systems.</p>
<p>See the <a href="/en/kvkk-privacy-notice">KVKK Privacy Notice</a> and the <a href="/en/cookie-policy">Cookie Policy</a> for details.</p>$body$,
 now(), now()),

('CEREZ', 'cerez-politikasi', 'cookie-policy',
 'Çerez Politikası', 'Cookie Policy',
$body$<!-- LAWYER REVIEW REQUIRED -->
<h2>Çerez Nedir?</h2>
<p>Çerezler, ziyaret ettiğiniz internet siteleri tarafından tarayıcınıza kaydedilen küçük metin dosyalarıdır.</p>
<h2>Kullandığımız Çerezler</h2>
<p><strong>Zorunlu çerezler:</strong> Oturum güvenliği (CSRF koruması) için gereklidir; site bu çerezler olmadan çalışmaz. Oturum süresince saklanır.</p>
<p><strong>Analitik çerezler:</strong> Şu anda kullanılmamaktadır. Kullanılmaya başlanırsa yalnızca açık onayınızla yüklenecektir.</p>
<h2>Çerez Tercihlerinizi Yönetme</h2>
<p>Çerez bandındaki "Sadece gerekli" seçeneğiyle zorunlu olmayan çerezleri reddedebilirsiniz. Tarayıcı ayarlarınızdan da çerezleri silebilir veya engelleyebilirsiniz.</p>
<h2>Dil Tercihi</h2>
<p>Dil tercihiniz çerezde değil, adres satırında (/tr, /en) tutulur; bu nedenle dil seçimi için çerez kullanılmaz.</p>$body$,
$body$<!-- LAWYER REVIEW REQUIRED -->
<h2>What Are Cookies?</h2>
<p>Cookies are small text files stored in your browser by websites you visit.</p>
<h2>Cookies We Use</h2>
<p><strong>Strictly necessary cookies:</strong> Required for session security (CSRF protection); the site cannot function without them. Kept for the session only.</p>
<p><strong>Analytics cookies:</strong> Not currently used. If introduced, they will load only with your explicit consent.</p>
<h2>Managing Preferences</h2>
<p>You can refuse non-essential cookies via the "Only necessary" option in the cookie banner, or delete/block cookies in your browser settings.</p>
<h2>Language Preference</h2>
<p>Your language preference lives in the URL path (/tr, /en), not in a cookie.</p>$body$,
 now(), now()),

('IPTAL_IADE', 'iptal-iade-cayma', 'returns-and-withdrawal',
 'İptal, İade ve Cayma Hakkı', 'Cancellation, Returns and Withdrawal',
$body$<!-- LAWYER REVIEW REQUIRED -->
<h2>14 Gün Koşulsuz Cayma Hakkı</h2>
<p>Mesafeli Sözleşmeler Yönetmeliği m.9 uyarınca, ürünün size veya gösterdiğiniz adresteki üçüncü kişiye tesliminden itibaren <strong>14 gün</strong> içinde herhangi bir gerekçe göstermeksizin ve cezai şart ödemeksizin sözleşmeden cayabilirsiniz.</p>
<p>Cayma hakkı konusunda usulüne uygun bilgilendirilmediyseniz 14 günlük süreyle bağlı değilsiniz; bu hâlde cayma hakkı, cayma süresinin bittiği tarihten itibaren <strong>1 yıl</strong> sonra sona erer.</p>
<h2>Cayma Bildirimi Nasıl Yapılır?</h2>
<p>Cayma bildirimi süresi içinde [İLETİŞİM KANALI — e-posta/iade formu] üzerinden yazılı olarak veya kalıcı veri saklayıcısı ile yapılır.</p>
<h2>İade Süreci</h2>
<p>Cayma bildiriminizden itibaren 10 gün içinde ürünü, Ön Bilgilendirme Formu'nda belirtilen anlaşmalı kargo firması <strong>[İADE KARGO FİRMASI]</strong> ile iade etmeniz gerekir. Anlaşmalı firma ile gönderimde iade kargo ücreti tarafımıza aittir.</p>
<h2>Geri Ödeme</h2>
<p>Cayma hakkınızı kullanmanız hâlinde, ürünü kargoya teslim ettiğiniz tarihten itibaren <strong>14 gün</strong> içinde tahsil edilen tüm ödemeler, ödeme aracınıza uygun şekilde tek seferde iade edilir.</p>
<h2>Cayma Hakkının İstisnaları</h2>
<p>İsteğiniz doğrultusunda kişiselleştirilen ürünler, ambalajı açıldığında sağlık ve hijyen açısından iadeye uygun olmayan ürünler ve Yönetmelik m.15'te sayılan diğer hâllerde cayma hakkı kullanılamaz.</p>
<h2>Ayıplı Ürün</h2>
<p>Ürünün ayıplı çıkması hâlinde 6502 sayılı Kanun m.11 kapsamındaki seçimlik haklarınız (ücretsiz onarım, ayıpsız misli ile değişim, bedel iadesi, ayıp oranında indirim) saklıdır.</p>$body$,
$body$<!-- LAWYER REVIEW REQUIRED -->
<h2>14-Day Unconditional Right of Withdrawal</h2>
<p>You may withdraw from the contract within <strong>14 days</strong> of delivery without giving any reason and without penalty (Regulation on Distance Contracts, art. 9).</p>
<p>If you were not duly informed of this right, you are not bound by the 14-day period; in that case the right expires <strong>1 year</strong> after the original withdrawal period ends.</p>
<h2>How to Notify</h2>
<p>Send your withdrawal notice in writing or via durable medium through [CONTACT CHANNEL] within the period.</p>
<h2>Return Process</h2>
<p>Return the product within 10 days of your notice using the designated carrier <strong>[RETURN CARRIER]</strong>; return shipping via this carrier is paid by us.</p>
<h2>Refund</h2>
<p>All payments are refunded in a single transaction within <strong>14 days</strong> of the date you hand the product to the carrier.</p>
<h2>Exceptions</h2>
<p>Personalised products, hygiene-sensitive products with opened packaging and other cases listed in art. 15 of the Regulation are excluded.</p>
<h2>Defective Products</h2>
<p>Your optional rights under art. 11 of Law No. 6502 (free repair, replacement, refund, price reduction) remain reserved.</p>$body$,
 now(), now()),

('TESLIMAT', 'teslimat-kargo', 'shipping-delivery',
 'Teslimat ve Kargo', 'Shipping and Delivery',
$body$<!-- LAWYER REVIEW REQUIRED -->
<h2>Hazırlık ve Kargoya Veriliş</h2>
<p>Siparişler, sipariş onayını takiben [1-3] iş günü içinde kargoya verilir. Teslimat süresi her hâlükârda 30 günü aşamaz.</p>
<h2>Kargo Ücreti</h2>
<p>[KARGO ÜCRETİ POLİTİKASI — örn. 1.500 TL üzeri siparişlerde kargo ücretsizdir; altındaki siparişlerde kargo ücreti sipariş özetinde gösterilir.]</p>
<h2>Teslimat Bölgesi</h2>
<p>Şu an yalnızca Türkiye içine teslimat yapılmaktadır. [Yurt dışı teslimat eklenirse gümrük vergi ve masraflarının sorumluluğu burada belirtilecek.]</p>
<h2>Hasarlı Paket</h2>
<p>Paketi teslim alırken hasar görürseniz kargo görevlisine tutanak tutturup paketi teslim almayınız ve bize bildiriniz. Ürünün size teslimine kadar oluşan kayıp ve hasarlardan satıcı sorumludur.</p>
<h2>Adres Değişikliği ve Teslim Edilemeyen Paketler</h2>
<p>Kargoya verilmeden önce adres değişikliği için bizimle iletişime geçebilirsiniz. Teslim edilemeyen paketler için kargo firmasının bildirimi üzerine yeni teslimat planlanır.</p>$body$,
$body$<!-- LAWYER REVIEW REQUIRED -->
<h2>Processing and Dispatch</h2>
<p>Orders are dispatched within [1-3] business days after confirmation. Delivery never exceeds 30 days.</p>
<h2>Shipping Cost</h2>
<p>[SHIPPING POLICY — e.g. free shipping over a threshold; otherwise the cost shown in the order summary.]</p>
<h2>Delivery Area</h2>
<p>We currently deliver within Türkiye only. [If international shipping is added, customs duties responsibility will be stated here.]</p>
<h2>Damaged Packages</h2>
<p>If the package is damaged on arrival, have the courier draw up a report and refuse delivery, then contact us. The seller bears the risk of loss and damage until delivery.</p>$body$,
 now(), now()),

('GARANTI', 'garanti', 'warranty',
 'Garanti Koşulları', 'Warranty',
$body$<!-- LAWYER REVIEW REQUIRED -->
<h2>Garanti Süresi</h2>
<p>Microline kesim makinesi, Garanti Belgesi Yönetmeliği m.6/1 uyarınca <strong>teslim tarihinden itibaren asgari 2 (iki) yıl</strong> garantilidir.</p>
<h2>Garanti Kapsamı</h2>
<p>Garanti süresi içinde malzeme, montaj veya işçilik hatalarından kaynaklanan arızalarda ürün; işçilik masrafı, değiştirilen parça bedeli veya başka herhangi bir ad altında ücret talep edilmeksizin onarılır.</p>
<p>Onarımda geçen süre garanti süresine eklenir (Yönetmelik m.6/3). Ürünün onarım süresi [azami tamir süresi — mevzuattaki güncel süre] iş gününü geçemez.</p>
<h2>Seçimlik Haklarınız</h2>
<p>Garanti süresi içinde arızalanan ürünler için 6502 sayılı Kanun m.11 kapsamında; ücretsiz onarım, ayıpsız misli ile değişim, bedel iadesi veya ayıp oranında indirim haklarından birini kullanabilirsiniz.</p>
<h2>Garanti Dışı Hâller</h2>
<p>Kullanım kılavuzuna aykırı kullanım, yetkisiz müdahale/söküm, düşme-darbe hasarları ve yetkisiz satış kanallarından edinilen ürünler garanti kapsamı dışındadır. Sarf malzemeleri (yedek bıçak modülü ambalajı açıldıktan sonra vb.) garanti kapsamında değildir; ulaşımda hasarlı gelen sarf malzemeleri ücretsiz yenisiyle değiştirilir.</p>
<h2>Başvuru</h2>
<p>Garanti taleplerinizi [İLETİŞİM KANALI] üzerinden iletebilirsiniz. Uyuşmazlık hâlinde Tüketici Hakem Heyeti'ne veya Tüketici Mahkemesi'ne başvurabilirsiniz.</p>
<p>Mesafeli kurulan satışlarda garanti belgesinde imza ve kaşe şartı aranmaz (Yönetmelik m.7/2).</p>$body$,
$body$<!-- LAWYER REVIEW REQUIRED -->
<h2>Warranty Period</h2>
<p>The Microline cutting machine is covered by a warranty of <strong>at least 2 (two) years from the date of delivery</strong>, per the Turkish Warranty Certificate Regulation art. 6/1.</p>
<h2>Coverage</h2>
<p>Failures caused by material, assembly or workmanship defects are repaired free of any charge. Time spent under repair is added to the warranty period (art. 6/3).</p>
<h2>Your Optional Rights</h2>
<p>Under art. 11 of Law No. 6502 you may choose free repair, replacement, refund or price reduction.</p>
<h2>Exclusions</h2>
<p>Use contrary to the manual, unauthorised disassembly, drop/impact damage and products from unauthorised channels are excluded. Consumables are not covered; consumables damaged in transit are replaced free of charge.</p>
<h2>Claims</h2>
<p>Submit warranty claims via [CONTACT CHANNEL]. In case of dispute you may apply to Consumer Arbitration Committees or Consumer Courts. No signature/stamp is required on warranty documents for distance sales (Regulation art. 7/2).</p>$body$,
 now(), now()),

('KULLANIM', 'kullanim-kosullari', 'terms-of-use',
 'Kullanım Koşulları', 'Terms of Use',
$body$<!-- LAWYER REVIEW REQUIRED -->
<h2>1. Genel</h2>
<p>www.microline.com.tr sitesini kullanarak işbu koşulları kabul etmiş sayılırsınız. Site, [ŞİRKET UNVANI] tarafından işletilmektedir.</p>
<h2>2. Fikri Mülkiyet</h2>
<p>Sitedeki tüm içerik (metin, görsel, logo, tasarım) aksi belirtilmedikçe Microline'a aittir; yazılı izin olmaksızın kopyalanamaz ve ticari amaçla kullanılamaz.</p>
<h2>3. Sitenin Kullanımı</h2>
<p>Siteyi hukuka aykırı amaçlarla kullanmak, güvenlik önlemlerini aşmaya çalışmak ve otomatik araçlarla aşırı yük oluşturmak yasaktır.</p>
<h2>4. Sorumluluk Sınırı</h2>
<p>Sitedeki bilgiler özenle hazırlanır; ancak yazım hataları ve güncelliğini yitirmiş bilgiler için sorumluluk kabul edilmez. Ürün fiyat ve stok bilgilerinde açık hata bulunması hâlinde sipariş iptal edilebilir.</p>
<h2>5. Değişiklik</h2>
<p>Microline işbu koşulları dilediği zaman güncelleyebilir; güncel sürüm bu sayfada yayımlanır.</p>
<h2>6. Uygulanacak Hukuk</h2>
<p>İşbu koşullar Türk hukukuna tabidir; uyuşmazlıklarda [YETKİLİ YER] mahkemeleri ve icra daireleri yetkilidir. Tüketici işlemlerinde tüketicinin yasal hakları saklıdır.</p>$body$,
$body$<!-- LAWYER REVIEW REQUIRED -->
<h2>1. General</h2>
<p>By using www.microline.com.tr you accept these terms. The site is operated by [COMPANY TITLE].</p>
<h2>2. Intellectual Property</h2>
<p>All content (text, images, logo, design) belongs to Microline unless stated otherwise and may not be copied or used commercially without written permission.</p>
<h2>3. Use of the Site</h2>
<p>Unlawful use, attempts to bypass security measures and excessive automated load are prohibited.</p>
<h2>4. Limitation of Liability</h2>
<p>Content is prepared with care; no liability is accepted for typographical errors or outdated information. Orders containing obvious price/stock errors may be cancelled.</p>
<h2>5. Changes</h2>
<p>Microline may update these terms at any time; the current version is published on this page.</p>
<h2>6. Governing Law</h2>
<p>These terms are governed by Turkish law. Statutory consumer rights remain reserved.</p>$body$,
 now(), now());
