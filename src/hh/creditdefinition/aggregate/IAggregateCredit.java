/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.creditdefinition.aggregate;

import hh.creditdefinition.Credit;
import hh.creditrepository.CreditRepository;
import java.util.Collection;
import java.util.HashMap;
import org.moeaframework.core.Population;
import org.moeaframework.core.Variation;

/**
 *
 * @author nozomihitomi
 */
public interface IAggregateCredit {
    
    /**
     * Computes all the credits received for each heuristic and returns the Credits they earn
     * @param population
     * @param heuristics
     * @param iteration
     * @return 
     */
    public HashMap<Variation, Credit> compute(Population population,Collection<Variation> heuristics,int iteration);
}
