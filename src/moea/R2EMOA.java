/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moea;

import org.moeaframework.algorithm.SMSEMOA;
import org.moeaframework.core.FastNondominatedSorting;
import org.moeaframework.core.FitnessEvaluator;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Selection;
import org.moeaframework.core.Variation;
import org.moeaframework.core.comparator.RankComparator;

/**
 * Implementation of R2-EMOA
 *
 * Source: "R2-EMOA : Focused Multiobjective Search Using R2-Indicator-Based
 * Selection"
 *
 * Trautmann, Heike, Tobias Wagner, and Dimo Brockhoff. 2013. “R2-EMOA: Focused
 * Multiobjective Search Using R2-Indicator-Based Selection.” In Computer,
 * 5313:70–74. doi:10.1007/978-3-642-44973-4_8.
 *
 * @author nozomihitomi
 */
public class R2EMOA extends SMSEMOA {

    /**
     * The fitness evaluator to use (e.g., hypervolume or additive-epsilon
     * indicator).
     */
    private FitnessEvaluator fitnessEvaluator;

    /**
     * The selection operator.
     */
    private Selection selection;

    /**
     * The variation operator.
     */
    private Variation variation;

    public R2EMOA(Problem problem, Initialization initialization, Variation variation, FitnessEvaluator fitnessEvaluator) {
        super(problem, initialization, variation, fitnessEvaluator);
    }

    @Override
    protected void iterate() {
        super.iterate(); //To change body of generated methods, choose Tools | Templates.
    }

}
