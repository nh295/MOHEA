/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.creditassignment.fitnessindicator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
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
     * Index of the solution that minimizes the population utility
     */
    private int[] minInd1;
    /**
     * Value of the smallest population utility
     */
    private double[] minPopUtil;

    /**
     * Index of the solution that achieves the 2nd smallest the population
     * utility
     */
    private int[] minInd2;

    /**
     * Value of the 2nd smallest population utility
     */
    private double[] minPopUtil2;

    /**
     * Constructor to initialize the weight vectors
     *
     * @param numObjs number of objectives
     * @param numVecs number of vectors
     */
    public R2Indicator(int numObjs, int numVecs) throws IOException {
        wtVecs = new ArrayList<>();
        initializeWts(numObjs, numVecs);
    }

    @Override
    public List<Double> computeContributions(NondominatedPopulation pop, Solution refPt) {
        computeContributors(pop, refPt);
        Double[] contributionsWPt = new Double[pop.size()];
        Double[] contributionsWoPt = new Double[pop.size()];
        Arrays.fill(contributionsWPt, 0.0);
        Arrays.fill(contributionsWoPt, 0.0);
        
        for(int i=0; i<pop.size();i++){
            for(int j=0; j<minInd1.length;j++){
                contributionsWPt[i]+=minPopUtil[j];
                if(minInd1[j]!=i)
                    contributionsWoPt[i]+=minPopUtil[j];
                else
                    contributionsWoPt[i]+=minPopUtil2[j];
            }
        }

        ArrayList out = new ArrayList(pop.size());
        for (int i=0; i<pop.size();i++) {
            out.add(i, (contributionsWoPt[i]-contributionsWPt[i]) / wtVecs.size()); 
        }
        return out;
    }

    /**
     * This method is a fast R2 contributor computation based on a combiniation
     * of Díaz-Manríquez, Alan, Gregorio Toscano-Pulido, Carlos A Coello Coello,
     * and Ricardo Landa-Becerra. 2013. “A Ranking Method Based on the R2
     * Indicator for Many-Objective Optimization.” 2013 IEEE Congress on
     * Evolutionary Computation, CEC 2013: 1523–1530.
     * doi:10.1109/CEC.2013.6557743. and Naujoks, B., N. Beume, and M. Emmerich.
     * 2005. “Multi-Objective Optimisation Using S-Metric Selection: Application
     * to Three-Dimensional Solution Spaces.” 2005 IEEE Congress on Evolutionary
     * Computation 2: 1282–1289. doi:10.1109/CEC.2005.1554838.
     *
     * Computes the R2 contribution by keeping the minimum and the 2nd minimum
     * values of the population utility for R2
     *
     * @param pop
     * @param refPt
     */
    private void computeContributors(NondominatedPopulation pop, Solution refPt) {
        minInd1 = new int[wtVecs.size()];
        Arrays.fill(minInd1, -1);
        minInd2 = new int[wtVecs.size()];
        Arrays.fill(minInd2, -1);
        minPopUtil = new double[wtVecs.size()];
        Arrays.fill(minPopUtil, Double.POSITIVE_INFINITY);
        minPopUtil2 = new double[wtVecs.size()];
        Arrays.fill(minPopUtil2, Double.POSITIVE_INFINITY);

        int vecInd = 0;
        for (WtVector vec : wtVecs) {
            for (int i = 0; i < pop.size(); i++) {
                double solnUtil = solnUtility(vec, pop.get(i), refPt);
                if (solnUtil < minPopUtil[vecInd]) {
                    minPopUtil2[vecInd] = minPopUtil[vecInd];
                    minPopUtil[vecInd] = solnUtil;
                    minInd2[vecInd] = minInd1[vecInd];
                    minInd1[vecInd] = i;
                } else if (solnUtil < minPopUtil2[vecInd]) {
                    minPopUtil2[vecInd] = solnUtil;
                    minInd2[vecInd] = i;
                }
            }

            vecInd++;
        }
    }

    @Override
    public double computeContribution(NondominatedPopulation pop, Solution offspring, Solution refPt) {

        //Create a nondominated popualtion without the offspring
        int offspringInd = -1;
        for (int i = 0; i < pop.size(); i++) {
            if (Arrays.equals(pop.get(i).getObjectives(), offspring.getObjectives())) {
                offspringInd=i;
            }
        }
        computeContributors(pop, refPt);
        double contributionsWPt = 0;
        double contributionsWoPt = 0;
        for(int j=0; j<minInd1.length;j++){
                contributionsWPt+=minPopUtil[j];
                if(minInd1[j]!=offspringInd)
                    contributionsWoPt+=minPopUtil[j];
                else
                    contributionsWoPt+=minPopUtil2[j];
            }
        double out = (contributionsWoPt-contributionsWPt)/wtVecs.size();
        if(out<0){
            throw new IllegalStateException("Negative reward even though solution added");
        }
        return out;
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
            double solnAUtil = solnUtility(vec, solnA, refPt);
            valA += solnAUtil;
            valB += Math.min(solnAUtil, solnUtility(vec, solnB, refPt));
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
        double popUtil = Double.POSITIVE_INFINITY;
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
        return solnUtil;
    }

    /**
     * Method from jmetal to load the weights for problems meeting certain
     * criteria such as number of objectives and population size. Returns true
     * if the weights can be loaded and false if the weights data is
     * unavailable.
     *
     * @param numObj
     * @param numVecs
     */
    private void initializeWts(int numObj,int numVecs) throws IOException {
        
        String dataFileName;
        dataFileName = "W" + numObj + "D_"
                + numVecs + ".dat";

        try {
            // Open the file
            FileInputStream fis = new FileInputStream("weight" + File.separator
                    + dataFileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

            wtVecs = new ArrayList<>(numVecs);
            int j = 0;
            String aux = br.readLine();
            while (aux != null) {
                StringTokenizer st = new StringTokenizer(aux);
                j = 0;
                double[] wts = new double[numObj];
                while (st.hasMoreTokens()) {
                    double value = (new Double(st.nextToken())).doubleValue();
                    wts[j] = value;
                    j++;
                }
                wtVecs.add(new WtVector(wts));
                aux = br.readLine();
            }
            br.close();
        } catch (IOException | NumberFormatException e) {
            System.out
                    .println("initUniformWeight: failed when reading for file: "
                            + "weight" + File.separator + dataFileName);
            Logger.getLogger(R2Indicator.class.getName()).log(Level.SEVERE, null, e);
            throw e;
        }
    }

    @Override
    public String toString() {
        return "R2";
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
