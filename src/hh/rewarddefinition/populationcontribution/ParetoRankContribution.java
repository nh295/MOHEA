/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.rewarddefinition.populationcontribution;

import hh.rewarddefinition.offspringpopulation.OffspringParetoRank;
import hh.rewarddefinition.Reward;
import hh.creditrepository.CreditRepository;
import hh.rewarddefinition.RewardDefinedOn;
import java.util.Collection;
import java.util.HashMap;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

/**
 * This method counts the number of credits a heuristic is responsible for that 
 * lies within the specified rank. 
 * @author nozomihitomi
 */
public class ParetoRankContribution extends AbstractPopulationContribution{
    
    private ParetoFrontContribution aggPFcredit;
    
    private final double withinRank;
    
    private final double outsideRank;
    
    private final int maxRank;
    
    /**
     * Constructor to specify the credits that are assigned when a solution is 
     * at or within the maximum rank to still receive credit
     * @param withinRank Credit received if a new solution is at or within the maximum rank with respect to the population
     * @param outsideRank Credit received if a new solution is not within the maximum rank with respect to the population
     * @param maxRank The maximum rank a solution can be to still receive credit
     * 
     */
    public ParetoRankContribution(double withinRank,double outsideRank,int maxRank) {
        
        operatesOn = RewardDefinedOn.POPULATION;
        //by giving AggregateParetoFrontCredit a 1,0 score we can count how many
        //solutions per rank a heuristic is responsible for
        aggPFcredit = new ParetoFrontContribution(withinRank, 0.0);
        throw new UnsupportedOperationException();
    }
    
    protected double compute(Iterable<Solution> population, Variation heuristic,int iteration){
        NondominatedPopulation ndpop = new NondominatedPopulation(population);
        double sumCredits =0;
        int rank = 0;
        while(rank<=maxRank){
//            sumCredits+=aggPFcredit.compute(ndpop, heuristic,iteration)*rankCredit(rank);
//            population.removeAll(ndpop);
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
        return "ParetoRankContribution";
    }

    @Override
    public HashMap<Variation, Reward> compute(Iterable<Solution> population, Collection<Variation> heuristics, int iteration) {
        Population pop = new Population(population);
        HashMap<Variation,Reward> credits = new HashMap();
        for(Variation heuristic:heuristics){
            credits.put(heuristic, new Reward(-1,compute(pop,heuristic,iteration)));
        }
        return credits;
    }

    
}
