package it.echoeswar.management.pack.repository;

import it.echoeswar.management.pack.entity.GamePackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GamePackageRepository extends JpaRepository<GamePackage, Long> {

    List<GamePackage> findAllByActiveTrueOrderByIdAsc();

    /**
     * Finds the highest sequential number among existing game package codes.
     * <p>
     * Only codes matching the {@code PACK_<number>} pattern (case-insensitive) are considered:
     * the numeric part is extracted by skipping the first five characters ({@code PACK_}).
     * The underscore is escaped so that it is matched literally and not as a single-character wildcard.
     *
     * @return the highest number found, or {@code 0} if no matching code exists
     */
    @Query("""
        select coalesce(max(cast(substring(g.code, 6) as integer)), 0)
        from GamePackage g
        where upper(g.code) like 'PACK!_%' escape '!'
        """)
    int findMaxNumber();
}
