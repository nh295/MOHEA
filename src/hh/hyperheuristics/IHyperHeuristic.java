/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.hyperheuristics;

import hh.history.CreditHistory;
import hh.history.OperatorQualityHistory;
import hh.history.OperatorSelectionHistory;
import hh.nextheuristic.INextHeuristic;
import hh.rewarddefinition.IRewardDefinition;
import org.moeaframework.core.Algorithm;

/**
 * Hyperheuristic is the framework using a credit assignment and heuristic selection strategy 
 * @author nozomihitomi
 */
public interface IHyperHeuristic extends Algorithm{
    
    /**
     * Returns the selection history stored in the hyper-heuristic
     * @return 
     */
    public OperatorSelectionHistory getSelectionHistory();
    
    /**
     * Resets the hyperheuristic so that it can run again for another seed.
     */
    public void reset();
    
    /**
     * gets the quality history stored for each heuristic in the hyper-heuristic
     * @return 
     */
    public OperatorQualityHistory getQualityHistory();
    
    /**
     * gets the credit history stored for each operator in the hyper-heuristic
     *
     * @return
     */
    public CreditHistory getCreditHistory();
    
    /**
     * Gets the credit definition being used.
     * @return 
     */
    public IRewardDefinition getCreditDefinition();
    
    /**
     * Gets the strategy that is used to generate or select the next heuristic 
     * @return 
     */
    public INextHeuristic getNextHeuristicSupplier();
    
    
    /**
     * Sets the hyper-heuristic's name
     * @param name
     */
    public void setName(String name);
    
    /**
     * Gets the hyper-heuristic's name
     * @return 
     */
    public String getName();
}
