package it.echoeswar.management.catalog.controller;

import it.echoeswar.management.catalog.service.CardService;
import it.echoeswar.management.common.api.ResponseApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("api")
public class CatalogController {

    private final CardService cardService;

    @GetMapping("v1/public/catalogs")
    public ResponseEntity<?> getAllCards (
            @RequestParam(defaultValue = "0") int pageNumber, // Zero-based page index.
            @RequestParam(defaultValue = "6") int pageSize, // Number of items per page.
            @RequestParam(defaultValue = "nameIt") String sortBy, // Column used for sorting.
            @RequestParam(defaultValue = "DESC") String direction // Sort direction, ASC or DESC.
    ){
        return ResponseApi
                .buildResponse(
                        HttpStatus.OK,
                        cardService.getAllCards(pageNumber,pageSize,sortBy,direction)
                );
    }
}
