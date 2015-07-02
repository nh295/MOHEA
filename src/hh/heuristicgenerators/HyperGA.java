/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.heuristicgenerators;

import hh.creditaggregation.ICreditAggregationStrategy;
import hh.creditdefinition.Credit;
import hh.creditrepository.AbstractCreditHistory;
import hh.creditrepository.CreditRepository;
import hh.heuristicPopulation.HeuristicIndividual;
import hh.nextheuristic.AbstractHeuristicGenerator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Stack;
import org.moeaframework.core.NondominatedSortingPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

/**
 * HyperGA is based on Cowling, P., & Kendall, G. (2002). An investigation of a 
 * hyperheuristic genetic algorithm applied to a trainer scheduling problem. 
 * In Proceedings of the 2002 Congress on Evolutionary Computation. 
 * CEC’02 (Cat. No.02TH8600) (Vol. 2, pp. 1185–1190). IEEE.
 * 
 * Also includes the guided operators from 
 * Han, L., & Kendall, G. (2003). Guided operators for a hyper-heuristic 
 * genetic algorithm. AI 2003: Advances in Artificial Intelligence.
 * 
 * Has adaptive chromosome length and adaptive mutation and crossover rates
 * 
 * Original HyperGA is a single-point algorithm. This has been modified to be 
 * applicable to population-based algorithms. Instead of all chromosomes being 
 * applied to the same solution, each chromosome is applied to a different 
 * solution pulled from the population
 * 
 * @author Nozomi
 */
public class HyperGA extends AbstractHeuristicGenerator {
    private final int keepN;
    private final int populationSize;
    private final int origChromosomeLength;
    private double mutationRate;
    private double crossoverRate;
    private double averageHeuristicSequenceLength;
    private int heuristicIndex;
    private ArrayList<Double> avgFitnessHistory;
    private NondominatedSortingPopulation population;
    private final int thresholdGen;
    private final double origMutationRate;
    private final double origCrossRate;
    private HeuristicSequence lastHeuristicSelected;
    
    /**
     * The credit history to be used for an individual. Keeps track of credits earned over time
     */
    private AbstractCreditHistory creditHistory;
    
    /**
     * The method to weight credits earned over time.
     */
    private ICreditAggregationStrategy aggregateStrategy;
    
    /**
     * Creates a new population that is empty.
     * @param heuristics the building blocks to create a sequence of heuristics
     * @param chromosomeLength length of initial chromosome length
     * @param thresholdGen the number of generations allowed without improvement before mutation and crossover rates change
     * @param keepN the number of best chromosomes to keep for the next generation
     * @param populationSize desired size of population
     * @param mutationRate initial mutation rate
     * @param crossoverRate initial crossover rate
     * @param creditHistory The credit history to be used for an individual. Keeps track of credits earned over time
     * @param aggregateStrategy The method to weight credits earned over time.
     */
    public HyperGA(Collection<Variation> heuristics,int chromosomeLength,
            int thresholdGen, int keepN, int populationSize,  double mutationRate,
            double crossoverRate,AbstractCreditHistory creditHistory, ICreditAggregationStrategy aggregateStrategy) {
        super(new CreditRepository(heuristics),heuristics);
        this.keepN = keepN;
        this.populationSize = populationSize;
        this.origChromosomeLength = chromosomeLength;
        this.mutationRate = mutationRate;
        this.origMutationRate = mutationRate;
        this.crossoverRate = crossoverRate;
        this.origCrossRate = crossoverRate;
        this.thresholdGen = thresholdGen;
        this.creditHistory = creditHistory;
        this.aggregateStrategy = aggregateStrategy;
        population = new NondominatedSortingPopulation();
        heuristicIndex=0;
        avgFitnessHistory = new ArrayList();
        generateRandomPopulation();
    }
    
    /**
     * Generates a population of random HeuristcIndividuals each containing its 
     * own sequence of heuristics.
     */
    private void generateRandomPopulation(){
        for(int i=0;i<populationSize;i++){
            HeuristicIndividual ind = new HeuristicIndividual(1, 1, 
                    getRandomHeuristic(origChromosomeLength),creditHistory,aggregateStrategy);
            population.add(ind);
        }
        averageHeuristicSequenceLength = (double)origChromosomeLength;
    }
    
