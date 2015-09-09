/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.rewarddefinition.fitnessindicator;

import java.util.Arrays;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.comparator.DominanceComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.indicator.Hypervolume;
import org.moeaframework.core.indicator.jmetal.FastHypervolume;

/**
 * Binary hypervolume indicator from Zitzler, E., & Simon, K. (2004).
 * Indicator-Based Selection in Multiobjective Search. 8th International
 * Conference on Parallel Problem Solving from Nature (PPSN VIII), 832–842.
 * doi:10.1007/978-3-540-30217-9_84
 *
 * @author nozomihitomi
 */
public class BinaryHypervolumeIndicator implements IBinaryIndicator {

    private DominanceComparator domComparator;

    private final FastHypervolume FHV;

    /**
     *
     * @param problem being solved
     */
    public BinaryHypervolumeIndicator(Problem problem) {
        NondominatedPopulation refPop = new NondominatedPopulation();
        //Create a refpopulation with solutions that have objectives all 0 except
        //in one dimension set to 1.0. This is so that when using the 
        //MOEAFramework HV calculator, the normalization doesn't do anything
        for(int i=0; i<problem.getNumberOfObjectives(); i++){
            Solution soln = problem.newSolution();
            for(int j=0; j<problem.getNumberOfObjectives(); j++){
                soln.setObjective(j, 0.0);
            }
            soln.setObjective(i,1.0);
            refPop.add(soln);
        }
        double[] refPoint = new double[problem.getNumberOfObjectives()];
        Arrays.fill(refPoint, 2.0);
        this.FHV = new FastHypervolume(problem, refPop,new Solution(refPoint));
        this.domComparator = new ParetoDominanceComparator();
    }

    /**
     * The population input order matters.
     * @param popA
     * @param popB (can be reference population)
     * @param refPt hypervolume requires a reference point
     * @return
     */
    @Override
    public double compute(NondominatedPopulation popA, NondominatedPopulation popB, Solution refPt) {
        //intended use of binary HV indicator in IBEA: compares two vectors only
        if (popA.size() == 1 && popB.size() == 1) {
            Solution soln1 = popA.get(0);
            Solution soln2 = popB.get(0);
            return compute(soln1, soln2,refPt);
        } else {
            throw new UnsupportedOperationException("No such method for computing binary HV value for populations of more than one");
        }
    }

    @Override
    public double compute(Solution solnA, Solution solnB, Solution refPt) {
        if (domComparator.compare(solnA, solnB) == -1) {
            return volume(solnB,refPt) - volume(solnA,refPt);
        } else {
            return volume(new Solution[]{solnA, solnB},refPt) - volume(solnA,refPt);
        }
    }

    /**
     * Calculates the hypervolume created by one solution
     *
     * @param soln
     * @return
     */
    private double volume(Solution soln,Solution refPt) {
        double vol = 0.0;
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
    private double volume(Solution[] solutions,Solution refPt) {
        return FHV.evaluate(new NondominatedPopulation(Arrays.asList(solutions)));
    }

    /**
     * In IBEA format binary indicator the order of the population inputs matter. Use this method if you have a reference population.
     * @param popA
     * @param popRef
     * @param refPt hypervolume require reference point
     * @return 
     */
    @Override
    public double computeWRef(NondominatedPopulation popA, NondominatedPopulation popRef, Solution refPt) {
        return compute(popA, popRef,refPt);
    }

    @Override
    public String toString() {
        return "BIHV";
    }

}
