/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.hyperheuristics;

import hh.nextheuristic.INextHeuristic;
import hh.qualityhistory.HeuristicQualityHistory;
import hh.rewarddefinition.IRewardDefinition;
import hh.rewarddefinition.Reward;
import hh.rewarddefinition.RewardDefinitionType;
import hh.rewarddefinition.offspringparent.AbstractOffspringParent;
import hh.rewarddefinition.offspringpopulation.AbstractOffspringPopulation;
import hh.rewarddefinition.populationcontribution.AbstractPopulationContribution;
import hh.selectionhistory.HeuristicSelectionHistory;
import hh.selectionhistory.IHeuristicSelectionHistory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import jmetal.core.SolutionSet;
import jmetal.util.JMException;
import jmetal.util.comparators.DominanceComparator;
import org.moeaframework.algorithm.AbstractEvolutionaryAlgorithm;
import org.moeaframework.core.EpsilonBoxDominanceArchive;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.ParallelPRNG;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Selection;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

/**
 * This is an indicator based Adpative operator selector. IBEA parts of the
 * algorithm are copied over from jmetal45
 *
 * @author Nozomi
 */
public class HIBEA extends AbstractEvolutionaryAlgorithm implements IHyperHeuristic {

    /**
     * This Solution is for IBEA. It extends Solution by just adding set and get
     * fitness methods
     */
    public class IBEASolution extends Solution {
        
        private double fitness;

        public IBEASolution(int numberOfVariables, int numberOfObjectives) {
            super(numberOfVariables, numberOfObjectives);
            fitness = -1.0;
        }
        
        /**
         * Sets the fitness of the individual
         * @param fitness 
         */
        public void setFitness(double fitness){
            this.fitness = fitness;
        }
        
        /**
         * Gets the fitness of the individual
         * @return fitness of the individual
         */
        public double getFitness(){
            return fitness;
        }
    }

    private static final long serialVersionUID = 3759178629236338535L;

    /**
     * The type of heuristic selection method
     */
    private final INextHeuristic heuristicSelector;

    /**
     * The Credit definition to be used that defines how much credit to receive
     * for certain types of solutions
     */
    private final IRewardDefinition creditDef;

    /**
     * The history that stores all the heuristics selected by the hyper
     * heuristics. History can be extracted by getSelectionHistory(). Used for
     * analyzing the results to see the dynamics of heuristics selected
     */
    private IHeuristicSelectionHistory heuristicSelectionHistory;

    /**
     * The set of heuristics that the hyper heuristic is able to work with
     */
    private final Collection<Variation> heuristics;

    /**
     * The selection operator.
     */
    private final Selection selection;

    /**
     * The history of the heuristics' qualities over time. Used for analyzing
     * the results to see the dynamics of the heuristic qualities
     */
    private HeuristicQualityHistory qualityHistory;

    /**
     * parallel purpose random generator
     */
    private final ParallelPRNG pprng;

    /**
     * Iteration count
     */
    private int iteration;

    /**
     * Name to id the hyper-heuristic
     */
    private String name;

    /**
     * Pareto Front
     */
    private NondominatedPopulation paretoFront;

    /**
     * The population contribution rewards from the previous iteration
     */
    private HashMap<Variation, Reward> prevPopContRewards;

    /**
     * The population size to maintain throughout a run
     */
    private final int populationSize;

    /**
     * Stores the value of the indicator between each pair of solutions into the
     * solution set
     */
    private List<List<Double>> indicatorValues_;

    /**
     *
     */
    private double maxIndicatorValue_;

