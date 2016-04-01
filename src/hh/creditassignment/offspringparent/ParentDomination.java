/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.creditassignment.offspringparent;

import hh.creditassigment.CreditFitnessFunctionType;
import hh.creditassigment.CreditDefinedOn;
import org.moeaframework.core.FastNondominatedSorting;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;

/**
 * This credit definition compares offspring to its parents
 *
 * @author Nozomi
 */
public class ParentDomination extends AbstractOffspringParent {

    /**
     * Credit that is assigned if the offspring dominates parent
     */
    private final double creditOffspringDominates;

    /**
     * Credit that is assigned if the parent dominates offspring
     */
    private final double creditParentDominates;

    /**
     * Credit that is assigned if neither the offspring or parent dominates the
     * other
     */
    private final double creditNoOneDominates;

    /**
     * Constructor to specify the amount of reward that will be assigned and the
     * dominance comparator to be used
     *
     * @param rewardOffspringDominates Reward that is assigned if the offspring
     * dominates parent
     * @param rewardParentDominates Reward that is assigned if the parent
     * dominates offspring
     * @param rewardNoOneDominates Reward that is assigned if neither the
     * offspring or parent dominates the other
     */
    public ParentDomination(double rewardOffspringDominates, double rewardNoOneDominates, double rewardParentDominates) {
        super();
        operatesOn = CreditDefinedOn.PARENT;
        fitType = CreditFitnessFunctionType.Do;
        this.creditOffspringDominates = rewardOffspringDominates;
        this.creditParentDominates = rewardParentDominates;
        this.creditNoOneDominates = rewardNoOneDominates;
    }

    /**
     * Computes the reward of an offspring solution with respect to its parents.
     *
     * @param offspring offspring solutions that will receive credits
     * @param parent the parent solutions to compare the offspring solutions
     * with
     * @param pop population may be needed to calculate the fitness of the
     * offspring and parent solutions. This implementation does not utilize this
     * information
     * @param removedSolution the solution index that was just removed from the
     * population. This implementation does not utilize this information
     * @return the value of reward to resulting from the solution
     */
    @Override
    public double compute(Solution offspring, Solution parent, Population pop, Solution removedSolution) {
        int parentRank = (int) parent.getAttribute(FastNondominatedSorting.RANK_ATTRIBUTE);
        int offspringRank = (int) offspring.getAttribute(FastNondominatedSorting.RANK_ATTRIBUTE);
        if (parentRank > offspringRank) {
            return creditOffspringDominates;
        } else if (parentRank == offspringRank) {
            return creditNoOneDominates;
        } else {
            return creditParentDominates;
        }
    }

    /**
     * Returns the credit defined for when the offspring solution dominates the
     * parent solution
     *
     * @return the credit defined for when the offspring solution dominates the
     * parent solution
     */
    public double getCreditOffspringDominates() {
        return creditOffspringDominates;
    }

    /**
     * Returns the credit defined for when the parent solution dominates the
     * offspring solution
     *
     * @return the credit defined for when the parent solution dominates the
     * offspring solution
     */
    public double getCreditParentDominates() {
        return creditParentDominates;
    }

    /**
     * Returns the credit defined for when neither the offspring solution nor
     * the parent solution dominates the other
     *
     * @return the credit defined for when neither the offspring solution nor
     * the parent solution dominates the other
     */
    public double getCreditNoOneDominates() {
        return creditNoOneDominates;
    }

    @Override
    public String toString() {
        return "OP-Do";
    }
}
