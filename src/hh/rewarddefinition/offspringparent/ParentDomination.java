/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.rewarddefinition.offspringparent;

import hh.rewarddefinition.RewardDefinedOn;
import org.moeaframework.core.ParallelPRNG;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.core.comparator.DominanceComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;

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
     * The inputType of dominance comparator to be used
     */
    private final DominanceComparator comparator;

    /**
     * Constructor to specify the amount of reward that will be assigned. A
     * default dominance comparator will be used: ParetoDominanceComparator
     *
     * @param rewardOffspringDominates Reward that is assigned if the offspring
     * dominates parent
     * @param rewardParentDominates Reward that is assigned if the parent
     * dominates offspring
     * @param rewardNoOneDominates Reward that is assigned if neither the
     * offspring or parent dominates the other
     */
    public ParentDomination(double rewardOffspringDominates, double rewardNoOneDominates, double rewardParentDominates) {
        this(rewardOffspringDominates, rewardNoOneDominates, rewardParentDominates, new ParetoDominanceComparator());
    }

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
     * @param comparator the comparator to be used that defines dominance
     */
    public ParentDomination(double rewardOffspringDominates, double rewardNoOneDominates, double rewardParentDominates, DominanceComparator comparator) {
        super();
        operatesOn = RewardDefinedOn.PARENT;
        this.creditOffspringDominates = rewardOffspringDominates;
        this.creditParentDominates = rewardParentDominates;
        this.creditNoOneDominates = rewardNoOneDominates;
        this.comparator = comparator;
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
        switch (comparator.compare(parent, offspring)) {
            case -1:
                return creditParentDominates;
            case 0:
                return creditNoOneDominates;
            case 1:
                return creditOffspringDominates;
            default:
                throw new Error("Comparator returned invalid value: " + comparator.compare(parent, offspring));
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
