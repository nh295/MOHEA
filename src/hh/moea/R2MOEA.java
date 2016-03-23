/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.moea;

import hh.creditassignment.fitnessindicator.DoubleComparator;
import hh.creditassignment.fitnessindicator.R2Indicator;
import hh.creditassignment.fitnessindicator.SortedLinkedList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.moeaframework.algorithm.AbstractEvolutionaryAlgorithm;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.ParallelPRNG;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

/**
 * R2 MOEA is steady state indicator based algorithm developed by Diaz et al. It
 * is an indicator based algorithm that uses nondominated sorting, then R2
 * contribution to achieve a total order of the individuals in the population
 *
 *
 * References:
 * <p>
 * <ol>
 * <li>
 * "A ranking method based on the R2 indicator for many-objective optimization"
 * 2013 IEEE Congress on Evolutionary Computation
 * </ol>
 *
 * @author SEAK2
 */
public class R2MOEA extends AbstractEvolutionaryAlgorithm {

    /**
     * The r2rank comparator.
     */
    private final R2RankComparator rankComparator;

    /**
     * The r2contribution comparator.
     */
    private final R2ContributionComparator contributionComparator;

    private final ArrayList<WtVector> wtVecs;

    /**
     * The variation operator.
     */
    private final Variation variation;

    /**
     * The utopia point (point not dominated by any individual in the
     * population)
     */
    private Solution utopia;

    /**
     * Only maintain the min objectives and the max objectives
     */
    private Solution minObjs;
    private Solution maxObjs;

    /**
     * stores all the objective values in sorted order to get bounds
     */
    private ArrayList<SortedLinkedList<Double>> sortedObjs;

    /**
     * the number of children to create in each generation
     */
    private final int childNumber;

    /**
     * parallel purpose random generator
     */
    private final ParallelPRNG pprng;

    /**
     * Constructs a new IBEA instance.
     *
     * @param problem the problem
     * @param childNumer the number of children to create in each generation
     * @param numVecs the number of uniformly spaced vectors to use
     * @param archive the external archive; or {@code null} if no external
     * archive is used
     * @param initialization the initialization operator
     * @param variation the variation operator
     */
    public R2MOEA(Problem problem, int childNumer, int numVecs, NondominatedPopulation archive,
            Initialization initialization, Variation variation) {
        super(problem, new Population(), archive, initialization);
        this.childNumber = childNumer;
        this.variation = variation;
        this.rankComparator = new R2RankComparator();
        this.contributionComparator = new R2ContributionComparator();
        this.pprng = new ParallelPRNG();
        this.wtVecs = initializeWts(problem.getNumberOfObjectives(), numVecs);
    }

    @Override
    protected void initialize() {
        super.initialize();

        computeUtopia(population);
        computeBounds(population);

        for (Solution soln : population) {
            soln.setAttribute("contribution", 0.0);
            soln.setAttribute("rank", 0);
        }
        System.out.println("Finished initializing. Running...");
    }

    @Override
    protected void iterate() {
        Population offspring = new Population();

        while (offspring.size() < childNumber) {
            Solution[] parents = selectParents(variation.getArity(),
                    population);
            Solution[] children = variation.evolve(parents);
            offspring.addAll(children);
        }

        evaluateAll(offspring);
        population.addAll(offspring);

        //Update the utopia point with added offspring
        for (Solution child : offspring) {
            updateBoundsInsert(child);
            updateMinMax();
            updateUtopia(child);
            updateMinMax();
        }

        ArrayList<Integer> solutionsToRemove = fastR2Sorting(offspring);
        for (Integer index : solutionsToRemove) {
            updateBoundsRemove(population.get(index));
            population.remove(index);
        }
    }

