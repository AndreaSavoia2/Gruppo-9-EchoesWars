// Regole del mazzo (dal Figma). Servono per MOSTRARE contatori e avvisi.
// ⚠️ Il controllo vero lo fa il backend quando salvi: se cambiano, vanno cambiate anche lì.
export const REGOLE_MAZZO = {
  carte: 30, // carte esatte in un mazzo
  punti: 98, // costo massimo
  copie: 3, // copie massime della stessa carta
  echi: 2, // Echi per mazzo
  lunghezzaNome: 24, // caratteri massimi del nome
}

// Regole degli scambi (dal Figma): servono solo per i testi. Le applica il backend.
export const REGOLE_SCAMBI = {
  oreProposta: 24, // una proposta scade dopo 24 ore
  orePausa: 48, // dopo uno scambio accettato, pausa di 48 ore
  partitePerSbloccare: 10, // il Mercato degli Echi si sblocca dopo 10 partite
}