    /**
     * Selects the next sequence of heuristics from the population of 
     * HeuristicIndividual. A queue of heuristic sequences is made, and the 
     * next one is selected until all sequences have been selected once, and 
     * once only. If there are no more sequences that have not been selected, 
     * the next generation of chromosomes is created
     * @return 
     */
    @Override
    public HeuristicSequence nextHeuristic(){
        HeuristicSequence out;
        if(heuristicIndex<population.size()){
            out = ((HeuristicIndividual)population.get(heuristicIndex)).getSequence();
        }else{
            int avgFitnessCheck = updateAndCheckFitnessImprovement(computeAverageFitness());
            updateMutCrossRate(avgFitnessCheck);
            nextGeneration(keepN);
            heuristicIndex = 0;
            out = ((HeuristicIndividual)population.get(heuristicIndex)).getSequence();
        }
        heuristicIndex++;
        lastHeuristicSelected = out;
        return out; 
    }
    
    /**
     * After applying all the chromosomes in the population, the fitness of a 
     * chromosome is based on the amount of credits that each operator in the
     * chromosome has. The fitness for the chromosome is the sum of the credits 
     * of each operator in the chromosome
     */
    private double computeAverageFitness(){
        double sumPopulationFitness = 0;
        Iterator<Solution> iter = population.iterator();
        
        while(iter.hasNext()){
            double sumChromoFitness = 0;
            HeuristicIndividual chromo = (HeuristicIndividual)iter.next();
            for(Variation buildingBlock: chromo.getSequence().getSequence()){
                sumChromoFitness+=creditRepo.getCurrentCredit(buildingBlock).getValue();
            }
            chromo.setObjective(0,sumChromoFitness);
            sumPopulationFitness+=sumChromoFitness;
        }
        
        return sumPopulationFitness/population.size();
    }
    
    /**
     * Generates the next generation of chromosomes using the mutation, 
     * crossover, and guided operators. Keeps the top n best chromosomes
     * @param keepN the top n chromosomes to keep from previous generation
     */
    private void nextGeneration(int keepN){
        NondominatedSortingPopulation newPopulation = new NondominatedSortingPopulation(population);
        newPopulation.prune(keepN);
        NondominatedSortingPopulation crossOffspring = crossover(population);
        NondominatedSortingPopulation mutOffspring = mutate(crossOffspring);
        
        newPopulation.addAll(mutOffspring);
        population = newPopulation;
        population.prune(populationSize);
        updateAverageHeuristicSequenceLength();
    }
    
    /**
     * Computes the average heuristic sequence length of the current population
     */
    private void updateAverageHeuristicSequenceLength(){
        Iterator<Solution> iter = population.iterator();
        int sum = 0;
        while(iter.hasNext()){
            sum+=((HeuristicIndividual)iter.next()).getSequence().getLength();
        }
        averageHeuristicSequenceLength = sum/population.size();
    }
    
    /**
     * Select the parent chromosomes for crossover and crosses them using the guided operators
     */
    private NondominatedSortingPopulation crossover(NondominatedSortingPopulation pop){
        NondominatedSortingPopulation crossedPop = new NondominatedSortingPopulation();
        Iterator<Solution> iter = pop.iterator();
        ArrayList<HeuristicSequence> crossoverList = new ArrayList();
        while(iter.hasNext()){
            if(random.nextDouble()<=crossoverRate) //selection for crossover. if selected GAIndividual gets inserted into crossoverList
                crossoverList.add(((HeuristicIndividual)iter.next()).getSequence());
        }
        Collections.shuffle(crossoverList); //shuffle up the entries
        Stack<HeuristicSequence> crossoverStack = new Stack();
        crossoverStack.addAll(crossoverList);
        while(crossoverStack.size()>1){ //if there is an odd number of chromosomes to cross, don't do the last one in the stack
            ArrayList<HeuristicSequence> offspring = new ArrayList();
            if(random.nextBoolean()){
                offspring.addAll(bestBestCrossover(crossoverStack.pop(), crossoverStack.pop()));
            }else{
                offspring.addAll(onePointCrossover(crossoverStack.pop(), crossoverStack.pop()));
            }
            
            Iterator<HeuristicSequence> offspringIter = offspring.iterator();
            while(offspringIter.hasNext()){
                crossedPop.add(new HeuristicIndividual(1,1,offspringIter.next(),creditHistory,aggregateStrategy));
            }
        }
        return crossedPop;
    }
    
