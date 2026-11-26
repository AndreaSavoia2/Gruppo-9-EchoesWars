# Card images

List of the image files of the cards, so the backend can serve them and send their address.
The files are in this repository, in `frontend/game-app/public/images/`.

## Illustrations (`cards.image_url` → `artUrl` in `CardResponse`)

One image per card, named after the card **code**. The frontend draws the frame and the texts on top,
so the illustration must be the art only (no frame, no text).

| Code | Card | Echo | File |
| --- | --- | --- | --- |
| DOM01 | Cadetto | Dominio | `images/illustrations/DOM01.webp` |
| DOM02 | Scudiero del Bastione | Dominio | `images/illustrations/DOM02.webp` |
| DOM03 | Lanciera della Corona | Dominio | `images/illustrations/DOM03.webp` |
| DOM04 | Guardia della Porta | Dominio | `images/illustrations/DOM04.webp` |
| DOM05 | Veterano dell’Assedio | Dominio | `images/illustrations/DOM05.webp` |
| DOM06 | Martello del Re | Dominio | `images/illustrations/DOM06.webp` |
| DOM07 | Campione delle Mura | Dominio | `images/illustrations/DOM07.webp` |
| DOM08 | Furia del Trono | Dominio | `images/illustrations/DOM08.webp` |
| DOM09 | Araldo della Vittoria | Dominio | `images/illustrations/DOM09.webp` |
| DOM10 | Alchimista d’Assedio | Dominio | `images/illustrations/DOM10.webp` |
| DOM11 | Spia del Bastione | Dominio | `images/illustrations/DOM11.webp` |
| DOM12 | Predone Corazzato | Dominio | `images/illustrations/DOM12.webp` |
| SAP01 | Allievo delle Rune | Sapienza | `images/illustrations/SAP01.webp` |
| SAP02 | Custode dei Tomi | Sapienza | `images/illustrations/SAP02.webp` |
| SAP03 | Novizia del Sigillo | Sapienza | `images/illustrations/SAP03.webp` |
| SAP04 | Geometra Arcano | Sapienza | `images/illustrations/SAP04.webp` |
| SAP05 | Mistico della Soglia | Sapienza | `images/illustrations/SAP05.webp` |
| SAP06 | Sapiente dei Riflessi | Sapienza | `images/illustrations/SAP06.webp` |
| SAP07 | Oracolo del Vuoto | Sapienza | `images/illustrations/SAP07.webp` |
| SAP08 | Maestro delle Ere | Sapienza | `images/illustrations/SAP08.webp` |
| SAP09 | Custode del Codice | Sapienza | `images/illustrations/SAP09.webp` |
| SAP10 | Guardiana degli Archivi | Sapienza | `images/illustrations/SAP10.webp` |
| SAP11 | Esploratrice del Sapere | Sapienza | `images/illustrations/SAP11.webp` |
| SAP12 | Segugio dei Segreti | Sapienza | `images/illustrations/SAP12.webp` |
| OBL01 | Eco Svanito | Oblio | `images/illustrations/OBL01.webp` |
| OBL02 | Ombra dei Sepolcri | Oblio | `images/illustrations/OBL02.webp` |
| OBL03 | Ladra di Ricordi | Oblio | `images/illustrations/OBL03.webp` |
| OBL04 | Predatore del Silenzio | Oblio | `images/illustrations/OBL04.webp` |
| OBL05 | Spettro della Cripta | Oblio | `images/illustrations/OBL05.webp` |
| OBL06 | Cultista del Crepuscolo | Oblio | `images/illustrations/OBL06.webp` |
| OBL07 | Stregone dei Nomi | Oblio | `images/illustrations/OBL07.webp` |
| OBL08 | Lamento dell’Abisso | Oblio | `images/illustrations/OBL08.webp` |
| OBL09 | Custode delle Tombe | Oblio | `images/illustrations/OBL09.webp` |
| OBL10 | Cavaliere Sepolcrale | Oblio | `images/illustrations/OBL10.webp` |
| OBL11 | Boia del Crepuscolo | Oblio | `images/illustrations/OBL11.webp` |
| OBL12 | Sentinella Perduta | Oblio | `images/illustrations/OBL12.webp` |
| ING01 | Borseggiatore del Velo | Inganno | `images/illustrations/ING01.webp` |
| ING02 | Messaggero Mascherato | Inganno | `images/illustrations/ING02.webp` |
| ING03 | Doppio Gioco | Inganno | `images/illustrations/ING03.webp` |
| ING04 | Lama Velata | Inganno | `images/illustrations/ING04.webp` |
| ING05 | Truffatore di Corte | Inganno | `images/illustrations/ING05.webp` |
| ING06 | Predone delle Ombre | Inganno | `images/illustrations/ING06.webp` |
| ING07 | Maestro delle Maschere | Inganno | `images/illustrations/ING07.webp` |
| ING08 | Acrobata dell’Ultimo Atto | Inganno | `images/illustrations/ING08.webp` |
| ING09 | Illusionista dei Vicoli | Inganno | `images/illustrations/ING09.webp` |
| ING10 | Incantatore del Dubbio | Inganno | `images/illustrations/ING10.webp` |
| ING11 | Guardia Infiltrata | Inganno | `images/illustrations/ING11.webp` |
| ING12 | Duellante dell’Ombra | Inganno | `images/illustrations/ING12.webp` |

