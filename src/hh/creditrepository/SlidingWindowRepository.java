/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.creditrepository;

import hh.creditdefinition.Credit;
import hh.credithistory.ICreditHistory;
import java.util.Collection;
import java.util.LinkedList;
import org.moeaframework.core.Variation;

/**
 * This repository contains one sliding window of size W ; it contains all the
 * heuristics. This is in opposition to CreditHistoryRepository which maintains
 * a separate history for each heuristic.
 *
 * @author SEAK2
 */
public class SlidingWindowRepository extends CreditHistoryRepository{
    
    /**
     * The number of iterations to maintain credits for
     */
    private int windowSize;

    public SlidingWindowRepository(Collection<Variation> heuristics, ICreditHistory history, int windowSize) {
        super(heuristics, history);
        this.windowSize = windowSize;
    }
    
    /**
     * Adds the new credit to the history of the credits
     * @param heuristic the heuristic to query
     * @param credit that will be added to the history
     */
    @Override
    public void update(Variation heuristic, Credit credit) {
        updateSuper(heuristic, credit);
        creditHistory.get(heuristic).addCredit(credit);
        truncateHistroy(credit, creditHistory.get(heuristic));
    }
    
    /**
     * Truncates the credits from the history that is past specified window size. Assumes the input credit has the current iteration count
     */
    private void truncateHistroy(Credit currentCredit,ICreditHistory history){
        int currentIter = currentCredit.getIteration();
        LinkedList<Credit> hist = history.getHistory();
        while(true){
            if(currentIter - hist.getLast().getIteration()>windowSize)
               break;
            
            
        }
    }
    

}
