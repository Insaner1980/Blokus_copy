# Blokus — Tekninen Spesifikaatio

## 1. Yleiskatsaus

**Tyyppi:** Yksitiedostoinen selainpohjainen lautapeli — klassisen Blokus-strategiapelin toteutus.

**Kieli/Locale:** Suomi (fi). Kaikki käyttöliittymätekstit, virheilmoitukset, aria-labelit ja tilaviestit ovat suomeksi.

**Teknologiat:**
- Yksittäinen HTML-tiedosto (~5715 riviä, ~600KB)
- HTML5, ARIA-saavutettavuusattribuutit kauttaaltaan
- CSS3, CSS Custom Properties (design tokens), animaatiot, media queries
- Vanilla JavaScript (ei frameworkeja tai kirjastoja)
- Web Audio API ääniefekteille
- `localStorage` persistenssiin (profiilit, pelitallennukset, historia)
- Content Security Policy `<meta>`-tagissa
- Itsenäinen: fontit (Quicksand ja Material Symbols Rounded) upotettu base64-data-URI:na `@font-face`-määrittelyissä
- Logo upotettu base64-PNG:nä
- Favicon inline SVG data URI (4 värillistä neliötä, jotka edustavat 4 pelaajan värejä)
- Ulkoinen API: DiceBear (`api.dicebear.com`) avatar-generointiin

**Content Security Policy:**
```
default-src 'self' data:;
script-src 'self' 'unsafe-inline';
style-src 'self' 'unsafe-inline';
img-src 'self' data: https://api.dicebear.com;
font-src 'self' data:;
connect-src 'none';
media-src 'none';
object-src 'none';
base-uri 'none';
frame-ancestors 'none';
form-action 'self'
```

---

## 2. HTML-rakenne

### Dokumentti
- `<!DOCTYPE html>`, `<html lang="fi">`

### Head
- Charset: UTF-8
- Viewport-meta responsiivisuudelle
- CSP-meta-tagi
- Title: `"Blokus"`
- Favicon: inline SVG (4 värillistä suorakaidetta)
- Yksi `<style>`-lohko, joka sisältää kaiken CSS:n (~1850 riviä)

### Body-hierarkia

```
<a class="skip-link">                    — Siirry peliin -linkki (saavutettavuus)
#game-timer                              — Kiinteäasentoinen ajastin ikonilla ja näytöllä (aria-live="off")
#game-container                          — Pää-flex-konttori
  #logo                                  — Logo-kuva (base64-PNG, käännetty -3°)
  <h1 class="sr-only">Blokus</h1>       — Ruudunlukija-only-otsikko
  <main id="main-area">                  — Flex-layout kolmella sarakkeella
    <aside id="left-panel">              — Pelitiedot (pelaajatiedot)
      #info-bar                          — Pelaajatietorivit (dynaamisesti generoitu)
    <section id="board-wrapper">         — Pelilauta
      #board                             — 20×20-ruudukko (dynaamisesti generoitu)
    <aside id="side-panel">              — Pelin ohjaimet
      #difficulty-selector               — Vaikeustasopainikkeet (1, 2, 3) + historia, profiili, ohje
      #controls                          — Kierto VVS/MS, peilaus, passaus, tallennus, uusi peli
      #turn-status                       — Tilaviestialue (role="status", aria-live="polite")
      #preview-box                       — Valitun palan esikatselu ("Valittu pala")
      #pieces-panel                      — Palakortit (dynaamisesti generoitu)
```

### Modaalit (6 kpl)
Kaikki `role="dialog"`, `aria-modal="true"`:

| # | ID | Tarkoitus |
|---|-----|-----------|
| 1 | `#game-over` | Peli ohi -näkymä, lopulliset pisteet, "Pelaa uudestaan" |
| 2 | `#confirm-modal` | Yleinen vahvistusikkuna (dynaaminen teksti, OK/Peruuta) |
| 3 | `#save-notification` | Kiinteäasentoinen tallennusilmoitus (`role="status"`) |
| 4 | `#resume-modal` | Tallennetun pelin jatko ("Jatka peliä" / "Uusi peli") |
| 5 | `#help-modal` | Ohje/apu, näppäinoikotiet ja pelin ohjaus |
| 6 | `#profile-modal` | Profiilinhallinta |
| 7 | `#history-modal` | Historia ja tilastot, välilehtinavigointi |

### Saavutettavuusominaisuudet
- `#sr-announcer` — ruudunlukijan live-alue (`aria-live="polite"`, `aria-atomic="true"`)
- Skip-linkki
- Kaikissa painikkeissa `aria-label`-attribuutit
- Laudan soluilla `role="gridcell"`, `aria-rowindex`, `aria-colindex`, `aria-selected`
- Laudalla `role="grid"`, `aria-rowcount`, `aria-colcount`
- Välilehtipaneelit: `role="tablist"`, `role="tab"`, `role="tabpanel"`
- Fokuksenhallinta: `FOCUSABLE_SELECTOR`-vakio, fokusloukku modaaleissa, `aria-activedescendant` laudalla

---

## 3. CSS-tyylit

### Design-järjestelmä / Custom Properties

#### Solukoko
- `--cell-size: 32px`
- Responsiivinen: 30px @ 820px, 28px @ 640px, 36px karkealle osoittimelle (touch)

#### Laudan koko
- `--board-size: 20`

#### Väripaletti

| Muuttuja | Arvo | Kuvaus |
|----------|------|--------|
| `--brand-primary` | `#395596` | Sininen (pääväri) |
| `--brand-primary-dark` | `#2d4278` | Tumma sininen |
| `--accent-yellow` | `#f3b625` | Keltainen aksentti |
| `--accent-red` | `#af2116` | Punainen aksentti |
| `--accent-green` | `#65a04c` | Vihreä aksentti |
| `--accent-gold` | `#d4a820` | Kulta |
| `--accent-gold-dark` | `#c89510` | Tumma kulta |
| `--accent-green-dark` | `#4a8030` | Tumma vihreä |

