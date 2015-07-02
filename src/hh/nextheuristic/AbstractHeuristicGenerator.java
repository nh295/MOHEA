/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.nextheuristic;

import hh.creditdefinition.Credit;
import hh.creditrepository.CreditRepository;
import hh.creditrepository.ICreditRepository;
import hh.heuristicgenerators.HeuristicSequence;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import org.moeaframework.core.Variation;


/**
 * This abstract implements the interface INextHeuristic. Classes that 
 * extend this abstract class are required to have some credit repository
 * @author nozomihitomi
 */
public abstract class AbstractHeuristicGenerator implements INextHeuristic{
    
    /**
     * Collection of building blocks to generate a heuristic
     */
    protected ArrayList<Variation> buildingBlocks;
    
    /**
     * Random number generator for selecting heuristics.
     */
    protected final Random random = new Random();
    
    /**
     * the number of building blocks available.
     */
    protected final int nBuildingBlocks;
    
    /**
     * Credit repository to store credits earned by heuristics.
     */
    protected ICreditRepository creditRepo;
    
    /**
     * The number of iterations that counts how many times nextHeuristic() has been called
     */
    private int iterations;
    
    /**
     * Constructor requires the set of building blocks to generate a heuristic
     * @param creditRepo the credit repository to be used
     * @param buildingBlocks the set of building blocks to generate a heuristic
     */
    public AbstractHeuristicGenerator(ICreditRepository creditRepo, Collection<Variation> buildingBlocks){
        this.creditRepo = creditRepo;
        this.buildingBlocks = new ArrayList(buildingBlocks);
        this.nBuildingBlocks = buildingBlocks.size();
    }
    
    /**
     * Generates a random heuristic of desired length from a random sequence of 
     * building blocks. Each building block is selected with a uniform probability.
     * @param num number of buildings blocks for desired heuristic
     * @return the randomly generated heuristic
     */
    protected HeuristicSequence getRandomHeuristic(int num){
        HeuristicSequence randHeuristic = new HeuristicSequence();
        if(num<=0){
            throw new IllegalArgumentException("number of buildings blocks to "
                    + "use to generate heuristic must be positive and nonzero");
        }
        for(int i=0;i<num;i++){
            int randInt = random.nextInt(nBuildingBlocks);
            randHeuristic.appendOperator(buildingBlocks.get(randInt));
        }
        incrementIterations();
        return randHeuristic;
    }
    
    @Override
    public HashMap<Variation, Credit> getAllCurrentCredits() {
        HashMap<Variation, Credit> out = new HashMap();
        for(Variation heuristic:creditRepo.getHeuristics()){
            out.put(heuristic, creditRepo.getSumCredit(getNumberOfIterations(),heuristic));
        }
        return out;
    }
    
    @Override
    public void update(Variation heuristic, Credit credit) {
        creditRepo.update(heuristic, credit);
    }
    
    @Override
    public void reset() {
        creditRepo.clear();
    }
    
    @Override
    public void update(ICreditRepository creditRepo) {
        this.creditRepo = creditRepo;
    }
    
    /**
     * Increments the number of times nextHeuristic() has been called by one
     */
    protected void incrementIterations(){
        iterations++;
    }
    
    /**
     * Returns the number of times nextHeuristic() has been called
     * @return the number of times nextHeuristic() has been called
     */
    @Override
    public int getNumberOfIterations(){
        return iterations;
    }
    
}

