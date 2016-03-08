/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.heuristicselectors;

import hh.history.OperatorSelectionHistory;
import hh.nextheuristic.AbstractOperatorSelector;
import java.util.Collection;
import java.util.Iterator;
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
    
    private Iterator<Variation> opIter;
    
    private int selectionCount;

    /**
     * 
     * @param operators operators available to select
     * @param c coefficient to balance exploration and exploitation
     */
    public AbstractMAB(Collection<Variation> operators, double c) {
        super(operators);
        this.tempHistory = new OperatorSelectionHistory(operators);
        this.c = c;
        opIter = operators.iterator();
    }
    
    protected void resetSelectionHistory(){
        tempHistory.reset();
        selectionCount = 0;
        opIter = operators.iterator();
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
        Variation out;
        //first select each operator at least once
        if(opIter.hasNext())
            out = opIter.next();
        else
            out = argMax(operators);
        
        selectionCount++;
        tempHistory.add(out, selectionCount);
        return out;
    }
}
