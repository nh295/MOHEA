///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package hh.hyperheuristics;
//
//import hh.nextheuristic.INextHeuristic;
//import hh.qualityhistory.HeuristicQualityHistory;
//import hh.rewarddefinition.IRewardDefinition;
//import hh.rewarddefinition.Reward;
//import hh.rewarddefinition.RewardDefinitionType;
//import hh.rewarddefinition.fitnessindicator.IBinaryIndicator;
//import hh.rewarddefinition.offspringparent.AbstractOffspringParent;
//import hh.rewarddefinition.offspringpopulation.AbstractOffspringPopulation;
//import hh.selectionhistory.HeuristicSelectionHistory;
//import hh.selectionhistory.IHeuristicSelectionHistory;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import jmetal.util.JMException;
//import org.moeaframework.algorithm.AbstractEvolutionaryAlgorithm;
//import org.moeaframework.core.EpsilonBoxDominanceArchive;
//import org.moeaframework.core.Initialization;
//import org.moeaframework.core.NondominatedPopulation;
//import org.moeaframework.core.ParallelPRNG;
//import org.moeaframework.core.Population;
//import org.moeaframework.core.Problem;
//import org.moeaframework.core.Selection;
//import org.moeaframework.core.Solution;
//import org.moeaframework.core.Variation;
//
///**
// * This is an indicator based Adpative operator selector. IBEA parts of the
// * algorithm are copied over from jmetal45
// *
// * @author Nozomi
// */
//public class HIBEA extends AbstractEvolutionaryAlgorithm implements IHyperHeuristic {
//
//    private static final long serialVersionUID = 3759178629236338535L;
//
//    /**
//     * The type of heuristic selection method
//     */
//    private final INextHeuristic heuristicSelector;
//
//    /**
//     * The Credit definition to be used that defines how much credit to receive
//     * for certain types of solutions
//     */
//    private final IRewardDefinition creditDef;
//
//    /**
//     * The history that stores all the heuristics selected by the hyper
//     * heuristics. History can be extracted by getSelectionHistory(). Used for
//     * analyzing the results to see the dynamics of heuristics selected
//     */
//    private IHeuristicSelectionHistory heuristicSelectionHistory;
//
//    /**
//     * The set of heuristics that the hyper heuristic is able to work with
//     */
//    private final Collection<Variation> heuristics;
//
//    /**
//     * The selection operator.
//     */
//    private final Selection selection;
//
//    /**
//     * The history of the heuristics' qualities over time. Used for analyzing
//     * the results to see the dynamics of the heuristic qualities
//     */
//    private HeuristicQualityHistory qualityHistory;
//
//    /**
//     * parallel purpose random generator
//     */
//    private final ParallelPRNG pprng;
//
//    /**
//     * Iteration count
//     */
//    private int iteration;
//
//    /**
//     * Name to id the hyper-heuristic
//     */
//    private String name;
//
//    /**
//     * Pareto Front
//     */
//    private NondominatedPopulation paretoFront;
//
//    /**
//     * The population contribution rewards from the previous iteration
//     */
//    private HashMap<Variation, Reward> prevPopContRewards;
//
//    /**
//     * The population size to maintain throughout a run
//     */
//    private final int populationSize;
//
//    /**
//     * The indicator to compute the fitness of individuals in the population
//     */
//    private final IBinaryIndicator indicator;
//
//    /**
//     * Stores the value of the indicator between each pair of solutions into the
//     * solution set
//     */
//    private List<List<Double>> indicatorValues_;
//
//    /**
//     * Fitness values
//     */
//    private List<Double> fitnessValues;
//
//    /**
//     * the maximum indicator value
//     */
//    private double maxIndicatorValue_;
//
//    /**
//     * The reference point to use for indicators such as R2
//     */
//    private Solution referencePoint;
//
//    /**
//     * Parameter in fitness function
//     */
//    private final double kappa;
//
//    /**
//     * The maximum objective values present in the population
//     */
//    private double[] maxObjValues;
//
//    /**
//     * The minimum objective values present in the population
//     */
//    private double[] minObjValues;
//
//    /**
//     * Creates an instance of a hyper heuristic e-MOEA
//     *
//     * @param problem the problem to solve
//     * @param population the type of population
//     * @param archive the type of archive to implement
//     * @param selection type of selection used to select higher fitness
//     * individuals
//     * @param initialization the method to initialize the population
//     * @param indicator that computes the fitness of each individual in the
//     * population
//     * @param heuristicSelector the heuristic selector to use in the
//     * hyper-heuristic
//     * @param creditDef the credit definition to score the performance of the
//     * low-level heuristics
//     */
//    public HIBEA(Problem problem, Population population,
//            EpsilonBoxDominanceArchive archive, Selection selection,
//            Initialization initialization, IBinaryIndicator indicator, double kappa, INextHeuristic heuristicSelector,
//            IRewardDefinition creditDef) {
//        super(problem, population, archive, initialization);
//
//        this.heuristics = heuristicSelector.getHeuristics();
//        this.selection = selection;
//        this.heuristicSelector = heuristicSelector;
//        this.creditDef = creditDef;
//        this.indicator = indicator;
//        this.kappa = kappa;
//        this.heuristicSelectionHistory = new HeuristicSelectionHistory(heuristics);
//        this.qualityHistory = new HeuristicQualityHistory(heuristics);
//        this.pprng = new ParallelPRNG();
//        this.iteration = 0;
//
//        //Initialize the stored pareto front
//        super.initialize();
//        this.paretoFront = new NondominatedPopulation(getPopulation());
//        populationSize = getPopulation().size();
//
//        //initialize the previous population contribution rewards to all zero for each heuristic
//        prevPopContRewards = new HashMap<>();
//        for (Variation heur : heuristics) {
//            prevPopContRewards.put(heur, new Reward(0, 0.0));
//        }
//
//        //initializes the fitness values of the initial population
//    }
//
//    @Override
//    public String getName() {
//        return name;
//    }
//
//    @Override
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    /**
//     * An iterations of this algorithm will select the next heuristic to be
//     * applied, select the parents for that heuristic and select one child to
//     * get evaluated. For heuristics that create more than one offspring, a
//     * random offspring will be selected with uniform probability
//     *
//     * Runs the IBEA algorithm for one evaluation
//     *
//     * @throws JMException
//     */
//    @Override
//    public void iterate() {
//        iteration++;
//
//        //if population meets its population size limit, check with archive to 
//        //see which solutions should remain in archive. Clear population for next generation
//        if (population.size() > populationSize) {
//            population.addAll(archive);
//            calculateFitness(population);
//            archive.clear();
//            archive.addAll(population);
//            while (archive.size() > populationSize) {
//                removeWorst(archive);
//            }
//            population.clear();
//        }
//
//        //select next heuristic
//        Variation heuristic = heuristicSelector.nextHeuristic();
//
//        //Choose parents
//        Solution[] parents = selection.select(heuristic.getArity(), archive);
//
//        //recombine parents
//        Solution[] children = heuristic.evolve(parents);
//
//        if (creditDef.getType() == RewardDefinitionType.OFFSPRINGPARENT) {
//            Solution refParent = parents[0];
//
//            for (Solution child : children) {
//                evaluate(child);
//                population.add(child);
//                double creditValue;
//                //credit definitions operating on population and archive does 
//                //NOT modify the population by adding the child to the population/archive
//                switch (creditDef.getOperatesOn()) {
//                    case PARENT:
//                        creditValue = ((AbstractOffspringParent) creditDef).compute(child, refParent, null, );
//                        break;
//                    case POPULATION:
//                        creditValue = ((AbstractOffspringParent) creditDef).compute(child, refParent, population, -1);
//                        break;
//                    default:
//                        throw new NullPointerException("Credit definition not "
//                                + "recognized. Used " + creditDef.getType() + ".");
//                }
//                archive.add(child);
//                heuristicSelector.update(new Reward(iteration, creditValue), heuristic);
//            }
//        } else if (creditDef.getType() == RewardDefinitionType.OFFSPRINGPOPULATION) {
//            for (Solution child : children) {
//                evaluate(child);
//                double creditValue;
//                //credit definitions operating on population and archive will 
//                //modify the population by adding the child to the population/
//                //archive. For computational efficiency (e.g. don't have to 
//                //compute dominance for reward computation and for population update)
//                switch (creditDef.getOperatesOn()) {
//                    case POPULATION:
//                        creditValue = ((AbstractOffspringPopulation) creditDef).compute(child, population);
//                        archive.add(child);
//                        break;
//                    case PARETOFRONT:
//                        creditValue = ((AbstractOffspringPopulation) creditDef).compute(child, paretoFront);
//                        archive.add(child);
//                        break;
//                    default:
//                        throw new NullPointerException("Credit definition not "
//                                + "recognized. Used " + creditDef.getType() + ".");
//                }
//                population.add(child);
//                heuristicSelector.update(new Reward(iteration, creditValue), heuristic);
//            }
//        } else if (creditDef.getType() == RewardDefinitionType.POPULATIONCONTRIBUTION) {
//            for (Solution child : children) {
//                evaluate(child);
//                child.setAttribute("iteration", new SerializableVal(this.getNumberOfEvaluations()));
//                child.setAttribute("heuristic", new SerializableVal(heuristic.toString()));
//                population.add(child);
//            }
//            HashMap<Variation, Reward> popContRewards;
//            switch (creditDef.getOperatesOn()) {
//                case POPULATION:
//
//                    break;
//                default:
//                    throw new NullPointerException("Credit definition not "
//                            + "recognized. Used " + creditDef.getType() + ".");
//            }
////            Iterator<Variation> iter = popContRewards.keySet().iterator();
////            while (iter.hasNext()) {
////                Variation operator = iter.next();
////                heuristicSelector.update(popContRewards.get(operator), operator);
////            }
//        } else {
//            throw new UnsupportedOperationException("RewardDefinitionType not recognized ");
//        }
////        heuristicSelectionHistory.add(heuristic);
////        updateCreditHistory();
//        updateQualityHistory();
//
//    }
//
//    /**
//     * reuses the previous population contribution rewards. This method updates
//     * the iteration coutner in the rewards from the previous iteration
//     *
//     * @return the rewards for each heuristic with the up to date iteration
//     * count
//     */
//    private HashMap<Variation, Reward> reusePrevPopContRewards() {
//        for (Variation heur : heuristics) {
//            Reward r = new Reward(iteration, prevPopContRewards.get(heur).getValue());
//            prevPopContRewards.put(heur, r);
//        }
//        return prevPopContRewards;
//    }
//
//    /**
//     * Updates the quality history every iteration for each heuristic according
//     * to the INextHeuristic class used
//     */
//    private void updateQualityHistory() {
//        HashMap<Variation, Double> currentQualities = heuristicSelector.getQualities();
//        for (Variation heuristic : heuristics) {
//            qualityHistory.add(heuristic, currentQualities.get(heuristic));
////            System.out.print(currentQualities.get(heuristic) + "\t");
//        }
////        System.out.println();
//    }
//
//    /**
//     * Returns the ordered history of heuristics that were selected
//     *
//     * @return The ordered history of heuristics that were selected
//     */
//    @Override
//    public IHeuristicSelectionHistory getSelectionHistory() {
//        return heuristicSelectionHistory;
//    }
//
//    /**
//     * gets the quality history stored for each heuristic in the hyper-heuristic
//     *
//     * @return
//     */
//    @Override
//    public HeuristicQualityHistory getQualityHistory() {
//        return qualityHistory;
//    }
//
//    /**
//     * Reset the hyperheuristic. Clear all selection history and the credit
//     * repository
//     */
//    @Override
//    public void reset() {
//        iteration = 0;
//        heuristicSelectionHistory.clear();
//        heuristicSelector.reset();
//        numberOfEvaluations = 0;
//        qualityHistory.clear();
//    }
//
//    @Override
//    public IRewardDefinition getCreditDefinition() {
//        return creditDef;
//    }
//
//    @Override
//    public INextHeuristic getNextHeuristicSupplier() {
//        return heuristicSelector;
//    }
//
////    /**
////     * calculates the hypervolume of that portion of the objective space that is
////     * dominated by individual a but not by individual b
////     */
////    double calcHypervolumeIndicator(jmetal.core.Solution p_ind_a,
////            jmetal.core.Solution p_ind_b,
////            int d,
////            double maximumValues[],
////            double minimumValues[]) {
////        double a, b, r, max;
////        double volume = 0;
////        double rho = 2.0;
////
////        r = rho * (maximumValues[d - 1] - minimumValues[d - 1]);
////        max = minimumValues[d - 1] + r;
////
////        a = p_ind_a.getObjective(d - 1);
////        if (p_ind_b == null) {
////            b = max;
////        } else {
////            b = p_ind_b.getObjective(d - 1);
////        }
////
////        if (d == 1) {
////            if (a < b) {
////                volume = (b - a) / r;
////            } else {
////                volume = 0;
////            }
////        } else {
////            if (a < b) {
////                volume = calcHypervolumeIndicator(p_ind_a, null, d - 1, maximumValues, minimumValues)
////                        * (b - a) / r;
////                volume += calcHypervolumeIndicator(p_ind_a, p_ind_b, d - 1, maximumValues, minimumValues)
////                        * (max - b) / r;
////            } else {
////                volume = calcHypervolumeIndicator(p_ind_a, p_ind_b, d - 1, maximumValues, minimumValues)
////                        * (max - b) / r;
////            }
////        }
////
////        return (volume);
////    }
//    /**
//     * This structure store the indicator values of each pair of elements
//     */
//    public void computeIndicatorValuesHD(Population population,
//            double[] maximumValues,
//            double[] minimumValues) {
//        NondominatedPopulation A, B;
//        // Initialize the structures
//        indicatorValues_ = new ArrayList<List<Double>>();
//        maxIndicatorValue_ = -Double.MAX_VALUE;
//
//        for (int j = 0; j < population.size(); j++) {
//            A = new NondominatedPopulation();
//            A.add(population.get(j));
//
//            List<Double> aux = new ArrayList<Double>();
//            for (int i = 0; i < population.size(); i++) {
//                B = new NondominatedPopulation();
//                B.add(population.get(i));
//
//                double value = indicator.computeWRef(B, A, referencePoint);
//
//                //Update the max value of the indicator
//                maxIndicatorValue_ = Math.max(maxIndicatorValue_, value);
//                aux.add(value);
//            }
//            indicatorValues_.add(aux);
//        }
//    } // computeIndicatorValues
//
//    /**
//     * Inserts the new solution into the population and updates the fitness of
//     * each solution in the population
//     *
//     * @param population to update
//     * @param newSoln to add to the population
//     */
//    private void updateFitness(Population population, Solution newSoln) {
//        population.add(newSoln);
//        //update min and max objective values
//        for (int obj = 0; obj < problem.getNumberOfObjectives(); obj++) {
//            double value = newSoln.getObjective(obj);
//            maxObjValues[obj] = Math.max(maxObjValues[obj], value);
//            minObjValues[obj] = Math.max(minObjValues[obj], value);
//        }
//    }
//
//    /**
//     * Calculate the fitness for the entire population. Used to initialize the
//     * fitness values of all the solutions in the population
//     */
//    private void calculateFitness(Population population) {
//        // Obtains the lower and upper bounds of the population
//        double[] maximumValues = new double[problem.getNumberOfObjectives()];
//        double[] minimumValues = new double[problem.getNumberOfObjectives()];
//        Arrays.fill(maximumValues, -Double.MAX_VALUE);
//        Arrays.fill(minimumValues, Double.MAX_VALUE);
//
//        for (Solution soln : population) {
//            for (int obj = 0; obj < problem.getNumberOfObjectives(); obj++) {
//                double value = soln.getObjective(obj);
//                maximumValues[obj] = Math.max(maximumValues[obj], value);
//                minimumValues[obj] = Math.max(minimumValues[obj], value);
//            }
//        }
//
//        computeIndicatorValuesHD(population, maximumValues, minimumValues);
//        fitnessValues = new ArrayList<>();
//        for (int pos = 0; pos < population.size(); pos++) {
//            double fitness = 0.0;
//            for (int i = 0; i < population.size(); i++) {
//                if (i != pos) {
//                    fitness += Math.exp((-1 * indicatorValues_.get(i).get(pos) / maxIndicatorValue_) / kappa);
//                }
//            }
//            fitnessValues.set(pos, fitness);
//        }
//    }
//
//    /**
//     * Update the fitness before removing an individual
//     *
//     * @param population to remove the worst individuals from
//     */
//    public void removeWorst(Population population) {
//        // Find the worst;
//        double worst = fitnessValues.get(0);
//        int worstIndex = 0;
//
//        for (int i = 1; i < fitnessValues.size(); i++) {
//            if (fitnessValues.get(i) > worst) {
//                worst = fitnessValues.get(i);
//                worstIndex = i;
//            }
//        }
//
//        // Update the population
//        for (int i = 0; i < population.size(); i++) {
//            if (i != worstIndex) {
//                double fitness = fitnessValues.get(i);
//                fitness -= Math.exp((-indicatorValues_.get(worstIndex).get(i) / maxIndicatorValue_) / kappa);
//                fitnessValues.set(i, fitness);
//            }
//        }
//
//        // remove worst from the indicatorValues list
//        indicatorValues_.remove(worstIndex); // Remove its own list
//        Iterator<List<Double>> it = indicatorValues_.iterator();
//        while (it.hasNext()) {
//            it.next().remove(worstIndex);
//        }
//
//        // remove the worst individual from the population
//        population.remove(worstIndex);
//    } // removeWorst
//
//    /**
//     * Dynamically adjusts reference point as described in Phan, D. H., &
//     * Suzuki, J. (2013). R2-IBEA: R2 indicator based evolutionary algorithm for
//     * multiobjective optimization. IEEE Congress on Evolutionary Computation,
//     * 1836–1845.
//     *
//     * @param population
//     */
//    private void getReferencePoint() {
//        double maxdiff = -Double.MAX_VALUE;
//        for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
//            maxdiff = Math.max(maxdiff, maxObjValues[i] - minObjValues[i]);
//        }
//        referencePoint = problem.newSolution();
//        for (int i = 0; i < referencePoint.getNumberOfObjectives(); i++) {
//            referencePoint.setObjective(i, minObjValues[i] - maxdiff);
//        }
//    }
//}
