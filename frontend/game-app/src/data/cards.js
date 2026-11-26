// 🃏 Everything needed to DRAW a card (frame + illustration + texts).
// Taken from the team's "card.html" file. The sizes are in "card points":
// the card is 750 × 1050 points, whatever its size on the screen.

// The backend can send the names in English (as in the team's file) or in Italian:
// these dictionaries always translate them into Italian (they are shown to the user).
export const ECHOES = { DOMINION: 'Dominio', WISDOM: 'Sapienza', OBLIVION: 'Oblio', DECEPTION: 'Inganno' }
export const SCHOOLS = { WARRIOR: 'Guerrieri', MAGE: 'Maghi', ROGUE: 'Furfanti' }
export const RARITY = { COMMON: 'Comune', RARE: 'Rara', LEGENDARY: 'Leggendaria' }

// The effects: name + explanation (rule texts)
export const EFFECTS = {
  PIERCING: ['Perforante', 'I suoi attacchi ignorano le riduzioni dirette ai danni, come Resistenza, ma non ignorano DEF o Protezione.'],
  ANALYSIS: ['Analisi', 'Contro un avversario con ATK base stampato maggiore del proprio, ottiene 1 punto di Protezione una sola volta per quell’avversario. La Protezione dura fino al consumo o alla fine dell’incontro.'],
  CURSE: ['Maledizione', 'Quando entra in campo, riduce di 2 la DEF residua della carta avversaria già in campo, fino a un minimo di 1. Non distrugge la carta né danneggia i PV; ignora la Protezione senza consumarla.'],
  BERSERKER: ['Berserker', 'Dal secondo round in campo, all’inizio del round perde 1 DEF residua per ottenere +2 ATK, fino a un massimo di +4 ATK. Non riduce la DEF sotto 1; se non può pagare la DEF, non ottiene il bonus.'],
  DRAIN: ['Drenaggio', 'Quando sconfigge una carta avversaria e sopravvive alla risoluzione completa, l’avversario scarta la carta in cima al proprio mazzo. Non si attiva se la carta è distrutta da Riflesso.'],
}

// The "zones" of every frame (the images come from the backend; in public/images/frames
// there are local copies, used as a fallback and in demo mode).
// File names: {echo}_{rarity}[_{school}].webp, e.g. "oblio_rara_mage".
//   Without the school suffix the frame has the school icon of the Echo itself
//   (dominio = warrior, sapienza = mage, inganno = rogue); Oblio frames always have it.
// ⚠️ When a NEW frame arrives, it must be added here with its sizes.
// The key must be the same as the file name.
//   name:  [x0, y0, x1, y1]  name plate at the top
//   text:  [x0, y0, x1, y1]  parchment with description/effect
//   artBottom: y where the illustration ends
//   art:   [x0, yStart, x1] window of the illustration
// Every frame is hand drawn, so the zones change a bit from one to another.
// The frames with a school suffix that derive from another frame share its zones.
export const FRAMES = {
  dominio_comune: { name: [176, 43, 640, 103], text: [54, 658, 698, 903], artBottom: 567, art: [44, 120, 706] },
  dominio_comune_mage: { name: [176, 43, 640, 103], text: [54, 658, 698, 903], artBottom: 567, art: [44, 120, 706] },
  dominio_comune_rogue: { name: [176, 43, 640, 103], text: [54, 658, 698, 903], artBottom: 567, art: [44, 120, 706] },
  dominio_leggendaria: { name: [184, 43, 653, 105], text: [54, 726, 692, 926], artBottom: 630, art: [44, 115, 706] },
  dominio_rara: { name: [168, 50, 662, 111], text: [50, 669, 702, 903], artBottom: 582, art: [44, 125, 706] },
  dominio_rara_mage: { name: [168, 50, 662, 111], text: [50, 669, 702, 903], artBottom: 582, art: [44, 125, 706] },
  dominio_rara_rogue: { name: [168, 50, 662, 111], text: [50, 669, 702, 903], artBottom: 582, art: [44, 125, 706] },
  inganno_comune: { name: [186, 36, 621, 105], text: [60, 728, 685, 933], artBottom: 633, art: [44, 116, 706] },
  inganno_comune_mage: { name: [186, 36, 621, 105], text: [60, 728, 685, 933], artBottom: 633, art: [44, 116, 706] },
  inganno_comune_warrior: { name: [186, 36, 621, 105], text: [60, 728, 685, 933], artBottom: 633, art: [44, 116, 706] },
  inganno_leggendaria: { name: [175, 37, 667, 107], text: [45, 724, 704, 937], artBottom: 630, art: [44, 118, 706] },
  inganno_leggendaria_warrior: { name: [175, 37, 667, 107], text: [45, 724, 704, 937], artBottom: 630, art: [44, 118, 706] },
  inganno_rara: { name: [175, 36, 666, 106], text: [52, 723, 697, 934], artBottom: 630, art: [44, 104, 706] },
  inganno_rara_mage: { name: [175, 36, 666, 106], text: [52, 723, 697, 934], artBottom: 630, art: [44, 104, 706] },
  oblio_comune_mage: { name: [178, 39, 658, 101], text: [50, 658, 694, 903], artBottom: 567, art: [44, 115, 706] },
  oblio_comune_rogue: { name: [177, 39, 666, 106], text: [46, 656, 704, 905], artBottom: 567, art: [44, 117, 706] },
  oblio_comune_warrior: { name: [180, 40, 656, 103], text: [50, 658, 698, 901], artBottom: 567, art: [44, 115, 706] },
  oblio_leggendaria_mage: { name: [173, 39, 667, 104], text: [46, 725, 703, 939], artBottom: 634, art: [44, 115, 706] },
  oblio_leggendaria_rogue: { name: [174, 40, 656, 103], text: [58, 724, 684, 939], artBottom: 628, art: [44, 114, 706] },
  oblio_leggendaria_warrior: { name: [173, 40, 660, 104], text: [50, 724, 698, 938], artBottom: 628, art: [44, 114, 706] },
  oblio_rara_mage: { name: [177, 37, 664, 105], text: [51, 677, 698, 914], artBottom: 586, art: [44, 117, 706] },
  oblio_rara_rogue: { name: [175, 38, 666, 105], text: [44, 677, 702, 913], artBottom: 586, art: [44, 117, 706] },
  oblio_rara_warrior: { name: [175, 37, 668, 106], text: [46, 676, 704, 911], artBottom: 586, art: [44, 117, 706] },
  sapienza_comune: { name: [179, 37, 665, 106], text: [46, 724, 700, 931], artBottom: 632, art: [44, 119, 706] },
  sapienza_comune_rogue: { name: [179, 37, 665, 106], text: [46, 724, 700, 931], artBottom: 632, art: [44, 119, 706] },
  sapienza_comune_warrior: { name: [179, 37, 665, 106], text: [46, 724, 700, 931], artBottom: 632, art: [44, 119, 706] },
  sapienza_leggendaria: { name: [183, 43, 651, 105], text: [59, 726, 685, 930], artBottom: 630, art: [44, 115, 706] },
  sapienza_rara: { name: [175, 38, 670, 106], text: [46, 678, 705, 913], artBottom: 588, art: [44, 118, 706] },
  sapienza_rara_warrior: { name: [175, 38, 670, 106], text: [46, 678, 705, 913], artBottom: 588, art: [44, 118, 706] },
}