#### Pintavärit

| Muuttuja | Arvo |
|----------|------|
| `--surface-base` | `#e8e4de` |
| `--surface-strong` | `#fff` |
| `--surface-hover` | `#fafafa` |
| `--surface-muted` | `#e0ddd8` |

#### Tekstivärit

| Muuttuja | Arvo |
|----------|------|
| `--text-main` | `#333` |
| `--text-soft` | `#444` |
| `--text-muted` | `#555` |
| `--text-subtle` | `#888` |
| `--text-faint` | `#aaa` |

#### Laudan värit

| Muuttuja | Arvo |
|----------|------|
| `--board-surface` | `#f5f5f5` |
| `--board-grid` | `#ffffff` |
| `--board-border` | `#e0e0e0` |
| `--cell-bg` | `#ffffff` |

#### Modaalit
- `--modal-backdrop: rgba(0,0,0,0.4)`
- `--modal-surface: #f8f6f2`

#### Välit (Spacing)

| Token | Arvo |
|-------|------|
| `--space-xs` | 4px |
| `--space-sm` | 8px |
| `--space-md` | 12px |
| `--space-lg` | 16px |
| `--space-xl` | 24px |
| `--space-2xl` | 32px |

#### Typografia

| Token | Arvo |
|-------|------|
| `--font-xs` | 0.65rem |
| `--font-sm` | 0.75rem |
| `--font-base` | 0.85rem |
| `--font-md` | 1rem |
| `--font-lg` | 1.4rem |
| `--font-xl` | 1.6rem |

#### Reunan pyöristys

| Token | Arvo |
|-------|------|
| `--radius-sm` | 4px |
| `--radius-md` | 8px |
| `--radius-lg` | 10px |
| `--radius-xl` | 12px |
| `--radius-2xl` | 16px |

#### Siirtymäajat
- `--duration-fast: 0.15s`
- `--duration-normal: 0.3s`

### Fontit
- **Quicksand** (sans-serif): Upotettu base64-TTF, `font-display: swap`
- **Material Symbols Rounded**: Upotettu base64-TTF, `font-display: block`

### Palikkatyylitys — "Kiiltävä läpikuultava muovi" -efekti

Jokainen täytetty solu käyttää monikerrosefektiä:
- **Perusgradientti:** `linear-gradient(180deg, rgba(255,255,255,0.35) 0%, rgba(255,255,255,0.05) 40%, transparent 60%)`
- **Monivarjo-reuna:** sisäinen yläkorostus, alavario, vasen korostus, oikea varjo, ulkovarjo
- **`::before`-pseudoelementti:** Sisäinen syvennetty lokero (4px inset, viistetty reuna)
- **`::after`-pseudoelementti:** Yläreuna-kiiltokorostus (5px korkea gradientti)

#### Pelaajakohtaiset värit

| Pelaaja | Pohja | Reuna |
|---------|-------|-------|
| 0 (Sininen) | `#395596` | `#182850` |
| 1 (Keltainen) | `#f3b625` | `#906008` |
| 2 (Punainen) | `#af2116` | `#580c06` |
| 3 (Vihreä) | `#65a04c` | `#245018` |

### Haamusolut (esikatselu laudalla)
- `.ghost-0` – `.ghost-3`: Läpinäkyvät pelaajan värit (alpha 0.35–0.40)
- `.ghost-invalid`: Punainen diagonaaliraitatäyttö punaisella reunavarjolla

### Responsiiviset breakpointit

| Breakpoint | Muutokset |
|------------|-----------|
| `max-width: 1180px` | Flex-wrap-layout, paneelit uudelleenjärjestetty |
| `max-width: 820px` | Solukoko 30px, ajastin staattiseksi, modaalit 92vw |
| `max-width: 640px` | Solukoko 28px, pienennetty padding |
| `pointer: coarse` | Solukoko 36px (kosketusnäytöt) |
| `prefers-reduced-motion: reduce` | Kaikki animaatiot pois, koriste-pseudoelementit piilotettu |

### Tooltipit
- Puhdas CSS `[data-tooltip]`-attribuutilla + `::after`-pseudoelementillä

---

## 4. Pelilogiikka

### Laudan esitys
- `BOARD_SIZE = 20` (20×20 ruudukko = 400 solua)
- `board[][]`: 2D-taulukko, jokainen solu on `-1` (tyhjä) tai pelaajaindeksi (0–3)
- Laudan solut tallennettu myös litteään taulukkoon `boardCells[]` nopeaa DOM-käsittelyä varten

### Palamäärittelyt

`PIECE_DEFS` — 21 palaa, jokainen muodossa `{ name, cells }`, jossa `cells` on taulukko `[rivi, sarake]`-koordinaatteja:

| # | Nimi | Koko | Muoto |
|---|------|------|-------|
| 1 | `1` | 1 | Yksittäinen neliö (monomino) |
| 2 | `2` | 2 | Domino (vaakapari) |
| 3 | `I3` | 3 | Suora tromino |
| 4 | `V3` | 3 | L-muotoinen tromino |
| 5 | `I4` | 4 | Suora tetromino |
| 6 | `O4` | 4 | Neliötetromino |
| 7 | `T4` | 4 | T-muotoinen tetromino |
| 8 | `L4` | 4 | L-muotoinen tetromino |
| 9 | `S4` | 4 | S/Z-muotoinen tetromino |
| 10 | `F5` | 5 | F-pentomino |
| 11 | `I5` | 5 | Suora pentomino |
| 12 | `L5` | 5 | L-pentomino |
| 13 | `N5` | 5 | N-pentomino |
| 14 | `P5` | 5 | P-pentomino |
| 15 | `T5` | 5 | T-pentomino |
| 16 | `U5` | 5 | U-pentomino |
| 17 | `V5` | 5 | V-pentomino |
| 18 | `W5` | 5 | W-pentomino |
| 19 | `X5` | 5 | X/plus-pentomino |
| 20 | `Y5` | 5 | Y-pentomino |
| 21 | `Z5` | 5 | Z-pentomino |

