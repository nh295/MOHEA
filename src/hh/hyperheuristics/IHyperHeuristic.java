/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.hyperheuristics;

import hh.creditdefinition.Credit;
import hh.creditdefinition.ICreditDefinition;
import hh.creditrepository.CreditHistoryRepository;
import hh.nextheuristic.INextHeuristic;
import hh.qualityhistory.HeuristicQualityHistory;
import hh.selectionhistory.IHeuristicSelectionHistory;
import java.util.Collection;
import java.util.HashMap;
import org.moeaframework.core.Algorithm;
import org.moeaframework.core.Variation;

/**
 * Hyperheuristic is the framework using a credit assignment and heuristic selection strategy 
 * @author nozomihitomi
 */
public interface IHyperHeuristic extends Algorithm{
    
    /**
     * Returns the selection history stored in the hyper-heuristic
     * @return 
     */
    public IHeuristicSelectionHistory getSelectionHistory();
    
    /**
     * Resets the hyperheuristic so that it can run again for another seed.
     */
    public void reset();
    
    /**
     * Returns the credit history stored for each heuristic in the hyper-heuristic
     * @return 
     */
    public CreditHistoryRepository getCreditHistory();
    
    /**
     * gets the quality history stored for each heuristic in the hyper-heuristic
     * @return 
     */
    public HeuristicQualityHistory getQualityHistory();
    
    /**
     * Gets the credit definition being used.
     * @return 
     */
    public ICreditDefinition getCreditDefinition();
    
    /**
     * Gets the strategy that is used to generate or select the next heuristic 
     * @return 
     */
    public INextHeuristic getNextHeuristicSupplier();
    
    /**
     * Returns the latest credit received by each heuristic
     * @return the latest credit received by each heuristic
     */
    public HashMap<Variation,Credit> getLatestCredits();
}
