/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.heuristicPopulation;

import hh.creditaggregation.ICreditAggregationStrategy;
import hh.creditdefinition.Credit;
import hh.credithistory.AbstractCreditHistory;
import hh.credithistory.ICreditHistory;
import hh.heuristicgenerators.HeuristicSequence;
import org.moeaframework.core.Solution;



/**
 * Wrapper on HeuristicSequence so that it can extend Solution to be used in
 * the Heuristic population
 */
public class HeuristicIndividual extends Solution{
    private static final long serialVersionUID = -3543224029400120396L;
    
    /**
     * The credit history to be used for this individual. Keeps track of credits earned over time
     */
    private AbstractCreditHistory creditHistory;
    
    /**
     * The method to weight credits earned over time.
     */
    private final ICreditAggregationStrategy aggregateStrategy;
    
    /**
     * The sequence of building blocks that create the heuristic
     */
    private final HeuristicSequence heuristicSequence;
    
    public HeuristicIndividual(int numberOfVariables, int numberOfObjectives,
            HeuristicSequence heuristicSequence,AbstractCreditHistory creditHistory,
            ICreditAggregationStrategy aggregateStrategy) {
        super(numberOfVariables, numberOfObjectives);
        this.heuristicSequence = heuristicSequence;
        this.creditHistory = (AbstractCreditHistory)creditHistory.getInstance();
        this.aggregateStrategy = aggregateStrategy;
    }
    
    public HeuristicSequence getSequence(){
        return heuristicSequence;
    }
    
    public void updateCredit(Credit credit){
        creditHistory.addCredit(credit);
        Credit currentCredit = 
                aggregateStrategy.aggregateCredit(credit.getIteration(), creditHistory);
        setObjective(0, currentCredit.getValue());
    }
    
    
}