**Vakiot:** `PIECE_COUNT = 21`, `TOTAL_CELLS = 89` (kaikkien palojen solujen summa)

### Palantransformaatiot

| Funktio | Kuvaus | Kaava |
|---------|--------|-------|
| `rotateCW(cells)` | 90° myötäpäivään | `[r,c] → [c,-r]` |
| `rotateCCW(cells)` | 90° vastapäivään | `[r,c] → [-c,r]` |
| `flipH(cells)` | Vaakapeilaus | `[r,c] → [r,-c]` |
| `normalize(cells)` | Origoon siirto, leksikografinen järjestys | min rivi/sarake = 0 |
| `getAllOrientations(cells)` | Kaikki uniikit orientaatiot (max 8) | 4 kiertoa × 2 peilausta |
| `cellsKey(cells)` | Kanoninen merkkijonoavain deduplikointiin | — |

Orientaatiot välimuistitetaan `orientationCache`-Mapissa.

### Pelaajajärjestelmä
- `PLAYER_COUNT = 4`
- `PLAYER_ORDER = [0, 1, 2, 3]`
- Pelaaja 0 on aina ihmispelaaja
- Pelaajat 1, 2, 3 ovat tekoälyvastustajat
- Perusnimet: `['Sinä', 'Keltainen', 'Punainen', 'Vihreä']`
- Perusvärit: Sininen, Keltainen, Punainen, Vihreä
- Pelaaja voi valita värinsä profiileissa (värinvaihto oletuspaikan kanssa)

#### Pelaajaobjektin rakenne
```javascript
{
  pieces: [...PIECE_DEFS],  // kopio kaikista 21 palan määrittelystä
  usedPieces: [false × 21], // mitkä palat on asetettu
  piecesPlaced: 0,          // asetettujen palojen lukumäärä
  score: 89,                // TOTAL_CELLS miinus asetetut solut (pienempi = parempi)
  passed: false             // onko pelaaja passannut
}
```

### Aloituskulmat ja vuorojärjestys
- `START_CORNERS` (oletukset): `[[19,19], [0,19], [0,0], [19,0]]`
- `CLOCKWISE_CORNERS`: `[[19,19], [19,0], [0,0], [0,19]]`
- Pelaaja 0 (ihminen) aloittaa aina myötäpäiväisestä positiosta 0 (oikea alakulma, `[19,19]`)
- AI-pelaajien kulmat sekoitetaan satunnaisesti joka pelissä (`shuffleAiCorners()`)
- Vuorojärjestys seuraa myötäpäivään ihmisestä

### Sijoitussäännöt (`isValidPlacement`)

1. **Laudan rajat:** Kaikki palan solut pysyvät laudan rajojen sisällä (0–19)
2. **Tyhjät solut:** Kaikki palan solut osuvat tyhjiin soluihin (`board[r][c] === -1`)
3. **Ei reunavierekkäisyyttä:** Ortogonaalinen vierekkäisyys (ylös/alas/vasen/oikea) saman pelaajan olemassa olevien palojen kanssa on kielletty
4. **Ensimmäinen pala:** Palan on peitettävä pelaajan aloituskulmasolu
5. **Myöhemmät palat:** Palan on koskettava vähintään yhtä saman pelaajan olemassa olevan palan diagonaalista naapuria

### Kulmaposition välimuisti
- `cornerCache[playerIdx]`: Välimuistitettu joukko kelvollisia kulmapositioita per pelaaja
- `getCornerPositions(playerIdx)`: Laskee kelvolliset diagonaalipositiot uusille paloille
  - Ensimmäiselle palalle: 9×9 alue aloituskulman ympärillä
  - Myöhemmille: Skannaa laudan kaikki diagonaalisesti vierekkäiset tyhjät solut, jotka eivät ole reunavierekkäisiä samaan väriin
- Välimuisti tyhjätään jokaisella palanasetuksella

### Pisteytysjärjestelmä
- Aloituspisteet: `TOTAL_CELLS = 89` per pelaaja
- Jokainen asetettu solu: pisteet laskevat yhdellä
- **Pienempi pistemäärä = parempi** (jäljellä olevat asettamattomat solut)
- Loppusijoitus: järjestetty pisteiden mukaan nousevasti (pienin ensin = voittaja)
- Tasapeli ratkaistaan pelaajaideksin mukaan

### Pelin päättymisolosuhteet
- `consecutivePasses >= PLAYER_COUNT` (4): kaikki pelaajat ovat passanneet peräkkäin
- Pelaaja passaa kun:
  - Hän vapaaehtoisesti passaa (vain ihminen, vahvistusikkuna)
  - Hänellä ei ole kelvollisia siirtoja (`hasValidMove` palauttaa false) — automaattipassaus
  - Passaamisen jälkeen `player.passed = true` ja hänet ohitetaan tulevissa vuoroissa

---

## 5. Tekoälyjärjestelmä

### Arkkitehtuuri
Kolme tekoälypelaajaa (indeksit 1, 2, 3), joista jokaisella on kiinteä **persoonallisuusarkkityyppi**, joita moduloi globaali **vaikeustaso**.

### Tekoälypersoonallisuudet (`AI_PERSONALITIES`)

#### Pelaaja 1 — Ekspansionisti (Keltainen)
- Maksimoi alueen ja kulmat
- Korkea levittäytymisbonus (1.4) — suosii laajaa levittäytymistä
- Korkea kulmabonus (1.4)
- Matala keskusfokus (0.3)
- Helpolla vaikeustasolla reunahakuinen

