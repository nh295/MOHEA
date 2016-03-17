/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moea;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import org.moeaframework.core.FastNondominatedSorting;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;

/**
 * Fast Nondominated sorting for steady state evolutionary algorithms.
 * Implements Efficient Non-domination Level Update (ENLU) from Li, Ke,
 * Kalyanmoy Deb, Qingfu Zhang, Senior Member, and Qiang Zhang. 2015. “Efficient
 * Non-Domination Level Update Method for Steady-State Evolutionary
 * Multi-Objective Optimization.” COIN Report Number 2015022
 *
 * @author nozomihitomi
 */
public class SteadyStateFastNonDominatedSorting extends FastNondominatedSorting {
    
    private int[][] rankIdx_;
    
    private int numRanks;
    
    private HashSet<Integer> lastFront;
    
    public SteadyStateFastNonDominatedSorting() {
        super();
    }

    /**
     * update the non-domination level when adding a solution. Modified code
     * obtained from https://github.com/JerryI00/publication_codes.git
     *
     * @param individual individual to add to population
     * @param population population to add individual to
     */
    public void addSolution(Solution individual, Population population) {

        //Compute the new domination levels
        numRanks = add(individual, population);
        population.add(individual);
        
        lastFront = new HashSet<>();

        for(int i=0; i< rankIdx_[numRanks-1].length; i++){
            if(rankIdx_[numRanks-1][i]==1)
                lastFront.add(i);
        }
        if((int)individual.getAttribute(RANK_ATTRIBUTE)==numRanks-1)
            lastFront.add(population.size()-1);
    }
    
    /**
     * Returns the indices of the solutions in the last front
     * @return the indices of the solutions in the last front
     */
    public Collection<Integer> getLastFront(){
        return lastFront;
    }

