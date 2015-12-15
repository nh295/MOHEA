/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.rewarddefinition.populationcontribution;

import hh.hyperheuristics.SerializableVal;
import hh.rewarddefinition.Reward;
import hh.rewarddefinition.RewardDefinedOn;
import java.util.Collection;
import java.util.HashMap;
import org.moeaframework.core.Population;
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
    
    @Override
    public String toString() {
        return "CS-Do-PF";
    }
    /**
     * 
     * @param population for this implementation it should be the pareto front
     * @param enteringSolutions not used
     * * @param removedSolutions not used
     * @param operators
     * @param iteration
     * @return 
     */
    @Override
    public HashMap<Variation, Reward> compute(Population population,Collection<Solution> enteringSolutions,Collection<Solution> removedSolutions, Collection<Variation> operators, int iteration) {
        HashMap<String,Double> rewards = new HashMap();
        //give all operators 0 credits first
        for(Variation op:operators){
            rewards.put(op.toString(), 0.0);
        }
        //iterate through solutions in neighborhood
        for(Solution soln:population){
            if(soln.hasAttribute("heuristic")){
                String opName = ((SerializableVal)soln.getAttribute("heuristic")).getSval();
                int createdIteration = ((SerializableVal)soln.getAttribute("iteration")).getIval();
                Reward r = new Reward(createdIteration,1);
                rewards.put(opName,rewards.get(opName) + rewardInPF*r.fractionOriginalVal(iteration));
            }
        }
        
        HashMap<Variation,Reward> out = new HashMap();
        for(Variation op:operators){
            double r = rewards.get(op.toString());
            if(r>0)
                out.put(op,new Reward(iteration, r));
            else
                out.put(op, new Reward(iteration, rewardNotInPF));
        }
        return out;
    }
}
