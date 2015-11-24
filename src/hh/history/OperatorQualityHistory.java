/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.history;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import org.moeaframework.core.Variation;

/**
 * Stores the history of qualities associated with each heuristic. Mostly for analysis purposes
 * @author nozomihitomi
 */
public class OperatorQualityHistory implements Serializable{
    private static final long serialVersionUID = -2323214225020219554L;
    
    protected HashMap<Variation,Stack<Double>> history;
    
    public OperatorQualityHistory(Collection<Variation> operators){
        history = new HashMap();
        Iterator<Variation> iter = operators.iterator();
        while(iter.hasNext()){
            history.put(iter.next(),  new Stack());
        }
    }

    /**
     * Gets the heuristics involved in the selection process
     * @return a collection containing the heuristics involved in the selection process
     */
    public Collection<Variation> getOperators() {
        return history.keySet();
    }

    /**
     * This adds the quality of a heuristic to the history
     * @param heuristic the heuristic to add to the history
     * @param quality the quality value to add
     */
    public void add(Variation heuristic, double quality) {
        history.get(heuristic).push(quality);
    }

    /**
     * Gets the quality history of a particular heuristic
     * @param heuristic of interest
     * @return the quality history of the specified heuristic
     */
    public Collection<Double> getHistory(Variation heuristic) {
        return history.get(heuristic);
    }

    /**
     * Gets the latest quality of each heuristic
     * @return 
     */
    public HashMap<Variation, Double> getLatest() {
        HashMap<Variation,Double> out = new HashMap<>();
        for(Variation operator:getOperators()){
            out.put(operator, this.getHistory(operator).iterator().next());
        }
        return out;
    }

    /**
     * Clears the history
     */
    public void clear() {
        for(Variation heuristic:getOperators()){
            history.get(heuristic).clear();
        }
    }
    
    
}