    /**
     * Selects the chromosomes to mutate and mutates them according to the guided operators 
     */
    private NondominatedSortingPopulation mutate(NondominatedSortingPopulation pop){
        NondominatedSortingPopulation mutatedPop = new NondominatedSortingPopulation();
        Iterator<Solution> iter = pop.iterator();
        while(iter.hasNext()){
            if(random.nextDouble()<=mutationRate){
                HeuristicSequence offspring;
                HeuristicSequence ind = ((HeuristicIndividual)iter.next()).getSequence();
                if(ind.getLength()>averageHeuristicSequenceLength){
                    offspring = removeWorstMutation(ind);
                }else{
                    offspring = insertGoodMutation(ind);
                }
                mutatedPop.add(new HeuristicIndividual(1, 1, offspring,creditHistory,aggregateStrategy));
            }
        }
        return mutatedPop;
    }
    
    private ArrayList<HeuristicSequence> bestBestCrossover(HeuristicSequence mother, HeuristicSequence father){
        //find the best group of genes in random chromosome
        int[] indicesMom = getStartEndIndexOfGroup(mother,0);
        int[] indicesDad = getStartEndIndexOfGroup(father,0);
        
        //splice the genes into their parts
        HeuristicSequence firstThirdChild1 = new HeuristicSequence();
        HeuristicSequence lastThirdChild1 = new HeuristicSequence();
        HeuristicSequence firstThirdChild2 = new HeuristicSequence();
        HeuristicSequence lastThirdChild2 = new HeuristicSequence();
        HeuristicSequence momsBest = new HeuristicSequence();
        HeuristicSequence dadsBest = new HeuristicSequence();

        for(int i=0;i<mother.getLength();i++){
            if(i<indicesMom[0])
                firstThirdChild1.appendOperator(mother.get(i));
            else if (i>indicesMom[1])
                lastThirdChild1.appendOperator(mother.get(i));
            else
                momsBest.appendOperator(mother.get(i));
        }
        for(int i=0;i<father.getLength();i++){
            if(i<indicesDad[0])
                firstThirdChild2.appendOperator(father.get(i));
            else if (i>indicesDad[1])
                lastThirdChild2.appendOperator(father.get(i));
            else
                dadsBest.appendOperator(father.get(i));
        }
        
        //appendOperator up all the bits to finish crossover
        HeuristicSequence child1 = new HeuristicSequence();
        child1.appendAllOperators(firstThirdChild1.getSequence());
        child1.appendAllOperators(dadsBest.getSequence());
        child1.appendAllOperators(lastThirdChild1.getSequence());

        HeuristicSequence child2 = new HeuristicSequence();
        child2.appendAllOperators(firstThirdChild2.getSequence());
        child2.appendAllOperators(momsBest.getSequence());
        child2.appendAllOperators(lastThirdChild2.getSequence());
        
        ArrayList<HeuristicSequence> offspring = new ArrayList();
        offspring.add(child1);
        offspring.add(child2);
        
        return offspring;
    }
    
    private ArrayList<HeuristicSequence> onePointCrossover(HeuristicSequence mother, HeuristicSequence father){
        //find the shorter of the two parents
        int diff = mother.getLength() - father.getLength();
        int xpoint;
        int longerLength;
        if(diff<=0){
            if((mother.getLength()-1)<=0)
                System.out.println();
            xpoint = random.nextInt(mother.getLength()-1); //minus 1 because don't want to just exchange the whole chromosome
            longerLength = father.getLength();
        }else{
            if((father.getLength()-1)<=0)
                System.out.println();
            xpoint =  random.nextInt(father.getLength()-1);
            longerLength = mother.getLength();
        }
        
        HeuristicSequence child1 = new HeuristicSequence();
        HeuristicSequence child2 = new HeuristicSequence();
        
        for(int i=0;i<longerLength;i++){
            if(i<xpoint){
                child1.appendOperator(mother.get(i));
                child2.appendOperator(father.get(i));
            }else{
                if(i<father.getLength())
                    child1.appendOperator(father.get(i));
                if(i<mother.getLength())
                    child2.appendOperator(mother.get(i));
            }
        }
        ArrayList<HeuristicSequence> offspring = new ArrayList();
        offspring.add(child1);
        offspring.add(child2);
        return offspring;
    }
    
