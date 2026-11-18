package it.echoeswar.management.pack.repository;

import it.echoeswar.management.pack.entity.PackagePullRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Access to immutable draw rules for package versions.
 */
public interface PackagePullRuleRepository extends JpaRepository<PackagePullRule, Long> {

    /**
     * Retrieves the historical rule associated with a package commercial version.
     *
     * @param packageVersionId identifier of the purchased version
     * @return immutable version rule, if configured
     */
    @Query("""
            select pullRule
            from PackagePullRule pullRule
            where pullRule.packageVersion.id = :packageVersionId
            """)
    Optional<PackagePullRule> findByPackageVersionId(
            @Param("packageVersionId") Long packageVersionId
    );
}