#### Pelaaja 2 — Aggressiivinen (Punainen)
- Keskustan hallinta, blokkaa johtajaa
- Korkein keskusbonus (1.5)
- Korkein blokkausbonus (1.6) — tuhoaa aktiivisesti vastustajan kulmia
- Kohdistaa erityisesti johtavaan vastustajaan

#### Pelaaja 3 — Puolustava (Vihreä)
- Pysyy lähellä omaa aluetta, suojaa omia kulmia
- Matala levittäytymisbonus (0.6) — kompakti pelityyli
- Korkea reunabonus (1.2) — suosii omaa neljännestä
- Matala keskusfokus (0.1)

### Vaikeustason parametrit (`DIFFICULTY_PARAMS`)

| Parametri | Helppo (1) | Keskitaso (2) | Vaikea (3) |
|-----------|------------|---------------|------------|
| `temperature` | 2.4 | 1.0 | 0.3 |
| `skipFilterChance` | 0.6 | 0.1 | 0.0 |
| `localFocusRadius` | 4 | 20 | 20 |
| `frustrationDecay` | 5 | 1 | 0 |
| `frustrationPenalty` | 0.5 | 0.1 | 0 |
| `aestheticChance` | 0.5 | 0.15 | 0.0 |
| `maxCandidates` | 15 | 80 | 200 |
| `pieceBias` | Suosii pieniä (T4:1.3, L4:1.3 jne.) | Ei | Ei |

### Tekoälyn pelajakohtainen tila (`aiState`)
```javascript
{
  lastDirection: [dr, dc],  // liikesuunta (momentum)
  frustration: 0,           // vuorot laatusakon kestosta jäljellä
  lastPlacedPos: [r, c]     // viimeksi asetetun palan keskipiste
}
```

### Pelin vaiheet (`getGamePhase`)

| Vaihe | Ehto |
|-------|------|
| `opening` (avaus) | 0–4 palaa asetettu |
| `midgame` (keskipeli) | 5–13 palaa asetettu |
| `endgame` (loppupeli) | 14+ palaa asetettu |

Vaihe vaikuttaa arviointipainoihin (koko, kulma, keskusta, blokkaus, alue, reuna).

### Tekoälyn päätösprosessi (`aiPlay`)

1. **Kontekstin kerääminen:** Lasketaan vastustajien kulmat, tunnistetaan johtaja (aggressiiviselle persoonallisuudelle), arvioidaan alue
2. **Frustraatiojärjestelmä:** Jos estetty (helppo taso), laatu heikkenee useiksi vuoroiksi
3. **Siirtojen generointi** (`getValidMoves`): Iteroi käytettävissä olevat palat × orientaatiot × kulmapositiot
   - Käyttää paikallista fokusta (helppo: vain lähellä viimeistä sijoitusta, säde 4)
   - Rajaa kandidaatit (`maxCandidates`)
   - Palautuu tyhjentävään hakuun jos rajoitettu tai tyhjä
4. **Siirron arviointi** (`evaluateMove`): Monikriteerinen pisteytys:
   - Palan koko × paino
   - Luodut uudet kulmat (laatu painotettu etäisyydellä keskustasta)
   - Tuhotut omat kulmat (sakko)
   - Tuhotut vastustajien kulmat (blokkausbonus, vahvistettu aggressiiviselle vs johtaja)
   - Keskustaetäisyys
   - Reuna/neljännesaffiniteetti (helppo)
   - Alueen arvioidelta (keskitaso/vaikea)
   - Levittäytymis/kompaktiuspreferenssi
   - Päätösmomentti (linjaus edellisen suunnan kanssa)
5. **Anti-pattern-suodatin** (`filterImplausible`):
   - Älä pelaa pieniä paloja kun suuria on saatavilla (paitsi loppupelissä)
   - Hylkää nollakulmaiset siirrot kun vaihtoehtoja on
   - Ohita suodatin todennäköisyydellä `skipFilterChance`
6. **Softmax-valinta:** Probabilistinen valinta parhaista kandidaateista pisteikkunan sisällä, moduloitu `temperature`-arvolla
7. **Varavalinta:** Jos tekoälyn valitsema siirto epäonnistuu validoinnissa, palautuu `findFirstValidMove`-funktioon

### Alue-estimaatio (`estimateTerritory`)
BFS-tulvatäyttö kulmapositioista tyhjien solujen läpi, jotka ovat saavutettavissa ilman reunavierekkäisyysrikkomuksia. Palauttaa saavutettavien solujen lukumäärän.

---

## 6. Käyttöliittymä ja vuorovaikutus

### Palan valinta ja esikatselu
- Palat näytetään pieninä ruudukkokorteina `#pieces-panel`-paneelissa
- Jokainen kortti on `<button>` klikkaus-käsittelijällä
- Mini-ruudukon renderöinti: 15px solut palakorteissa, 19px esikatselussa, 22px preview-cellissä
- Käytetyt palat: opacity 0.1, pointer-events none, `aria-disabled="true"`
- Valittu pala: `transform: scale(1.12)`, drop-shadow-hehku pelaajan värissä
- Hover-efekti paloissa: `translateY(-2px)` drop-shadow'lla
- Porrastettu intro-animaatio: `fadeInScale` asteittaisella viiveellä (0.02s per pala)

### Palan esikatselulaatikko (`#preview-box`)
- Näyttää valitun palan suuremmassa koossa
- 3D-perspektiivitransformaatio (`perspective: 500px`)
- Esikatseluruudukko pyörii/peilautuu CSS-transformilla vastaten nykyistä kiertoa/peilausta: `scaleX(flip) rotate(rotation deg)`
- Päivittyy reaaliaikaisesti kun palaa kierretään/peilataan

### Laudan vuorovaikutus

#### Hiiri/Osoitin
- Hover laudan solun yllä: Näyttää haamuesikatstelun (keskitetty kursorin kohdalle)
- Klikkaus laudan solulle: Yrittää sijoittaa palan
- Haamuesikatselun käyttää `setGhostPreview()`-funktiota välimuistilla DOM-päivitysten minimointiin

