/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.rewarddefinition.fitnessindicator;

import java.util.Arrays;
import jmetal.util.comparators.DominanceComparator;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.indicator.Hypervolume;

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
    
    private final Hypervolume HV;
    
    private final Solution referencePt;

    /**
     *
     * @param referencePt Reference point from which to compute the hypervolume
     */
    public BinaryHypervolumeIndicator(Solution referencePt) {
        NondominatedPopulation refPop = new NondominatedPopulation();
        refPop.add(referencePt);
        this.HV = new Hypervolume(null, refPop);
        this.referencePt = referencePt;
    }

    /**
     * 
     * @param popA
     * @param popB (can be reference population)
     * @return 
     */
    @Override
    public double compute(NondominatedPopulation popA, NondominatedPopulation popB) {
        //intended use of binary HV indicator in IBEA: compares two vectors only
        if (popA.size() == 1 && popB.size() == 1) {
            Solution soln1 = popA.get(0);
            Solution soln2 = popB.get(0);
            if (domComparator.compare(soln1, soln2) == -1) {
                return volume(soln2) - volume(soln1);
            } else {
                return volume(new Solution[]{soln1, soln2}) - volume(soln1);
            }
        }else
            throw new UnsupportedOperationException("No such method for computing binary HV value for populations of more than one");
    }

    /**
     * Calculates the hypervolume created by one solution
     *
     * @param soln
     * @return
     */
    private double volume(Solution soln) {
        double vol = 0.0;
        for (int i = 0; i < soln.getNumberOfObjectives(); i++) {
            vol *= referencePt.getObjective(i) - soln.getObjective(i);
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
    private double volume(Solution[] solutions) {
        return HV.evaluate(new NondominatedPopulation(Arrays.asList(solutions)));
    }

    @Override
    public double computeWRef(NondominatedPopulation popA, NondominatedPopulation popRef) {
        return compute(popA, popRef);
    }

    @Override
    public String toString() {
        return "BIHV";
    }
    

}
