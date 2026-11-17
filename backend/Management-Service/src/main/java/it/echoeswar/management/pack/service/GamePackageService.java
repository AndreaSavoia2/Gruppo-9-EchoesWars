package it.echoeswar.management.pack.service;

import it.echoeswar.management.pack.payload.response.GamePackageResponse;
import it.echoeswar.management.pack.repository.GamePackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Provides read operations for the game-package catalogue.
 */
@Service
@RequiredArgsConstructor
public class GamePackageService {

    private final GamePackageRepository gamePackageRepository;

    /**
     * Retrieves all packages that are currently available to users.
     *
     * @return available packages ordered by their identifier
     */
    @Transactional(readOnly = true)
    public List<GamePackageResponse> getAllAvailablePackages() {
        return gamePackageRepository.findAllByActiveTrueOrderByIdAsc()
                .stream()
                .map(GamePackageResponse::from)
                .toList();
    }
}
