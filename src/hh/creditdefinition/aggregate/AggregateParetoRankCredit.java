/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.creditdefinition.aggregate;

import hh.creditdefinition.Credit;
import hh.creditdefinition.immediate.*;
import hh.creditrepository.CreditRepository;
import java.util.Collection;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

/**
 * This method counts the number of credits a heuristic is responsible for that 
 * lies within the specified rank. 
 * @author nozomihitomi
 */
public class AggregateParetoRankCredit extends ImmediateParetoRankCredit implements IAggregateCredit{
    
    private AggregateParetoFrontCredit aggPFcredit;
    
    /**
     * Constructor to specify the credits that are assigned when a solution is 
     * at or within the maximum rank to still receive credit
     * @param withinRank Credit received if a new solution is at or within the maximum rank with respect to the population
     * @param outsideRank Credit received if a new solution is not within the maximum rank with respect to the population
     * @param minRank The maximum rank a solution can be to still receive credit
     * 
     */
    public AggregateParetoRankCredit(double withinRank,double outsideRank,int minRank) {
        super(withinRank, outsideRank,minRank);
        //by giving AggregateParetoFrontCredit a 1,0 score we can count how many
        //solutions per rank a heuristic is responsible for
        aggPFcredit = new AggregateParetoFrontCredit(withinRank, 0.0);
    }
    
    protected double compute(Population population, Variation heuristic,int iteration){
        NondominatedPopulation ndpop = new NondominatedPopulation(population);
        double sumCredits =0;
        int rank = 0;
        while(rank<=minRank){
            sumCredits+=aggPFcredit.compute(ndpop, heuristic,iteration)*rankCredit(rank);
            population.removeAll(ndpop);
            ndpop = new NondominatedPopulation(population);
            rank++;
        }
        
        if(sumCredits>0)
            return sumCredits;
        else 
            return outsideRank;
    }
    
    @Override
    public String toString() {
        return "AggregateParetoRankCredit";
    }
    
    @Override
    public boolean isImmediate() {
       return false;
    }
    
    @Override
    public CreditRepository compute(Population population, Collection<Variation> heuristics,int iteration) {
        Population pop = new Population(population);
        CreditRepository creditRepo = new CreditRepository(heuristics);
        for(Variation heuristic:heuristics){
            creditRepo.update(heuristic, new Credit(-1,compute(pop,heuristic,iteration)));
        }
        return creditRepo;
    }

    
}
