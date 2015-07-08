/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.heuristicselectors;

import hh.creditaggregation.CreditAggregator;
import hh.creditaggregation.ICreditAggregationStrategy;
import hh.creditdefinition.Credit;
import hh.creditrepository.CreditHistoryRepository;
import hh.creditrepository.ICreditRepository;
import hh.nextheuristic.AbstractHeuristicSelector;
import hh.selectionhistory.HeuristicSelectionHistory;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.moeaframework.core.Variation;

/**
 * Based on the adaptive operator selection from
 *
 * "DaCosta, L., & Fialho, A. (2008). Adaptive operator selection with dynamic
 * multi-armed bandits. Genetic and Evolutionary Computation Conference."
 *
 *
 * @author nozomihitomi
 */
public class DMAB extends AbstractHeuristicSelector {

    /**
     * Coefficient to control exploration vs exploitation of low-level
     * heuristics
     */
    private final double beta;

    /**
     * History to keep count of how many times a heuristic has been played
     */
    private HeuristicSelectionHistory heuristicSelectionHistory;

    private HashMap<Variation, Arm> arms;

    /**
     * PH test tolerance parameter
     */
    private double delta;

    /**
     * PH test threshold
     */
    private double lambda;

    /**
     * Sums/averages the credits stored in a credit history
     */
    private CreditAggregator crediAggregator;

    /**
     * Constructor requires a credit repository type: only ICreditRepository
     * with ICreditHistory makes sense for implementing DMAB.
     *
     * @param heuristics
     * @param beta Coefficient to control exploration vs exploitation of
     * low-level heuristics
     * @param delta Tolerance parameter for PH test
     * @param lambda Threshold parameter for PH test
     */
    public DMAB(Collection<Variation> heuristics, double beta, double delta, double lambda) {
        super(heuristics);
        this.beta = beta;
        this.crediAggregator = new CreditAggregator();

        heuristicSelectionHistory = new HeuristicSelectionHistory(heuristics);
        arms = new HashMap();
        Iterator<Variation> iter = heuristics.iterator();
        while (iter.hasNext()) {
            arms.put(iter.next(), new Arm(delta, lambda));
        }
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
        if (heuristicSelectionHistory.getTotalSelectionCount() == 0) {
            vari = getRandomHeuristic(heuristics);
        } else {
            vari = argMax(heuristics);
        }
        incrementIterations();
        return vari;
    }

    /**
     * This function is Upper Confidence Bound used in multi-armed bandit
     * policies
     *
     * @param heuristic the heuristic to be evaluated
     * @return the value resulting from the evaluation of the heuristic
     * @throws NoSuchMethodException
     */
    @Override
    protected double function2maximize(Variation heuristic) throws NoSuchMethodException {
        int numPlayed = heuristicSelectionHistory.getSelectedTimes(heuristic);
        int totalPlayCount = heuristicSelectionHistory.getTotalSelectionCount();
        return arms.get(heuristic).avg + beta * Math.sqrt(Math.log10(numPlayed) / totalPlayCount);
    }

    /**
     * Clears credit repository, selection history, and information stored in
     * each arm on credit deviations
     */
    @Override
    public final void reset() {
        heuristicSelectionHistory.clear();
        Iterator<Variation> iter = heuristics.iterator();
        while (iter.hasNext()) {
            arms.get(iter.next()).reset();
        }
    }

    /**
     * Updates the information on each arm and performs the PH test upon update
     * for each heuristic that was just rewarded
     *
     * @param creditRepo
     * @param creditAgg
     */
    @Override
    public void update(ICreditRepository creditRepo, ICreditAggregationStrategy creditAgg) {
        Collection<Variation> heuristicsRewarded = creditRepo.getLastRewardedHeuristic();
        Iterator<Variation> rewardIter = heuristicsRewarded.iterator();
        while (rewardIter.hasNext()) {
            Variation heuristic = rewardIter.next();
            //update the arm and execute PH test
            boolean PHtest = arms.get(heuristic).updateArm(creditRepo.getLatestCredit(heuristic),
                    crediAggregator.mean(getNumberOfIterations(), ((CreditHistoryRepository) creditRepo).getHistory(heuristic)));
            if (PHtest) {
                //if PH test is true, reset all counters, credits, and arms
                reset();
            }
        }
    }

    /**
     * An arm represents a heuristic. It maintains information on the deviations
     * in the credits received. It is responsible for executing the PH test that
     * detects significant changes in performance.
     *
     * PH test is implemented as "DaCosta, L., & Fialho, A. (2008). Adaptive
     * operator selection with dynamic multi-armed bandits. Genetic and
     * Evolutionary Computation Conference."
     */
    private class Arm {

        /**
         * Average deviation in the received credits
         */
        private double avgDev = 0;

        /**
         * Maximum deviation observed in the received credits
         */
        private double maxDev = 0;

        /**
         * Average credit earned by the heuristic
         */
        private double avg = 0;

        /**
         * Tolerance parameter for PH test
         */
        private final double delta;

        /**
         * Threshold parameter for PH test
         */
        private final double lambda;

        /**
         * Each arm requires parameters for the PH test
         *
         * @param delta Tolerance parameter for PH test
         * @param lambda Threshold parameter for PH test
         */
        public Arm(double delta, double lambda) {
            reset();
            this.delta = delta;
            this.lambda = lambda;
        }

        /**
         * Use after every play of an arm. This method updates the average
         * deviation and maximum deviation, and then executes the PH test. If
         * there has been a significant deviation (i.e. a significant change in
         * the arm's performance) then the PH test is positive (depends on
         * lambda).
         *
         * @param receivedCredit the credit received in this iteration
         * @param averageCredit the average of all credits including the credit
         * received this iteration
         * @return true if PH test detects change
         */
        public boolean updateArm(Credit receivedCredit, Credit averageCredit) {
            avg = averageCredit.getValue();
            avgDev = avgDev + (avg - receivedCredit.getValue() + delta);
            maxDev = Math.max(maxDev, avgDev);
            boolean PHtest = ((maxDev - avgDev) > lambda);
            return PHtest;
        }

        private void reset() {
            avgDev = 0;
            maxDev = 0;
            avg = 0;
        }
    }

    @Override
    public String toString() {
        return "DMAB";
    }
}
