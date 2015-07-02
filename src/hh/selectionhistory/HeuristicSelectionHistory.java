/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.selectionhistory;

import hh.heuristicgenerators.HeuristicSequence;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import org.moeaframework.core.Variation;

/**
 *
 * @author nozomihitomi
 */
public class HeuristicSelectionHistory implements IHeuristicSelectionHistory, Serializable{
    private static final long serialVersionUID = -2323214225020219554L;
    
    protected HashMap<Variation,Stack<Integer>> history;
    protected int selectionCount;
    
    public HeuristicSelectionHistory(Collection<Variation> heuristics){
        history = new HashMap();
        Iterator<Variation> iter = heuristics.iterator();
        while(iter.hasNext()){
            history.put(iter.next(),  new Stack());
        }
        this.selectionCount = 0;
    }
    
    @Override
    public Stack<Variation> getOrderedHistory(){
        Stack<Variation> out = new Stack();
        for(int i=selectionCount-1;i>0;i--){
            Iterator<Variation> iter = history.keySet().iterator();
            while (iter.hasNext()) {
                Variation heuristic = iter.next();
                if(!history.get(heuristic).empty() && history.get(heuristic).peek()==i){
                    out.push(heuristic);
                    history.get(heuristic).pop();
                    break;
                }
            }
        }
        return out;
    }

    /**
     * Adds the heuristic to the history. If the Variation is a sequence of 
     * heuristics, the heuristics in the sequence will be added to the history
     * @param heuristic to add to the history
     */
    @Override
    public void add(Variation heuristic) {
        if(heuristic.getClass().equals(HeuristicSequence.class)){
            Iterator<Variation> iter = ((HeuristicSequence)heuristic).getSequence().iterator();
            while(iter.hasNext())
                this.add(iter.next());
        }else{
            history.get(heuristic).push(selectionCount);
            selectionCount++;
        }
    }

    @Override
    public int getSelectedTimes(Variation heuristic) {
        return history.get(heuristic).size();
    }

    /**
     * Clears all selection history
     */
    @Override
    public void clear() {
        Iterator<Variation> iter = history.keySet().iterator();
        while (iter.hasNext()) {
            history.get(iter.next()).clear();
        }
        selectionCount = 0;
    }

    @Override
    public int getTotalSelectionCount() {
        return selectionCount;
    }

    @Override
    public Collection<Variation> getHeuristics() {
        return history.keySet();
    }
    
    
}