#### Näppäimistönavigaatio (3-tilainen järjestelmä)

**Tila 1 — Ei palaa valittu:**
- Ylös/Alas-nuolet: Selaa paloja

**Tila 2 — Pala valittu, ei laudalla:**
- Vasen/Oikea: Kiertä palaa
- Ylös/Alas: Selaa paloja
- F: Peilaa
- Enter/Space: Siirry laudalle

**Tila 3 — Laudalla (`kbMode=true`):**
- Nuolinäppäimet: Liikuta kursoria laudalla
- R: Kiertä myötäpäivään
- Q: Kiertä vastapäivään
- F: Peilaa
- Enter/Space: Aseta pala
- Escape: Palaa palanvalintaan

Visuaalinen fokusindikaattori: `kb-focus`-luokka nykyisellä solulla. `aria-activedescendant` päivitetään laudan elementille.

### Haamu/esikatselujärjestelmä
- `ghostCells[]`-taulukko `{r, c, valid}`-merkinnöillä
- Kelvolliset haamut: Pelaajan värinen läpinäkyvä peite
- Kelvoton haamu: Punainen diagonaaliraitatäyttö
- Inkrementaalinen renderöinti: Päivittää vain muuttuneet solut kehysten välillä
- `renderedGhostState`-Map seuraa nykyistä haamu-DOM-tilaa

### Sijoituspalaute
- **Kelvollinen sijoitus:** `popIn`-animaatio (skaalauspomppu), sijoitusääni
- **Kelvoton sijoitus:** Laudan wrapperin `shake`-animaatio (vaakavärähtely), 180ms cooldown
- **Laudan vuorovaikutuslukko:** 160ms sijoituksen jälkeen tuplaklikkausten estämiseksi

### Ohjaimet

| Painike | Toiminto | Tyyli |
|---------|----------|-------|
| `#rotate-ccw-btn` | Kiertä vastapäivään | Ryhmitelty painike, Material Symbols -ikoni |
| `#rotate-cw-btn` | Kiertä myötäpäivään | Ryhmitelty painike, Material Symbols -ikoni |
| `#flip-btn` | Peilaa vaakasuunnassa | Normaali painike |
| `#pass-btn` | Passaa vuoro | Punainen "danger"-tyyli, vahvistusikkuna |
| `#save-btn` | Tallenna pelitila | Normaali painike |
| `#new-game-btn` | Uusi peli | Sininen "primary"-tyyli, vahvistus jos peli käynnissä |

### Tilanäyttö (`#turn-status`)
- Näkyy/piiloutuu `.show`-luokalla
- Busy-tila: sykkivä piste-animaatio (`pulseBusy`)
- Viestit ilmoitetaan `announce()`-kautta `#sr-announcer`-elementille ruudunlukijoille

### Pisteiden näyttö
- Pelaajatietopalkki näyttää: Väripiste, nimi, pisteet
- Aktiivinen pelaaja: Korostettu `pulseName`-animaatiolla (skaala + väripulssi)
- Aktiivisen pelaajan piste: Hehkuva box-shadow (10px säde pelaajan värissä)
- Passanneet pelaajat: 30% läpinäkyvyys, yliviivattu teksti

### Ajastin (`#game-timer`)
- Kiinteä positio oikeassa yläkulmassa (staattinen mobiililla)
- Muoto: `m:ss`
- Käynnistyy ensimmäisellä palanasetuksella
- Pysähtyy pelin päättyessä
- Quicksand-fontti (2rem, paino 700)
- Käynnissä-tila muuttaa ikonin/tekstin väriä

---

## 7. Ääni/Audio

### Web Audio API -toteutus
- **Lazy-alustus:** `AudioContext` luodaan ensimmäisellä `pointerdown`- tai `keydown`-tapahtumalla
- **Selainyhteensopivuus:** Tarkistaa `window.AudioContext || window.webkitAudioContext`
- **Yksi ääniefekti:** `playPlaceSound()` — laukeaa vain ihmispelaajalle (pelaaja 0)

### Äänen ominaisuudet
- Oskillaattorityyppi: `triangle`
- Aloitustaajuus: 400Hz
- Eksponentiaalinen ramppaus alas 80Hz:iin 0.15s aikana
- Gain: Nopea nousu (0 → 0.8 0.02s aikana), eksponentiaalinen vaimeneminen 0.001:een 0.2s aikana
- Kokonaiskesto: 0.2s
- Kuvattu "lämpimäksi, kuuluvaksi tok-ääneksi"

### Virheenkäsittely
- Jos audiokonteksti on keskeytetty, yritetään jatkamista
- Ajonaikaisen virheen sattuessa asetetaan `audioCtx = null` toistuvan virheen estämiseksi

---

## 8. Tilanhallinta

### Ydin-pelitilamuuttujat
```javascript
board = []              // 2D-taulukko [20][20], arvot -1 tai 0-3
players = []            // 4 pelaajaobjektin taulukko
currentPlayer = 0       // nykyisen pelaajan indeksi (0-3)
selectedPieceIdx = -1   // ihmisen valittu pala (-1 = ei valittu)
currentShape = null     // valitun palan normalisoidut solut (kiertojen/peilausten jälkeen)
ghostCells = []         // nykyiset haamuesikatstelun solut
gameActive = true       // false pelin päätyttyä
consecutivePasses = 0   // nollataan 0:aan onnistuneen sijoituksen jälkeen
lastPlacedCells = []    // viimeksi asetettujen solujen litteat indeksit (animaatiota varten)
turnOrder = [0,1,2,3]   // satunnaistetaan joka pelissä
turnPosition = 0        // indeksi turnOrder-taulukkoon
previewRotation = 0     // asteet esikatselun CSS-transformia varten
previewFlip = 1         // 1 tai -1 esikatselun scaleX-arvoa varten
kbCursorR/kbCursorC     // näppäimistökursorin positio (-1 = inaktiivinen)
kbMode = false          // true kun navigoidaan laudalla näppäimistöllä
```

