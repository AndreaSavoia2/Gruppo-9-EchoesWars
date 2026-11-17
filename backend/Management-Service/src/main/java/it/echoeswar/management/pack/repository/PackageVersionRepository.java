package it.echoeswar.management.pack.repository;

import it.echoeswar.management.pack.entity.PackageVersion;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

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
}
