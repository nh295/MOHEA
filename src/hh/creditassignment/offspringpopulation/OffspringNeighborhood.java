/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.creditassignment.offspringpopulation;

import hh.creditassigment.CreditFunctionInputType;
import hh.creditassigment.CreditFitnessFunctionType;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;

/**
 * Credit assignment for decomposition method. Rewards an operator based on how
 * many neighborhood solutions an offspring replaces.
 *
 * @author nozomihitomi
 */
public class OffspringNeighborhood extends AbstractOffspringPopulation{

    public OffspringNeighborhood() {
        fitType = CreditFitnessFunctionType.De;
        inputType = CreditFunctionInputType.SI;
    }

    /**
     * Right now, credit is computed within MOEADHH class
     * @param offspring
     * @param population
     * @return 
     */
    @Override
    public double compute(Solution offspring, Population population) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public String toString() {
        return "SI-De";
    }
}
