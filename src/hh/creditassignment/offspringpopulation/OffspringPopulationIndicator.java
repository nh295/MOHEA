///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package hh.creditassignment.offspringpopulation;
//
//import hh.creditassigment.CreditFunctionInputType;
//import hh.creditassigment.CreditFitnessFunctionType;
//import hh.creditassigment.CreditDefinedOn;
////import hh.creditassignment.fitnessindicator.HypervolumeIndicator;
//import hh.creditassignment.fitnessindicator.IIndicator;
//import hh.creditassignment.fitnessindicator.R2Indicator;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.Iterator;
//import org.moeaframework.core.NondominatedPopulation;
//import org.moeaframework.core.Population;
//import org.moeaframework.core.Solution;
//
///**
// * Reward definition that computes an offspring's improvement to an indicator's
// * value to the parteo front before and after the offspring is added to the
// * population
// *
// * @author nozomihitomi
// */
//public class OffspringPopulationIndicator extends AbstractOffspringPopulation {
//
//    /**
//     * Indicator used to compute indicator
//     */
//    private final IIndicator indicator;
//
//    /**
//     * Reference point. Some indicators require a reference point.
//     */
//    private Solution refPt;
//
//    /**
//     * Only maintain the min objectives and the max objectives
//     */
//    private Solution minObjs;
//    private Solution maxObjs;
//    
//    /**
//     *
//     * @param indicator Indicator to use to reward heuristics
//     * @param operatesOn Enum to specify whether to compare the improvement on
//     * the population or the archive
//     */
//    public OffspringPopulationIndicator(IIndicator indicator, CreditDefinedOn operatesOn) {
//        this.indicator = indicator;
//        this.operatesOn = operatesOn;
//        fitType = CreditFitnessFunctionType.I;
//        inputType = CreditFunctionInputType.SI;
//        if (!this.operatesOn.equals(CreditDefinedOn.ARCHIVE) && !this.operatesOn.equals(CreditDefinedOn.PARETOFRONT)) {
//            throw new IllegalArgumentException(this.operatesOn + " is invalid option. Needs to be archive or pareto front");
//        }
//    }
//
//    /**
//     * Computes the reward based on the improvement of the indicator value of
//     * the population before the offspring solution is added to the population
//     * with the offspring solution added
//     *
//     * @param offspring
//     * @param pop nondominated population: pareto front or archive
//     * @return the improvement in the indicator value. 0.0 if no improvement
//     */
//    @Override
//    public double compute(Solution offspring, Population pop) {
//        //add offspring to ndpop to see if it entered
//        NondominatedPopulation ndpop = (NondominatedPopulation)pop;
//        Collection<Solution> removedSolns = ndpop.addAndReturnRemovedSolutions(offspring);
//
//        if (!ndpop.isChanged())  //if offspring doesn't improve the approximate set.
//            return 0.0;
//        
//        //only run on initial run.
//        boundUpdate:
//        if (sortedObjs == null) {
//            updateMinMax(ndpop);
//        } else {
//            if(removedSolns != null) {
//                Iterator<Solution> iter = removedSolns.iterator();
//                while (iter.hasNext()) {
//                    //check to see if removing solution will change upperbound.
//                    Solution soln = iter.next();
//                    for (int i = 0; i < soln.getNumberOfObjectives(); i++) {
//                        if (soln.getObjective(i) == maxObjs.getObjective(i)) {
//                            updateMinMax(ndpop);
//                            break boundUpdate;
//                        }
//                    }
//                }
//            }
//            for (int i = 0; i < offspring.getNumberOfObjectives(); i++) {
//                if (offspring.getObjective(i) < minObjs.getObjective(i)) {
//                    minObjs.setObjective(i, offspring.getObjective(i));
//                }
//            }
//        }
//
//        //normalize solutions using max and min bounds of the ndpop with the new solution
//        NondominatedPopulation normNDpop = new NondominatedPopulation();
//        for (Solution soln : ndpop) {
//            Solution normSoln = new Solution(normalizeObjectives(soln));
//            normNDpop.forceAddWithoutCheck(normSoln);
//        }
//
//        if (indicator.getClass().equals(HypervolumeIndicator.class)) {
//            double[] hvRefPoint = new double[offspring.getNumberOfObjectives()];
//            Arrays.fill(hvRefPoint, 2.0);
//            refPt = new Solution(hvRefPoint);
//        } else if (indicator.getClass().equals(R2Indicator.class)) {
//            double[] r2RefPoint = new double[offspring.getNumberOfObjectives()];
//            Arrays.fill(r2RefPoint, -1.0); //since everything is normalized, utopia point is 0 vector
//            refPt = new Solution(r2RefPoint);
//        }
//
//        //improvements over old population will result in a non negative value
//        Solution normOffspring = new Solution(normalizeObjectives(offspring));
//        double reward = indicator.computeContribution(normNDpop, normOffspring, refPt);
//        //can use below to check monotonicity of reward function
//        if (reward < 0) {
////            System.err.println(reward);
//            reward = 0;
////            throw new RuntimeException("Reward is negative even though nondominated population improved. Use monotonic indicator!");
//        }
//        return reward;
//    }
//    
//        private void updateMinMax(NondominatedPopulation population){
//        computeBounds(population);
//        int numObj = population.get(0).getNumberOfObjectives();
//        double[] minObjsArr = new double[numObj];
//        double[] maxObjsArr = new double[numObj];
//        for (int i = 0; i < numObj; i++) {
//            minObjsArr[i]=sortedObjs.get(i).getFirst();
//            maxObjsArr[i]=sortedObjs.get(i).getLast();
//        }
//        minObjs = new Solution(minObjsArr);
//        maxObjs = new Solution(maxObjsArr);
//    }
//
//    @Override
//    public String toString() {
//        return "SI-" + indicator.toString() + operatesOn;
//    }
//
//}
