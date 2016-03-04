/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.heuristicselectors;

import hh.creditassigment.Credit;
import java.util.Collection;
import java.util.HashMap;
import org.moeaframework.core.Variation;


/**
 * Selects heuristics based on probability which is proportional to the 
 * heuristics credits. Each heuristic gets selected with a minimum probability
 * of pmin. If current credits in credit repository becomes negative, zero 
 * credit is re-assigned to that heuristic. For the first iteration, heuristics
 * are selected with uniform probability.
 * @author nozomihitomi
 */
public class ProbabilityMatching extends RouletteWheel {
    
    /**
     * Alpha is the adaptation rate
     */
    private final double alpha;

    /**
     * Constructor to initialize probability map for selection
     * @param heuristics from which to select from 
     * @param alpha The adaptive rate
     * @param pmin The minimum probability for a heuristic to be selected
     */
    public ProbabilityMatching(Collection<Variation> heuristics, double alpha,double pmin) {
        super(heuristics,pmin);
        this.alpha = alpha;
        qualities = new HashMap<>();
        reset();
    }
    
    @Override
    public String toString() {
        return "ProbabilityMatching";
    }
    
    /**
     * Updates the quality of the heuristic based on the last reward received by
     * the heuristic. Only those heuristics who received a reward will be
     * updated. The update rule is Q(t+1) = (1-alpha)Q(t)+ R. If the quality
     * becomes negative, it is reset to 0.0. Only updates those heuristics that
     * were just rewarded.
     *
     * @param reward given to the heuristic
     * @param heuristic to be rewarded
     */
    @Override
    public void update(Credit reward, Variation heuristic) {
        double newQuality = (1-alpha)*qualities.get(heuristic) + reward.getValue();
        qualities.put(heuristic, newQuality);
        super.checkQuality();
        super.updateProbabilities();
    }
    
    
}
