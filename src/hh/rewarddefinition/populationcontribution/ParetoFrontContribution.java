/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.rewarddefinition.populationcontribution;

import hh.rewarddefinition.Reward;
import hh.hyperheuristics.SerializableVal;
import hh.rewarddefinition.RewardDefinedOn;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

/**
 * This Reward definition gives credit to all solutions created by the specified
 * heuristic, including the solution given, that lie on the Pareto front. 
 * @author Nozomi
 */
public class ParetoFrontContribution extends AbstractPopulationContribution{

    /**
     * Penalty/reward for not being in the Pareto Front
     */
    private final double rewardNotInPF;
    
    /**
     * Reward value for being in the Pareto Front
     */
    private final double rewardInPF;
    
    /**
     * Constructor to specify the rewards to give to the heuristic responsible 
     * for each solution on the Pareto front.
     * @param rewardInPF reward to assign to each solution on the Pareto Front that the heuristic created
     * @param rewardNotInPF reward to assign if there are no solutions on the Pareto Front created by the heuristic 
     */
    public ParetoFrontContribution(double rewardInPF,double rewardNotInPF) {
        super();
        operatesOn = RewardDefinedOn.PARETOFRONT;
        this.rewardNotInPF = rewardNotInPF;
        this.rewardInPF = rewardInPF;
    }
    
    /**
     * This method counts the number of solutions the heuristic is responsible 
     * for in the given population. For each solution it finds, it calculates the 
     * discounted reward based on the DecayingRewards. The sum total is the 
     * reward to be assigned
     * @param population pareto front for this implementation
     * @param heuristic
     * @param iteration
     * @return 
     */
    protected double compute(Iterable<Solution> population,Variation heuristic,int iteration){
        double sumReward=0;
        Iterator<Solution> iter = population.iterator();
        while(iter.hasNext()){
            Solution soln = iter.next();
            if(soln.hasAttribute("heuristic")){
                if(((SerializableVal)soln.getAttribute("heuristic")).getSval().equalsIgnoreCase(heuristic.toString())){
                    int createdIteration = ((SerializableVal)soln.getAttribute("iteration")).getIval();
                    Reward r = new Reward(createdIteration,1);
                    sumReward+=rewardInPF*r.fractionOriginalVal(iteration);
                }
            }
        }
        if(sumReward>0){
            return sumReward;
        }else 
            return rewardNotInPF;
    }
    
    @Override
    public String toString() {
        return "ParetoFrontContribution";
    }
    /**
     * 
     * @param population for this implementation it should be the pareto front
     * @param heuristics
     * @param iteration
     * @return 
     */
    @Override
    public HashMap<Variation, Reward> compute(NondominatedPopulation population, Collection<Variation> heuristics, int iteration) {
        HashMap<Variation,Reward> rewards = new HashMap();
        for(Variation heuristic:heuristics){
            rewards.put(heuristic, new Reward(iteration,compute(population,heuristic, iteration)));
        }
        return rewards;
    }
}
