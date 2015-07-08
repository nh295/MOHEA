/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.creditrepository;

import hh.creditaggregation.ICreditAggregationStrategy;
import hh.creditdefinition.Credit;
import java.util.Collection;
import java.util.HashMap;
import org.moeaframework.core.Variation;

/**
 * Intergace to store the credit histories of multiple heuristics
 * @author nozomihitomi
 */
public interface ICreditRepository {
    

    /**
     * Method returns the current aggregated credit stored for the specified heuristic
     * @param creditAgg The method to aggregate the history of credits
     * @param iteration the iteration to aggregate up to
     * @param heuristic
     * @return the current credit stored for the specified heuristic
     */
    public Credit getAggregateCredit(ICreditAggregationStrategy creditAgg, int iteration,Variation heuristic);
    
    /**
     * Updates the credit history for the specified credit
     * @param heuristic
     * @param credit 
     */
    public void update(Variation heuristic, Credit credit);
    
    /**
     * Used when updating the credit repository with more than one heuristic at 
     * a time (e.g when using aggregated credit definitions)
     * @param credits
     */
    public void update(HashMap<Variation,Credit> credits);
    
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
    public Credit getLatestCredit(Variation heuristic);
}
