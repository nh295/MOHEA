/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.hyperheuristics;

import hh.heuristicselectors.AdaptivePursuit;
import hh.heuristicselectors.ProbabilityMatching;
import hh.heuristicselectors.RandomSelect;
import hh.nextheuristic.AbstractHeuristicGenerator;
import hh.nextheuristic.AbstractHeuristicSelector;
import java.util.Collection;
import org.moeaframework.core.Variation;
import org.moeaframework.util.TypedProperties;

/**
 * Factory methods for creating an instance of IHyperHeuristicc
 * @author SEAK2
 */
public class HHFactory {
    
    /**
     * The default problem factory.
     */
    private static HHFactory instance;

    /**
     * private constructor to enforce singleton
     */
    private HHFactory(){
        super();
    }
    
    /**
     * Returns an instance of the hyper-heuristic factory
     * @return 
     */
    public static HHFactory getInstance(){
        if(instance==null)
            return new HHFactory();
        else 
            return instance;
    }
    
    public AbstractHeuristicSelector getHeuristicSelector(String name, TypedProperties properties,Collection<Variation> heuristics){
        AbstractHeuristicSelector heuristicSelector = null;
        
        switch(name){
            case "Random": //uniform random selection
                heuristicSelector = new RandomSelect(heuristics);
                break;
            case "PM":{ //Probability matching
                double pmin = properties.getDouble("pmin", 0.1);
                heuristicSelector = new ProbabilityMatching(heuristics,pmin);
                }
                break;
            case "AP":{ //Adaptive Pursuit
                double pmin = properties.getDouble("pmin", 0.1);
                double beta = properties.getDouble("beta", 0.8);
                heuristicSelector = new AdaptivePursuit(heuristics, pmin, beta);
                }
                break;
            case "DMAB": //Dynamic Armed Bandit
                break;
            default: throw new IllegalArgumentException("Invalid heuristic selector specified:" + name);
        }
        
        return heuristicSelector;
    }
    
    public AbstractHeuristicGenerator getHeuristicGenerator(String name, TypedProperties properties){
         AbstractHeuristicGenerator heuristicGenerator = null;
        
        switch(name){
            case "HGA":{ //Probability matching
                double pmin = properties.getDouble("pmin", 0.1);
                double alpha = properties.getDouble("alpha", 0.8);
//                heuristicGenerator = new HyperGA(creditRepo, creditAgg, pmin, alpha);
                }
                break;
            default: throw new IllegalArgumentException("Invalid heuristic selector specified:" + name);
        }
        
        return heuristicGenerator; 
    }
}
