/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.creditassignment.populationcontribution;

import hh.creditassigment.CreditFunctionInputType;
import hh.creditassigment.CreditFitnessFunctionType;

/**
 *
 * @author nozomihitomi
 */
public class DecompositionContribution extends ParetoFrontContribution{
    
    /**
     * Constructor to specify the rewards to give to the heuristic responsible 
     * for each solution in a given neighborhood
     * @param rewardInN reward to assign to each solution in the neighborhood that the heuristic created
     * @param rewardNotInN reward to assign if there are no solutions in the neighborhood created by the heuristic 
     */
    public DecompositionContribution(double rewardInN,double rewardNotInN) {
        super(rewardInN, rewardNotInN);
        this.fitType = CreditFitnessFunctionType.De;
        this.inputType = CreditFunctionInputType.CS;
    }
    
    @Override
    public String toString() {
        return "CS-De";
    }
}
