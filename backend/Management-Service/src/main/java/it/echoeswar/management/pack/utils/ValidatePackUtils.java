package it.echoeswar.management.pack.utils;

import it.echoeswar.management.common.exception.ResourceConflictException;
import it.echoeswar.management.pack.entity.PackagePullRule;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ValidatePackUtils {

    /**
     * Additional safeguard against configurations inserted manually outside application constraints.
     */
    public void validatePullRule(PackagePullRule pullRule) {
        long guaranteedCards = (long) pullRule.getGuaranteedCommonCards()
                + pullRule.getGuaranteedRareCards();

        boolean invalidCardDistribution = pullRule.getCardsPerPackage() <= guaranteedCards;
        boolean invalidThresholds = pullRule.getRandomCommonThreshold() < 0
                || pullRule.getRandomCommonThreshold() > pullRule.getRandomRareThreshold()
                || pullRule.getRandomRareThreshold() > 100;

        if (pullRule.getCardsPerPackage() <= 0
                || pullRule.getGuaranteedCommonCards() < 0
                || pullRule.getGuaranteedRareCards() < 0
                || pullRule.getCycleLength() <= 0
                || invalidCardDistribution
                || invalidThresholds) {
            throw new ResourceConflictException("The selected package version has an invalid pull rule.");
        }
    }
}