## Frames (`tiers.image_url` → `frameImageUrl` in `CardResponse`)

The frontend recognises a frame from its **file name** (e.g. `dominio_comune`), because every frame has its own
text zones (`FRAMES` in `src/data/cards.js`). Keep these names when the files are served by the backend.
The 29 frames available today:

| File |
| --- |
| `images/frames/dominio_comune.webp` |
| `images/frames/dominio_comune_mage.webp` |
| `images/frames/dominio_comune_rogue.webp` |
| `images/frames/dominio_leggendaria.webp` |
| `images/frames/dominio_rara.webp` |
| `images/frames/dominio_rara_mage.webp` |
| `images/frames/dominio_rara_rogue.webp` |
| `images/frames/inganno_comune.webp` |
| `images/frames/inganno_comune_mage.webp` |
| `images/frames/inganno_comune_warrior.webp` |
| `images/frames/inganno_leggendaria.webp` |
| `images/frames/inganno_leggendaria_warrior.webp` |
| `images/frames/inganno_rara.webp` |
| `images/frames/inganno_rara_mage.webp` |
| `images/frames/oblio_comune_mage.webp` |
| `images/frames/oblio_comune_rogue.webp` |
| `images/frames/oblio_comune_warrior.webp` |
| `images/frames/oblio_leggendaria_mage.webp` |
| `images/frames/oblio_leggendaria_rogue.webp` |
| `images/frames/oblio_leggendaria_warrior.webp` |
| `images/frames/oblio_rara_mage.webp` |
| `images/frames/oblio_rara_rogue.webp` |
| `images/frames/oblio_rara_warrior.webp` |
| `images/frames/sapienza_comune.webp` |
| `images/frames/sapienza_comune_rogue.webp` |
| `images/frames/sapienza_comune_warrior.webp` |
| `images/frames/sapienza_leggendaria.webp` |
| `images/frames/sapienza_rara.webp` |
| `images/frames/sapienza_rara_warrior.webp` |

Note: `tiers.image_url` has one image per rarity, but the frames also change with the Echo
and the school: the file name format is `echo_rarity[_school]`, e.g. `oblio_rara_mage.webp`.
Without the school suffix the frame shows the default school of that Echo (dominio = warrior, sapienza = mage, inganno = rogue).

## What the frontend does
- `artUrl` present → it uses that image; empty → it uses `/images/illustrations/{code}.webp` from the frontend.
- `frameImageUrl` present → it uses that image; empty → it picks one from `/images/frames` by Echo + rarity + school.