### Vuorovaiheen hallinta
```javascript
turnPhase = 'idle'      // yksi: 'idle', 'ai_action', 'turn_advance', 'human_auto_pass_advance'
aiRunning = false       // estää samanaikaisen AI-suorituksen
aiTurnTimeout           // timeout-ID AI-viiveelle
turnAdvanceTimeout      // timeout-ID vuoronvaihdon viiveelle
placementLockedUntil    // aikaleima vuorovaikutuslukkolle
```

### Renderöinnin optimointi
```javascript
boardCells = []              // välimuistitettu taulukko DOM-solujen elementeistä
renderedBoardState = []      // litteä taulukko seuraten nykyistä CSS-tilaa per solu
renderedGhostState = Map()   // seuraa haamu-CSS-luokkia per soluindeksi
cornerCache = [null × 4]     // pelaajakohtainen kulmaposition välimuisti
orientationCache = Map()     // palan orientaation deduplikointivälimuisti
```

### Persistenssi (localStorage)

#### Tallenna/Lataa peli (`SAVE_KEY = 'blokus-saved-game'`)

Tila serialisoituna JSON:ina sisältäen: board, players (usedPieces, passed), currentPlayer, turnOrder, turnPosition, consecutivePasses, lastPlacedCells, gameActive, timerElapsed, currentDifficulty, aiState, startCorners, turnPhase, savedAt-aikaleima.

Laaja normalisointi/validointi latauksessa:
- `normalizeBoardState()`: Validoi mitat, soluarvot
- `normalizeSavedPlayers()`: Rekonstruoi palat PIECE_DEFS:istä, laskee pisteet uudelleen laudan tilasta
- `normalizeAiState()`: Validoi AI-muisti
- `normalizeStartCorners()`: Validoi uniikit ja rajat
- `normalizeTurnOrder()`: Validoi permutaatio
- `normalizeLastPlacedCells()`: Validoi indeksit

#### Profiilisystem (`PROFILES_KEY = 'blokus-profiles'`)
```javascript
{
  id: number,           // aikaleimapohjainen uniikki ID
  name: string,         // max 30 merkkiä
  colorIdx: 0-3,        // valittu pelaajan väri
  avatarStyle: string,  // DiceBear-tyylin nimi
  avatarSeed: string,   // satunnainen siemen avatarille
  avatarData: string,   // valinnainen base64 mukautettu avatar (max 160KB data URL)
  active: boolean,      // yksi profiili aktiivinen kerrallaan
  history: []           // pelihistoriamerkinnät (max 50)
}
```

#### Historiamerkintä
```javascript
{
  date: ISO-merkkijono,
  rank: 1-4,
  score: 0-89,
  difficulty: 1-3,
  time: sekuntia,
  scores: [{name, score, idx}]  // kaikki 4 pelaajaa
}
```

#### Demodata
Alustuksen yhteydessä, jos profiilit nimeltä "Aatto" tai "Emma" ovat olemassa ilman historiaa, kylvetään demopelidata (12 peliä Aatolle, 15 Emmalle) realistisilla vaihdellulilla tuloksilla.

---

## 9. Tapahtumankäsittely

### Globaalit tapahtumankuuntelijat
- `document.addEventListener('pointerdown', initAudio, { once: true })` — lazy audio init
- `document.addEventListener('keydown', initAudio, { once: true })` — lazy audio init
- `document.addEventListener('keydown', ...)` — pääkäppäimistökäsittelijä

### Laudan tapahtumankuuntelijat
Sidotaan kerran `boardListenersBound`-lipulla:
- `boardEl.addEventListener('mouseover', onBoardPointerOver)` — haamuesikatselun hover
- `boardEl.addEventListener('click', onBoardClick)` — palansijoitus

### Näppäinoikotiet (3-tilainen kone)

#### Kun modaali on auki:
- Tab: Loukkuun modaalin sisällä (`trapModalFocus()`)
- Escape: Sulkee aktiivisen modaalin

#### Kun pala ei valittu (ja ihmisen vuoro):
- Ylös: Edellinen pala
- Alas: Seuraava pala

#### Pala valittu, ei laudalla:
- Vasen: Kiertä vastapäivään
- Oikea: Kiertä myötäpäivään
- Ylös/Alas: Selaa paloja
- F: Peilaa
- Enter/Space: Siirry laudalle

#### Laudalla (`kbMode=true`):
- Nuolinäppäimet: Liikuta kursoria
- R: Kiertä myötäpäivään
- Q: Kiertä vastapäivään
- F: Peilaa
- Enter/Space: Aseta pala
- Escape: Poistu laudalta, palaa palanvalintaan

### Painikkeiden klikkaus-käsittelijät

| Painike | Funktio |
|---------|---------|
| `#history-btn` | `showHistory()` |
| `#profile-btn` | `showProfileModal()` |
| `#help-btn` | `showHelp()` |
| `#rotate-ccw-btn` | `rotatePieceCCW()` |
| `#rotate-cw-btn` | `rotatePiece()` |
| `#flip-btn` | `flipPiece()` |
| `#pass-btn` | `passTurn()` |
| `#save-btn` | `saveAndQuit()` |
| `#new-game-btn` | `newGame()` |
| `#play-again-btn` | `newGame()` |
| `#help-close-btn` | `closeHelp()` |
| `#history-tab-history/stats` | `switchHistoryTab()` |
| `#history-close-btn` | `closeHistory()` |
| `#confirm-ok` | Suorittaa tallennetun callbackin, sulkee |
| `#confirm-cancel` | Sulkee |
| `#resume-continue` | Lataa tallennetun pelin |
| `#resume-discard` | Tyhjentää tallennuksen, aloittaa uuden pelin |