    /**
     * Inserts the best group of genes from a randomly selected chromosome to a random point of the desired chromosome
     * @param parent
     * @return 
     */
    private HeuristicSequence insertGoodMutation(HeuristicSequence parent){
        if(parent.getLength()<=0)
            System.out.print("arg");
        int insertionPoint = random.nextInt(parent.getLength());
        HeuristicSequence firstHalf = new HeuristicSequence();
        HeuristicSequence secondHalf = new HeuristicSequence();
        for(int i=0; i<parent.getLength();i++){ //splice the chromosome to get it ready for insertion
            if(i<insertionPoint)
                firstHalf.appendOperator(parent.get(i));
            else
                secondHalf.appendOperator(parent.get(i));
        }
        HeuristicIndividual randInd = (HeuristicIndividual)population.get(random.nextInt(population.size())); //random chromosome
        HeuristicSequence randHeuristicSequence = randInd.getSequence();
        
        //find the best group of genes in random chromosome
        int[] indices = getStartEndIndexOfGroup(randHeuristicSequence,0);
        HeuristicSequence goodGene = new HeuristicSequence();
        for(int i=0;i<randHeuristicSequence.getLength();i++){
            if(i>=indices[0]&&i<=indices[1]){//if index is part of good gene
                goodGene.appendOperator(randHeuristicSequence.get(i));
            }
        }
        
        HeuristicSequence newHeuristicSequence = new HeuristicSequence();
        newHeuristicSequence.appendAllOperators(firstHalf);
        newHeuristicSequence.appendAllOperators(goodGene);
        newHeuristicSequence.appendAllOperators(secondHalf);
        
        return newHeuristicSequence;
    }
    
    /**
     * finds the longest gene in the chromosome that doesn't contribute to any progress. Removes that gene from the chromosome
     * @param parent
     * @return 
     */
    private HeuristicSequence removeWorstMutation(HeuristicSequence parent){
        int[] indices = getStartEndIndexOfGroup(parent,1);
        HeuristicSequence oldHeuristicSequence = parent;
        HeuristicSequence newHeuristicSequence = new HeuristicSequence();
        for(int i=0;i<oldHeuristicSequence.getLength();i++){
            if(i<indices[0]||i>indices[1]){//if index is not part of bad gene
                newHeuristicSequence.appendOperator(oldHeuristicSequence.get(i));
            }
        }
        
        //if all operators were bad do one bit mutation
        if(newHeuristicSequence.isEmpty()){
            newHeuristicSequence = oneBitMutation(newHeuristicSequence);
        }
        return newHeuristicSequence;
    }
    
    public HeuristicSequence oneBitMutation(HeuristicSequence parent){
        int randIndex = random.nextInt(parent.getLength());
        ArrayList<Variation> heuristics = new ArrayList(creditRepo.getHeuristics());
        HeuristicSequence mutant = new HeuristicSequence();
        Iterator<Variation> iter = parent.getSequence().iterator();
        int ind = 0;
        while(iter.hasNext()){
            if(ind == randIndex){
                iter.next();
                mutant.appendOperator(heuristics.get(randIndex));
            }else 
                mutant.appendOperator(iter.next());
        }
        
        return mutant;
    }
    
