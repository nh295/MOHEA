/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.heuristicselectors;

import hh.rewarddefinition.Reward;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import org.moeaframework.core.Variation;

/**
 * Implements the fitness-rate-rank-based multiarmed bandit
 *
 * @author SEAK2
 */
public class FRRMAB extends AbstractMAB {

    /**
     * Sliding window to store FIR values
     */
    private final LinkedList<FIR> window;

    /**
     * Size of the sliding window
     */
    private final int windowSize;

    /**
     * Rank decay value
     */
    private final double d;

    private final OperatorRewardComparator comp;

    public FRRMAB(Collection<Variation> operators, double c, int windowSize, double d) {
        super(operators, c);
        this.window = new LinkedList<>();
        this.windowSize = windowSize;
        this.d = d;
        this.comp = new OperatorRewardComparator();
    }

    /**
     * This implementation inserts the reward in the FIR sliding window
     *
     * @param reward
     * @param heuristic
     */
    @Override
    public void update(Reward reward, Variation heuristic) {
        //update the sliding window
        window.addLast(new FIR(heuristic, reward));
        if (window.size() > windowSize) {
            window.removeFirst();
        }

        //update qualities
        HashMap<Variation, Double> rewardSums = computeRewardSum();
        HashMap<Variation, Double> FRR = computeFRR(rewardSums);
        for (Variation op : operators) {
            qualities.put(op, FRR.get(op));
        }
    }

    private HashMap<Variation, Double> computeRewardSum() {
        HashMap<Variation, Double> out = new HashMap<>();
        for (Variation op : operators) {
            out.put(op, 0.0);
        }
        //sum rewards
        for (FIR fir : window) {
            Variation op = fir.getOperator();
            out.put(op, out.get(op) + fir.value.getValue());
        }
        return out;
    }

    /**
     * Computes and returns the fitness rate rank FRR of each operator
     *
     * @param rewardSums
     * @return FRR values of each operator
     */
    private HashMap<Variation, Double> computeFRR(HashMap<Variation, Double> rewardSums) {
        //find rank of each operator
        ArrayList<OperatorRewardPair> rewardSumSorted = new ArrayList<>(operators.size());
        for (Variation op : operators) {
            rewardSumSorted.add(new OperatorRewardPair(op, rewardSums.get(op)));
        }
        //sort in descending order
        Collections.sort(rewardSumSorted, comp);

        // compute decay values
        HashMap<Variation, Double> decay = new HashMap<>();
        double sumDecay = 0;
        for (int i = 0; i < rewardSumSorted.size(); i++) {
            OperatorRewardPair orp = rewardSumSorted.get(i);
            double decay_i = Math.pow(d, i + 1) * orp.getReward();
            decay.put(orp.getOperator(), decay_i);
            sumDecay += decay_i;
        }

        //compute FRR
        HashMap<Variation, Double> out = new HashMap<>();
        for (Variation op : operators) {
            qualities.put(op, decay.get(op) / sumDecay);
        }
        return out;
    }

    /**
     * Data structure to store fitness improvement rate value and the operator
     * responsible for it
     */
    private class FIR {

        private final Variation operator;
        private final Reward value;

        public FIR(Variation operator, Reward value) {
            this.operator = operator;
            this.value = value;
        }

        /**
         * returns the operator responsible for the FIR
         *
         * @return
         */
        public Variation getOperator() {
            return operator;
        }

        /**
         * Returns the value of the FIR
         *
         * @return
         */
        public Reward getValue() {
            return value;
        }

    }

    /**
     * An ordered pair of the operator and a Reward
     */
    private class OperatorRewardPair {

        private final Variation operator;
        private final double reward;

        public OperatorRewardPair(Variation operator, double reward) {
            this.operator = operator;
            this.reward = reward;
        }

        public Variation getOperator() {
            return operator;
        }

        public double getReward() {
            return reward;
        }

    }

    /**
     * Compares operator reward pairs based on the reward value. Will sort in descending order.
     *
     * @return the value 0 if t1 is has the same reward value as t2; a value
     * greater than 0 if t1 has a reward value less than that of t2; and a value
     * less than 0 if t1 has a reward value greater than that of t2.
     */
    private class OperatorRewardComparator implements Comparator<OperatorRewardPair> {

        @Override
        public int compare(OperatorRewardPair t1, OperatorRewardPair t2) {
            return -Double.compare(t1.getReward(), t2.getReward());
        }

    }

}
