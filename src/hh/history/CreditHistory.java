/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.history;

import hh.creditassigment.Credit;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.moeaframework.core.Variation;

/**
 * This class stores the history of credits received by each operator over the
 * course of a run.
 *
 * @author nozomihitomi
 */
public class CreditHistory implements Serializable {

    private static final long serialVersionUID = -2323214225020219554L;

    protected HashMap<Variation, ArrayList<Credit>> history;
    
    private int maxIteration = 0;

    public CreditHistory(Collection<Variation> operators) {
        history = new HashMap();
        Iterator<Variation> iter = operators.iterator();
        while (iter.hasNext()) {
            history.put(iter.next(), new ArrayList());
        }
    }

    public Collection<Variation> getOperators() {
        return history.keySet();
    }

    /**
     * Add a reward to the history. Credit is tagged with iteration from when
 the reward is issued
     *
     * @param heuristic
     * @param reward
     */
    public void add(Variation heuristic, Credit reward) {
        history.get(heuristic).add(reward);
        maxIteration = Math.max(maxIteration, reward.getIteration());
    }

    /**
     * Returns the collection of rewards received by a specific operator
     * @param operator
     * @return 
     */
    public Collection<Credit> getHistory(Variation operator) {
        return history.get(operator);
    }

    /**
     * Returns a map of the last rewards received by each operator
     * @return 
     */
    public HashMap<Variation, Credit> getLatest() {
        HashMap<Variation, Credit> out = new HashMap<>();
        for (Variation operator : getOperators()) {
            out.put(operator, this.getHistory(operator).iterator().next());
        }
        return out;
    }

    /**
     * Clears the history for all operators.
     */
    public void clear() {
        for (Variation heuristic : getOperators()) {
            history.get(heuristic).clear();
        }
    }
    
    /**
     * Returns the iteration that the latest rewarded was given
     * @return 
     */
    public int getMaxIteration(){
        return maxIteration;
    }
}
