///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package hh.rewarddefinition.populationcontribution;
//
//import hh.hyperheuristics.SerializableVal;
//import hh.rewarddefinition.Reward;
//import hh.rewarddefinition.RewardDefinedOn;
//import hh.rewarddefinition.fitnessindicator.IIndicator;
//import java.util.Collection;
//import java.util.HashMap;
//import org.moeaframework.core.NondominatedPopulation;
//import org.moeaframework.core.Solution;
//import org.moeaframework.core.Variation;
//
///**
// *
// * @author SEAK2
// */
//public class IndicatorContribution extends AbstractPopulationContribution{
//    
//    /**
//     * Indicator used to compute indicator value
//     */
//    private final IIndicator indicator;
//
//    /**
//     * Reference point. Some indicators require a reference point.
//     */
//    private Solution refPt;
//    
//    /**
//     *
//     * @param indicator Indicator to use to reward heuristics
//     * @param operatesOn Enum to specify whether to compare the improvement on the population or the archive
//     */
//    public IndicatorContribution(IIndicator indicator,RewardDefinedOn operatesOn) {
//        this.indicator = indicator;
//        this.operatesOn = operatesOn;
//        if(!this.operatesOn.equals(RewardDefinedOn.ARCHIVE)&&!this.operatesOn.equals(RewardDefinedOn.PARETOFRONT))
//            throw new IllegalArgumentException(this.operatesOn + " is invalid option. Needs to be archive or pareto front");
//    }
//
//    @Override
//    public HashMap<Variation, Reward> compute(NondominatedPopulation population, Collection<Variation> heuristics, int iteration) {
//        indicatorz
//        
//        
//        
//        HashMap<String,NondominatedPopulation> ndpops = new HashMap<>();
//        for(Variation heuristic:heuristics){
//            ndpops.put(heuristic.toString(), new NondominatedPopulation());
//        }
//        for(Solution soln:population){
//             if(soln.hasAttribute("heuristic")){
//                String operator = ((SerializableVal)soln.getAttribute("heuristic")).getSval();
//                ndpops.get(operator).forceAddWithoutCheck(soln); //build a nondominated population belonging to each solution
//                
//            }
//        }
//        
//        HashMap<Variation,Reward> rewards = new HashMap();
//        for(Variation heuristic:heuristics){
//            rewards.put(heuristic, new Reward(iteration,compute(population,heuristic, iteration)));
//        }
//        return rewards;
//    }
//    
//}
