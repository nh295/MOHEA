/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.rewarddefinition.offspringpopulation;

import hh.rewarddefinition.RewardDefinedOn;
import hh.rewarddefinition.fitnessindicator.IBinaryIndicator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.moeaframework.core.EpsilonBoxDominanceArchive;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

/**
 * Reward definition that computes an offspring's improvement to an indicator's
 * value to the parteo front before and after the offspring is added to the
 * population
 *
 * @author nozomihitomi
 */
public class OffspringPopulationIndicator extends AbstractOffspringPopulation {

    private final IBinaryIndicator indicator;

    /**
     *
     * @param indicator Indicator to use to reward heuristics
     * @param operatesOn Enum to specify whether to compare the improvement on the population or the archive
     */
    public OffspringPopulationIndicator(IBinaryIndicator indicator,RewardDefinedOn operatesOn) {
        this.indicator = indicator;
        this.operatesOn = operatesOn;
    }

    /**
     * Computes the reward based on the improvement of the indicator value of
     * the population before the offspring solution is added to the population
     * with the offspring solution added
     *
     * @param offspring
     * @param paretofront
     * @param heuristic
     * @return the improvement in the indicator value. 0.0 if no improvement
     */
    @Override
    public double compute(Solution offspring, Iterable<Solution> paretofront, Variation heuristic) {
        try {
            if (!paretofront.getClass().equals(NondominatedPopulation.class)) {
                throw new IllegalArgumentException("Invalid solution collection: " + paretofront.getClass() + ". Needs to by NondominatedPopulation.");
            }
            NondominatedPopulation beforeOffspring = ((NondominatedPopulation) paretofront).clone(); 
            NondominatedPopulation afterOffspring;
            if(operatesOn == RewardDefinedOn.ARCHIVE){
                afterOffspring = (EpsilonBoxDominanceArchive) paretofront;
            }else{
                afterOffspring = (EpsilonBoxDominanceArchive) paretofront;
            }
            
            if (afterOffspring.add(offspring)) {
                return indicator.computeWRef(afterOffspring, beforeOffspring);
            } else {
                return 0.0;
            }
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(OffspringPopulationIndicator.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException("Crashed in compute of OffspringPopulationIndicator");
        }
    }

    @Override
    public String toString() {
        return "OPop_" + indicator.toString() + operatesOn;
    }
    
    
}
