/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.heuristicselectors;

import hh.creditrepository.ICreditRepository;
import hh.nextheuristic.AbstractHeuristicSelector;
import hh.qualityestimation.IQualityEstimation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.moeaframework.core.ParallelPRNG;
import org.moeaframework.core.Variation;

/**
 * Multi-armed bandit Upper confidence bound formulation as shown in Da Costa,
 * L., Fialho, Á., Schoenauer, M., & Sebag, M. (2008). Adaptive operator
 * selection with dynamic multi-armed bandits. Genetic and Evolutionary
 * Computation Conference. Retrieved from
 * http://dl.acm.org/citation.cfm?id=1389272
 *
 * @author nozomihitomi
 */
public class MAB extends AbstractHeuristicSelector {

    /**
     * keeps count of how many times a heuristic has been played
     */
    private HashMap<Variation, Integer> selectionCounts;

    /**
     * Coefficient to control exploration vs exploitation of low-level
     * heuristics
     */
    private final double c;
    
    private final ParallelPRNG pprng;

    /**
     * Constructor requires a credit repository type: only ICreditRepository
     * with ICreditHistory makes sense for implementing DMAB.
     *
     * @param heuristics
     * @param c Coefficient to control exploration vs exploitation of low-level
     * heuristics
     */
    public MAB(Collection<Variation> heuristics, double c) {
        super(heuristics);
        this.c = c;
        this.selectionCounts = new HashMap<>();
        this.pprng = new ParallelPRNG();
        init();
    }

    /**
     * Selects the next heuristic based on DMAB method. If selection count is
     * zero, a random heuristic is selected with uniform probability
     *
     * @return
     */
    @Override
    public Variation nextHeuristic() {
        Variation vari;
        ArrayList<Variation> unplayed = unplayedHeuristics();
        if(!unplayed.isEmpty()){
            vari = unplayed.get(pprng.nextInt(unplayed.size()));
        } else {
            vari = argMax(heuristics);
        }
        incrementIterations();
        selectionCounts.put(vari, selectionCounts.get(vari) + 1);
        return vari;
    }

    /**
     * This function is Upper Confidence Bound used in multi-armed bandit
     * policies
     *
     * @param heuristic the heuristic to be evaluated
     * @return the value resulting from the evaluation of the heuristic
     */
    @Override
    protected double function2maximize(Variation heuristic) {
        int numPlayed = selectionCounts.get(heuristic);
        return qualities.get(heuristic) + c * Math.sqrt(2 * getNumberOfIterations() / Math.log(numPlayed));
    }

    @Override
    public void update(ICreditRepository creditRepo, IQualityEstimation creditAgg) {
        updateQuality(creditRepo, creditAgg);
    }

    /**
     * sets the heuristic selection counts to 0.
     */
    @Override
    public void reset() {
        super.reset();
        init();
    }

    /**
     * initializes the selection counts for each heuristic to 0
     */
    private void init() {
        for (Variation heuristic : heuristics) {
            selectionCounts.put(heuristic, 0);
        }
    }

    /**
     * Checks to see if there are unplayed heuristics. If there are, then a
     * random one is selected with uniform probability from the unplayed
     * heuristics
     *
     * @return
     */
    private ArrayList<Variation> unplayedHeuristics() {
        ArrayList<Variation> unplayed = new ArrayList<>();
        for (Variation heuristic : heuristics) {
            if (selectionCounts.get(heuristic) == 0) {
                unplayed.add(heuristic);
            }
        }
        return unplayed;
    }

    @Override
    public String toString() {
        return "MAB";
    }
}
