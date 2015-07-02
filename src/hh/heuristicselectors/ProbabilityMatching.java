/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.heuristicselectors;

import hh.creditdefinition.Credit;
import hh.creditrepository.CreditRepository;
import hh.creditrepository.ICreditRepository;
import hh.nextheuristic.AbstractHeuristicSelector;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.moeaframework.core.Variation;


/**
 * Selects heuristics based on probability which is proportional to the 
 * heuristics credits. Each heuristic gets selected with a minimum probability
 * of pmin. If current credits in credit repository becomes negative, zero 
 * credit is re-assigned to that heuristic. For the first iteration, heuristics
 * are selected with uniform probability.
 * @author nozomihitomi
 */
public class ProbabilityMatching extends AbstractHeuristicSelector {
    
    /**
     * Hashmap to store the selection probabilities of each heuristic
     */
    protected HashMap<Variation,Double> probabilities;
    
    /**
     * The minimum probability for a heuristic to be selected
     */
    protected final double pmin;
    
    /**
     * A temporary credit repository used in the update process. Stores the 
     * current credits for each heuristic. Its use allows for handling 
     * situations when the current credit becomes negative by storing them as 0 
     * values
     */
    protected CreditRepository tmpCreditRepo;

    /**
     * Constructor to initialize probability map for selection
     * @param creditRepo the type of credit repository to be used
     * @param pmin The minimum probability for a heuristic to be selected
     */
    public ProbabilityMatching(ICreditRepository creditRepo, double pmin) {
        super(creditRepo);
        this.pmin = pmin;
        this.probabilities = new HashMap();
        this.tmpCreditRepo = new CreditRepository(creditRepo.getHeuristics());
        reset();
    }

    /**
     * Will return the next heuristic that gets selected based on probability 
     * proportional to a heuristics credits. Each heuristic gets selected with a
     * minimum probability of pmin
     * @return 
     */
    @Override
    public Variation nextHeuristic() {
        double p = random.nextDouble();
        Iterator<Variation> iter = probabilities.keySet().iterator();
        double sum = 0.0;
        Variation heuristic;
        while(iter.hasNext()){
            heuristic = iter.next();
            sum+=probabilities.get(heuristic);
            if(sum>=p)
                return heuristic;
        }
        throw new NullPointerException("No heuristic was selected by Probability matching heuristic selector. Check probabilities");
    }

    /**
     * Updates the probabilities stored in the map
     * @param heuristic heuristic that just earned credit
     * @param credit that was earned by the heuristic
     */
    @Override
    public void update(Variation heuristic, Credit credit) {
        
        creditRepo.update(heuristic, credit);
        tmpCreditRepo.update(heuristic, creditRepo.getCurrentCredit(heuristic));
        
        //if current credit becomes negative, adjust to 0
        if(tmpCreditRepo.getCurrentCredit(heuristic).getValue()<=0.0){
            tmpCreditRepo.update(heuristic, new Credit(credit.getIteration(),0.0));
        }
        
        //calculate the sum of all credits across all heuristics
        double sum = 0.0;
        Iterator<Variation> iter = probabilities.keySet().iterator();
        while(iter.hasNext()){
            Variation heuristic_next = iter.next();
            sum+= tmpCreditRepo.getCurrentCredit(heuristic_next).getValue();
        }
        
        // if the credits sum up to zero, apply uniform probabilty to  heuristics
        iter = probabilities.keySet().iterator();
        if(Math.abs(sum)<Math.pow(10.0, -14)){
            while(iter.hasNext()){
                Variation heuristic_i = iter.next();
                probabilities.put(heuristic_i,1.0/this.nHeuristics);
            }
        }else{
            while(iter.hasNext()){
                Variation heuristic_i = iter.next();
                double newProb = pmin + (1-probabilities.size()*pmin)
                        * (tmpCreditRepo.getCurrentCredit(heuristic_i).getValue()/sum);
                probabilities.put(heuristic_i,newProb);
            }
        }
    }
    
    /**
     * Clears the credit repository and resets the selection probabilities
     */
    @Override
    public final void reset(){
        probabilities.clear();
        Collection<Variation> heuristics = creditRepo.getHeuristics();
        Iterator<Variation> iter = heuristics.iterator();
        while(iter.hasNext()){
            //all heuristics get uniform selection probability at beginning
            probabilities.put(iter.next(), 1.0/this.nHeuristics);
        }
        creditRepo.clear();
        tmpCreditRepo.clear();
    }
    
    @Override
    public String toString() {
        return "ProbabilityMatching";
    }
    
    
}
