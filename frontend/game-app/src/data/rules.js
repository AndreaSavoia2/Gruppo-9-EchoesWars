// Deck rules (from the Figma). They are used to SHOW counters and warnings.
// ⚠️ The real check is done by the backend when you save: if they change, they must change there too.
export const DECK_RULES = {
  cards: 30, // exact number of cards in a deck
  points: 98, // maximum cost
  copies: 3, // maximum copies of the same card
  echoes: 2, // Echoes per deck
  nameLength: 24, // maximum characters of the name
}

// Trade rules (from the Figma): only used for the texts. The backend applies them.
export const TRADE_RULES = {
  proposalHours: 24, // a proposal expires after 24 hours
  pauseHours: 48, // after an accepted trade, 48-hour pause
  matchesToUnlock: 10, // the Echo Market unlocks after 10 matches
}
