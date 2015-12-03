/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.nextheuristic;

import hh.rewarddefinition.Reward;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.moeaframework.core.ParallelPRNG;
import org.moeaframework.core.Variation;

/**
 * This abstract implements the interface INextHeuristic. Classes that 
 * extend this abstract class are required to have some credit repository
 * @author nozomihitomi
 */
public abstract class AbstractOperatorSelector implements INextHeuristic{
    
    /**
     * Random number generator for selecting heuristics.
     */
    protected final ParallelPRNG pprng = new ParallelPRNG();
    
    /**
     * The number of times nextHeuristic() is called
     */
    private int iterations;
    
    /**
     * Hashmap to store the qualities of the heuristics
     */
    protected HashMap<Variation,Double> qualities;
    
    /**
     * The heuristics from which the selector can choose from
     */
    protected Collection<Variation> operators;
    
    /**
     * Constructor requires a credit repository that stores credits earned by 
     * heuristics.
     * @param operators the collection of operators used to conduct search
     */
    public AbstractOperatorSelector(Collection<Variation> operators){
        this.iterations = 0;
        this.qualities = new HashMap<>();
        this.operators = operators;
        resetQualities();
    }
    
    /**
     * Method finds the heuristic that maximizes the function to be maximized. 
     * The function to be maximized may be related to credits or a function of 
     * credits. If there are two or more heuristics that maximize the function 
     * (i.e. there is a tie) a random heuristic will be selected from the tied 
     * maximizing heuristics
     * @param operators the set of heuristics to maximize over
     * @return the heuristic that maximizes the function2maximize
     */
    protected Variation argMax(Collection<Variation> operators){
        Iterator<Variation> iter = operators.iterator();
        ArrayList<Variation> ties = new ArrayList();
        Variation leadOperator = null;
        double maxVal = Double.NEGATIVE_INFINITY;
        try{
        while(iter.hasNext()){
            Variation operator_i = iter.next();
            if(leadOperator==null){
                leadOperator = operator_i;
                maxVal = function2maximize(operator_i);
                continue;
            }
            if(function2maximize(operator_i)>maxVal){
                maxVal = function2maximize(operator_i);
                leadOperator = operator_i;
                ties.clear();
            }else if(function2maximize(operator_i)==maxVal){
                ties.add(operator_i);
            }
        }
        //if there are any ties in the credit score, select randomly (uniform 
        //probability)from the heuristics that tied at lead
        if(!ties.isEmpty()){
            leadOperator = getRandomOperator(ties);
        }
        }catch(NoSuchMethodException ex){
            Logger.getLogger(AbstractOperatorSelector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return leadOperator;
    }
    
    /**
     * The function to be maximized by argMax(). The function to be maximized 
     * may be related to credits or a function of credits. If an 
     * IHeuristicSeletor uses this method, it should be overridden
     * @param operator input to the function
     * @return the value of the function with the given input
     * @throws java.lang.NoSuchMethodException If this method is used without 
     * being overridden, then it throws a NoSuchMethodException
     */
    protected double function2maximize(Variation operator) throws NoSuchMethodException{
        throw new NoSuchMethodException("Need to override this method");
    }
    
    /**
     * Selects a random heuristic from a collection of heuristics with uniform 
     * probability
     * @param operators the collection to draw a random heuristic from 
     * @return the randomly selected heuristic
     */
    protected Variation getRandomOperator(Collection<Variation> operators){
        return pprng.nextItem(new ArrayList<>(operators));
    }
    
    /**
     * Increments the number of times nextHeuristic() has been called by one
     */
    protected void incrementIterations(){
        iterations++;
    }
    
    /**
     * Returns the number of times nextHeuristic() has been called
     * @return the number of times nextHeuristic() has been called
     */
    @Override
    public int getNumberOfIterations(){
        return iterations;
    }
    
    /**
     * Resets stored qualities and iteration count
     */
    @Override
    public void reset(){
        resetQualities();
        iterations = 0;
    }
    
    /**
     * Clears qualities and resets them to 0.
     */
    public final void resetQualities(){
        Iterator<Variation> iter = operators.iterator();
        while(iter.hasNext()){
            //all heuristics have 0 quality at the beginning
            qualities.put(iter.next(), 0.0);
        }
    }

    @Override
    public HashMap<Variation, Double> getQualities() {
        return qualities;
    }
    
    /**
     * Gets the heuristics available to the hyper-heuristic.
     * @return 
     */
    @Override
    public Collection<Variation> getOperators(){
        return operators;
    }
    
    /**
     * Checks the quality of the heuristic. If the quality becomes negative, it is reset
     * to 0.0. Only updates those heuristics that were just rewarded.
     */
    protected void checkQuality(){
        for(Variation heuristic:qualities.keySet()) {
            //if current quality becomes negative, adjust to 0
            double qual = qualities.get(heuristic);
            if (qual < 0.0 || Double.isNaN(qual)) {
                qualities.put(heuristic, 0.0);
            }
        }
    }
    
}
