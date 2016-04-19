/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.creditassignment.offspringparent;

import hh.creditassigment.CreditFitnessFunctionType;
import hh.creditassigment.CreditDefinedOn;
//import hh.creditassignment.fitnessindicator.HypervolumeIndicator;
import hh.creditassignment.fitnessindicator.IIndicator;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;

/**
 * These indicators take the form of the indicators used in IBEA. It uses the
 * population to compute the fitness of individuals via quality indicators. It
 * does not compute the quality of the population
 *
 * Indicator fitness calculation based on Zitzler, E., & Simon, K. (2004).
 * Indicator-Based Selection in Multiobjective Search. 8th International
 * Conference on Parallel Problem Solving from Nature (PPSN VIII), 832–842.
 * doi:10.1007/978-3-540-30217-9_84
 *
 * @author nozomihitomi
 */
public class OPBinaryIndicator extends AbstractOffspringParent {
    
    /**
     * The binary indicator to use
     */
    private final IIndicator indicator;

    /**
     *
     * @param indicator The indicator to use
     * @param prob
     */
    public OPBinaryIndicator(IIndicator indicator, Problem prob) {
        this.indicator = indicator;
        //has to be the population because parent may not lie on PF or in archive
        this.operatesOn = CreditDefinedOn.PARENT;
        this.fitType = CreditFitnessFunctionType.I;
    }

    /**
     * This method compares the fitness of the offspring and its parent 
     *
     * @param offspring
     * @param parent of the offspring solution
     * @param pop population is assumed to contain both the parent solution and
     * the offspring solution
     * @param removedSolution the solution index that was just removed from the
     * population
     * @return the positive percent improvement in fitness. If no improvement
     * then return 0.0
     */
    @Override
    public double compute(Solution offspring, Solution parent, Population pop, Solution removedSolution) {
        return Math.max(0.0, indicator.compute(parent, offspring));
    }

    @Override
    public String toString() {
        return "OP-" + indicator.toString() + operatesOn;
    }

}