### Modaalin taustapinnan klikkaus
Kaikki 6 modaalia: Klikkaus taustapinnalle (target === modaalielementti) sulkee modaalin.

### Historia-välilehtien näppäimistönavigointi
Vasen/Oikea nuoli, Home/End vaihtaa Historia- ja Tilastot-välilehtien välillä.

### Vaikeustasopainikkeet
Kaikki `.diff-btn[data-level]`-painikkeet: klikkaus laukaisee `setDifficulty(level)`.

---

## 10. Vakiot ja konfiguraatio

```javascript
BOARD_SIZE = 20
PLAYER_COUNT = 4
PIECE_COUNT = 21
TOTAL_CELLS = 89
EDGE_INDEX = 19
MAX_HISTORY_ENTRIES = 50
BOARD_INTERACTION_LOCK_MS = 160
INVALID_FEEDBACK_COOLDOWN_MS = 180
AI_TURN_DELAY_MIN_MS = 300
AI_TURN_DELAY_RANGE_MS = 400
TURN_ADVANCE_DELAY_MS = 400
HUMAN_AUTO_PASS_DELAY_MS = 1500
SAVE_NOTIFICATION_DURATION_MS = 2000
MAX_AVATAR_DIMENSION = 160
MAX_AVATAR_FILE_SIZE = 5 * 1024 * 1024  // 5MB
MAX_AVATAR_DATA_URL_LENGTH = 160000
AVATAR_STYLES = ['bottts', 'avataaars', 'pixel-art', 'fun-emoji', 'lorelei', 'thumbs']
ALLOWED_AVATAR_MIME_TYPES = ['image/webp', 'image/png', 'image/jpeg', 'image/gif']
SAVE_KEY = 'blokus-saved-game'
PROFILES_KEY = 'blokus-profiles'
HISTORY_KEY = 'blokus-history'
MODAL_IDS = ['game-over', 'confirm-modal', 'history-modal', 'profile-modal', 'help-modal', 'resume-modal']
```

Oletusaloituskulmat: `[[19,19], [0,19], [0,0], [19,0]]`
Myötäpäiväkulmat: `[[19,19], [19,0], [0,0], [0,19]]`

---

## 11. Apufunktiot

### Väriapurit