    /**
     * A method to find the start and end indices of a good or bad gene
     * @param ind
     * @param bestWorstmode set mode to 0 for a good gene, 1 for a bad gene
     * @return an array of indices. First index is the start of the good or bad gene. The second is the last index of the good or bad gene. Return index is inclusive
     */
    private int[] getStartEndIndexOfGroup(HeuristicSequence chromosome,int bestWorstmode){
        if(bestWorstmode==0){ //find the good part
            HeuristicSequence bestGene = new HeuristicSequence();
            int lastIndexOfBestGene = 0;
            HeuristicSequence challengerGene = new HeuristicSequence();
            for(int i=0;i<chromosome.getLength();i++){
                Variation heuristic = chromosome.get(i);
                if (creditRepo.getCurrentCredit(heuristic).getValue()>0){ //yes improvement
                    challengerGene.appendOperator(heuristic);
                }else{
                    //check to see which gene is the longest one
                    if(bestGene.getLength()<challengerGene.getLength()){
                        lastIndexOfBestGene = i-1;
                        bestGene = new HeuristicSequence(challengerGene);
                        challengerGene.clear();
                    }
                }
            }
            //check again at end
            if(bestGene.getLength()<challengerGene.getLength()){
                lastIndexOfBestGene = chromosome.getLength()-1;
                bestGene = new HeuristicSequence(challengerGene);
                challengerGene.clear();
            }
            int startIndexOfGoodGene = -1;
            if(bestGene.getLength()>0)
                startIndexOfGoodGene = lastIndexOfBestGene - bestGene.getLength()+1;
            return new int[]{startIndexOfGoodGene,lastIndexOfBestGene};
        }else if(bestWorstmode==1){ //find the worst part
            HeuristicSequence worstGene = new HeuristicSequence();
            int lastIndexOfWorstGene = 0;
            HeuristicSequence challengerGene = new HeuristicSequence();
            for(int i=0;i<chromosome.getLength();i++){
                Variation heuristic = chromosome.get(i);
                if (creditRepo.getCurrentCredit(heuristic).getValue()<=0){ //yes improvement
                    challengerGene.appendOperator(heuristic);
                }else{
                    //check to see which gene is the longest one
                    if(worstGene.getLength()<challengerGene.getLength()){
                        lastIndexOfWorstGene = i-1;
                        worstGene = new HeuristicSequence(challengerGene);
                        challengerGene.clear();
                    }
                }
            }
            //check again at end
            if(worstGene.getLength()<challengerGene.getLength()){
                lastIndexOfWorstGene = chromosome.getLength()-1;
                worstGene = new HeuristicSequence(challengerGene);
                challengerGene.clear();
            }
            int startIndexOfGoodGene = -1;
            if(worstGene.getLength()>0)
                startIndexOfGoodGene = lastIndexOfWorstGene - worstGene.getLength()+1;
            return new int[]{startIndexOfGoodGene,lastIndexOfWorstGene};
        }else throw new UnsupportedOperationException("No such mode is supported: " + bestWorstmode);
    }
    /**
     * Checks the improvement in average fitness value. If the average fitness 
     * shows no improvement returns -1, if the average fitness shows 
     * improvement returns 1. If there are not enough generations to compute the check, return 0; 
     * @param currentAvgFitness
     * @return 
     */
    private int updateAndCheckFitnessImprovement(double currentAvgFitness) {
        avgFitnessHistory.add(currentAvgFitness);
        //The threshold number of generations that experiences 
        //no improvements. 3 is used in Han et al        
        if(avgFitnessHistory.size()>thresholdGen){ 
            avgFitnessHistory.remove(0);
        }
        
        //check to see if there are enough generations to compute check. IF not enough, return 0
        for(Double avg: avgFitnessHistory){
            if(avg==null)
                return 0;
        }
        
        for(int i=1;i<avgFitnessHistory.size();i++){
            if((avgFitnessHistory.get(i)-avgFitnessHistory.get(i-1))>0)
                return 1;
        }
        
        return -1;
    }

    /**
     * Updates the crossover and mutation rate if there are or are not improvements to the average fitness function.
     * If the input is 1 average fitness has improved. Increase crossover rate, decrease mutation rate
     * If the input is 0 do nothing
     * If the input is -1 average fitness has not improved. Decrease crossover rate, increase mutation rate
     * @param fitnessCheck 
     */
    private void updateMutCrossRate(int fitnessCheck) {
        if(fitnessCheck == 1){
            mutationRate = mutationRate/2;
            crossoverRate = (crossoverRate+1)/2;
        }else if(fitnessCheck ==-1){
            mutationRate = (mutationRate+1)/2;
            crossoverRate = crossoverRate/2;
        }
    }
    
    @Override 
    public void update(Variation heuristic, Credit credit) {
        if(!lastHeuristicSelected.equals(heuristic))
            throw new IllegalArgumentException("Input Variation " + heuristic + " is not the most recently selected Variation");
        ((HeuristicIndividual)population.get(heuristicIndex-1)).updateCredit(credit);
    }

    @Override
    public String toString() {
        return "HyperGA";
    }
    
    @Override
    public void reset(){
        this.mutationRate = origMutationRate;
        this.crossoverRate = origCrossRate;
        population = new NondominatedSortingPopulation();
        generateRandomPopulation();
        heuristicIndex=0;
        avgFitnessHistory = new ArrayList();
    }
}