    /**
     *
     * @param individual individual to add to population
     * @param population population to add individual to
     * @return the number of domination levels
     */
    private int add(Solution individual, Population population) {
        int flag = 0;
        int flag1, flag2, flag3;

        rankIdx_ = new int[population.size()][population.size()];
        for (int i = 0; i < population.size(); i++) {
            Arrays.fill(rankIdx_[i], 0);
        }

        // count the number of non-domination levels
        HashMap<Integer, Integer> frontSize = new HashMap();
        
        for (int i = 0; i < population.size(); i++) {
            
            int rank = (int) population.get(i).getAttribute(RANK_ATTRIBUTE);
            if (frontSize.containsKey(rank)) {
                frontSize.put(rank, frontSize.get(rank) + 1);
            } else {
                frontSize.put(rank, 1);
            }
            rankIdx_[rank][i] = 1;
        }
        int num_ranks = frontSize.keySet().size();

        Vector<Integer> dominateList = new Vector<>();	// used to keep the solutions dominated by 'individual'
        int level = 0;
        for (int i = 0; i < num_ranks; i++) {
            level = i;
            if (flag == 1) {	// 'individual' is non-dominated with all solutions in the ith non-domination level, then 'individual' belongs to the ith level
                individual.setAttribute(RANK_ATTRIBUTE, i - 1);
                return num_ranks;
            } else if (flag == 2) {	// 'individual' dominates some solutions in the ith level, but is non-dominated with some others, then 'individual' belongs to the ith level, and move the dominated solutions to the next level
                individual.setAttribute(RANK_ATTRIBUTE, i - 1);

                int prevRank = i - 1;

                // process the solutions belong to 'prevRank'th level and are dominated by 'individual' ==> move them to 'prevRank+1'th level and find the solutions dominated by them
                int curIdx;
                int newRank = prevRank + 1;
                int curListSize = dominateList.size();
                for (int j = 0; j < curListSize; j++) {
                    curIdx = dominateList.get(j);
                    rankIdx_[prevRank][curIdx] = 0;
                    rankIdx_[newRank][curIdx] = 1;
                    population.get(curIdx).setAttribute(RANK_ATTRIBUTE,newRank);
                }
                //finds which solutions from prevRank + 1 are dominated by solutions that were just moved to prevRank + 1 from prevRank
                for (int j = 0; j < population.size(); j++) {
                    if (rankIdx_[newRank][j] == 1) {
                        for (int k = 0; k < curListSize; k++) {
                            curIdx = dominateList.get(k);
                            if (getComparator().compare(population.get(j),population.get(curIdx)) == 1) {
                                dominateList.addElement(j);
                                break;
                            }

                        }
                    }
                }
                //removes all the solutions that we just moved from prevRank to prevRank + 1
                for (int j = 0; j < curListSize; j++) {
                    dominateList.remove(0);
                }

                // if there are still some other solutions moved to the next level, check their domination situation in their new level
                prevRank = newRank;
                newRank = newRank + 1;
                curListSize = dominateList.size();
                if (curListSize == 0) {
                    return num_ranks;
                } else {
                    int allFlag = 0;
                    do {
                        for (int j = 0; j < curListSize; j++) {
                            curIdx = dominateList.get(j);
                            rankIdx_[prevRank][curIdx] = 0;
                            rankIdx_[newRank][curIdx] = 1;
                            population.get(curIdx).setAttribute(RANK_ATTRIBUTE,newRank);
                        }
                        for (int j = 0; j < population.size(); j++) {
                            if (rankIdx_[newRank][j] == 1) {
                                for (int k = 0; k < curListSize; k++) {
                                    curIdx = dominateList.get(k);
                                    if (getComparator().compare(population.get(j),population.get(curIdx)) == 1) {
                                        dominateList.addElement(j);
                                        break;
                                    }
                                }
                            }
                        }
                        for (int j = 0; j < curListSize; j++) {
                            dominateList.remove(0);
                        }

                        curListSize = dominateList.size();
                        if (curListSize != 0) {
                            prevRank = newRank;
                            newRank = newRank + 1;
                            if (curListSize == frontSize.get(prevRank)) {	// if all solutions in the 'prevRank'th level are dominated by the newly added solution, move them all to the next level
                                allFlag = 1;
                                break;
                            }
                        }
                    } while (curListSize != 0);

                    if (allFlag == 1) {	// move the solutions after the 'prevRank'th level to their next levels
                        int remainSize = num_ranks - prevRank;
                        int[][] tempRecord = new int[remainSize][population.size()];

                        int tempIdx = 0;
                        
                        for (int j = 0; j < dominateList.size(); j++) {
                            tempRecord[0][tempIdx] = dominateList.get(j);
                            tempIdx++;
                        }

                        int k = 1;
                        int curRank = prevRank + 1;
                        while (curRank < num_ranks) {
                            tempIdx = 0;
                            for (int j = 0; j < population.size(); j++) {
                                if (rankIdx_[curRank][j] == 1) {
                                    tempRecord[k][tempIdx] = j;
                                    tempIdx++;
                                }
                            }
                            curRank++;
                            k++;
                        }

                        k = 0;
                        curRank = prevRank;
                        while (curRank < num_ranks) {
                            int level_size = frontSize.get(curRank);

                            int tempRank;
                            for (int j = 0; j < level_size; j++) {
                                curIdx = tempRecord[k][j];
                                tempRank = (int)population.get(curIdx).getAttribute(RANK_ATTRIBUTE);
                                newRank = tempRank + 1;
                                population.get(curIdx).setAttribute(RANK_ATTRIBUTE,newRank);

                                rankIdx_[tempRank][curIdx] = 0;
                                rankIdx_[newRank][curIdx] = 1;
                            }
                            curRank++;
                            k++;
                        }
                        num_ranks++;
                    }

                    if (newRank == num_ranks) {
                        num_ranks++;
                    }

                    return num_ranks;
                }
            } else if (flag == 3 || flag == 0) {	// if 'individual' is dominated by some solutions in the ith level, skip it, and term to the next level
                flag1 = flag2 = flag3 = 0;
                for (int j = 0; j < population.size(); j++) {
                    if (rankIdx_[i][j] == 1) {
                        switch (getComparator().compare(population.get(j),individual)) {
                            case 1: {
                                flag1 = 1;
                                dominateList.addElement(j);
                                break;
                            }
                            case 0: {
                                flag2 = 1;
                                break;
                            }
                            case -1: {
                                flag3 = 1;
                                break;
                            }
                        }

                        if (flag3 == 1) {
                            flag = 3;
                            break;
                        } else if (flag1 == 0 && flag2 == 1) {
                            flag = 1;
                        } else if (flag1 == 1 && flag2 == 1) {
                            flag = 2;
                        } else if (flag1 == 1 && flag2 == 0) {
                            flag = 4;
                        } else {
                            continue;
                        }
                    }
                }

            } else {	// (flag == 4) if 'indiv' dominates all solutions in the ith level, solutions in the current level and beyond move their current next levels
                individual.setAttribute(RANK_ATTRIBUTE,i - 1);
                i = i - 1;
                int remainSize = num_ranks - i;
                int[][] tempRecord = new int[remainSize][population.size()];

                int k = 0;
                while (i < num_ranks) {
                    int tempIdx = 0;
                    for (int j = 0; j < population.size(); j++) {
                        if (rankIdx_[i][j] == 1) {
                            tempRecord[k][tempIdx] = j;
                            tempIdx++;
                        }
                    }
                    i++;
                    k++;
                }

                k = 0;
                i = (int)individual.getAttribute(RANK_ATTRIBUTE);
                while (i < num_ranks) {
                    int level_size = frontSize.get(i);

                    int curIdx;
                    int curRank, newRank;
                    for (int j = 0; j < level_size; j++) {
                        curIdx = tempRecord[k][j];
                        curRank = (int)population.get(curIdx).getAttribute(RANK_ATTRIBUTE);
                        newRank = curRank + 1;
                        population.get(curIdx).setAttribute(RANK_ATTRIBUTE,newRank);

                        rankIdx_[curRank][curIdx] = 0;
                        rankIdx_[newRank][curIdx] = 1;
                    }
                    i++;
                    k++;
                }
                num_ranks++;

                return num_ranks;
            }
        }
        
        
        // if flag is still 3 after the for-loop, it means that 'indiv' is in the current last level
        if (flag == 1) {
            individual.setAttribute(RANK_ATTRIBUTE,level);
        } else if (flag == 2) {
            individual.setAttribute(RANK_ATTRIBUTE,level);

            int curIdx;
            int tempSize = dominateList.size();
            for (int i = 0; i < tempSize; i++) {
                curIdx = dominateList.get(i);
                population.get(curIdx).setAttribute(RANK_ATTRIBUTE,level + 1);

                rankIdx_[level][curIdx] = 0;
                rankIdx_[level + 1][curIdx] = 1;
            }
            num_ranks++;
        } else if (flag == 3) {
            individual.setAttribute(RANK_ATTRIBUTE,level + 1);
            num_ranks++;
        } else {
            individual.setAttribute(RANK_ATTRIBUTE,level);
            for (int i = 0; i < population.size(); i++) {
                if (rankIdx_[level][i] == 1) {
                    population.get(i).setAttribute(RANK_ATTRIBUTE,level + 1);

                    rankIdx_[level][i] = 0;
                    rankIdx_[level + 1][i] = 1;
                }
            }
            num_ranks++;
        }

        return num_ranks;
    }
}
