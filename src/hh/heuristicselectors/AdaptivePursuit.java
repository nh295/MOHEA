/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.heuristicselectors;

import hh.qualityestimation.IQualityEstimation;
import hh.creditrepository.ICreditRepository;
import java.util.Collection;
import java.util.Iterator;
import org.moeaframework.core.Variation;

/**
 * Adaptive pursuit algorithm is based on Thierens, D. (2005). An adaptive
 * pursuit strategy for allocating operator probabilities. Belgian/Netherlands
 * Artificial Intelligence Conference, 385–386. doi:10.1145/1068009.1068251
 *
 * @author nozomihitomi
 */
public class AdaptivePursuit extends ProbabilityMatching {

    /**
     * The maximum probability that the heuristic with the highest credits can
     * be selected. It is implicitly defined as 1.0 - m*pmin where m is the
     * number of heuristics used and pmin is the minimum selection probability
     */
    double pmax;

    /**
     * The Learning Rate
     */
    private final double beta;

    /**
     * Constructor to initialize adaptive pursuit map for selection. The maximum
     * selection probability is implicitly defined as 1.0 - m*pmin where m is
     * the number of heuristics defined in the given credit repository and pmin
     * is the minimum selection probability
     *
     * @param heuristics from which to select from 
     * @param pmin the minimum selection probability
     * @param beta the learning rate
     */
    public AdaptivePursuit(Collection<Variation> heuristics, double pmin, double beta) {
        super(heuristics, pmin);
        this.pmax = 1 - (probabilities.size() - 1) * pmin;
        this.beta = beta;
        if (pmax < pmin) {
            throw new IllegalArgumentException("the implicit maxmimm selection "
                    + "probability " + pmax + " is less than the minimum selection probability " + pmin);
        }

        //Initialize the probabilities such that a random heuristic gets the pmax
        int heurisitic_lead = pprng.nextInt(probabilities.size());
        Iterator<Variation> iter = probabilities.keySet().iterator();
        int count = 0;
        while (iter.hasNext()) {
            if (count == heurisitic_lead) {
                probabilities.put(iter.next(), pmax);
            } else {
                probabilities.put(iter.next(), pmin);
            }
            count++;
        }
    }

    /**
     * Updates the probabilities stored in the map by finding the heuristic with
     * the most credits and apply pmax to that heuristic and pmin to all other
     * heuristics
     * @param creditRepo the credit repository that store the past earned credits
     * @param creditAgg method to aggregate the past credits to compute the heuristic's reward
     */
    @Override
    public void update(ICreditRepository creditRepo, IQualityEstimation creditAgg) {
        super.updateQuality(creditRepo, creditAgg);

        Variation leadHeuristic = argMax(creditRepo.getHeuristics());

        Iterator<Variation> iter = heuristics.iterator();
        while (iter.hasNext()) {
            Variation heuristic_i = iter.next();
            double prevProb = probabilities.get(heuristic_i);
            if (heuristic_i == leadHeuristic) {
                probabilities.put(heuristic_i, prevProb+beta*(pmax-prevProb));
            } else {
                probabilities.put(heuristic_i, prevProb+beta*(pmin-prevProb));
            }
        }
    }

    /**
     * Want to find the heuristic that has the maximum quality
     *
     * @param heuristic
     * @return the current quality of the specified heuristic
     */
    @Override
    protected double function2maximize(Variation heuristic) {
        return qualities.get(heuristic);
    }

    @Override
    public String toString() {
        return "AdaptivePursuit";
    }
    
}