// Translates a value with the dictionary (if it is already in Italian it leaves it as it is)
const translate = (dictionary, value) => dictionary[value] ?? value

/**
 * Prepares the card data in a single format, all in Italian.
 * Example: { echo: "DOMINION", school: "WARRIOR" } → { echo: "Dominio", school: "Guerrieri" }
 * @param {object} card
 * @returns {object}
 */
export function normalizeCard(card) {
  const effect = card.effect ? EFFECTS[card.effect] : null
  return {
    ...card,
    echo: translate(ECHOES, card.echo),
    school: translate(SCHOOLS, card.school),
    rarity: translate(RARITY, card.rarity),
    // effect: the backend can send the code ("PIERCING") or already { name, text }
    effect: effect ? { name: effect[0], text: effect[1] } : card.effect ?? null,
  }
}

// Which frame to use and where to get it.
// Returns { name, src }:
//   name → used to find the "zones" in FRAMES (where name, text, illustration go)
//   src  → the address of the image
//
// 1. The backend sends "frame": "https://.../cornici/dominio_comune.webp"
//    → we use that image, and the name is taken from the file ("dominio_comune")
// 2. The backend does not send it (e.g. demo mode)
//    → we choose it among those in public/images/frames, based on Echo + rarity + school
// (the backend uses the singular names: Guerriero, Mago, Ladro)
const SCHOOL_FILE = { Guerrieri: 'warrior', Maghi: 'mage', Furfanti: 'rogue', Guerriero: 'warrior', Mago: 'mage', Ladro: 'rogue' }

/**
 * Chooses the frame of a card (see the explanation above).
 * @param {object} card
 * @param {string|null} card.frame - frame address sent by the backend, if any
 * @param {string} card.echo
 * @param {string} card.rarity
 * @param {string} card.school
 * @returns {{ name: string, src: string }}
 */
export function pickFrame({ frame, echo, rarity, school }) {
  if (frame) {
    // ".../cornici/dominio_comune.webp?v=2" → "dominio_comune"
    const fileName = frame.split('/').pop().split(/[?#]/)[0].replace(/\.[a-z]+$/i, '')
    // If it is a new frame we do not know, we use the zones of a similar one (same Echo)
    const name = FRAMES[fileName] ? fileName : findFrame(echo, rarity, school)
    return { name, src: frame }
  }
  const name = findFrame(echo, rarity, school)
  return { name, src: `/images/frames/${name}.webp` }
}

// Searches from the most precise to the most generic:
//   1. echo + rarity + school  (e.g. "oblio_comune_rogue")
//   2. echo + rarity           (e.g. "dominio_comune")
//   3. any one with the same echo (until all the frames are there)
/**
 * Finds the best known frame for an Echo + rarity + school.
 * @param {string} [echo='']
 * @param {string} [rarity='']
 * @param {string} [school]
 * @returns {string} key in FRAMES
 */
function findFrame(echo = '', rarity = '', school) {
  const e = echo.toLowerCase()
  const r = rarity.toLowerCase()
  const candidates = [`${e}_${r}_${SCHOOL_FILE[school]}`, `${e}_${r}`]
  const found = candidates.find((name) => FRAMES[name]) ?? Object.keys(FRAMES).find((name) => name.startsWith(`${e}_`))
  return found ?? 'dominio_comune'
}
