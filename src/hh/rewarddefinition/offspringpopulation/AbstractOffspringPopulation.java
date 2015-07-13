/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.rewarddefinition.offspringpopulation;

import hh.rewarddefinition.AbstractRewardDefintion;
import hh.rewarddefinition.RewardDefinedOn;
import hh.rewarddefinition.RewardDefinitionType;
import java.util.List;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

/**
 * Class defining the type for reward definition based on comparing the offspring solution to a population/archive
 * @author nozomihitomi
 */
public abstract class AbstractOffspringPopulation extends AbstractRewardDefintion{

    public AbstractOffspringPopulation(){
        type = RewardDefinitionType.OFFSPRINGPOPULATION;
    }
    
    /**
     * Computes the credit of an offspring solution with respect to some archive
     * @param offspring solution that will receive credits
     * @param archive the archive to compare the offspring solutions with
     * @param heuristic that created offspring solution
     * @return the value of credit to resulting from the solution
     */
    public abstract double compute(Solution offspring, Iterable<Solution> archive,Variation heuristic);
}
