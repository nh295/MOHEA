/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.history;

import hh.heuristicgenerators.HeuristicSequence;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import org.moeaframework.core.Variation;

/**
 * This class stores the history of which operator was selected at a certain
 * iteration
 *
 * @author nozomihitomi
 */
public class OperatorSelectionHistory implements Serializable {

    private static final long serialVersionUID = -2323214225020219554L;

    protected HashMap<Variation, Stack<Integer>> history;
    protected int selectionCount;

    public OperatorSelectionHistory(Collection<Variation> heuristics) {
        history = new HashMap();
        Iterator<Variation> iter = heuristics.iterator();
        while (iter.hasNext()) {
            history.put(iter.next(), new Stack());
        }
        this.selectionCount = 0;
    }

    /**
     * Returns the history of the selected heuristics in the ordered they
     * occurred.
     *
     * @return a stack of Variations which contains the history of the selected
     * heuristics in the ordered they occurred. Selections at the beginning of
     * the search are at the top of the Stack.
     */
    public Stack<Variation> getOrderedHistory() {
        Stack<Variation> out = new Stack();
        for (int i = selectionCount - 1; i > 0; i--) {
            Iterator<Variation> iter = history.keySet().iterator();
            while (iter.hasNext()) {
                Variation operator = iter.next();
                if (!history.get(operator).empty() && history.get(operator).peek() == i) {
                    out.push(operator);
                    history.get(operator).pop();
                    break;
                }
            }
        }
        return out;
    }

    /**
     * Adds the operator to the history. If the Variation is a sequence of
     * operators, the operator in the sequence will be added to the history
     *
     * @param operator to add to the history
     */
    public void add(Variation operator) {
        if (operator.getClass().equals(HeuristicSequence.class)) {
            Iterator<Variation> iter = ((HeuristicSequence) operator).getSequence().iterator();
            while (iter.hasNext()) {
                this.add(iter.next());
            }
        } else {
            history.get(operator).push(selectionCount);
            selectionCount++;
        }
    }

    /**
     * Gets the number of times the specified heuristic was selected using the
     * history's memory
     *
     * @param heuristic
     * @return
     */
    public int getSelectedTimes(Variation heuristic) {
        return history.get(heuristic).size();
    }

    /**
     * Clears all selection history
     */
    public void reset() {
        Iterator<Variation> iter = history.keySet().iterator();
        while (iter.hasNext()) {
            history.get(iter.next()).clear();
        }
        selectionCount = 0;
    }

    /**
     * Returns the number of selections made so far. This is the sum of the
     * selection counts for each heuristic across all heuristics
     *
     * @return the total number of selections made so far
     */
    public int getTotalSelectionCount() {
        return selectionCount;
    }

    /**
     * Gets the heuristics involved in the selection process
     *
     * @return a collection containing the heuristics involved in the selection
     * process
     */
    public Collection<Variation> getOperators() {
        return history.keySet();
    }

}
