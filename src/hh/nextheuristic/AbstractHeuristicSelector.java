/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.nextheuristic;

import hh.qualityestimation.IQualityEstimation;
import hh.rewarddefinition.Reward;
import hh.creditrepository.ICreditRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.moeaframework.core.ParallelPRNG;
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
        double maxVal = Double.NEGATIVE_INFINITY;
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
        return pprng.nextItem(new ArrayList<>(heuristics));
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
    
    /**
     * Updates the quality of the heuristic based on the aggregation applied the
     * heuristic's credit history. If the quality becomes negative, it is reset
     * to 0.0. Only updates those heuristics that were just rewarded.
     * @param creditRepo the credit repository that store the past earned rewards
     * @param qualEst method to aggregate the past credits to compute the heuristic's reward
     */
    protected void updateQuality(ICreditRepository creditRepo, IQualityEstimation qualEst){
        qualities = creditRepo.estimateQuality(qualEst, getNumberOfIterations());
        for(Variation heuristic:qualities.keySet()) {
            //if current quality becomes negative, adjust to 0
            double qual = qualities.get(heuristic);
            if (qual < 0.0 || Double.isNaN(qual)) {
                qualities.put(heuristic, 0.0);
            }
        }
    }
    

//    
}
