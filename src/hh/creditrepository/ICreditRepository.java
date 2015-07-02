/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.creditrepository;

import hh.creditdefinition.Credit;
import java.util.Collection;
import org.moeaframework.core.Variation;

/**
 *
 * @author nozomihitomi
 */
public interface ICreditRepository {
    

    /**
     * Method returns the current credit stored for the specified heuristic
     * @param heuristic
     * @return the current credit stored for the specified heuristic
     */
    public Credit getCurrentCredit(Variation heuristic);
    
    /**
     * Updates the credit history for the specified credit
     * @param heuristic
     * @param credit 
     */
    public void update(Variation heuristic, Credit credit);
    
    /**
     * Gets the collection of heuristics stored in the credit repository
     * @return the collection of heuristics stored in the credit repository
     */
    public Collection<Variation> getHeuristics();
    
    /**
     * Clears the credit stored in the repository
     */
    public void clear();
}
