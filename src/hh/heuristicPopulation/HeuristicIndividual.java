/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.heuristicPopulation;

import hh.qualityestimation.IQualityEstimation;
import hh.rewarddefinition.Reward;
import hh.credithistory.AbstractRewardHistory;
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
    private AbstractRewardHistory creditHistory;
    
    /**
     * The method to weight credits earned over time.
     */
    private final IQualityEstimation aggregateStrategy;
    
    /**
     * The sequence of building blocks that create the heuristic
     */
    private final HeuristicSequence heuristicSequence;
    
    public HeuristicIndividual(int numberOfVariables, int numberOfObjectives,
            HeuristicSequence heuristicSequence,AbstractRewardHistory creditHistory,
            IQualityEstimation aggregateStrategy) {
        super(numberOfVariables, numberOfObjectives);
        this.heuristicSequence = heuristicSequence;
        this.creditHistory = (AbstractRewardHistory)creditHistory.getInstance();
        this.aggregateStrategy = aggregateStrategy;
    }
    
    public HeuristicSequence getSequence(){
        return heuristicSequence;
    }
    
    public void updateReward(Reward reward){
        creditHistory.add(reward);
        double qual = 
                aggregateStrategy.estimate(reward.getIteration(), creditHistory);
        setObjective(0, qual);
    }
    
    
}



