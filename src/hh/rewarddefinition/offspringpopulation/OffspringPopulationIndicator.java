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
     * Reference point. Some indicators require a reference point
     */
    private final Solution refPt;
    
    /**
     *
     * @param indicator Indicator to use to reward heuristics
     * @param operatesOn Enum to specify whether to compare the improvement on the population or the archive
     * @param refPt some of the indicators require a reference point
     */
    public OffspringPopulationIndicator(IBinaryIndicator indicator,RewardDefinedOn operatesOn, Solution refPt) {
        this.indicator = indicator;
        this.operatesOn = operatesOn;
        this.refPt = refPt;
    }

    /**
     * Computes the reward based on the improvement of the indicator value of
     * the population before the offspring solution is added to the population
     * with the offspring solution added
     *
     * @param offspring
     * @param ndpop nondominated population: pareto front or archive
     * @param heuristic
     * @return the improvement in the indicator value. 0.0 if no improvement
     */
    @Override
    public double compute(Solution offspring, Iterable<Solution> ndpop, Variation heuristic) {
        try {
            if (!ndpop.getClass().equals(NondominatedPopulation.class)) {
                throw new IllegalArgumentException("Invalid solution collection: " + ndpop.getClass() + ". Needs to by NondominatedPopulation.");
            }
            NondominatedPopulation beforeOffspring = ((NondominatedPopulation) ndpop).clone(); 
            NondominatedPopulation afterOffspring = (NondominatedPopulation) ndpop;
            
            if (afterOffspring.add(offspring)) {
                //improvements over old population will result in a negative 
                return -indicator.computeWRef(afterOffspring,beforeOffspring,refPt);
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
