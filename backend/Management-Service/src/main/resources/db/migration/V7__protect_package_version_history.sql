-- A package version is a permanent historical record. Its commercial fields
-- cannot change after creation; only its availability lifecycle may change.
CREATE FUNCTION reject_package_version_historical_mutation()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        RAISE EXCEPTION
            'Package versions are immutable. Retire the version instead of deleting it.';
        RETURN OLD;
    END IF;

    IF NEW.id IS DISTINCT FROM OLD.id
        OR NEW.package_id IS DISTINCT FROM OLD.package_id
        OR NEW.version_number IS DISTINCT FROM OLD.version_number
        OR NEW.price_credits IS DISTINCT FROM OLD.price_credits
        OR NEW.published_at IS DISTINCT FROM OLD.published_at
        OR NEW.created_at IS DISTINCT FROM OLD.created_at THEN
        RAISE EXCEPTION
            'Package version historical fields are immutable. Create a new version instead.';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_package_versions_historical_fields_immutable
BEFORE UPDATE OR DELETE ON package_versions
FOR EACH ROW
EXECUTE FUNCTION reject_package_version_historical_mutation();
