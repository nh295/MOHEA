/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.heuristicselectors;

import hh.history.OperatorSelectionHistory;
import hh.nextheuristic.AbstractOperatorSelector;
import java.util.Collection;
import org.moeaframework.core.Variation;

/**
 * This selector implements a multi-armed bandit selection
 * @author SEAK2
 */
public abstract class AbstractMAB extends AbstractOperatorSelector{
    
    /**
     * Stores the selection history
     */
    private final OperatorSelectionHistory tempHistory;
    
    private final double c;

    /**
     * 
     * @param operators operators available to select
     * @param c coefficient to balance exploration and exploitation
     */
    public AbstractMAB(Collection<Variation> operators, double c) {
        super(operators);
        this.tempHistory = new OperatorSelectionHistory(operators);
        this.c = c;
    }
    
    protected void resetSelectionHistory(){
        tempHistory.reset();
    }
    
    /**
     * This function is the upper confidence bound algorithm (UCB)
     * @param operator
     * @return 
     */
    @Override
    protected double function2maximize(Variation operator){
        return qualities.get(operator)+c*Math.sqrt((2*Math.log(tempHistory.getTotalSelectionCount()))/tempHistory.getSelectedTimes(operator));
    }
    
    /**
     * Returns the operator that maximizes the UCB algorithm
     * @return 
     */
    @Override
    public Variation nextHeuristic() {
        return argMax(operators);
    }
}
