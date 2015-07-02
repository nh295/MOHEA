/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.heuristicgenerators;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import org.moeaframework.core.FrameworkException;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.operator.CompoundVariation;

/**
 * A heuristic composed of building blocks. Each building block must implement 
 * the Variation interface. 
 * @author nozomihitomi
 */
public class HeuristicSequence extends CompoundVariation{

    public HeuristicSequence(Variation... operators) {
        super(operators);
    }
    
    @Override
    public Solution[] evolve(Solution[] parents) {
        Solution[] result = Arrays.copyOf(parents, parents.length);
        
        for (Variation operator : operators) {
            if (result.length == operator.getArity()) {
                result = operator.evolve(result);
            } else if (operator.getArity() == 1) {
                for (int j = 0; j < result.length; j++) {
                    result[j] = operator.evolve(new Solution[] { result[j] })[0];
                }
            } else {
                throw new FrameworkException("invalid number of parents");
            }
        }
        
        return result;
    }
    
    /**
     * Returns the number of parents required by the heuristic requiring the most parent solutions
     * @return  the number of parents required by the heuristic requiring the most parent solutions
     */
    @Override
    public int getArity(){
        int maxParent = 0;
        for (Variation operator : operators) {
            if(operator.getArity()>maxParent)
                maxParent = operator.getArity();
        }
        return maxParent;
    }
    
    /**
     * Returns the number of building blocks that compose this heuristic
     * @return 
     */
    public int getLength(){
        return operators.size();
    }
    
    /**
     * Gets the building block at index i
     * @param i index of the desired building block
     * @return the building block at index i of this heuristic
     */
    public Variation get(int i){
        return operators.get(i);
    }
    
    /**
     * Gets the sequence of building blocks
     * @return the sequence of building blocks
     */
    public Collection<Variation> getSequence(){
        return operators;
    }
    
    /**
     * Appends all the given buildingBlocks in the order of the iterator for the
     * given collection
     * @param buildingBlocks to append to the heuristic sequence
     */
    public void appendAllOperators(Collection<Variation> buildingBlocks){
        Iterator<Variation> iter = buildingBlocks.iterator();
        while(iter.hasNext()){
            appendOperator(iter.next());
        }
    }
    
    /**
     * Appends  the given heuristic sequence in the order of getSequence();
     * given collection
     * @param sequence to append to the heuristic sequence
     */
    public void appendAllOperators(HeuristicSequence sequence){
        appendAllOperators(sequence.getSequence());
    }
    
    /**
     * Checks if the sequence is empty.
     * @return true if is emtpy. else false.
     */
    public boolean isEmpty(){
        return getLength()<=0;
    }
    
    /**
     * Clears the sequence
     */
    public void clear(){
        operators.clear();
    }
}
