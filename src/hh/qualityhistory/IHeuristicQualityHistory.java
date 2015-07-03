/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.qualityhistory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Stack;
import org.moeaframework.core.Variation;

/**
 * This interface keeps the history of which heuristic was selected by the IHeusristicSelector
 * @author nozomihitomi
 */
public interface IHeuristicQualityHistory {
    
    /**
     * This adds the quality of a heuristic to the history
     * @param heuristic the heuristic to add to the history
     * @param quality the quality value to add
     */
    public void add(Variation heuristic,double quality);
    
    /**
     * Gets the quality history of a particular heuristic
     * @param heuristic of interest
     * @return the quality history of the specified heuristic
     */
    public Collection<Double> getHistory(Variation heuristic);
    
    /**
     * Gets the heuristics involved in the selection process
     * @return a collection containing the heuristics involved in the selection process
     */
    public Collection<Variation> getHeuristics();
    
    /**
     * Gets the latest quality of each heuristic
     * @return 
     */
    public HashMap<Variation,Double> getLatest();
    
    /**
     * Clears the history
     */
    public void clear();
}
