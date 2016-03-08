/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.creditassignment.populationcontribution;

import hh.creditassigment.AbstractRewardDefintion;
import hh.creditassigment.CreditFunctionInputType;
import hh.creditassigment.Credit;
import java.util.Collection;
import java.util.HashMap;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

/**
 *
 * @author nozomihitomi
 */
public abstract class AbstractPopulationContribution extends AbstractRewardDefintion{
    
    public AbstractPopulationContribution(){
        inputType = CreditFunctionInputType.CS;
    }
    
    /**
     * Computes all the credits received for each heuristic and returns the Credits they earn
     * @param population
     * @param enteringÏSolutions solutions that entered the nondominated set
     * @param removedSolutions solutions that were removed after offspring were added to nondominated set
     * @param heuristics
     * @param iteration
     * @return 
     */
    public abstract HashMap<Variation, Credit> compute(Population population,
            Collection<Solution> enteringÏSolutions,Collection<Solution> removedSolutions,Collection<Variation> heuristics,int iteration);
}
