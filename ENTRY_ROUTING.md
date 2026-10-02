# Bel9ja Board Club — маршрутизація запуску (сайт / WebView / Remote Config)

## Таблиця відповідності згенерованих назв

| Додаток | Сервіс | Призначення | Фактична назва |
|---|---|---|---|
| com.bel9ja.boardclub | Firebase Remote Config (`bel9as-club`) | Стан перевірки (Boolean, у додатку за замовчуванням `false`) | `kestrel_saffron_njv` |
| com.bel9ja.boardclub | Firebase Remote Config (`bel9as-club`) | Початкова HTTPS-URL сайту (String) | `ibis_sorrel_ecx` |
| com.bel9ja.boardclub | Сайт (query-параметр) | Короткоживучий ідентифікатор спроби | `welcv` |
| com.bel9ja.boardclub | SharedPreferences | Файл налаштувань | `basalt_garnet_tk1` |
| com.bel9ja.boardclub | SharedPreferences | hasOpenedWebView | `marlin_pewter_hrn` |
| com.bel9ja.boardclub | SharedPreferences | initialEntryUrl | `umber_garnet_pyi` |
| com.bel9ja.boardclub | SharedPreferences | lastWebViewUrl | `nimbus_quill_trx` |
| com.bel9ja.boardclub | SharedPreferences | Ознака «lastWebViewUrl несправна» | `umber_nimbus_as7` |
| com.bel9ja.boardclub | Диплінк (custom scheme) | Повернення з сайту в додаток | `amberlarch4xo://zephyr-larch-s2p` |
| com.bel9ja.boardclub | App Link | Повернення з сайту в додаток | `https://liktoria.org/zephyr-larch-s2p` |

Назви ключів зашиті в `app/src/main/java/com/bel9ja/boardclub/launch/RouteKeys.kt`.
Змінювати їх після релізу не можна без оновлення додатка.

## Налаштування Firebase Remote Config (менеджер)

1. Firebase Console → проєкт **bel9as-club** → Remote Config.
2. Створити параметри:
   - `kestrel_saffron_njv` — тип **Boolean**, значення за замовчуванням `false`.
   - `ibis_sorrel_ecx` — тип **String**, значення — HTTPS-адреса сайту, напр. `https://site.example/entry`.
3. Гео: додати умову (Condition) **Country/Region in …** і для неї поставити `kestrel_saffron_njv = true`.
   Для решти країн лишити `false`.
4. Опублікувати зміни. Release-збірка кешує конфіг до 10 хв, debug — без кешу.

## Логіка в додатку

**Новий користувач (WebView ще не відкривався):**
Remote Config → якщо помилка / ключа немає / `false` / некоректна URL → функціонал.
Якщо `true` → GET `entryUrl?welcv=<id>` без проходження редиректів:
- `200` → функціонал (ознака WebView не ставиться);
- `301/302/303/307/308` з `Location` на погоджений HTTPS-домен → зберігаються `initialEntryUrl` (без `welcv`) і `hasOpenedWebView`, WebView відкриває **ту саму** URL з тим самим `welcv`;
- будь-що інше (`304`, `4xx/5xx`, таймаут, TLS-помилка) → функціонал.

**Користувач, що вже був у WebView (прапорець Remote Config не використовується):**
- є робоча `lastWebViewUrl` → WebView відкривається з неї, сайт сам вирішує, чи потрібна верифікація;
- `lastWebViewUrl` відсутня або несправна (помилка основної сторінки, TLS, 404/410) → позначити несправною, один раз за запуск GET до `initialEntryUrl` з новим `welcv`:
  `200` → функціонал (наступного запуску — знову перевірка `initialEntryUrl`);
  редирект → WebView, нова успішна сторінка замінює стару;
  недоступна → функціонал, повтор при наступному запуску.

**Диплінк із сайту** (`amberlarch4xo://zephyr-larch-s2p?...`) перехоплюється у WebView (або приходить ззовні), відкриває функціонал і не вважається звичайним запуском. `hasOpenedWebView` не скидається; при наступному окремому запуску WebView відкривається знову. Диплінк ніколи не зберігається як `lastWebViewUrl`.

`lastWebViewUrl` зберігається лише для успішно завантаженої основної сторінки (`isForMainFrame`) погодженого HTTPS-домену; помилки допоміжних ресурсів ігноруються, TLS-помилки не ігноруються (`handler.cancel()`).

## Погоджений домен

Довіреними вважаються:
- зареєстрований домен `initialEntryUrl` (останні дві мітки) і всі його піддомени;
- `liktoria.org` і всі його піддомени (домен верифікації) — `EXTRA_TRUSTED_HOSTS` у `launch/UrlRules.kt`.

## Вимоги до сайту

- Початкова URL працює по HTTPS без службових редиректів (будь-який редирект = запуск WebView).
- Пропуск — `200 OK`; верифікація — тимчасовий редирект (`302/307`) з `Location`, `Cache-Control: no-store`.
- Параметр `welcv` — ідентифікатор спроби: GET додатка і перший запит WebView приходять з однаковим значенням і мають отримати однакове рішення. Cookies з першої відповіді також передаються у WebView. User-Agent GET-запиту = User-Agent WebView.
- Повернення в додаток: `amberlarch4xo://zephyr-larch-s2p` (або App Link, див. нижче). Перед видачею диплінка бекенд перевіряє результат верифікації / одноразовий токен.

## App Link (увімкнено)

- Домен: `liktoria.org`, шлях: `/zephyr-larch-s2p` (`RouteKeys.APP_LINK_HOST`, `intent-filter` з `autoVerify` в маніфесті).
- На сервері розмістити файл `assetlinks.json` (лежить у корені проєкту) за адресою
  `https://liktoria.org/.well-known/assetlinks.json` — HTTP 200, `Content-Type: application/json`, без редиректів.
- У файлі вже є SHA-256 ключа завантаження (взято з `app/release/app-release.aab`).
  Для версії з Google Play **додати другий відбиток** — ключ Play App Signing:
  Play Console → додаток → Тест і випуск → App integrity → App signing → «SHA-256 certificate fingerprint»
  (там же є готовий фрагмент Digital Asset Links JSON).
- Перевірка: `adb shell pm verify-app-links --re-verify com.bel9ja.boardclub`,
  потім `adb shell pm get-app-links com.bel9ja.boardclub` → `liktoria.org: verified`.
- Якщо сайт використовує також `www.liktoria.org`, додати ще один `<data android:host="www.liktoria.org" .../>` і той самий assetlinks.json на www.

## Версії збірки

| Компонент | Версія |
|---|---|
| Gradle (wrapper) | 8.14.3 |
| Android Gradle Plugin | 8.13.0 |
| Kotlin + Compose compiler plugin | 2.0.21 |
| google-services plugin | 4.4.2 |
| compileSdk / targetSdk / minSdk | 36 / 36 / 26 |
| Java target | 17 |

Gradle JDK в Android Studio: **17 або 21** (Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK → «jbr-21»).
Gradle 8.14 не запускається на JDK 25/26.

## Файли

- `launch/RouteKeys.kt` — згенеровані назви
- `launch/RemoteSwitch.kt` — Firebase Remote Config
- `launch/EntryProbe.kt` — GET без редиректів
- `launch/LaunchRouter.kt` — розділи 2.1 / 2.2
- `launch/RouteStore.kt` — локальний стан
- `launch/WebPortalActivity.kt` — WebView, збереження сторінки, диплінки
- `MainActivity.kt` — сплеш + вибір маршруту
- `app/google-services.json` — конфіг Firebase
