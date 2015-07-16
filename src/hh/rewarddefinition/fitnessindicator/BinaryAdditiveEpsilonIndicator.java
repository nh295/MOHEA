/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.rewarddefinition.fitnessindicator;

import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;

/**
 * Computation based on Zitzler, E., Thiele, L., Laumanns, M., Fonseca, C. M., &
 * Da Fonseca, V. G. (2003). Performance assessment of multiobjective
 * optimizers: An analysis and review. IEEE Transactions on Evolutionary
 * Computation, 7(2), 117–132. doi:10.1109/TEVC.2003.810758
 *
 * @author nozomihitomi
 */
public class BinaryAdditiveEpsilonIndicator implements IBinaryIndicator {

    /**
     * Computes the additive &epsilon;-indicator for the specified solution
     * sets. The order of the inputs matters. While not necessary, the
     * approximation and reference sets should be normalized. Returns
     * {@code Double.POSITIVE_INFINITY} if the approximation set is empty.
     *
     * @param popA solution set 1
     * @param popB solution set 2 (can be reference population)
     * @param refPt additive epsilon indicator does not need a reference point
     * @return the additive &epsilon;-indicator value
     */
    @Override
    public double compute(NondominatedPopulation popA, NondominatedPopulation popB, Solution refPt) {
        double eps_i = 0.0;
        for (int i = 0; i < popB.size(); i++) {
            Solution solution1 = popB.get(i);
            double eps_j = Double.POSITIVE_INFINITY;

            for (int j = 0; j < popA.size(); j++) {
                Solution solution2 = popA.get(j);
                eps_j = Math.min(eps_j, compute(solution1, solution2,refPt));
            }
            eps_i = Math.max(eps_i, eps_j);
        }
        return eps_i;
    }

    /**
     * 
     * @param solnA
     * @param solnB
     * @param refPt additive epsilon indicator does not need a reference point
     * @return 
     */
    @Override
    public double compute(Solution solnA, Solution solnB, Solution refPt) {
        int numObjs = solnA.getNumberOfObjectives();
        double eps_k = 0.0;
        for (int k = 0; k < numObjs; k++) {
            eps_k = Math.max(eps_k, solnB.getObjective(k)
                    - solnA.getObjective(k));
        }
        return eps_k;
    }

    /**
     * Use if population A is to be compared with a reference population.
     * @param popA
     * @param popRef
     * @param refPt additive epsilon indicator does not need a reference point
     * @return 
     */
    @Override
    public double computeWRef(NondominatedPopulation popA, NondominatedPopulation popRef, Solution refPt) {
        return compute(popA, popRef,refPt);
    }

    @Override
    public String toString() {
        return "BIAE";
    }

}
