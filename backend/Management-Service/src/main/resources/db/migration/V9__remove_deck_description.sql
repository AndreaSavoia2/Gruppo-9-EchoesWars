-- La descrizione non fa più parte del dominio del deck né delle API esposte.
ALTER TABLE decks
    DROP COLUMN description;
