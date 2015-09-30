/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.heuristicselectors;

import hh.nextheuristic.AbstractHeuristicSelector;
import hh.rewarddefinition.Reward;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.moeaframework.core.Variation;

/**
 * Selects heuristics based on probability which is proportional to the
 * heuristics credits. Each heuristic gets selected with a minimum probability
 * of pmin. If current credits in credit repository becomes negative, zero
 * credit is re-assigned to that heuristic. For the first iteration, heuristics
 * are selected with uniform probability.
 *
 * @author nozomihitomi
 */
public class RouletteWheel extends AbstractHeuristicSelector {

    /**
     * Hashmap to store the selection probabilities of each heuristic
     */
    protected HashMap<Variation, Double> probabilities;

    /**
     * The minimum probability for a heuristic to be selected
     */
    protected final double pmin;

    /**
     * Constructor to initialize probability map for selection
     *
     * @param heuristics from which to select from
     * @param pmin The minimum probability for a heuristic to be selected
     */
    public RouletteWheel(Collection<Variation> heuristics, double pmin) {
        super(heuristics);
        this.pmin = pmin;
        this.probabilities = new HashMap();
        reset();
    }

    /**
     * Will return the next heuristic that gets selected based on probability
     * proportional to a heuristics credits. Each heuristic gets selected with a
     * minimum probability of pmin
     *
     * @return
     */
    @Override
    public Variation nextHeuristic() {
        double p = pprng.nextDouble();
        Iterator<Variation> iter = probabilities.keySet().iterator();
        double sum = 0.0;
        Variation heuristic = null;
        while (iter.hasNext()) {
            heuristic = iter.next();
            sum += probabilities.get(heuristic);
            if (sum >= p) {
                break;
            }
        }
        incrementIterations();
        if (heuristic == null) {
            throw new NullPointerException("No heuristic was selected by Probability matching heuristic selector. Check probabilities");
        } else {
            return heuristic;
        }
    }

    /**
     * calculate the sum of all qualities across the heuristics
     *
     * @return the sum of the heuristics' qualities
     */
    protected double sumQualities() {
        double sum = 0.0;
        Iterator<Variation> iter = probabilities.keySet().iterator();
        while (iter.hasNext()) {
            sum += qualities.get(iter.next());
        }
        return sum;
    }

    /**
     * Clears the credit repository and resets the selection probabilities
     */
    @Override
    public final void reset() {
        super.resetQualities();
        super.reset();
        probabilities.clear();
        Iterator<Variation> iter = heuristics.iterator();
        while (iter.hasNext()) {
            //all heuristics get uniform selection probability at beginning
            probabilities.put(iter.next(), 1.0 / (double) heuristics.size());
        }
    }

    @Override
    public String toString() {
        return "ProbabilityMatching";
    }

    /**
     * Updates the selection probabilities of the heuristics according to the
     * qualities of each heuristic.
     */
    protected void updateProbabilities(){
        double sum = sumQualities();

        // if the credits sum up to zero, apply uniform probabilty to  heuristics
        Iterator<Variation> iter = heuristics.iterator();
        if (Math.abs(sum) < Math.pow(10.0, -14)) {
            while (iter.hasNext()) {
                Variation heuristic_i = iter.next();
                probabilities.put(heuristic_i, 1.0 / (double) heuristics.size());
            }
        } else { //else update probabilities proportional to quality
            while (iter.hasNext()) {
                Variation heuristic_i = iter.next();
                double newProb = pmin + (1 - probabilities.size() * pmin)
                        * (qualities.get(heuristic_i) / sum);
                probabilities.put(heuristic_i, newProb);
            }
        }
    }

    /**
     * Selection probabilities are updated
     * @param reward given to the heuristic
     * @param heuristic to be rewarded
     */
    @Override
    public void update(Reward reward, Variation heuristic) {
        qualities.put(heuristic, qualities.get(heuristic)+reward.getValue());
        super.checkQuality();
        updateProbabilities();
    }
}