    /**
     * Computes the contribution of a solution to the R2 indicator. Solutions
     * that do not contribute to the R2 indicator get a higher rank.
     *
     * @return the population indices of the solution to remove from the
     * population
     */
    private ArrayList<Integer> fastR2Sorting(Population offspringPopulation) {
        ArrayList<Integer> indicesUnrankedSolutions = new ArrayList<>(population.size());
        ArrayList<Solution> normalizedPopulation = new ArrayList<>(population.size());
        int k=0;
        for (Solution soln : population) {
            soln.setAttribute("contribution", 0.0);
            indicesUnrankedSolutions.add(k);
            k++;
            normalizedPopulation.add(normalizeObjectives(soln));
        }

        int rank = 1;
        while (!indicesUnrankedSolutions.isEmpty()) {
            HashSet<Integer> contributed = new HashSet();
            for (WtVector wt : wtVecs) {
                double popUtility = Double.POSITIVE_INFINITY;
                int aIndex = -1;
                for (int i = 0; i < indicesUnrankedSolutions.size(); i++) {
                    double tmpUtility = solnUtility(wt, normalizedPopulation.get(indicesUnrankedSolutions.get(i)), utopia);
                    if (tmpUtility < popUtility) {
                        popUtility = tmpUtility;
                        aIndex = i;
                    }
                }
                Solution a = population.get(indicesUnrankedSolutions.get(aIndex));
                a.setAttribute("contribution", popUtility + (double) a.getAttribute("contribution"));
                contributed.add(aIndex);
            }
            //remove from unranked solutions starting from largest index. This will keep unranked solutions ordered
            ArrayList<Integer> indicesToRemove = new ArrayList<>(contributed);
            Collections.sort(indicesToRemove);
            Collections.reverse(indicesToRemove);
            Iterator iter = indicesToRemove.iterator();
            while (iter.hasNext()) {
                int indexToRemove = (int) iter.next();
                Solution soln = population.get(indicesUnrankedSolutions.get(indexToRemove));
                soln.setAttribute("rank", rank);
                indicesUnrankedSolutions.remove(indexToRemove);
            }
            rank++;
        }

        ArrayList<Integer> leastContributors = new ArrayList<>();
        //remove as many individuals as new offspring
        while (leastContributors.size() < offspringPopulation.size()) {
            rank--;
            //find solutions with highest rank
            ArrayList<Integer> highestRankedSolutions = new ArrayList<>();
            for (int i = 0; i < population.size(); i++) {
                if ((int) population.get(i).getAttribute("rank") == rank) {
                    highestRankedSolutions.add(i);
                    if (leastContributors.size() < offspringPopulation.size()) {
                        return leastContributors;
                    }
                }
            }

            //find solution with highest rank and lowest contribution to R2 value
            if (highestRankedSolutions.size() == 1) {
                leastContributors.add(highestRankedSolutions.get(0));
                if (leastContributors.size() < offspringPopulation.size()) {
                    return leastContributors;
                }
            } else {
                double minContribution = Double.POSITIVE_INFINITY;
                for (Integer index : highestRankedSolutions) {
                    double contribution = (double) population.get(index).getAttribute("contribution");
                    if (contribution < minContribution) {
                        minContribution = contribution;
                        leastContributors.add(index);
                    }
                }
            }
        }
        return leastContributors;
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
        for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
            solnUtil = Math.max(solnUtil, vec.get(i) * Math.abs(solution.getObjective(i) - refPt.getObjective(i)));
        }
        return solnUtil;
    }

    private void updateMinMax() {
        minObjs = problem.newSolution();
        maxObjs = problem.newSolution();
        for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
            minObjs.setObjective(i, sortedObjs.get(i).getFirst());
            maxObjs.setObjective(i, sortedObjs.get(i).getLast());
        }
    }

    /**
     * Finds the utopia point in the population
     *
     * @param population the population to find the utopia point
     * @return the utopia point
     */
    private Solution computeUtopia(Population population) {
        utopia = problem.newSolution();
        for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
            double min = Double.MAX_VALUE;
            for (Solution soln : population) {
                min = Math.min(min, soln.getObjective(i));
            }
            utopia.setObjective(i, min);
        }
        return utopia;
    }

    /**
     * Updates the utopia based on the new incoming solution. This method
     * assumes that the utopia point never deteriorates
     *
     * @param offspring new solution entering the population
     * @return the updated utopia point
     */
    private Solution updateUtopia(Solution offspring) {
        for (int i = 0; i < offspring.getNumberOfObjectives(); i++) {
            utopia.setObjective(i, Math.min(utopia.getObjective(i), offspring.getObjective(i)));
        }
        return utopia;
    }

    /**
     * Normalizes the objective vector of a given individual and copies over
     * rank and contribution attributes
     *
     * @param solution
     * @return
     */
    private Solution normalizeObjectives(Solution solution) {
        Solution normalizedObjs = solution.copy();
        for (int i = 0; i < solution.getNumberOfObjectives(); i++) {
            double lowBound = minObjs.getObjective(i);
            double upBound = maxObjs.getObjective(i);
            normalizedObjs.setObjective(i, (solution.getObjective(i) - lowBound) / (upBound - lowBound));
        }
        return normalizedObjs;
    }

    //Computes the bounds on population
    private void computeBounds(Population pop) {
        sortedObjs = new ArrayList<>(pop.get(0).getNumberOfObjectives());
        for (int i = 0; i < pop.get(0).getNumberOfObjectives(); i++) {
            ArrayList<Double> objs = new ArrayList(pop.size());
            for (Solution soln : pop) {
                objs.add(soln.getObjective(i));
            }
            sortedObjs.add(new SortedLinkedList<>(objs, new DoubleComparator()));
        }
    }

    //updates the bounds based on a solution exiting the population
    private void updateBoundsRemove(Solution removedSoln) {
        for (int i = 0; i < removedSoln.getNumberOfObjectives(); i++) {
            int index = sortedObjs.get(i).binaryFind(removedSoln.getObjective(i));
            sortedObjs.get(i).remove(index);
        }
    }

    //updates the bounds based on a solution exiting the population
    private void updateBoundsInsert(Solution newSolution) {
        for (int i = 0; i < newSolution.getNumberOfObjectives(); i++) {
            sortedObjs.get(i).add(newSolution.getObjective(i));
        }
    }

    /**
     * This method is a binary tournament selection of parents based on the
     * fitness values (i.e. R2 contribution rank)
     *
     * @param arity
     * @param population
     * @return
     */
    private Solution[] selectParents(int arity, Population population) {
        Solution[] result = new Solution[arity];

        for (int i = 0; i < arity; i++) {
            Solution winner = population.get(pprng.nextInt(population.size()));

            for (int j = 1; j < 2; j++) {
                Solution candidate = population
                        .get(pprng.nextInt(population.size()));

                int flag = rankComparator.compare(winner, candidate);

                //first compare using rank. then use contribution
                if (flag > 0) {
                    winner = candidate;
                } else if (flag == 0) {
                    flag = contributionComparator.compare(winner, candidate);
                    if (flag > 0) {
                        winner = candidate;
                    }
                }
            }
            result[i] = winner;
        }

        return result;
    }

    /**
     * This comparator compares the R2 rank computed from fast R2 sorting
     */
    private class R2RankComparator implements Comparator<Solution> {

        @Override
        public int compare(Solution t, Solution t1) {
            int rank1 = (int) t.getAttribute("rank");
            int rank2 = (int) t.getAttribute("rank");
            return Integer.compare(rank1, rank2);
        }
    }

    /**
     * This comparator compares the R2 contribution
     */
    private class R2ContributionComparator implements Comparator<Solution> {

        @Override
        public int compare(Solution t, Solution t1) {
            double ca1 = (double) t.getAttribute("contribution");
            double ca2 = (double) t.getAttribute("contribution");
            return Double.compare(ca1, ca2);
        }
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
    private ArrayList<WtVector> initializeWts(int numObj, int numVecs) {

        String dataFileName;
        dataFileName = "W" + numObj + "D_"
                + numVecs + ".dat";

        ArrayList<WtVector> out = new ArrayList<>(numVecs);
        try {
            // Open the file
            FileInputStream fis = new FileInputStream("weight" + File.separator
                    + dataFileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

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
                out.add(new WtVector(wts));
                aux = br.readLine();
            }
            br.close();
        } catch (IOException | NumberFormatException e) {
            System.out
                    .println("initUniformWeight: failed when reading for file: "
                            + "weight" + File.separator + dataFileName);
            Logger.getLogger(R2Indicator.class.getName()).log(Level.SEVERE, null, e);
        }
        return out;
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
