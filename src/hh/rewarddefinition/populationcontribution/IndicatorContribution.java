/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.rewarddefinition.populationcontribution;

import hh.hyperheuristics.SerializableVal;
import hh.rewarddefinition.Reward;
import hh.rewarddefinition.RewardDefinedOn;
import hh.rewarddefinition.fitnessindicator.HypervolumeIndicator;
import hh.rewarddefinition.fitnessindicator.IIndicator;
import hh.rewarddefinition.fitnessindicator.R2Indicator;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

/**
 *
 * @author SEAK2
 */
public class IndicatorContribution extends AbstractPopulationContribution{
    
    /**
     * Indicator used to compute indicator value
     */
    private final IIndicator indicator;

    /**
     * Reference point. Some indicators require a reference point.
     */
    private Solution refPt;
    
    /**
     * Only maintain the min objectives and the max objectives
     */
    private Solution minObjs;
    private Solution maxObjs;
    
    /**
     *
     * @param indicator Indicator to use to reward heuristics
     * @param operatesOn Enum to specify whether to compare the improvement on the population or the archive
     */
    public IndicatorContribution(IIndicator indicator,RewardDefinedOn operatesOn) {
        this.indicator = indicator;
        this.operatesOn = operatesOn;
        if(!this.operatesOn.equals(RewardDefinedOn.ARCHIVE)&&!this.operatesOn.equals(RewardDefinedOn.PARETOFRONT))
            throw new IllegalArgumentException(this.operatesOn + " is invalid option. Needs to be archive or pareto front");
    }

    /**
     * @param population
     * @param enteringSolutions
     * @param removedSolutions used to update the bounds in order to normalize metrics
     * @param heuristics
     * @param iteration
     * @return 
     */
    @Override
    public HashMap<Variation, Reward> compute(Population population,
            Collection<Solution> enteringSolutions,Collection<Solution> removedSolutions, Collection<Variation> heuristics, int iteration) {
        NondominatedPopulation ndpop = (NondominatedPopulation)population;
        //only run on initial run.
        boundUpdate:
        if (sortedObjs == null) {
            updateMinMax(ndpop);
        } else {
            Iterator<Solution> iter = removedSolutions.iterator();
            while (iter.hasNext()) {
                //check to see if removing solution will change upperbound.
                Solution soln = iter.next();
                for(int i =0; i<soln.getNumberOfObjectives();i++){
                    if(soln.getObjective(i)==maxObjs.getObjective(i)){
                        updateMinMax(ndpop);
                        break boundUpdate;
                    }
                }
            }
            for (Solution offspring : enteringSolutions) {
                for(int i =0; i<offspring.getNumberOfObjectives();i++){
                    if(offspring.getObjective(i)<minObjs.getObjective(i)){
                        minObjs.setObjective(i,offspring.getObjective(i));
                    }
                }
            }
        }
       
        //normalize solutions using max and min bounds of the ndpop with the new solution
        NondominatedPopulation normNDpop = new NondominatedPopulation();
        for(Solution soln:ndpop){ 
            Solution normSoln = new Solution(normalizeObjectives(soln));
            if (soln.hasAttribute("heuristic")) {
                normSoln.setAttribute("heuristic", new SerializableVal(((SerializableVal) soln.getAttribute("heuristic")).getSval()));
            }
            normNDpop.forceAddWithoutCheck(normSoln);
        }
        if (indicator.getClass().equals(HypervolumeIndicator.class)) {
            double[] hvRefPoint = new double[enteringSolutions.iterator().next().getNumberOfObjectives()];
            Arrays.fill(hvRefPoint, 2.0);
            refPt = new Solution(hvRefPoint);
        }else if(indicator.getClass().equals(R2Indicator.class)){
            double[] r2RefPoint = new double[enteringSolutions.iterator().next().getNumberOfObjectives()];
            Arrays.fill(r2RefPoint, -1.0); //since everything is normalized, utopia point is 0 vector
            refPt = new Solution(r2RefPoint);
        }
        
        
        //improvements over old population will result in a non negative value
        List<Double> contributions = indicator.computeContributions(normNDpop, refPt);
        
        //find solutions in nondominated set that belongs to each heuristic
        HashMap<String,Double> rewardVals = new HashMap<>();
        for(Variation heuristic:heuristics){
            rewardVals.put(heuristic.toString(), 0.0);
        }

        for (int i = 0; i < normNDpop.size(); i++) {
            //solutions created in initial population get no reward
            Solution soln = normNDpop.get(i);
            if (soln.hasAttribute("heuristic")) {
                String operator = ((SerializableVal) soln.getAttribute("heuristic")).getSval();
                rewardVals.put(operator, rewardVals.get(operator) + contributions.get(i));
            }
        }
        
        HashMap<Variation,Reward> rewards = new HashMap();
        for(Variation heuristic:heuristics){
            rewards.put(heuristic, new Reward(iteration,rewardVals.get(heuristic.toString())));
        }
        return rewards;
    }
    
    private void updateMinMax(NondominatedPopulation population){
        computeBounds(population);
        int numObj = population.get(0).getNumberOfObjectives();
        double[] minObjsArr = new double[numObj];
        double[] maxObjsArr = new double[numObj];
        for (int i = 0; i < numObj; i++) {
            minObjsArr[i]=sortedObjs.get(i).getFirst();
            maxObjsArr[i]=sortedObjs.get(i).getLast();
        }
        minObjs = new Solution(minObjsArr);
        maxObjs = new Solution(maxObjsArr);
    }

    @Override
    protected double[] normalizeObjectives(Solution solution) {
        double[] normalizedObjs = new double[solution.getNumberOfObjectives()];
        for (int i = 0; i < solution.getNumberOfObjectives(); i++) {
            double lowBound = minObjs.getObjective(i);
            double upBound = maxObjs.getObjective(i);
            normalizedObjs[i] = (solution.getObjective(i) - lowBound) / (upBound - lowBound);
        }
        return normalizedObjs;
    }
    
    
    
    @Override
    public String toString() {
        return "CNI_" + indicator.toString() + operatesOn;
    }
    
}
