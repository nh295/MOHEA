/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.creditdefinition.immediate;

import hh.creditdefinition.ArchiveBasedCredit;
import hh.creditdefinition.immediate.ImmediateParetoFrontCredit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.moeaframework.core.EpsilonBoxDominanceArchive;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

/**
 * This credit definition gives credit to the specified solution if it makes it
 * in the epsilon archive. Credit is only assigned to the specified solution.
 * @author nozomihitomi
 */
public class ImmediateEArchiveCredit extends ArchiveBasedCredit{

    protected EpsilonBoxDominanceArchive ndpop;
    protected double[] epsilon;
    /**
     * Credit received if a new solution is in the archive 
     */
    protected final double inArchive;
    
    /**
     * Credit received if a new solution is not in the archive 
     */
    protected final double notInArchive;
    
    /**
     * The constructor needs the value for credit when a solution is in the 
     * e-archive and for when a solution is not in the e-archive
     * @param inArchive credit to assign when solution is in the archive 
     * @param notInArchive credit to assign when solution is not in the archive 
     * @param epsilon the epsilon to use for the e-archive
     */
    public ImmediateEArchiveCredit(double inArchive, double notInArchive, double[] epsilon) {
        this.notInArchive = notInArchive;
        this.inArchive = inArchive;
        this.ndpop = new EpsilonBoxDominanceArchive(epsilon);
        this.epsilon = epsilon;
    }
    
    /**
     * Computes the credit of an offspring solution with respect to some archive
     * @param offspring solution that will receive credits
     * @param population the population to compare the offspring solutions with
     * @param heuristic that created offspring solution
     * @return the value of credit to resulting from the solution
     */
    @Override
    public double compute(Solution offspring, Population population,Variation heuristic) {
        ndpop.clear();
        ndpop.addAll(population);
        
        if(ndpop.add(offspring))
            return inArchive;
        else
            return notInArchive;
    }
    
    /**
     * Computes the credit of an offspring solution with respect to the population
     * @param offsprings a list of offspring solutions that will receive credits
     * @param population the population to compare the offspring solutions with
     * @param heuristic that created offspring solution
     * @return the value of credit to resulting from the solution
     */
    @Override
    public List<Double> computeAll(Solution[] offsprings, Population population,Variation heuristic) {
        List offspringList = Arrays.asList(offsprings);
        Collections.shuffle(offspringList);
        
        ndpop.clear();
        ndpop.addAll(population);
        
        Iterator<Solution> iter = offspringList.iterator();
        ArrayList<Double> creditvals = new ArrayList();
        while(iter.hasNext()){
            if(ndpop.add(iter.next()))
                creditvals.add(inArchive);
            else
                creditvals.add(notInArchive);
        }
        return creditvals;
    }
    
    @Override
    public String toString() {
        return "ImmediateEArchiveCredit";
    }

    @Override
    public boolean isImmediate() {
        return true;
    }
    
}
