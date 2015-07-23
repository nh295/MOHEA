/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.creditrepository;

import hh.qualityestimation.IQualityEstimation;
import hh.rewarddefinition.Reward;
import java.util.Collection;
import java.util.HashMap;
import org.moeaframework.core.Variation;

/**
 * Interface to store the credit histories of multiple heuristics
 * @author nozomihitomi
 */
public interface ICreditRepository {
    

    /**
     * Method returns the current quality of the specified heuristic
     * @param qualEst The method to estimate the quality of a heuristic based on the history of credits
     * @param iteration the iteration to aggregate up to from beginning of stored history
     * @param heuristic to update the quality for
     * @return the current quality of the specified heuristic
     */
    public double estimateQuality(IQualityEstimation qualEst, int iteration,Variation heuristic);
    
    /**
     * Method returns the current qualities of all heuristics based on credit history
     * @param qualEst The method to estimate the quality of a heuristic based on the history of credits
     * @param iteration the iteration to aggregate up to from beginning of stored history
     * @return 
     */
    public HashMap<Variation,Double> estimateQuality(IQualityEstimation qualEst, int iteration);
    
    /**
     * Updates the credit history for the specified credit
     * @param heuristic
     * @param reward 
     */
    public void update(Variation heuristic, Reward reward);
    
    /**
     * Used when updating the credit repository with more than one heuristic at 
     * a time (e.g when using aggregated credit definitions)
     * @param rewards
     */
    public void update(HashMap<Variation,Reward> rewards);
    
    /**
     * Gets the collection of heuristics stored in the credit repository
     * @return the collection of heuristics stored in the credit repository
     */
    public Collection<Variation> getHeuristics();
    
    /**
     * Clears the credit stored in the repository
     */
    public void clear();
    
    /**
     * Gets the heuristic that was rewarded the most recently
     * @return 
     */
    public Collection<Variation> getLastRewardedHeuristic();
    
    /**
     * gets the most recent credit in the repository for a specified heuristic
     * @param heuristic of interest
     * @return the most recent credit in the repository for the specified heuristic
     */
    public Reward getLatestReward(Variation heuristic);
}
