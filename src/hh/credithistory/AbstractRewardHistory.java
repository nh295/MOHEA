/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.credithistory;

import hh.qualityestimation.QualityEstimator;
import hh.rewarddefinition.Reward;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * This class stores the history of credits earned by a particular heuristic or operator.
 * @author nozomihitomi
 */
public abstract class AbstractCreditHistory implements ICreditHistory, Serializable{
    private static final long serialVersionUID = -41148639682799251L;

    protected LinkedList<Reward> creditHistory;
    protected QualityEstimator creditAdder;
    
    public AbstractCreditHistory(){
        creditHistory = new LinkedList<>();
        creditAdder = new QualityEstimator();
    }
    
    /**
     * Gets the most recent credit in the credit history
     * @return the most recent credit in the credit history
     */
    public Reward getMostRecentCredit(){
        return creditHistory.getFirst();
    }
    
    /**
     * Gets the ith most recent credit.
     * @param i the index of the credit desired. i=0 is the most recent credit
     * @return the ith most recent credit.
     */
    public Reward get(int i){
        return creditHistory.get(i);
    }
    
    /**
     * Adds the credit to the head of the list
     * @param credit to add
     */
    @Override
    public void addCredit(Reward credit) {
        creditHistory.addFirst(credit);
    }

    /**
     * Returns the entire stored history as a linkedList. The first items in the
     * list are the most recent
     * @return entire stored history as a linkedList
     */
    @Override
    public LinkedList<Reward> getHistory() {
        return creditHistory;
    }
    
    /**
     * Returns the iterator that iterates over the credits in the history. 
     * Iterator should start from the most recent credits and iterate back in 
     * time
     * @return iterator that iterates over the credits in the history
     */
    @Override
    public Iterator<Reward> iterator() {
        return creditHistory.iterator();
    }
    
    /**
     * Clears the stored history.
     */
    @Override
    public void clear(){
        creditHistory.clear();
    }
    
    /**
     * Returns the number of credits stored in the history
     * @return the number of credits stored in the history
     */
    @Override
    public int size(){
        return creditHistory.size();
    }
    
    /**
     * Returns the latest credit in the history. If the history is empty, returns a Credit with 0.0 value
     * @return 
     */
    @Override
    public Reward getLatest(){
        if(creditHistory.isEmpty())
            return new Reward(-1,0.0);
        else
            return creditHistory.getFirst();
    }
}
