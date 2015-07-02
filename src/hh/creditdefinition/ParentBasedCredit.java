/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.creditdefinition;

import java.util.List;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

/**
 * This type of class is for when you want to assign credit for offpsring 
 * solution based on the quality of the parent solution
 * @author Nozomi
 */
public abstract class ParentBasedCredit implements ICreditDefinition{
    
    /**
     * Computes the credit of 1 offpring solution with repsect to a number of parent solutions
     * @param offspring that was created from parent solutions
     * @param parents used to create offspring solution
     * @param heuristic the heuristic that created the offspring solution
     * @return 
     */
    abstract public double compute(Solution offspring, Solution[] parents, Variation heuristic);
    
    /**
     * Compute the credit for multiple solution with respect to a populations
     * @param offsprings that was created from parent solutions
     * @param parents used to create offspring solution
     * @param heuristic the heuristic that created the offspring solution
     * @return 
     */
    abstract public List<Double> computeAll(Solution[] offsprings,Solution[] parents, Variation heuristic);
    
    
    /**
     * Gets the type of credit definition
     * @return 
     */
    @Override
    public CreditDefinitionType getType() {
        return CreditDefinitionType.PARENT;
    }
}
