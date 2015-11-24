/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.rewarddefinition.fitnessindicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.comparator.DominanceComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.indicator.jmetal.FastHypervolume;

/**
 * Binary hypervolume indicator from Zitzler, E., & Simon, K. (2004).
 * Indicator-Based Selection in Multiobjective Search. 8th International
 * Conference on Parallel Problem Solving from Nature (PPSN VIII), 832–842.
 * doi:10.1007/978-3-540-30217-9_84
 *
 * @author nozomihitomi
 */
public class HypervolumeIndicator implements IIndicator {

    private final DominanceComparator domComparator;

    private final FastHypervolume FHV;

    /**
     *
     * @param problem being solved
     */
    public HypervolumeIndicator(Problem problem) {
        NondominatedPopulation refPop = new NondominatedPopulation();
        //Create a refpopulation with solutions that have objectives all 0 except
        //in one dimension set to 1.0. This is so that when using the 
        //MOEAFramework HV calculator, the normalization doesn't do anything
        for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
            Solution soln = problem.newSolution();
            for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
                soln.setObjective(j, 0.0);
            }
            soln.setObjective(i, 1.0);
            refPop.add(soln);
        }
        this.domComparator = new ParetoDominanceComparator();
        this.FHV = new FastHypervolume(problem, refPop, null);
    }

    @Override
    public double compute(Solution solnA, Solution solnB, Solution refPt) {
        int dom = domComparator.compare(solnA, solnB);
        if (dom == -1) {
            return 0;
        } else if (dom == 1) {
            return volume(solnB, refPt) - volume(solnA, refPt);
        } else {
            return volume(new Solution[]{solnA, solnB}, refPt) - volume(solnA, refPt);
        }
    }

    /**
     * Calculates the hypervolume created by one solution
     *
     * @param soln
     * @return
     */
    private double volume(Solution soln, Solution refPt) {
        double vol = 1.0;
        for (int i = 0; i < soln.getNumberOfObjectives(); i++) {
            vol *= refPt.getObjective(i) - soln.getObjective(i); //assume minimization problem
        }
        if (vol < 0) //if point is dominated by reference point
        {
            vol = 0;
        }
        return vol;
    }

    /**
     * Calculates the Hypervolume created by the union of multiple solutions
     *
     * @param solutions
     * @return
     */
    private double volume(Solution[] solutions, Solution refPt) {
        FHV.updateReferencePoint(refPt);
        return FHV.evaluate(new NondominatedPopulation(Arrays.asList(solutions)));
    }

    /**
     * Calculates the Hypervolume created by the union of multiple solutions in
     * a nondominated population
     */
    private double volume(NondominatedPopulation ndpop, Solution refPt) {
        Solution[] solns = new Solution[ndpop.size()];
        for (int i = 0; i < ndpop.size(); i++) {
            solns[i] = ndpop.get(i);
        }
        return volume(solns, refPt);
    }

    @Override
    public String toString() {
        return "BIHV";
    }

    @Override
    public List<Double> computeContributions(NondominatedPopulation pop, Solution refPt) {
        List<Double> contribution = new ArrayList<>(pop.size());

        for (int i = 0; i < pop.size(); i++) {
            contribution.add(computeContribution(pop, pop.get(i), refPt));
        }
        return contribution;
    }
