/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.nextheuristic;

import hh.rewarddefinition.Reward;
import java.util.Collection;
import java.util.HashMap;
import org.moeaframework.core.Variation;

/**
 * Interface to control methods used to select or generate next heuristic(s) to 
 * be used in hyper-heuristic
 * @author nozomihitomi
 */
public interface INextHeuristic{
        
    /**
     * Method to select or generate the next heuristic based on some selection 
     * or generation method
     * @return the next heuristic to be applied
     */
    public Variation nextHeuristic();
    
    /**
     * Method to update the internals of the hyper-heuristic selector or 
     * generator based on the given CreditRepository
     * @param reward received 
     * @param heuristic to be rewarded
     */
    public void update(Reward reward,Variation heuristic);
    
    /**
     * Resets all stored history, qualities and credits
     */
    public void reset();
    
    /**
     * Gets the current quality of each heuristic stored
     * @return the current quality for each heuristic stored
     */
    public HashMap<Variation,Double> getQualities();
    
    /**
     * Returns the number of times nextHeuristic() has been called
     * @return the number of times nextHeuristic() has been called
     */
    public int getNumberOfIterations();
    
    /**
     * Gets the heuristics available to the hyper-heuristic.
     * @return 
     */
    public Collection<Variation> getOperators();
}
