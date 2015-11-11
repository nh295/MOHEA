/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.rewarddefinition.fitnessindicator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;

/**
 *
 * @author nozomihitomi
 */
public class R2Indicator implements IIndicator {

    protected ArrayList<WtVector> wtVecs;

    /**
     * Constructor to initialize the weight vectors
     *
     * @param numObjs number of objectives
     * @param numVecs number of vectors
     */
    public R2Indicator(int numObjs, int numVecs) {

        wtVecs = new ArrayList<>();
        initializeWts(numObjs, numVecs);
    }

    @Override
    public List<Double> computeContributions(NondominatedPopulation pop, Solution refPt) {
        List<Double> contribution = new ArrayList<>(pop.size());

        try {
            NondominatedPopulation pop2 = pop.clone();
            for (int i = 0; i < pop.size(); i++) {
                Solution removed = pop2.get(0); //take the solution at the head of the population
                pop2.remove(0);
                contribution.add(computeContribution(pop, pop2, refPt));
                pop2.forceAddWithoutCheck(removed); //put solution back at the tail of the population

            }
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(R2Indicator.class.getName()).log(Level.SEVERE, null, ex);
        }

        return contribution;
    }

    @Override
    public double computeContribution(NondominatedPopulation pop, NondominatedPopulation popWOSolution, Solution refPt) {
        double valWSoln = 0;
        double valWoSoln = 0;
        for (WtVector vecs : wtVecs) {
            valWoSoln += popUtility(vecs, popWOSolution, refPt);
            valWSoln += popUtility(vecs, pop, refPt);
        }
        return (valWSoln - valWoSoln) / wtVecs.size();
    }

    /**
     * TODO this is really slow! In this implementation the order of the inputs
     * matter. formula based on Phan, D. H., & Suzuki, J. (2013). R2-IBEA: R2
     * indicator based evolutionary algorithm for multiobjective optimization.
     * IEEE Congress on Evolutionary Computation, 1836–1845.
     * doi:10.1109/CEC.2013.6557783
     *
     * @param solnA
     * @param solnB
     * @return
     */
    @Override
    public double compute(Solution solnA, Solution solnB, Solution refPt) {
        double valA = 0.0;
        double valB = 0.0;
        for (WtVector vec : wtVecs) {
            valA += solnUtility(vec, solnA, refPt);
            valB += Math.min(solnUtility(vec, solnA, refPt), solnUtility(vec, solnB, refPt));
        }
        return (valA / wtVecs.size()) - (valB / wtVecs.size());
    }

    /**
     * Returns the minimum value over all the solution utilities wrt to a weight
     * vector
     *
     * @param vec weight vector
     * @param pop
     * @param refPt reference point
     * @return the utility of the nondominated population
     */
    protected double popUtility(WtVector vec, NondominatedPopulation pop, Solution refPt) {
        double popUtil = Double.NEGATIVE_INFINITY;
        for (Solution solution : pop) {
            popUtil = Math.min(popUtil, solnUtility(vec, solution, refPt));
        }
        return popUtil;
    }

    /**
     * Computes the utility of a solution wrt to a weight vector using a
     * Tchebycheff function. Tchebycheff function: u_w(z) = -max{w_j*|z'_j -
     * z_j|} where w_j is the jth component of the weight vector, z' is the
     * reference point and z is the objective value.
     *
     * @param vec weight vector
     * @param solution
     * @param refPt reference point
     * @return utility of a solution wrt to a weight vectorq
     */
    private double solnUtility(WtVector vec, Solution solution, Solution refPt) {
        double solnUtil = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < solution.getNumberOfObjectives(); i++) {
            solnUtil = Math.max(solnUtil, vec.get(i) * Math.abs(solution.getObjective(i) - refPt.getObjective(i)));
        }
        return 1.0 * solnUtil;
    }

    /**
     * Used when want weights for more than 2 dimensions
     *
     * @param numObj
     * @param numVecs
     */
    private void initializeWts(int numObj, int numVecs) {
        // creates full factorial matrix. Code is based on 2013a Matlab 
        //fullfact(levels). Eliminate rows with sum != the number of vectors.
        int numExp = (int) Math.pow(numVecs, numObj);
        int[][] experiments = new int[numExp][numObj];

        int ncycles = numExp;

        for (int k = 0; k < numObj; k++) {
            int numLevels4kthFactor = numVecs;
            int nreps = numExp / ncycles;
            ncycles = ncycles / numLevels4kthFactor;
            int[] settingReps = new int[nreps * numLevels4kthFactor];
            int index = 0;
            for (int j = 0; j < numLevels4kthFactor; j++) {
                for (int i = 0; i < nreps; i++) {
                    settingReps[index] = j;
                    index++;
                }
            }
            index = 0;
            for (int j = 0; j < ncycles; j++) {
                for (int i = 0; i < settingReps.length; i++) {
                    experiments[index][k] = settingReps[i];
                    index++;
                }
            }
        }

        //Find valid row vectors (ones that add up to numVecs) 
        for (int i = 0; i < numExp; i++) {
            double sum = 0.0;
            for (int j = 0; j < numObj; j++) {
                sum += experiments[i][j];
            }
            if (sum == numVecs - 1) {
                double[] wts = new double[numObj];
                for (int k = 0; k < numObj; k++) {
                    wts[k] = ((double) experiments[i][k]) / ((double) (numVecs - 1));
                }
                wtVecs.add(new WtVector(wts));
            }
        }
    }

    @Override
    public String toString() {
        return "BIR2";
    }

    protected class WtVector {

        /**
         * Weights for vector
         */
        private final double[] weights;

        public WtVector(double[] weights) {
            this.weights = weights;
        }

        public double get(int i) {
            return weights[i];
        }
    }
}
