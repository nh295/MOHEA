/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.rewarddefinition.offspringpopulation;

import hh.rewarddefinition.RewardDefinedOn;
import hh.rewarddefinition.fitnessindicator.HypervolumeIndicator;
import hh.rewarddefinition.fitnessindicator.IIndicator;
import hh.rewarddefinition.fitnessindicator.R2Indicator;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;

/**
 * Reward definition that computes an offspring's improvement to an indicator's
 * value to the parteo front before and after the offspring is added to the
 * population
 *
 * @author nozomihitomi
 */
public class OffspringPopulationIndicator extends AbstractOffspringPopulation {

    /**
     * Indicator used to compute indicator
     */
    private final IIndicator indicator;

    /**
     * Reference point. Some indicators require a reference point.
     */
    private Solution refPt;
    
    /**
     * Indicator value for previous population 
     */
    private double prevIValue;
    
    /**
     *
     * @param indicator Indicator to use to reward heuristics
     * @param operatesOn Enum to specify whether to compare the improvement on the population or the archive
     */
    public OffspringPopulationIndicator(IIndicator indicator,RewardDefinedOn operatesOn) {
        this.indicator = indicator;
        this.operatesOn = operatesOn;
        this.prevIValue=0;
        if(!this.operatesOn.equals(RewardDefinedOn.ARCHIVE)&&!this.operatesOn.equals(RewardDefinedOn.PARETOFRONT))
            throw new IllegalArgumentException(this.operatesOn + " is invalid option. Needs to be archive or pareto front");
    }

    /**
     * Computes the reward based on the improvement of the indicator value of
     * the population before the offspring solution is added to the population
     * with the offspring solution added
     *
     * @param offspring  
     * @param removedSolution
     * @param ndpop nondominated population: pareto front or archive
     * @return the improvement in the indicator value. 0.0 if no improvement
     */
    @Override
    public double compute(Solution offspring, NondominatedPopulation ndpop) {    
        NondominatedPopulation oldNDpop = null;
        try {
            //create temporary nondominated population that maintains old nondominated population
            oldNDpop = ndpop.clone();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(OffspringPopulationIndicator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //add offspring to ndpop to see if it entered
        Collection<Solution> removedSolns = ndpop.addAndReturnRemovedSolutions(offspring);
        
        //only run on initial run.
        if (sortedObjs == null) {
            computeBounds(ndpop);
        } else {
            if (removedSolns!=null) { //if this condition is met then the offspring replaced a solution in the population
                Iterator<Solution> iter = removedSolns.iterator();
                while(iter.hasNext()){
                    //updates the bounds based on just the new incoming solution and the removed solution
                    Solution soln = iter.next();
                    updateBoundsRemove(soln);
                }
                updateBoundsInsert(offspring);
            }else //if offspring doesn't replace a solution in the nondominated set, it cannot improve the approximate set.
                return 0.0;
        }
        
        //normalize solutions using max and min bounds of the ndpop with the new solution
        NondominatedPopulation normNDpop = new NondominatedPopulation();
        for(Solution soln:ndpop){
            normNDpop.forceAddWithoutCheck(new Solution(normalizeObjectives(soln)));
        }
//        NondominatedPopulation normNDpopWOSoln = new NondominatedPopulation();
//        for(Solution soln:oldNDpop){
//            normNDpopWOSoln.forceAddWithoutCheck(new Solution(normalizeObjectives(soln)));
//        }
        
        if (indicator.getClass().equals(HypervolumeIndicator.class)) {
            double[] hvRefPoint = new double[offspring.getNumberOfObjectives()];
            Arrays.fill(hvRefPoint, 2.0);
            refPt = new Solution(hvRefPoint);
        }else if(indicator.getClass().equals(R2Indicator.class)){
            double[] r2RefPoint = new double[offspring.getNumberOfObjectives()];
            Arrays.fill(r2RefPoint, 0.0); //since everything is normalized, utopia point is 0 vector
            refPt = new Solution(r2RefPoint);
        }
        
        //improvements over old population will result in a non negative value
        double reward = indicator.computeContribution(normNDpop, prevIValue, refPt);
        //can use below to check monotonicity of reward function
        if (reward < 0) {
//            System.err.println(reward);
            reward = 0;
//            throw new RuntimeException("Reward is negative even though nondominated population improved. Use monotonic indicator!");
        }
        return reward;
    }

    @Override
    public String toString() {
        return "OPop_" + indicator.toString() + operatesOn;
    }
    
    
}
