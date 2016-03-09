/* Copyright 2009-2015 David Hadka
 *
 * This file is part of the MOEA Framework.
 *
 * The MOEA Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * The MOEA Framework is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the MOEA Framework.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.moeaframework.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.moeaframework.core.comparator.EpsilonBoxDominanceComparator;

/**
 * A non-dominated population using an &epsilon;-box dominance comparator.  
 * &epsilon;-Dominance archives provide several important theoretical 
 * properties, such as guaranteed convergence and diversity if certain other 
 * conditions hold [1].  In addition, this archive also tracks 
 * &epsilon;-progress [2] by counting the number of &epsilon;-box improvements.
 * <p>
 * References:
 * <ol>
 *   <li>Laumanns, M. et al.  "Combining Convergence and Diversity in
 *       Evolutionary Multi-Objective Optimization."  Evolutionary Computation,
 *       10(3):263-282, 2002.
 *   <li>Hadka, D. and Reed, P.  "Borg: An Auto-Adaptive Many-Objective
 *       Evolutionary Computing Framework."  Evolutionary Computation,
 *       21(2):231-259, 2013.
 * </ol>
 */
public class EpsilonBoxDominanceArchive extends NondominatedPopulation {

	/**
	 * The number of &epsilon;-box improvements that have occurred.
	 */
	private int numberOfImprovements;

	/**
	 * The number of &epsilon;-box improvements dominating an existing solution
	 * that have occurred.
	 */
	private int numberOfDominatingImprovements;

	/**
         * The number of times the archive is used for injection
         */
        private int injectionCount;

	/**
	 * Constructs an empty &epsilon;-box dominance archive using an additive
	 * &epsilon;-box dominance comparator with the specified &epsilon;.
	 * 
	 * @param epsilon the &epsilon; value used by the additive &epsilon;-box
	 *        dominance comparator
	 */
	public EpsilonBoxDominanceArchive(double epsilon) {
		super(new EpsilonBoxDominanceComparator(epsilon));
	}

	/**
	 * Constructs an &epsilon;-box dominance archive using an additive
	 * &epsilon;-box dominance comparator with the specified &epsilon; and
	 * initialized with the specified solutions.
	 * 
	 * @param epsilon the &epsilon; value used by the additive &epsilon;-box
	 *        dominance comparator
	 * @param iterable the solutions used to initialize this archive
	 */
	public EpsilonBoxDominanceArchive(double epsilon,
			Iterable<? extends Solution> iterable) {
		super(new EpsilonBoxDominanceComparator(epsilon), iterable);
	}
	
	/**
	 * Constructs an empty &epsilon;-box dominance archive using an additive
	 * &epsilon;-box dominance comparator with the specified &epsilon; values.
	 * 
	 * @param epsilon the &epsilon; values used by the additive &epsilon;-box
	 *        dominance comparator
	 */
	public EpsilonBoxDominanceArchive(double[] epsilon) {
		super(new EpsilonBoxDominanceComparator(epsilon));
	}
	
	/**
	 * Constructs an &epsilon;-box dominance archive using an additive
	 * &epsilon;-box dominance comparator with the specified &epsilon; values
	 * and initialized with the specified solutions.
	 * 
	 * @param epsilon the &epsilon; values used by the additive &epsilon;-box
	 *        dominance comparator
	 * @param iterable the solutions used to initialize this archive
	 */
	public EpsilonBoxDominanceArchive(double[] epsilon,
			Iterable<? extends Solution> iterable) {
		super(new EpsilonBoxDominanceComparator(epsilon), iterable);
	}

	/**
	 * Constructs an empty &epsilon;-box dominance archive using the specified
	 * &epsilon;-box dominance comparator.
	 * 
	 * @param comparator the &epsilon;-box dominance comparator used by this
	 *        archive
	 */
	public EpsilonBoxDominanceArchive(EpsilonBoxDominanceComparator comparator) {
		super(comparator);
	}

	/**
	 * Constructs an &epsilon;-box dominance archive using the specified
	 * &epsilon;-box dominance comparator and initialized with the specified
	 * solutions.
	 * 
	 * @param comparator the &epsilon;-box dominance comparator used by this
	 *        archive
	 * @param iterable the solutions used to initialize this archive
	 */
	public EpsilonBoxDominanceArchive(EpsilonBoxDominanceComparator comparator,
			Iterable<? extends Solution> iterable) {
		super(comparator, iterable);
	}

	@Override
	public boolean add(Solution newSolution) {
		return addAndReturnRemovedSolutions(newSolution) != null;
	}
        
     /**
     * If {@code newSolution} is dominates any solution or is non-dominated with
     * all solutions in this population, the dominated solutions are removed and
     * {@code newSolution} is added to this population. Otherwise,
     * {@code newSolution} is dominated and is not added to this population.
     *
     * @return returns a collection of the solutions that are no longer in the
     * nondominated population. If return an empty list, new solution enters
     * nondominated set but doesn't replace any solutions. If return null, then
     * new solution didn't enter nondominated set
     */
     @Override
    public Collection<Solution> addAndReturnRemovedSolutions(Solution newSolution) {
        
        ArrayList<Solution> removed = new ArrayList<>();
		Iterator<Solution> iterator = iterator();

		boolean same = false;
		boolean dominates = false;
		while (iterator.hasNext()) {
			Solution oldSolution = iterator.next();
			int flag = getComparator().compare(newSolution, oldSolution);
			if (flag < 0) {
				if (getComparator().isSameBox()) {
					same = true;
				} else {
					dominates = true;
				}
                                removed.add(oldSolution);
				iterator.remove();
			} else if (flag > 0) {
				return null;
			}
		}
		if (!same) {
			numberOfImprovements++;
			if (dominates) {
				numberOfDominatingImprovements++;
			}
		}
		forceAddWithoutCheck(newSolution);

                changedFlag = true;
                return removed;
	}

	/**
	 * Returns the &epsilon;-box dominance comparator used by this archive.
	 * 
	 * @return the &epsilon;-box dominance comparator used by this archive
	 */
	@Override
	public EpsilonBoxDominanceComparator getComparator() {
		return (EpsilonBoxDominanceComparator)super.getComparator();
	}

	/**
	 * Returns the number of &epsilon;-box improvements that have occurred.
	 * 
	 * @return the number of &epsilon;-box improvements that have occurred
	 */
	public int getNumberOfImprovements() {
		return numberOfImprovements;
	}

	/**
	 * Returns the number of &epsilon;-box improvements dominating existing
	 * solutions that have occurred.
	 * 
	 * @return the number of &epsilon;-box improvements dominating existing
	 *         solutions that have occurred
	 */
	public int getNumberOfDominatingImprovements() {
		return numberOfDominatingImprovements;
	}

        /**
         * Increments the number of times the archive has been used for injection
         */
        public void incrementInjection(){
            injectionCount++;
        }
        
        /**
         * returns the number of times that the archive has been used for injection
         * @return 
         */
        public int getInjectionCount(){
            return injectionCount;
        }

}
