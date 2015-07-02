/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.selectionhistory;

import java.util.Collection;
import java.util.Stack;
import org.moeaframework.core.Variation;

/**
 * This interface keeps the history of which heuristic was selected by the IHeusristicSelector
 * @author nozomihitomi
 */
public interface IHeuristicSelectionHistory {
    
    /**
     * This adds the input heuristic to the history
     * @param heuristic the heuristic to add to the history
     */
    public void add(Variation heuristic);
    
    
    /**
     * Gets the number of times the specified heuristic was selected using the history's memory
     * @param heuristic
     * @return 
     */
    public int getSelectedTimes(Variation heuristic); 
    
    /**
     * Clears the selection history;
     */
    public void clear();
    
    /**
     * Returns the number of selections made so far. This is the sum of the 
     * selection counts for each heuristic across all heuristics
     * @return  the total number of selections made so far
     */
    public int getTotalSelectionCount();
    
    /**
     * Returns the history of the selected heuristics in the ordered they occurred.
     * @return a stack of Variations which contains the history of the selected 
     * heuristics in the ordered they occurred. Selections at the beginning of 
     * the search are at the top of the Stack. 
     */
    public Stack<Variation> getOrderedHistory();
    
    /**
     * Gets the heuristics involved in the selection process
     * @return a collection containing the heuristics involved in the selection process
     */
    public Collection<Variation> getHeuristics();
}
