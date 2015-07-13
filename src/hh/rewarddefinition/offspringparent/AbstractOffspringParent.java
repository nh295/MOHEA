/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.rewarddefinition.offspringparent;

import hh.rewarddefinition.AbstractRewardDefintion;
import hh.rewarddefinition.RewardDefinedOn;
import hh.rewarddefinition.RewardDefinitionType;
import java.util.List;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

/**
 * Class defining the type for reward definition based on comparing the offspring solution to the parent solution
 * @author nozomihitomi
 */
public abstract class AbstractOffspringParent extends AbstractRewardDefintion{

    public AbstractOffspringParent(){
        type = RewardDefinitionType.OFFSPRINGPARENT;
        operatesOn = RewardDefinedOn.PARENT;
    }
    
    
    /**
     * Computes the reward of an offspring solution with respect to multiple 
     * parents. Can be used if a heuristic produces one offspring solution. 
     * Finds the parent that is non-dominated to compare offspring solution. If 
     * there are multiple non-dominated parents, a random non-dominated parent
     * is selected.
     * @param offspring offspring solutions that will receive credits
     * @param parents the parent solutions to compare the offspring solutions with
     * @param heuristic that created offspring solution
     * @return the value of reward to resulting from the solution
     */
    public abstract double compute(Solution offspring, Iterable<Solution> parents,Variation heuristic);
    
    
    /**
     * Computes the reward of an offspring solution with respect to multiple 
     * parents. Can be used if a heuristic produces more than one offspring 
     * solution. Finds the parent that is non-dominated to compare offspring 
     * solution. If there are multiple non-dominated parents, a random 
     * non-dominated parent is selected. All offspring solutions are compared 
     * with the selected parent solution
     * @param offsprings a list of offspring solutions that will receive credits
     * @param parents the parent solutions to compare the offspring solutions with
     * @param heuristic that created offspring solution
     * @return the value of reward to resulting from the solution
     */
    public abstract List<Double> computeAll(Solution[] offsprings, Iterable<Solution> parents,Variation heuristic);
}
