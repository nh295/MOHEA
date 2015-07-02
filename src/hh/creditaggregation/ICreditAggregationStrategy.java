/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.creditaggregation;

import hh.creditdefinition.Credit;
import hh.creditrepository.AbstractCreditHistory;

/**
 *
 * @author nozomihitomi
 */
public interface ICreditAggregationStrategy {
    
    /**
     * Aggregates the history using an aggregation function to produce one 
     * Credit value
     * @param iteration the iteration of the search
     * @param creditHistory the history to aggregate
     * @return The aggregated credit
     */
    public Credit aggregateCredit(int iteration, AbstractCreditHistory creditHistory);
}
