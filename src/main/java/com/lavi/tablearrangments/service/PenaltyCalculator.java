package com.lavi.tablearrangments.service;

import com.lavi.tablearrangments.domain.Guest;
import com.lavi.tablearrangments.domain.GuestGroup;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PenaltyCalculator is responsible for calculating the penalty score
 * for assigning a specific guest group to a seating table.
 * The penalty score is used by the assignment algorithm
 * to determine the optimal placement of guest groups.
 */
public class PenaltyCalculator {

    private static final Logger log = LoggerFactory.getLogger(PenaltyCalculator.class);

    /**
     * Calculates the total penalty for assigning the given guest group to the specified table.
     *
     * @param ts    The current state of the seating table.
     * @param group The guest group being evaluated.
     * @return The calculated penalty score.
     */
    public int calculate(GuestAssignmentService.TableState ts, GuestGroup group) {
        int penalty = 0;

        penalty += computeNearStagePenalty(ts, group);
        penalty -= computeRelationBonus(ts, group);
        penalty -= computePreferredGuestsBonus(ts, group);
        penalty -= computeSidePreferenceBonus(ts, group);
        penalty += computeEmptySeatsPenalty(ts, group);

        log.info(
            "[Penalty] 📊 Final penalty for group '{}' at table '{}' = {}.",
            group.getNames(),
            ts.getTable().getTableNumber(),
            penalty
        );

        return penalty;
    }

    /**
     * Adds a penalty if the group requires near-stage seating and the table isn't near the stage.
     *
     * @param ts    The table state.
     * @param group The guest group.
     * @return Penalty value (200) or 0.
     */
    private int computeNearStagePenalty(GuestAssignmentService.TableState ts, GuestGroup group) {
        if (group.requiresNearStage() && !ts.getTable().getNearStage()) {
            log.info("[Penalty] Near stage requirement violated: +200");
            return 200;
        }
        return 0;
    }

    /**
     * Calculates a bonus if the group shares the same relation with guests already assigned to the table.
     *
     * @param ts    The table state.
     * @param group The guest group.
     * @return Relation bonus value.
     */
    private int computeRelationBonus(GuestAssignmentService.TableState ts, GuestGroup group) {
        String relation = group.getRelation();
        int sameRelationCount = relation != null ? ts.countSameRelation(relation) : 0;
        int bonus = sameRelationCount * 250;
        if (bonus > 0) log.info("[Penalty] Same relation bonus: -{}", bonus);
        return bonus;
    }

    /**
     * Calculates a bonus if the group has preferred guests already assigned to the table.
     *
     * @param ts    The table state.
     * @param group The guest group.
     * @return Preferred guests bonus value.
     */
    private int computePreferredGuestsBonus(GuestAssignmentService.TableState ts, GuestGroup group) {
        int preferredGuestsCount = ts.countPreferredGuests(group);
        int bonus = preferredGuestsCount * 150;
        if (bonus > 0) log.info("[Penalty] Preferred guests bonus: -{}", bonus);
        return bonus;
    }

    /**
     * Calculates a bonus if the group matches the preferred side (Bride/Groom) of the table.
     *
     * @param ts    The table state.
     * @param group The guest group.
     * @return Side preference bonus value.
     */
    private int computeSidePreferenceBonus(GuestAssignmentService.TableState ts, GuestGroup group) {
        String side = group.getGuests().stream().map(Guest::getSide).filter(Objects::nonNull).map(Enum::name).findFirst().orElse(null);

        if (side == null) return 0;

        boolean hasRelation = group.getRelation() != null;
        boolean tableNotFull = ts.getFreeSeats() > 0;
        int sameSideCount = ts.countSameSide(side);

        if (tableNotFull && (!hasRelation || sameSideCount > 0)) {
            int bonus = sameSideCount * 50;
            log.info("[Penalty] Side preference bonus: -{}", bonus);
            return bonus;
        }
        return 0;
    }

    /**
     * Adds a penalty based on the number of empty seats left after assigning the group.
     * The penalty is calculated using a cubic function to heavily penalize empty seats.
     *
     * @param ts    The table state.
     * @param group The guest group.
     * @return Empty seats penalty value.
     */
    private int computeEmptySeatsPenalty(GuestAssignmentService.TableState ts, GuestGroup group) {
        int freeSeatsLeft = ts.getFreeSeats() - group.getTotalSeats();
        if (freeSeatsLeft > 0) {
            int penalty = freeSeatsLeft * freeSeatsLeft * freeSeatsLeft * 10;
            log.info("[Penalty] Empty seats penalty (exponential): +{}", penalty);
            return penalty;
        }
        return 0;
    }
}
