/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.heuristicselectors;

import hh.creditaggregation.ICreditAggregationStrategy;
import hh.creditdefinition.Credit;
import hh.creditrepository.ICreditRepository;
import hh.nextheuristic.AbstractHeuristicSelector;
import org.moeaframework.core.Variation;

/**
 * RandomSelect randomly selects a heuristic with uniform probability from the 
 * given set of heuristics
 * @author nozomihitomi
 */
public class RandomSelect extends AbstractHeuristicSelector{

    /**
     * RandomSelect does not really utilize the credit repository so any 
     * repository will do
     * @param creditRepo any ICreditRepository
     * @param creditAgg the aggregation strategy to reward a heuristic a credit for the current iteration based on past performance
     */
    public RandomSelect(ICreditRepository creditRepo,ICreditAggregationStrategy creditAgg) {
        super(creditRepo,creditAgg);
    }

    /**
     * Randomly selects the next heuristic from the set of heuristics with 
     * uniform probability
     * @return 
     */
    @Override
    public Variation nextHeuristic() {
        incrementIterations();
        return getRandomHeuristic(creditRepo.getHeuristics());
    }

    /**
     * Updates the credit repository in order for credit history to be extracted
     * from hyperheuristic for post--analysis
     * @param heuristic heuristic who receives credit
     * @param credit credit earned by heuristic
     */
    @Override
    public void update(Variation heuristic, Credit credit) {
        creditRepo.update(heuristic, credit);
    }

    @Override
    public String toString() {
        return "RandomSelect";
    }
    
    
}
