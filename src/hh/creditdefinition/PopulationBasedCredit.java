/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.creditdefinition;

import java.util.List;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

/**
 * This is for when you want to compute credit of new solutions with respect to 
 * a population of solutions
 * @author Nozomi
 */
public abstract class PopulationBasedCredit implements ICreditDefinition{
    
    /**
     * Compute the credit for 1 solution with respect to a populations
     * @param offspring to compute credit for
     * @param population population to compute the credit for each offspring
     * @param heuristic that created offspring solution 
     * @return 
     */
    abstract public double compute(Solution offspring,Population population, Variation heuristic);
    
    /**
     * Compute the credit for multiple solution with respect to a populations
     * @param offsprings all offspring to compute credit for 
     * @param population population to compute the credit for each offspring
     * @param heuristic that created offspring solution 
     * 
     * @return 
     */
    abstract public List<Double> computeAll(Solution[] offsprings,Population population, Variation heuristic);
    
    
    /**
     * Gets the type of credit definition
     * @return 
     */
    @Override
    public CreditDefinitionType getType() {
        return CreditDefinitionType.POPULATION;
    }
    
}
