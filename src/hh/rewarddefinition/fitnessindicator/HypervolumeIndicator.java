/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.rewarddefinition.fitnessindicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    private DominanceComparator domComparator;
    
    private final Problem problem;
    
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
        for(int i=0; i<problem.getNumberOfObjectives(); i++){
            Solution soln = problem.newSolution();
            for(int j=0; j<problem.getNumberOfObjectives(); j++){
                soln.setObjective(j, 0.0);
            }
            soln.setObjective(i,1.0);
            refPop.add(soln);
        }
        this.problem = problem;
        this.domComparator = new ParetoDominanceComparator();
        this.FHV = new FastHypervolume(problem, refPop,null);
    }


    @Override
    public double compute(Solution solnA, Solution solnB, Solution refPt) {
        int dom = domComparator.compare(solnA, solnB);
        if (dom == -1) {
            return volume(solnB,refPt) - volume(solnA,refPt);
        }else if (dom == 1) {
            return volume(solnA,refPt) - volume(solnB,refPt);
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
        FHV.updateReferencePoint(refPt);
        return FHV.evaluate(new NondominatedPopulation(Arrays.asList(solutions)));
    }
    
    /**
     * Calculates the Hypervolume created by the union of multiple solutions in a nondominated population
     */
    private double volume(NondominatedPopulation ndpop,Solution refPt){
        Solution[] solns = new Solution[ndpop.size()];
        for(int i=0;i<ndpop.size();i++)
            solns[i] = ndpop.get(i);
        return volume(solns, refPt);
    }

    @Override
    public String toString() {
        return "BIHV";
    }

    @Override
    public List<Double> computeContributions(NondominatedPopulation pop, Solution refPt) {
        List<Double> contribution = new ArrayList<>(pop.size());

        try {
            NondominatedPopulation pop2 = pop.clone();
            for (int i = 0; i < pop.size(); i++) {
                Solution removed = pop2.get(0); //take the solution at the head of the population
                pop2.remove(0);
                contribution.add(computeContribution(pop, pop2, refPt));
                pop2.forceAddWithoutCheck(removed); //put solution back at the tail of the population

            }
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(R2Indicator.class.getName()).log(Level.SEVERE, null, ex);
        }

        return contribution;
    }

    @Override
    public double computeContribution(NondominatedPopulation pop, NondominatedPopulation popWOSolution, Solution refPt) {
        FHV.updateReferencePoint(refPt);
        return volume(pop,refPt)-volume(popWOSolution,refPt);
    }

}
