/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.creditrepository;

import hh.creditaggregation.CreditAggregateSum;
import hh.creditdefinition.Credit;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author nozomihitomi
 */
public abstract class AbstractCreditHistory implements ICreditHistory, Iterable<Credit>, Serializable{
    private static final long serialVersionUID = -41148639682799251L;

    protected LinkedList<Credit> creditHistory;
    protected CreditAggregateSum creditAdder;
    
    public AbstractCreditHistory(){
        creditHistory = new LinkedList<>();
        creditAdder = new CreditAggregateSum();
    }
    
    
    /**
     * Gets the most recent credit in the credit history
     * @return the most recent credit in the credit history
     */
    public Credit getMostRecentCredit(){
        return creditHistory.getFirst();
    }
    
    /**
     * Gets the ith most recent credit.
     * @param i the index of the credit desired. i=0 is the most recent credit
     * @return the ith most recent credit.
     */
    public Credit get(int i){
        return creditHistory.get(i);
    }
    
    /**
     * Returns the average credit, averaged over all credits stored in the history
     * @return the average credit, averaged over all credits stored in the history
     */
    @Override
    public Credit getAverageCredit(int iteration){
        return new Credit(iteration, getSumCredit(iteration).getValue()/creditHistory.size());
    }
    
    /**
     * Returns the average credit, averaged over all credits stored in the history
     * @return the average credit, averaged over all credits stored in the history. 
     * Returned credit is of type Credit not DecayingCredit
     */
    @Override
    public Credit getSumCredit(int iteration){
        return new Credit(iteration, creditAdder.aggregateCredit(iteration, this).getValue());
    }
    
    /**
     * Adds the credit to the head of the list
     * @param credit to add
     */
    @Override
    public void addCredit(Credit credit) {
        creditHistory.addFirst(credit);
    }

    /**
     * Returns the entire stored history as a linkedList. The first items in the
     * list are the most recent
     * @return entire stored history as a linkedList
     */
    @Override
    public LinkedList<Credit> getHistory() {
        return creditHistory;
    }
    
    /**
     * Returns the iterator that iterates over the credits in the history. 
     * Iterator should start from the most recent credits and iterate back in 
     * time
     * @return iterator that iterates over the credits in the history
     */
    @Override
    public Iterator<Credit> iterator() {
        return creditHistory.iterator();
    }
    
    /**
     * Clears the stored history.
     */
    @Override
    public void clear(){
        creditHistory.clear();
    }
}
