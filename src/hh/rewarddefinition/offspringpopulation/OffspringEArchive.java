/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.rewarddefinition.offspringpopulation;

import hh.rewarddefinition.RewardDefinedOn;
import org.moeaframework.core.EpsilonBoxDominanceArchive;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

/**
 * This credit definition gives credit to the specified solution if it makes it
 * in the epsilon archive. Credit is only assigned to the specified solution.
 * @author nozomihitomi
 */
public class OffspringEArchive extends AbstractOffspringPopulation{
    /**
     * Credit received if a new solution is in the archive 
     */
    protected final double inArchive;
    
    /**
     * Credit received if a new solution is not in the archive 
     */
    protected final double notInArchive;
    
    /**
     * The constructor needs the value for credit when a solution is in the 
     * e-archive and for when a solution is not in the e-archive
     * @param inArchive credit to assign when solution is in the archive 
     * @param notInArchive credit to assign when solution is not in the archive 
     */
    public OffspringEArchive(double inArchive, double notInArchive) {
        operatesOn = RewardDefinedOn.ARCHIVE;
        this.notInArchive = notInArchive;
        this.inArchive = inArchive;
    }
    
    /**
     * Adds the offspring solution to the archive to see if the Pareto front changes. If it changes, the heuristic will receive a reward
     * @param offspring solution that will receive credits
     * @param archive the archive to compare the offspring solutions with
     * @return the value of credit to resulting from the solution
     */
    @Override
    public double compute(Solution offspring, Iterable<Solution> archive) {
        if(!archive.getClass().equals(EpsilonBoxDominanceArchive.class))
            throw new ClassCastException("Need to be NondominatedPopulation: " + archive.getClass());
        EpsilonBoxDominanceArchive ndpop = (EpsilonBoxDominanceArchive)archive;
        if(ndpop.add(offspring))
            return inArchive;
        else
            return notInArchive;
    }
    
    @Override
    public String toString() {
        return "OffspringEArchive";
    }
    
}
