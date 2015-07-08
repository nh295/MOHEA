/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.creditrepository;

import hh.creditaggregation.CreditAggregator;
import hh.creditaggregation.ICreditAggregationStrategy;
import hh.creditdefinition.Credit;
import hh.credithistory.ICreditHistory;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.moeaframework.core.Variation;

/**
 * This class of credit repository stores the credit history over time for each 
 * heuristic.
 * @author nozomihitomi
 */
public class CreditHistoryRepository extends CreditRepository implements Serializable{
    private static final long serialVersionUID = -151125984931862164L;
    
    protected HashMap<Variation,ICreditHistory> creditHistory;
    
    /**
     * This constructor creates the credit repository that initialize 0 credit for each heuristic
     * @param heuristics An iterable set of the candidate heuristics to be used
     * @param history the type of history desired
     */
    public CreditHistoryRepository(Collection<Variation> heuristics,ICreditHistory history) {
        super(heuristics);
        creditHistory = new HashMap<>(heuristics.size());
        Iterator<Variation> iter = heuristics.iterator();
        while(iter.hasNext()){
            creditHistory.put(iter.next(), history.getInstance());
        }
    }
    
    /**
     * Gets the entire history of the specified heuristic
     * @param heuristic
     * @return 
     */
    public ICreditHistory getHistory(Variation heuristic){
        return creditHistory.get(heuristic);
    }
    
    protected void updateSuper(Variation heuristic, Credit credit){
        super.update(heuristic, credit);
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
    }
    
    /**
     * Clears the credit histories stored in the repository.
     */
    @Override
    public void clear(){
        Iterator<Variation> iter = creditHistory.keySet().iterator();
        while(iter.hasNext()){
            creditHistory.get(iter.next()).clear();
        }
    }
    
    /**
     * Gets the sum of all credit assigned to the specified heuristic, summed over the history
     * @param iteration The iteration to take the sum to
     * @param heuristic the heuristic to query
     * @return the sum of all credit assigned to the specified heuristic, summed over the history
     */
    @Override
    public Credit getAggregateCredit(ICreditAggregationStrategy creditAgg, int iteration,Variation heuristic){
        return creditAgg.aggregateCredit(iteration, creditHistory.get(heuristic));
    }
    
}
