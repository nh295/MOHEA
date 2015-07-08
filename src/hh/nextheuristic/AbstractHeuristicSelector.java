/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.nextheuristic;

import hh.creditaggregation.ICreditAggregationStrategy;
import hh.creditdefinition.Credit;
import hh.creditrepository.ICreditRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.moeaframework.core.Variation;

/**
 * This abstract implements the interface INextHeuristic. Classes that 
 * extend this abstract class are required to have some credit repository
 * @author nozomihitomi
 */
public abstract class AbstractHeuristicSelector implements INextHeuristic{
    
    /**
     * Random number generator for selecting heuristics.
     */
    protected final Random random = new Random();
    
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
    protected Collection<Variation> heuristics;
    
    /**
     * Constructor requires a credit repository that stores credits earned by 
     * heuristics.
     */
    public AbstractHeuristicSelector(Collection<Variation> heuristics){
        this.iterations = 0;
        this.qualities = new HashMap<>();
        this.heuristics = heuristics;
        resetQualities();
    }
    
    /**
     * Method finds the heuristic that maximizes the function to be maximized. 
     * The function to be maximized may be related to credits or a function of 
     * credits. If there are two or more heuristics that maximize the function 
     * (i.e. there is a tie) a random heuristic will be selected from the tied 
     * maximizing heuristics
     * @param heuristics the set of heuristics to maximize over
     * @return the heuristic that maximizes the function2maximize
     */
    protected Variation argMax(Collection<Variation> heuristics){
        Iterator<Variation> iter = heuristics.iterator();
        ArrayList<Variation> ties = new ArrayList();
        Variation leadHeuristic = null;
        double maxVal = -1000000000;
        try{
        while(iter.hasNext()){
            Variation heuristic_i = iter.next();
            if(leadHeuristic==null){
                leadHeuristic = heuristic_i;
                maxVal = function2maximize(heuristic_i);
                continue;
            }
            if(function2maximize(heuristic_i)>maxVal){
                maxVal = function2maximize(heuristic_i);
                leadHeuristic = heuristic_i;
                ties.clear();
            }else if(function2maximize(heuristic_i)==maxVal){
                ties.add(heuristic_i);
            }
        }
        //if there are any ties in the credit score, select randomly (uniform 
        //probability)from the heuristics that tied at lead
        if(!ties.isEmpty()){
            leadHeuristic = getRandomHeuristic(ties);
        }
        }catch(NoSuchMethodException ex){
            Logger.getLogger(AbstractHeuristicSelector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return leadHeuristic;
    }
    
    /**
     * The function to be maximized by argMax(). The function to be maximized 
     * may be related to credits or a function of credits. If an 
     * IHeuristicSeletor uses this method, it should be overridden
     * @param heuristic input to the function
     * @return the value of the function with the given input
     * @throws java.lang.NoSuchMethodException If this method is used without 
     * being overridden, then it throws a NoSuchMethodException
     */
    protected double function2maximize(Variation heuristic) throws NoSuchMethodException{
        throw new NoSuchMethodException("Need to override this method");
    }
    
    /**
     * Selects a random heuristic from a collection of heuristics with uniform 
     * probability
     * @param heuristics the collection to draw a random heuristic from 
     * @return the randomly selected heuristic
     */
    protected Variation getRandomHeuristic(Collection<Variation> heuristics){
        int randInt = random.nextInt(heuristics.size());
        Iterator<Variation> iter = heuristics.iterator();
        int count = 0;
        Variation randHeuristic = null;
        while(iter.hasNext()){
            if(count==randInt)
                randHeuristic = iter.next();
            else
                iter.next();
            count++;
        }
        return randHeuristic;
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
     * Clears credit reposit
     */
    @Override
    public void reset(){
        resetQualities();
    }
    
    /**
     * Clears qualities and resets them to 0.
     */
    public final void resetQualities(){
        Iterator<Variation> iter = heuristics.iterator();
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
    public Collection<Variation> getHeuristics(){
        return heuristics;
    }
    

//    
}
