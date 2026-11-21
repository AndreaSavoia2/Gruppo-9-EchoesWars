package it.echoeswar.management.pack.repository;

import it.echoeswar.management.pack.entity.PackageVersion;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PackageVersionRepository extends JpaRepository<PackageVersion, Long> {

    /**
     * Finds active commercial versions for an active package, newest version first.
     *
     * <p>The service selects the first result so that a package purchase always records
     * the current active version and its price.</p>
     *
     * @param gamePackageId identifier of the catalog package
     * @return active versions ordered from newest to oldest
     */
    @Query("""
            select packageVersion
            from PackageVersion packageVersion
            join fetch packageVersion.gamePackage gamePackage
            where gamePackage.id = :gamePackageId
              and gamePackage.active = true
              and packageVersion.active = true
            order by packageVersion.versionNumber desc
            """)
    List<PackageVersion> findAvailableByGamePackageId(
            @Param("gamePackageId") Long gamePackageId
    );

    /*query join per recuperare la versione associata al pacchetto tramite pacchettoId*/
    @Query("""
            select v from PackageVersion v join GamePackage p on v.gamePackage.id = p.id where p.id = :gamePackageId order by v.versionNumber desc limit 1
                                   
            """)
    Optional<PackageVersion> findVersionByPackageId(
            @Param("gamePackageId") Long gamePackageId
    );
}