    /**
     * Creates an instance of a hyper heuristic e-MOEA
     *
     * @param problem the problem to solve
     * @param population the type of population
     * @param archive the type of archive to implement
     * @param selection type of selection used to select higher fitness
     * individuals
     * @param initialization the method to initialize the population
     * @param heuristicSelector the heuristic selector to use in the
     * hyper-heuristic
     * @param creditDef the credit definition to score the performance of the
     * low-level heuristics
     */
    public HIBEA(Problem problem, Population population,
            EpsilonBoxDominanceArchive archive, Selection selection,
            Initialization initialization, INextHeuristic heuristicSelector,
            IRewardDefinition creditDef) {
        super(problem, population, archive, initialization);

        this.heuristics = heuristicSelector.getHeuristics();
        this.selection = selection;
        this.heuristicSelector = heuristicSelector;
        this.creditDef = creditDef;
        this.heuristicSelectionHistory = new HeuristicSelectionHistory(heuristics);
        this.qualityHistory = new HeuristicQualityHistory(heuristics);
        this.pprng = new ParallelPRNG();
        this.iteration = 0;

        //Initialize the stored pareto front
        super.initialize();
        this.paretoFront = new NondominatedPopulation(getPopulation());
        populationSize = getPopulation().size();

        //initialize the previous population contribution rewards to all zero for each heuristic
        prevPopContRewards = new HashMap<>();
        for (Variation heur : heuristics) {
            prevPopContRewards.put(heur, new Reward(0, 0.0));
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * An iterations of this algorithm will select the next heuristic to be
     * applied, select the parents for that heuristic and select one child to
     * get evaluated. For heuristics that create more than one offspring, a
     * random offspring will be selected with uniform probability
     *
     * Runs the IBEA algorithm for one evaluation
     *
     * @throws JMException
     */
    @Override
    public void iterate() {
        iteration++;

        //if population meets its population size limit, check with archive to 
        //see which solutions should remain in archive. Clear population for next generation
        if (population.size() > populationSize) {
            population.addAll(archive);
            calculateFitness(population);
            archive.clear();
            archive.addAll(population);
            while (archive.size() > populationSize) {
                removeWorst(archive);
            }
            population.clear();
        }

        //select next heuristic
        Variation heuristic = heuristicSelector.nextHeuristic();

        //Choose parents
        Solution[] parents = selection.select(heuristic.getArity(), archive);

        pprng.shuffle(parents);

        //recombine parents
        Solution[] children = heuristic.evolve(parents);

        if (creditDef.getType() == RewardDefinitionType.OFFSPRINGPARENT) {
            for (Solution child : children) {
                evaluate(child);

                population.add(child);
                double creditValue;
                //credit definitions operating on population and archive does 
                //NOT modify the population by adding the child to the population/archive
                switch (creditDef.getOperatesOn()) {
                    case PARENT:
                        creditValue = ((AbstractOffspringParent) creditDef).compute(child, refParent, null,);
                        break;
                    case POPULATION:
                        creditValue = ((AbstractOffspringParent) creditDef).compute(child, refParent, population,);
                        break;
                    default:
                        throw new NullPointerException("Credit definition not "
                                + "recognized. Used " + creditDef.getType() + ".");
                }
                archive.add(child);
                heuristicSelector.update(new Reward(iteration, creditValue), heuristic);
            }
        } else if (creditDef.getType() == RewardDefinitionType.OFFSPRINGPOPULATION) {
            for (Solution child : children) {
                evaluate(child);
                double creditValue;
                //credit definitions operating on population and archive will 
                //modify the population by adding the child to the population/
                //archive. For computational efficiency (e.g. don't have to 
                //compute dominance for reward computation and for population update)
                switch (creditDef.getOperatesOn()) {
                    case POPULATION:
                        creditValue = ((AbstractOffspringPopulation) creditDef).compute(child, population);
                        archive.add(child);
                        break;
                    case PARETOFRONT:
                        creditValue = ((AbstractOffspringPopulation) creditDef).compute(child, paretoFront);
                        archive.add(child);
                        break;
                    case ARCHIVE:
                        creditValue = ((AbstractOffspringPopulation) creditDef).compute(child, archive);
                        break;
                    default:
                        throw new NullPointerException("Credit definition not "
                                + "recognized. Used " + creditDef.getType() + ".");
                }
                population.add(child);
                heuristicSelector.update(new Reward(iteration, creditValue), heuristic);
            }
        } else if (creditDef.getType() == RewardDefinitionType.POPULATIONCONTRIBUTION) {
            for (Solution child : children) {
                evaluate(child);
                child.setAttribute("iteration", new SerializableVal(this.getNumberOfEvaluations()));
                child.setAttribute("heuristic", new SerializableVal(heuristic.toString()));
                population.add(child);
            }
            boolean archiveChanged = archive.addAll(children);
            HashMap<Variation, Reward> popContRewards;
            switch (creditDef.getOperatesOn()) {
                case PARETOFRONT:
                    boolean PFchanged = paretoFront.addAll(children); //updated only if reward def uses the PF
                    if (!PFchanged) {
                        popContRewards = reusePrevPopContRewards();
                    } else {
                        popContRewards = ((AbstractPopulationContribution) creditDef).compute(paretoFront, heuristics, this.getNumberOfEvaluations());
                        prevPopContRewards = popContRewards; //update prevPopContRewards for future iterations
                    }
                    break;
                case ARCHIVE:
                    if (!archiveChanged) {
                        popContRewards = reusePrevPopContRewards();
                    } else {
                        popContRewards = ((AbstractPopulationContribution) creditDef).compute(archive, heuristics, this.getNumberOfEvaluations());
                        prevPopContRewards = popContRewards; //update prevPopContRewards for future iterations
                    }
                    break;
                default:
                    throw new NullPointerException("Credit definition not "
                            + "recognized. Used " + creditDef.getType() + ".");
            }
            Iterator<Variation> iter = popContRewards.keySet().iterator();
            while (iter.hasNext()) {
                Variation operator = iter.next();
                heuristicSelector.update(popContRewards.get(operator), operator);
            }
        } else {
            throw new UnsupportedOperationException("RewardDefinitionType not recognized ");
        }
//        heuristicSelectionHistory.add(heuristic);
//        updateCreditHistory();
        updateQualityHistory();

    }

    /**
     * reuses the previous population contribution rewards. This method updates
     * the iteration coutner in the rewards from the previous iteration
     *
     * @return the rewards for each heuristic with the up to date iteration
     * count
     */
    private HashMap<Variation, Reward> reusePrevPopContRewards() {
        for (Variation heur : heuristics) {
            Reward r = new Reward(iteration, prevPopContRewards.get(heur).getValue());
            prevPopContRewards.put(heur, r);
        }
        return prevPopContRewards;
    }

    /**
     * Updates the quality history every iteration for each heuristic according
     * to the INextHeuristic class used
     */
    private void updateQualityHistory() {
        HashMap<Variation, Double> currentQualities = heuristicSelector.getQualities();
        for (Variation heuristic : heuristics) {
            qualityHistory.add(heuristic, currentQualities.get(heuristic));
//            System.out.print(currentQualities.get(heuristic) + "\t");
        }
//        System.out.println();
    }

    /**
     * Returns the ordered history of heuristics that were selected
     *
     * @return The ordered history of heuristics that were selected
     */
    @Override
    public IHeuristicSelectionHistory getSelectionHistory() {
        return heuristicSelectionHistory;
    }

    /**
     * gets the quality history stored for each heuristic in the hyper-heuristic
     *
     * @return
     */
    @Override
    public HeuristicQualityHistory getQualityHistory() {
        return qualityHistory;
    }

    /**
     * Reset the hyperheuristic. Clear all selection history and the credit
     * repository
     */
    @Override
    public void reset() {
        iteration = 0;
        heuristicSelectionHistory.clear();
        heuristicSelector.reset();
        numberOfEvaluations = 0;
        qualityHistory.clear();
    }

    @Override
    public IRewardDefinition getCreditDefinition() {
        return creditDef;
    }

    @Override
    public INextHeuristic getNextHeuristicSupplier() {
        return heuristicSelector;
    }

    /**
     * calculates the hypervolume of that portion of the objective space that is
     * dominated by individual a but not by individual b
     */
    double calcHypervolumeIndicator(jmetal.core.Solution p_ind_a,
            jmetal.core.Solution p_ind_b,
            int d,
            double maximumValues[],
            double minimumValues[]) {
        double a, b, r, max;
        double volume = 0;
        double rho = 2.0;

        r = rho * (maximumValues[d - 1] - minimumValues[d - 1]);
        max = minimumValues[d - 1] + r;

        a = p_ind_a.getObjective(d - 1);
        if (p_ind_b == null) {
            b = max;
        } else {
            b = p_ind_b.getObjective(d - 1);
        }

        if (d == 1) {
            if (a < b) {
                volume = (b - a) / r;
            } else {
                volume = 0;
            }
        } else {
            if (a < b) {
                volume = calcHypervolumeIndicator(p_ind_a, null, d - 1, maximumValues, minimumValues)
                        * (b - a) / r;
                volume += calcHypervolumeIndicator(p_ind_a, p_ind_b, d - 1, maximumValues, minimumValues)
                        * (max - b) / r;
            } else {
                volume = calcHypervolumeIndicator(p_ind_a, p_ind_b, d - 1, maximumValues, minimumValues)
                        * (max - b) / r;
            }
        }

        return (volume);
    }

    /**
     * This structure store the indicator values of each pair of elements
     */
    public void computeIndicatorValuesHD(SolutionSet solutionSet,
            double[] maximumValues,
            double[] minimumValues) {
        SolutionSet A, B;
        // Initialize the structures
        indicatorValues_ = new ArrayList<List<Double>>();
        maxIndicatorValue_ = -Double.MAX_VALUE;

        for (int j = 0; j < solutionSet.size(); j++) {
            A = new SolutionSet(1);
            A.add(solutionSet.get(j));

            List<Double> aux = new ArrayList<Double>();
            for (int i = 0; i < solutionSet.size(); i++) {
                B = new SolutionSet(1);
                B.add(solutionSet.get(i));

                int flag = (new DominanceComparator()).compare(A.get(0), B.get(0));

                double value = 0.0;
                if (flag == -1) {
                    value = -calcHypervolumeIndicator(A.get(0), B.get(0), problem.getNumberOfObjectives(), maximumValues, minimumValues);
                } else {
                    value = calcHypervolumeIndicator(B.get(0), A.get(0), problem.getNumberOfObjectives(), maximumValues, minimumValues);
                }

                //Update the max value of the indicator
                if (Math.abs(value) > maxIndicatorValue_) {
                    maxIndicatorValue_ = Math.abs(value);
                }
                aux.add(value);
            }
            indicatorValues_.add(aux);
        }
    } // computeIndicatorValues

    /**
     * Calculate the fitness for the individual at position pos
     */
    public void fitness(Population population, int pos) {
        double fitness = 0.0;
        double kappa = 0.05;

        for (int i = 0; i < population.size(); i++) {
            if (i != pos) {
                fitness += Math.exp((-1 * indicatorValues_.get(i).get(pos) / maxIndicatorValue_) / kappa);
            }
        }
        population.get(pos).setFitness(fitness);
    }

    /**
     * Calculate the fitness for the entire population.
  *
     */
    public void calculateFitness(Population population) {
        // Obtains the lower and upper bounds of the population
        double[] maximumValues = new double[problem.getNumberOfObjectives()];
        double[] minimumValues = new double[problem.getNumberOfObjectives()];

        for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
            maximumValues[i] = -Double.MAX_VALUE; // i.e., the minus maxium value
            minimumValues[i] = Double.MAX_VALUE; // i.e., the maximum value
        }

        for (int pos = 0; pos < population.size(); pos++) {
            for (int obj = 0; obj < problem.getNumberOfObjectives(); obj++) {
                double value = population.get(pos).getObjective(obj);
                if (value > maximumValues[obj]) {
                    maximumValues[obj] = value;
                }
                if (value < minimumValues[obj]) {
                    minimumValues[obj] = value;
                }
            }
        }

        computeIndicatorValuesHD(population, maximumValues, minimumValues);
        for (int pos = 0; pos < population.size(); pos++) {
            fitness(population, pos);
        }
    }

    /**
     * Update the fitness before removing an individual
     */
    public void removeWorst(Population population) {

        // Find the worst;
        double worst = ((IBEASolution)(population.get(0))).getFitness();
        int worstIndex = 0;
        double kappa = 0.05;

        for (int i = 1; i < population.size(); i++) {
            if ((IBEASolution)(population.get(i))).getFitness() > worst) {
                worst = population.get(i).getFitness();
                worstIndex = i;
            }
        }

        // Update the population
        for (int i = 0; i < population.size(); i++) {
            if (i != worstIndex) {
                double fitness = population.get(i).getFitness();
                fitness -= Math.exp((-indicatorValues_.get(worstIndex).get(i) / maxIndicatorValue_) / kappa);
                population.get(i).setFitness(fitness);
            }
        }

        // remove worst from the indicatorValues list
        indicatorValues_.remove(worstIndex); // Remove its own list
        Iterator<List<Double>> it = indicatorValues_.iterator();
        while (it.hasNext()) {
            it.next().remove(worstIndex);
        }

        // remove the worst individual from the population
        population.remove(worstIndex);
    } // removeWorst
}
