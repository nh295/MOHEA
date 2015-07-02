/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.nextheuristic;

import hh.creditdefinition.Credit;
import hh.creditrepository.ICreditRepository;
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
     * Method to update the selector's or generator's internal probabilities
     * @param heuristic the heuristic who just received credit
     * @param credit the credit received by the heuristic
     */
    public void update(Variation heuristic, Credit credit);
    
    /**
     * Method to replace the selector's or generator's credit repository 
     * @param creditRepo the new credit repository
     */
    public void update(ICreditRepository creditRepo);
    
    /**
     * Resets all stored history and credits
     */
    public void reset();
    
    /**
     * Gets the current credits for each heuristic stored in the repository
     * @return the current credits for each heuristic stored in the repository
     */
    public HashMap<Variation,Credit> getAllCurrentCredits();
}
