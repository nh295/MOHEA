/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.heuristicselectors;

import hh.creditdefinition.Credit;
import hh.creditrepository.ICreditRepository;
import java.util.Iterator;
import org.moeaframework.core.Variation;

/**
 *
 * @author nozomihitomi
 */
public class AdaptivePursuit extends ProbabilityMatching{
    
    /**
     * The maximum probability that the heuristic with the highest credits can
     * be selected. It is implicitly defined as 1.0 - m*pmin where m is the 
     * number of heuristics used and pmin is the minimum selection probability
     */
    double pmax;

    /**
     * Constructor to initialize adaptive pursuit map for selection. The maximum
     * selection probability is implicitly defined as 1.0 - m*pmin where m is 
     * the number of heuristics defined in the given credit repository and pmin 
     * is the minimum selection probability
     * @param creditRepo the type of credit repository to be used
     * @param pmin the minimum selection probability
     */
    public AdaptivePursuit(ICreditRepository creditRepo, double pmin) {
        super(creditRepo, pmin);
        this.pmax = 1 - (probabilities.size()-1)*pmin;
        if(pmax<pmin){
            throw new IllegalArgumentException("the implicit maxmimm selection "
                    + "probability " + pmax +" is less than the minimum selection probability " + pmin);
        }
        
        //Initialize the probabilities such that a random heuristic gets the pmax
        int heurisitic_lead = random.nextInt(probabilities.size());
        Iterator<Variation> iter = probabilities.keySet().iterator();
        int count = 0;
        while(iter.hasNext()){
            if(count == heurisitic_lead)
                probabilities.put(iter.next(),pmax);
            else
                probabilities.put(iter.next(),pmin);
            count++;
        }
    }
    
    /**
     * Updates the probabilities stored in the map by finding the heuristic with
     * the most credits and apply pmax to that heuristic and pmin to all other 
     * heuristics
     * @param heuristic heuristic that just earned credit
     * @param credit that was earned by the heuristic
     */
    @Override
    public void update(Variation heuristic, Credit credit) {
        creditRepo.update(heuristic, credit);
        
        Variation leadHeuristic = argMax(creditRepo.getHeuristics());
        
        Iterator<Variation> iter = creditRepo.getHeuristics().iterator();
        while(iter.hasNext()){
            Variation heuristic_i = iter.next();
            if(heuristic_i==leadHeuristic)
                probabilities.put(heuristic_i,pmax);
            else
                probabilities.put(heuristic_i,pmin);
        }
    }
    
    /**
     * Want to find the heuristic that has the maximum credit
     * @param heuristic
     * @return the current credit of the specified heuristic
     */
    @Override
    protected double function2maximize(Variation heuristic){
        return creditRepo.getCurrentCredit(heuristic).getValue();
    }
    
    @Override
    public String toString() {
        return "AdaptivePursuit";
    }
}
