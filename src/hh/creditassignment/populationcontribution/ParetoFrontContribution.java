/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.creditassignment.populationcontribution;

import hh.hyperheuristics.SerializableVal;
import hh.creditassigment.CreditFunctionInputType;
import hh.creditassigment.CreditFitnessFunctionType;
import hh.creditassigment.Credit;
import hh.creditassigment.CreditDefinedOn;
import java.util.Collection;
import java.util.HashMap;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

/**
 * This Credit definition gives credit to all solutions created by the specified
 heuristic, including the solution given, that lie on the Pareto front. 
 * @author Nozomi
 */
public class ParetoFrontContribution extends AbstractPopulationContribution{

    /**
     * Penalty/reward for not being in the Pareto Front
     */
    private final double rewardNotInPF;
    
    /**
     * Credit value for being in the Pareto Front
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
        this.operatesOn = CreditDefinedOn.PARETOFRONT;
        this.fitType = CreditFitnessFunctionType.Do;
        this.inputType = CreditFunctionInputType.CS;
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
     * @param operators
     * @param iteration
     * @return 
     */
    @Override
    public HashMap<Variation, Credit> compute(Population population,Collection<Variation> operators, int iteration) {
        HashMap<String,Double> rewards = new HashMap();
        //give all operators 0 credits first
        for(Variation op:operators){
            rewards.put(op.toString(), 0.0);
        }
        //iterate through solutions in neighborhood
        for(Solution soln:population){
            if(soln.hasAttribute("heuristic")){
                String opName = ((SerializableVal)soln.getAttribute("heuristic")).getSval();
                rewards.put(opName,rewards.get(opName) + rewardInPF);
            }
        }
        
        HashMap<Variation,Credit> out = new HashMap();
        for(Variation op:operators){
            double r = rewards.get(op.toString());
            if(r>0)
                out.put(op,new Credit(iteration, r));
            else
                out.put(op, new Credit(iteration, rewardNotInPF));
        }
        return out;
    }
}
