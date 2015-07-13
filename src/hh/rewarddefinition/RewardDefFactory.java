/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.rewarddefinition;

import hh.rewarddefinition.populationcontribution.EArchiveContribution;
import hh.rewarddefinition.populationcontribution.ParetoFrontContribution;
import hh.rewarddefinition.populationcontribution.ParetoRankContribution;
import hh.rewarddefinition.offspringpopulation.OffspringEArchive;
import hh.rewarddefinition.offspringpopulation.OffspringParetoFront;
import hh.rewarddefinition.offspringpopulation.OffspringParetoRank;
import hh.rewarddefinition.offspringparent.ParentDomination;
import org.moeaframework.util.TypedProperties;

/**
 * Factory methods for creating instances of ICreditDefinition
 * @author SEAK2
 */
public class RewardDefFactory {

    /**
     * The default problem factory.
     */
    private static RewardDefFactory instance;

    /**
     * private constructor to enforce singleton
     */
    private RewardDefFactory(){
        super();
    }
    
    /**
     * Returns an instance of the hyper-heuristic factory
     * @return 
     */
    public static RewardDefFactory getInstance(){
        if(instance==null)
            return new RewardDefFactory();
        else 
            return instance;
    }
    
    public IRewardDefinition getCreditDef(String name, TypedProperties prop){
        IRewardDefinition credDef = null;
        //Get values from properties or use default values
        double satisfy = prop.getDouble("satisfy", 1.0);
        double disatisfy = prop.getDouble("disatisfy", 0.0);
        double neither = prop.getDouble("neither", 0.0);
        int rank = prop.getInt("rankThresh", 5);
        switch(name){
            case "ODP": //offspring dominates parent
                credDef = new ParentDomination(satisfy, neither, disatisfy);
                break;
            case "IPF": //in pareto front
                credDef = new OffspringParetoFront(satisfy, disatisfy);
                break;
            case "IPR": //within pareto rank
                credDef = new OffspringParetoRank(satisfy, disatisfy,rank);
                break;
            case "IEA": //in epsilon archive
                credDef = new OffspringEArchive(satisfy, disatisfy);
                break;
            case "CPF": //contribution to pareto front
                credDef = new ParetoFrontContribution(satisfy,disatisfy);
                break;
            case "CPR": //contribution to pareto rank
                credDef = new ParetoRankContribution(satisfy, disatisfy,rank);
                break;
            case "CEA": //contribution to epsilon archive
                credDef = new EArchiveContribution(satisfy, disatisfy);
                break;
            default: throw new IllegalArgumentException("No such credit defintion: " + name);
        }
            
        return credDef;
    }
}
