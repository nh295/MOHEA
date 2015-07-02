/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.creditdefinition.aggregate;

import hh.creditdefinition.Credit;
import hh.creditdefinition.DecayingCredit;
import hh.creditdefinition.immediate.*;
import hh.creditrepository.CreditRepository;
import hh.hyperheuristics.SerializableVal;
import java.util.Collection;
import java.util.Iterator;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

/**
 * This credit definition gives credit to all solutions created by the specified
 * heuristic, including the solution given, that lie on the Pareto front. 
 * @author Nozomi
 */
public class AggregateParetoFrontCredit extends ImmediateParetoFrontCredit implements IAggregateCredit{

    /**
     * Constructor to specify the credits to give to the heuristic responsible 
     * for each solution on the Pareto front.
     * @param creditNonDominated credit to assign to each solution on the Pareto Front that the heuristic created
     * @param creditDominated credit to assign if there are no solutions on the Pareto Front created by the heuristic 
     */
    public AggregateParetoFrontCredit(double creditNonDominated,double creditDominated) {
        super(creditNonDominated, creditDominated);
    }
    
    /**
     * This method counts the number of solutions the heuristic is responsible 
     * for in the given population. For each solution it finds, it calculates the 
     * discounted credit based on the DecayingCredits. The sum total is the 
     * credits to be assigned
     * @param population
     * @param heuristic
     * @return 
     */
    protected double compute(Population population,Variation heuristic,int iteration){
        double sumCredit=0;
        Iterator<Solution> iter = population.iterator();
        while(iter.hasNext()){
            Solution soln = iter.next();
            if(soln.hasAttribute("heuristic")){
                if(((SerializableVal)soln.getAttribute("heuristic")).getSval().equalsIgnoreCase(heuristic.getClass().getSimpleName())){
                    int createdIteration = ((SerializableVal)soln.getAttribute("iteration")).getIval();
                    double alpha = ((SerializableVal)soln.getAttribute("alpha")).getDval();
                    DecayingCredit dc = new DecayingCredit(createdIteration,1,alpha);
                    sumCredit+=creditNonDominated*dc.fractionOriginalVal(iteration);
                }
            }
        }
        if(sumCredit>0){
            return sumCredit;
        }else 
            return creditDominated;
    }
    
    @Override
    public String toString() {
        return "AggregateParetoFrontCredit";
    }
    
    @Override
    public boolean isImmediate() {
       return false;
    }

    @Override
    public CreditRepository compute(Population population, Collection<Variation> heuristics,int iteration) {
        NondominatedPopulation ndpop = new NondominatedPopulation(population);
        CreditRepository creditRepo = new CreditRepository(heuristics);
        for(Variation heuristic:heuristics){
            creditRepo.update(heuristic, new Credit(-1,compute(ndpop,heuristic,iteration)));
        }
        return creditRepo;
    }
}
