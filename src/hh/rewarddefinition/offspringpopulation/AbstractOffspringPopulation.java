/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.rewarddefinition.offspringpopulation;

import hh.rewarddefinition.AbstractRewardDefintion;
import hh.rewarddefinition.CreditFunctionType;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;

/**
 * Class defining the type for reward definition based on comparing the offspring solution to a population/archive
 * @author nozomihitomi
 */
public abstract class AbstractOffspringPopulation extends AbstractRewardDefintion{

    public AbstractOffspringPopulation(){
        type = CreditFunctionType.SI;
    }
    
    /**
     * Computes the credit of an offspring solution with respect to some archive
     * @param offspring solution that will receive credits
     * @param population the population to compare the offspring solutions with
     * @return the value of credit to resulting from the solution
     */
    public abstract double compute(Solution offspring, Population population);
}
