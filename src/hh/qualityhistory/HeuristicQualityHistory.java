/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.qualityhistory;

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
public class HeuristicQualityHistory implements IHeuristicQualityHistory, Serializable{
    private static final long serialVersionUID = -2323214225020219554L;
    
    protected HashMap<Variation,Stack<Double>> history;
    
    public HeuristicQualityHistory(Collection<Variation> heuristics){
        history = new HashMap();
        Iterator<Variation> iter = heuristics.iterator();
        while(iter.hasNext()){
            history.put(iter.next(),  new Stack());
        }
    }

    @Override
    public Collection<Variation> getHeuristics() {
        return history.keySet();
    }

    @Override
    public void add(Variation heuristic, double quality) {
        history.get(heuristic).push(quality);
    }

    @Override
    public Collection<Double> getHistory(Variation heuristic) {
        return history.get(heuristic);
    }

    @Override
    public HashMap<Variation, Double> getLatest() {
        HashMap<Variation,Double> out = new HashMap<>();
        for(Variation heuristic:getHeuristics()){
            out.put(heuristic, this.getHistory(heuristic).iterator().next());
        }
        return out;
    }

    @Override
    public void clear() {
        for(Variation heuristic:getHeuristics()){
            history.get(heuristic).clear();
        }
    }
    
    
}