| Funktio | Kuvaus |
|---------|--------|
| `parseCssColor(color)` | Palauttaa `{r,g,b}` hex (#RGB, #RRGGBB) ja rgba()-merkkijonoista |
| `toHexColor({r,g,b})` | Palauttaa `#rrggbb` |
| `mixColor(color, target, amount)` | Sekoitettu hex-väri |
| `withAlpha(color, alpha)` | Palauttaa `rgba(r,g,b,a)` |
| `updatePlayerColorVariables()` | Asettaa CSS-custom-propertyt kaikille 4 pelaajalle |
| `clampColorIndex(value)` | Rajaa arvoon 0–3 |
| `readThemeColor(variableName, fallback)` | Lukee lasketun CSS-muuttujan |

### Datan normalisointi

| Funktio | Kuvaus |
|---------|--------|
| `normalizeString(value, fallback)` | Merkkijonon normalisointi |
| `normalizeBoolean(value)` | Boolean-normalisointi |
| `normalizeInteger(value, fallback, {min, max})` | Kokonaisluvun normalisointi |
| `normalizeProfileName(value)` | Trimmattu, max 30 merkkiä |
| `normalizeAvatarStyle(value)` | Avatar-tyylin validointi |
| `normalizeAvatarSeed(value, fallback)` | Avatar-siemenen validointi |
| `normalizeAvatarData(value)` | Avatar-datan validointi |
| `normalizeHistoryEntry(entry)` | Pelihistoriamerkinnän validointi |
| `normalizeProfiles(profiles)` | Varmistaa yhden aktiivisen, validoi kaikki kentät |

### Tallennusapurit

| Funktio | Kuvaus |
|---------|--------|
| `getStorage()` | Turvallinen `localStorage`-käyttö |
| `readStorageItem(key)` | Lukee arvon |
| `writeStorageItem(key, value, errorMessage)` | Kirjoittaa arvon |
| `removeStorageItem(key)` | Poistaa arvon |
| `readJsonStorageItem(key, fallback, errorMessage)` | Parsii JSON virheenpalautuksella |

### Avatar-funktiot

| Funktio | Kuvaus |
|---------|--------|
| `getAvatarUrl(profile)` | Palauttaa mukautetun data URL:n tai DiceBear API URL:n |
| `getFallbackAvatarData(label, bgColor)` | Generoi SVG-varakuvan nimikirjaimilla |
| `bindAvatarFallback(img, label)` | Asettaa virhekäsittelijän + 10s latausaikakatkaisun |
| `validateAvatarFile(file)` | Tarkistaa tyypin ja koon |
| `compressAvatarFile(file)` | Lukee, skaalaa max 160px, muuntaa WebP:ksi laadulla 0.82 |
| `fileToDataUrl(file)` | Promise-wrapperi |
| `loadImageElement(src)` | Promise-wrapperi |
| `createAvatarSeed(prefix)` | Generoi satunnainen siemen `crypto.randomUUID` tai `Math.random` |

### Käyttöliittymäapurit

| Funktio | Kuvaus |
|---------|--------|
| `announce(message, priority)` | Päivittää `#sr-announcer` ruudunlukijoille |
| `setMessage(msg, options)` | Päivittää `#turn-status` busy-tilalla |
| `buildPieceShapeGrid(cells, options)` | Luo palan mini-ruudukko-DOM-elementin |
| `shuffle(arr)` | Fisher-Yates-sekoitus |
| `formatTimer(seconds)` | Palauttaa `m:ss` |
| `formatDate(iso)` | Suomalainen päivämäärämuoto `d.m. h:mm` |

### Modaalinhallinta

| Funktio | Kuvaus |
|---------|--------|
| `openModal(id, options)` | Näyttää modaalin, tallentaa fokuskohteen, hallinnoi body scroll lock |
| `closeModal(id, options)` | Piilottaa modaalin, palauttaa fokuksen |
| `trapModalFocus(event)` | Tab/Shift+Tab-fokusloukku |
| `getOpenModal()` | Etsii avoimen modaalin |
| `closeActiveModal()` | Sulkee modaalin tyypin mukaan |
| `updateModalBodyState()` | Vaihtaa `modal-open`-luokan bodylle |

### Lauta-apurit

| Funktio | Kuvaus |
|---------|--------|
| `inBounds(r, c)` | Tarkistaa ovatko koordinaatit 0..19 sisällä |
| `invalidateCornerCache()` | Nollaa kaikki kulmavälimuistit |
| `lockBoardInteraction(ms)` | Estää nopeat klikkaukset |
| `isBoardInteractionLocked()` | Tarkistaa vuorovaikutuslukon |
| `clearPendingTurnTimers()` | Tyhjentää kaikki AI/vuoro-timeoutit |

---

## 12. Animaatiot ja visuaaliset efektit

### CSS Keyframe -animaatiot

| Nimi | Kuvaus | Kesto | Easing |
|------|--------|-------|--------|
| `popIn` | Skaalauspomppu palansijoituksessa | 0.45s | cubic-bezier(0.175, 0.885, 0.32, 1.275) |
| `pulseName` | Aktiivisen pelaajan nimen pulssi | 1.5s infinite | ease-in-out |
| `fadeInUp` | Modaalin sisääntuloanimaatio | 0.3s | ease-out |
| `slideDown` | Modaalin liu'utus ylhäältä (-100vh) | 1s | ease-out (0.15s viive) |
| `shake` | Kelvoton sijoitus -tärinä | 0.4s | ease-in-out |
| `pulseBusy` | AI:n ajattelun indikaattoripiste | 0.9s infinite | ease-in-out |
| `slideInLeft` | Vasemman paneelin sisääntuloanimaatio | 0.5s | ease-out |
| `fadeInScale` | Laudan/palakortin sisääntuloanimaatio | 0.3–0.6s | ease-out |
| `barGrow` | Historiatrendipalkin animaatio | 0.4s | ease-out |
| `slideOutLeft/Right` | Historiavälilehden vaihto | var(--duration-fast) | ease-in |
| `slideInRight` | Historiavälilehden vaihto | var(--duration-fast) | ease-out |
| `historySlideInLeft` | Historiavälilehden vaihto (nimetty uudelleen törmäyksen välttämiseksi) | var(--duration-fast) | ease-out |

### Sisääntulo-animaatiot
- Vasen paneeli: liu'uttaa vasemmalta
- Lauta: häivyttää sisään skaalauksella
- Oikea paneeli: liu'uttaa vasemmalta 0.1s viiveellä
- Palakortit: porrastettu `fadeInScale` 0.02s asteittaisella viiveellä, intro-luokka poistetaan animaation jälkeen

### Vuorovaikutusefektit

| Elementti | Efekti |
|-----------|--------|
| Painikkeet | `translateY(-1px)` hoverilla, `translateY(1px)` painettuna, varjomuutokset |
| Palakortit | `scale(1.12)` valittuna, drop-shadow-hehku pelaajan värissä |
| Palakortit (hover) | `translateY(-2px)` drop-shadow'lla |
| Laudan solut | Taustan siirtymä hoverilla (0.12s) |
| Esikatseluruudukko | 3D CSS-transformi `perspective: 500px`, cubic-bezier-siirtymä kierrossa |
| Tilastokortit | `translateY(-2px)` hoverilla |
| Trendipalkit | `brightness(1.15)` ja `scaleY(1.08)` hoverilla |
| Aktiivisen pelaajan piste | Hehkuva box-shadow (10px säde pelaajan värissä) |

### Tallennusilmoitus
- Liu'uttaa alas ylhäältä (translateY-animaatio)
- Piiloutuu automaattisesti 2000ms kuluttua

### Fokustyylit
- Mukautettu `focus-visible`: `inset 0 0 0 2px rgba(255,255,255,0.9)` tai `3px solid var(--focus-ring)` offsetilla
- Haamusolut fokuksella: `inset 0 0 0 2px rgba(20,20,20,0.25)`

---

## 13. Alustustoiminto

### Käynnistyskulku

1. `initProfiles()` — Lataa/luo profiilit, migroi vanhan historian, asettaa aktiivisen profiilin
2. `updateHistoryTabs()` — Asettaa alkuperäisen välilehtitilan
3. **Demodatan kylvö** — Kylvää historian "Aatto"- ja "Emma"-profiileille, jos ne ovat olemassa ilman historiaa
4. **Tallennetun pelin tarkistus:**
   - **Jos tallennettu peli löytyy:** Aloittaa uuden pelin (tallennusta tyhjentämättä), näyttää sitten resume-modaalin (Jatka/Hylkää)
   - **Jos ei tallennettua peliä:** Aloittaa uuden pelin normaalisti

### `startNewGame()`-kulku

1. Tyhjennä odottavat ajastimet, palan intro-timeoutit, tallennusilmoitus
2. Sulje game-over- ja resume-modaalit
3. Nollaa ajastin
4. Aseta profiilin väri (vaihda pelaaja 0:n väri tarvittaessa)
5. Sekoita AI-kulmat
6. Luo uusi 20×20 lauta
7. Alusta 4 pelaajaobjektia kaikilla 21 palalla, pisteet 89
8. Aseta pelaaja 0 aktiiviseksi, nollaa valintatila
9. Alusta laudan DOM, tietopalkki, palapaneeli, esikatselu
10. Näytä "Sinun vuorosi!" -viesti
11. Tyhjennä tallennettu peli tarvittaessa
12. Fokusoi laudan elementtiin
