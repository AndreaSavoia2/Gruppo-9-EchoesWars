package it.echoeswar.management.card.service;

import it.echoeswar.management.card.entity.Card;
import it.echoeswar.management.card.payload.response.CardResponse;
import it.echoeswar.management.card.repository.CardRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;

    @Transactional(readOnly = true)
    public CardResponse getCard(long id){
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Card with id: " + id + " not found"));
        return CardResponse.from(card);
    }

    @Transactional(readOnly = true)
    public List<CardResponse> getCardDetails(){
        return cardRepository.findAll()
                .stream()
                .map(CardResponse::from)
                .toList();
    }
}
