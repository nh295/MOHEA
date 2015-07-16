/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.rewarddefinition.fitnessindicator;

import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;

/**
 * Used for binary indicators
 * @author nozomihitomi
 */
public interface IBinaryIndicator {
    
    /**
     * Computes the indicator value when comparing population A to population B
     * @param popA
     * @param popB
     * @return 
     */
    public double compute(NondominatedPopulation popA, NondominatedPopulation popB);
    
    /**
     * Computes the indicator value when comparing solution A to solution B. Used in IBEA setting
     * @param solnA
     * @param solnB
     * @return 
     */
    public double compute(Solution solnA, Solution solnB);

    
    /**
     * Computes the indicator value when comparing popA to the refPop
     * @param popA
     * @param popRef reference population
     * @return indicator value 
     */
    public double computeWRef(NondominatedPopulation popA, NondominatedPopulation popRef);
    
}
