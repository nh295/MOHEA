/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.rewarddefinition;

import hh.rewarddefinition.fitnessindicator.DoubleComparator;
import hh.rewarddefinition.fitnessindicator.SortedLinkedList;
import java.util.ArrayList;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;

/**
 *
 * @author SEAK2
 */
public class AbstractRewardDefintion implements IRewardDefinition {

    protected CreditFunctionType type;

    protected RewardDefinedOn operatesOn;
    
    private Solution utopia;

    /**
     * stores all the objective values in sorted order to get bounds
     */
    protected ArrayList<SortedLinkedList<Double>> sortedObjs;

    @Override
    public CreditFunctionType getType() {
        return type;
    }

    @Override
    public RewardDefinedOn getOperatesOn() {
        return operatesOn;
    }


    /**
     * Finds the utopia point in the population
     *
     * @param population the population to find the utopia point
     * @return the utopia point
     */
    protected Solution computeUtopia(Population population) {
        utopia = new Solution(population.get(0).getNumberOfVariables(), population.get(0).getNumberOfObjectives());
        for (int i = 0; i < population.get(0).getNumberOfObjectives(); i++) {
            double min = Double.MAX_VALUE;
            for (Solution soln : population) {
                min = Math.min(min, soln.getObjective(i));
            }
            utopia.setObjective(i, min);
        }
        return utopia;
    }

    /**
     * Updates the utopia based on the new incoming solution. This method assumes that the utopia point never deteriorates
     *
     * @param offspring new solution entering the population
     * @return the updated utopia point
     */
    protected Solution updateUtopia(Solution offspring) {
        for (int i = 0; i < offspring.getNumberOfObjectives(); i++) {
            utopia.setObjective(i, Math.min(utopia.getObjective(i), offspring.getObjective(i)));
        }
        return utopia;
    }

    /**
     * Normalizes the objectives to the solution based on the upper and lower
     * bounds of the objectives of the solutions in the population
     *
     * @param solution
     * @return
     */
    protected double[] normalizeObjectives(Solution solution) {
        double[] normalizedObjs = new double[solution.getNumberOfObjectives()];
        for (int i = 0; i < solution.getNumberOfObjectives(); i++) {
            double lowBound = sortedObjs.get(i).get(0);
            double upBound = sortedObjs.get(i).get(sortedObjs.get(i).size() - 1);
            normalizedObjs[i] = (solution.getObjective(i) - lowBound) / (upBound - lowBound);
        }
        return normalizedObjs;
    }
    
    //Computes the bounds on population
    protected void computeBounds(Population pop){
        sortedObjs = new ArrayList<>(pop.get(0).getNumberOfObjectives());
        for(int i = 0; i < pop.get(0).getNumberOfObjectives(); i++) {
            ArrayList<Double> objs = new ArrayList(pop.size());
            for (Solution soln : pop) {
                objs.add(soln.getObjective(i));
            }
            sortedObjs.add(new SortedLinkedList<>(objs, new DoubleComparator()));
        }
    }
    
    //updates the bounds based on a solution exiting the population
    protected void updateBoundsRemove(Solution removedSoln) {
        for (int i = 0; i < removedSoln.getNumberOfObjectives(); i++) {
            int index = sortedObjs.get(i).binaryFind(removedSoln.getObjective(i));
            sortedObjs.get(i).remove(index);
        }
    }
    
    //updates the bounds based on a solution exiting the population
    protected void updateBoundsInsert(Solution newSolution) {
        for (int i = 0; i < newSolution.getNumberOfObjectives(); i++) {
            sortedObjs.get(i).add(newSolution.getObjective(i));
        }
    }
    
    protected void updateBounds(Solution newSolution, Solution oldSolution){
        updateBoundsRemove(oldSolution);
        updateBoundsInsert(newSolution);
    }

}