//    @Override
//    public List<Double> computeContributions(NondominatedPopulation pop, ArrayList<SortedLinkedList<Double>> sortedObjs, Solution refPt) {
//        List<Double> contribution = new ArrayList<>(pop.size());
//        for (int i = 0; i < pop.size(); i++) {
//            Solution soln = unnormalize(pop.get(i), sortedObjs);
//            contribution.add(computeContribution(pop, sortedObjs, soln, refPt));
//        }
//        return contribution;
//    }

    @Override
    public double computeContribution(NondominatedPopulation pop, Solution offspring, Solution refPt) {
        double contribution;
        switch (offspring.getNumberOfObjectives()) {
//            case 2:
//                contribution = computeContribution2D(sortedObjs, offspring, refPt);
//                break;
//            case 3:
//                contribution = computeContribution3D(sortedObjs,pop, offspring, refPt);
//                break;
            default:
                //Create a nondominated popualtion without the offspring
                NondominatedPopulation popWOSolution = new NondominatedPopulation();
                for (int i = 0; i < pop.size(); i++) {
                    if (!Arrays.equals(pop.get(i).getObjectives(), offspring.getObjectives())) {
                        popWOSolution.forceAddWithoutCheck(pop.get(i));
                    }
                }
                contribution = computeContribution(pop, popWOSolution, refPt);
                break;
        }
        return contribution;
    }
    
    private double computeContribution(NondominatedPopulation pop1, NondominatedPopulation pop2, Solution refPt){
        return volume(pop1, refPt)-volume(pop2, refPt);
    }

    /**
     * Assumes minimization problem. Uses 2D hypervolume contribution approach
     * from SMS-EMOA Beume, Nicola, Boris Naujoks, and Michael Emmerich. 2007.
     * “SMS-EMOA: Multiobjective Selection Based on Dominated Hypervolume.”
     * European Journal of Operational Research 181 (3): 1653–1669.
     * doi:10.1016/j.ejor.2006.08.008.
     *
     * @param list array of sorted lists of objective values. List should be in
     * ascending order
     * @param offspring
     * @param oldPopIndicatorVal
     * @param refPt
     * @return
     */
    private double computeContribution2D(ArrayList<SortedLinkedList<Double>> list, Solution offspring, Solution refPt) {
        double contribution = 1;
        for (int i = 0; i < offspring.getNumberOfObjectives(); i++) {
            int ind = list.get(i).binaryFind(offspring.getObjective(i));
            double min = list.get(i).getFirst();
            double max = list.get(i).getLast();
            if (ind < list.get(0).size() - 1) {
                contribution *= (list.get(i).get(ind + 1) - min) / (max - min) - (list.get(i).get(ind) - min) / (max - min);
            } else {
                contribution *= refPt.getObjective(i) - (list.get(i).get(ind) - min) / (max - min);
            }
        }
        if (contribution < 0) {
            throw new IllegalStateException("Hypervolume contribution negative: should be positive for nondominated set");
        }
        return contribution;
    }

    /**
     * Uses 3D hypervolume contribution approach as Naujoks, B., N. Beume, and
     * M. Emmerich. 2005. “Multi-Objective Optimisation Using S-Metric
     * Selection: Application to Three-Dimensional Solution Spaces.” 2005 IEEE
     * Congress on Evolutionary Computation 2: 1282–1289.
     * doi:10.1109/CEC.2005.1554838.
     *
     * @param list
     * @param ndpop
     * @param offspring
     * @param refPt
     * @return
     */
    private List<Double> computeContributions3D(ArrayList<SortedLinkedList<Double>> list,NondominatedPopulation ndpop,Solution refPt) {

        int numSoln = list.get(0).size();
        
        //create normalized sorted lists
        ArrayList<Double> alpha = new ArrayList<>(numSoln);
        ArrayList<Double> beta = new ArrayList<>(numSoln);
        for(int i=0;i<numSoln;i++){
            alpha.add(i, (list.get(0).get(i) - list.get(0).getFirst()) / (list.get(0).getLast() - list.get(0).getFirst()));
            beta.add(i, (list.get(1).get(i) - list.get(1).getFirst()) / (list.get(1).getLast() - list.get(1).getFirst()));
        }
        alpha.add(refPt.getObjective(0));
        beta.add(refPt.getObjective(1));
              
        double[][] best1_f3 = new double[numSoln][numSoln];
        double[][] best2_f3 = new double[numSoln][numSoln];
        for(int i=0;i<numSoln;i++){
            Arrays.fill(best1_f3[i], refPt.getObjective(2));
            Arrays.fill(best2_f3[i], refPt.getObjective(2));
        }
        int[][] ownerNumbers = new int[numSoln][numSoln];
        int[][] owners = new int[numSoln][numSoln];
        Double[] contributions = new Double[numSoln];
        Arrays.fill(contributions, 0.0);
        for (int i = 0; i < numSoln; i++) {
            for (int j = 0; j < numSoln; j++) {
                int ownerNumber = 0;
                int owner = -1;
                for (int k = 0; k < numSoln; k++) {
                    //check to see if s_k dominates cell (i,j) conc. f_1, f_2
                    if (ndpop.get(k).getObjective(0) <= alpha.get(i)
                            && ndpop.get(k).getObjective(1) <= beta.get(j)) {
                        ownerNumber++;
                        owner = k;
                        if (ndpop.get(k).getObjective(2) < best1_f3[i][j]) {
                            best2_f3[i][j] = best1_f3[i][j]; //update second best
                            best1_f3[i][j] = ndpop.get(k).getObjective(2); //update best
                        } else if (ndpop.get(k).getObjective(2) < best2_f3[i][j]) {
                            best2_f3[i][j] = ndpop.get(k).getObjective(2); //updated second best
                        }
                    }
                }
                ownerNumbers[i][j]=ownerNumber;
                owners[i][j]=owner;
            }
        }
         for (int i = 0; i < numSoln; i++) {
            for (int j = 0; j < numSoln; j++) {
                if(ownerNumbers[i][j]==1){//cell(i,j) is dominated disjoint
                    contributions[owners[i][j]]+=(alpha.get(i+1)-alpha.get(i))*(beta.get(i+1)-beta.get(i))*(best2_f3[i][j]-best1_f3[i][j]);
                }
            }
         }
        return Arrays.asList(contributions);
    }
    
    /**
     *
     * @param list
     * @param ndpop
     * @param offspring
     * @param refPt
     * @return
     */
    private double computeContribution3D(ArrayList<SortedLinkedList<Double>> list,NondominatedPopulation ndpop, Solution offspring, Solution refPt) {
        //find offspring in population
        int ind=-1;
        for(int i=0;i<ndpop.size();i++){
            if(Arrays.equals(ndpop.get(i).getObjectives(),offspring.getObjectives())){
                ind = i;
            }
        }
        List<Double> contributions = computeContributions3D(list, ndpop, refPt);
        
       return contributions.get(ind);
    }

}
