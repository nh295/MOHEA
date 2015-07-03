/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.creditrepository;

import hh.creditaggregation.ICreditAggregationStrategy;
import hh.creditdefinition.Credit;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.moeaframework.core.Variation;

/**
 * This class of credit repository stores credit for each heuristic. One Credit 
 * object is assigned for each heuristic. This does not store the history of 
 * credits received over time
 * @author nozomihitomi
 */
public class CreditRepository implements ICreditRepository,Serializable{
    private static final long serialVersionUID = 1004365209150732930L;
    
    protected HashMap<Variation,Credit> creditRepository;

    /**
     * This constructor creates the credit repository that initialize 0 credit for each heuristic
     * @param heuristics An iterable set of the candidate heuristics to be used
     */
    public CreditRepository(Collection<Variation> heuristics) {
        creditRepository = new HashMap(heuristics.size());
        Iterator<Variation> iter = heuristics.iterator();
        while(iter.hasNext()){
            creditRepository.put(iter.next(), new Credit(-1,0.0));
        }
    }

    /**
     * Method returns the current aggregated credit stored for the specified heuristic
     * @param creditAgg The method to aggregate the history of credits. In this implementation, a credit aggregation strategy is not needed
     * @param iteration the iteration to aggregate up to. In this implementation, an iteration is not needed
     * @param heuristic
     * @return the current credit stored for the specified heuristic
     */
    @Override
    public Credit getAggregateCredit(ICreditAggregationStrategy creditAgg, int iteration,Variation heuristic){
        return creditRepository.get(heuristic);
    }

    /**
     * Replaces the credit assigned to the specified heuristic with the given credit
     * @param heuristic the heuristic to query
     * @param credit that will replace old credit
     */
    @Override
    public void update(Variation heuristic, Credit credit) {
        creditRepository.put(heuristic, credit);
    }
    
    /**
     * Returns the heuristics that are stored in this repository
     * @return the heuristics that are stored in this repository
     */
    @Override
    public Collection<Variation> getHeuristics() {
        return creditRepository.keySet();
    }    
    
    /**
     * Clears the credit stored in the repository. Resets credits to 0
     */
    @Override
    public void clear() {
        Iterator<Variation> iter = creditRepository.keySet().iterator();
        while(iter.hasNext()){
            creditRepository.put(iter.next(), new Credit(-1,0.0));
        }
    }

    @Override
    public Credit getLatestCredit(Variation heuristic) {
        return creditRepository.get(heuristic);
    }
    
}
