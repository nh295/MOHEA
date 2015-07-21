/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.rewarddefinition.offspringpopulation;

import hh.rewarddefinition.RewardDefinedOn;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

/**
 * This method checks to see if a solution is within a specified rank and gives 
 * credit accordingly. Credit is only computed for the specified solution 
 * @author nozomihitomi
 */
public class OffspringParetoRank extends AbstractOffspringPopulation{
    
     /**
     * Credit received if a new solution is at or within the maximum rank with respect to the population
     */
    protected final double withinRank;
    
    /**
     * Credit received if a new solution is not within the maximum rank with respect to the population
     */
    protected final double outsideRank;
    
    /**
     * The maximum rank a solution can be to still receive credit
     */
    protected final int minRank;

    
    /**
     * Constructor to specify the credits that are assigned when a solution is 
     * at or within the maximum rank to still receive credit
     * @param withinRank Credit received if a new solution is at or within the maximum rank with respect to the population
     * @param outsideRank Credit received if a new solution is not within the maximum rank with respect to the population
     * @param minRank The maximum rank a solution can be to still receive credit
     * 
     */
    public OffspringParetoRank(double withinRank,double outsideRank,int minRank) {
        operatesOn = RewardDefinedOn.POPULATION;
        this.withinRank = withinRank;
        this.outsideRank = outsideRank;
        this.minRank = minRank;
    }

    /**
     * Function to assign credit as a function of rank
     * @param rank of solution. Rank is 0 if nondominated
     * @return credit value associated with the rank
     */
    protected double rankCredit(int rank){
        if(rank<=minRank)
            return withinRank;
        else
            return outsideRank;
    }
            
    @Override
    public double compute(Solution offspring, Iterable<Solution> population) {
        throw new UnsupportedOperationException("TODO fix");
//        Population pop = new Population(population);
//        pop.add(offspring);
//        NondominatedPopulation ndpop = new NondominatedPopulation(population);
//        int rank = 0;
//        while(!ndpop.contains(offspring)&&rank<=minRank){
//            pop.removeAll(ndpop);
//            ndpop = new NondominatedPopulation(pop);
//            rank++;
//        }
//        return rankCredit(rank);
    }

    
    @Override
    public String toString() {
        return "OffspringParetoRank";
    }

    
}
