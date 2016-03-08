//  FastHypervolume.java
//
//  Authors:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2013 Antonio J. Nebro, Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.
//

package org.moeaframework.core.indicator.jmetal;

import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.comparator.ParetoObjectiveComparator;
import org.moeaframework.core.indicator.NormalizedIndicator;

/**
 * Created with IntelliJ IDEA.
 * User: Antonio J. Nebro
 * Date: 26/08/13
 * Time: 10:20
 */
public class FastHypervolume extends NormalizedIndicator{
  Solution referencePoint_ ;
  int numberOfObjectives_ ;
  int numberOfVariables_;

  public FastHypervolume(Problem problem, NondominatedPopulation referenceSet, Solution referencePoint) {
      super(problem, referenceSet);
    referencePoint_ = null ;
    numberOfObjectives_ = 0 ;
    this.referencePoint_ = referencePoint;
  }
//
//  public double computeHypervolume(Population pop) {
//    double hv ;
//    if (pop.isEmpty())
//      hv = 0.0 ;
//    else {
//      numberOfObjectives_ = pop.get(0).getNumberOfObjectives() ;
//      numberOfVariables_ = pop.get(0).getNumberOfObjectives() ;
//      referencePoint_ = new Solution(numberOfVariables_,numberOfObjectives_) ;
//      updateReferencePoint(pop);
//      if (numberOfObjectives_ == 2) {
//        pop.sort(new JObjectiveComparator(numberOfObjectives_-1, true));
//        hv = get2DHV(pop) ;
//      }
//      else {
//        updateReferencePoint(pop);
//        Front front = new Front(pop.size(), numberOfObjectives_, pop) ;
//        hv = new WFGHV(numberOfObjectives_, pop.size(), referencePoint_).getHV(front) ;
//      }
//    }
//
//    return hv ;
//  }

  public double computeHypervolume(NondominatedPopulation population, Solution referencePoint) {
      //normalize objective values to upperbounds of the known PF in each objective
      NondominatedPopulation normPop = normalize(population);
      
      //filter out all the points that do no dominate the referencePoint
      Population pop = new Population();
      ParetoObjectiveComparator domComp = new ParetoObjectiveComparator();
      for(Solution soln:normPop){
            if(domComp.compare(soln, referencePoint)==-1)
                  pop.add(soln);
      }
      
      
    double hv;
    if (pop.isEmpty())
      hv = 0.0;
    else {
      numberOfObjectives_ = pop.get(0).getNumberOfObjectives();

      if (numberOfObjectives_ == 2) {
        pop.sort(new JObjectiveComparator(numberOfObjectives_ - 1, true));

        hv = get2DHV(pop);
      } else {
        WFGHV wfg = new WFGHV(numberOfObjectives_, pop.size());
        Front front = new Front(pop.size(), numberOfObjectives_, pop);
        hv = wfg.getHV(front, referencePoint);
      }
    }

    return hv;
  }


  /**
   * Updates the reference point
   */
  public void updateReferencePoint(Solution refPt) {
    this.referencePoint_ = refPt;
  }

  /**
   * Computes the HV of a solution set.
   * REQUIRES: The problem is bi-objective
   * REQUIRES: The archive is ordered in descending order by the second objective
   * @return
   */
  public double get2DHV(Population pop) {
    double hv = 0.0;
    if (pop.size() > 0) {
      hv = Math.abs((pop.get(0).getObjective(0) - referencePoint_.getObjective(0)) *
              (pop.get(0).getObjective(1) - referencePoint_.getObjective(1)));

      for (int i = 1; i < pop.size(); i++) {
        double tmp = Math.abs((pop.get(i).getObjective(0) - referencePoint_.getObjective(0)) *
                (pop.get(i).getObjective(1) - pop.get(i - 1).getObjective(1)));
        hv += tmp;
      }
    }
    return hv;
  }
//
//  /**
//   * Computes the HV contribution of the solutions
//   * @return
//   */
//  public void computeHVContributions(Population pop) {
//    double[] contributions = new double[pop.size()] ;
//    double solutionSetHV = 0 ;
//
//    solutionSetHV = computeHypervolume(pop) ;
//
//    for (int i = 0; i < pop.size(); i++) {
//      Solution currentPoint = pop.get(i);
//      pop.remove(i) ;
//
//      if (numberOfObjectives_ == 2) {
//        //updateReferencePoint(solutionSet);
//        //solutionSet.sort(new ObjectiveComparator(numberOfObjectives_-1, true));
//        contributions[i] = solutionSetHV - get2DHV(pop) ;
//      }
//      else {
//        Front front = new Front(pop.size(), numberOfObjectives_, pop) ;
//        double hv = new WFGHV(numberOfObjectives_, pop.size(), referencePoint_).getHV(front) ;
//        contributions[i] = solutionSetHV - hv ;
//      }
//      pop.add(i, currentPoint) ;
//    }
//
//    for (int i = 0; i < pop.size(); i++) {
//      pop.get(i).setCrowdingDistance(contributions[i]) ;
//    }
//  }

    @Override
    public double evaluate(NondominatedPopulation approximationSet) {
        return computeHypervolume(approximationSet,referencePoint_);
    }
}
