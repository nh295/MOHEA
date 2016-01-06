/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.rewarddefinition.offspringparent;

import hh.rewarddefinition.AbstractRewardDefintion;
import hh.rewarddefinition.CreditFunctionType;
import hh.rewarddefinition.FitnessFunctionType;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;

/**
 * Class defining the inputType for reward definition based on comparing the offspring solution to the parent solution
 * @author nozomihitomi
 */
public abstract class AbstractOffspringParent extends AbstractRewardDefintion{

    public AbstractOffspringParent(){
        inputType = CreditFunctionType.OP;
        fitType = FitnessFunctionType.Do;
    }
    
    
    /**
     * Computes the reward of an offspring solution with respect to multiple 
     * parents. Can be used if a heuristic produces one offspring solution. 
     * Finds the parent that is non-dominated to compare offspring solution. If 
     * there are multiple non-dominated parents, a random non-dominated parent
     * is selected.
     * @param offspring offspring solutions that will receive credits
     * @param parent the parent solution to compare the offspring solutions with
     * @param pop population may be needed to calculate the fitness of the offspring and parent solutions
     * @param removedSolution the solution that was just removed from the population
     * @return the value of reward to resulting from the solution
     */
    public abstract double compute(Solution offspring, Solution parent,Population pop, Solution removedSolution);
    
}
