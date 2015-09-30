/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.rewarddefinition;

import hh.rewarddefinition.fitnessindicator.BinaryAdditiveEpsilonIndicator;
import hh.rewarddefinition.fitnessindicator.BinaryHypervolumeIndicator;
import hh.rewarddefinition.fitnessindicator.BinaryR2Indicator;
import hh.rewarddefinition.fitnessindicator.BinaryR3Indicator;
import hh.rewarddefinition.offspringparent.IBEABinaryIndicator;
import hh.rewarddefinition.offspringparent.ParentDomination;
import hh.rewarddefinition.offspringpopulation.OffspringEArchive;
import hh.rewarddefinition.offspringpopulation.OffspringParetoFront;
import hh.rewarddefinition.offspringpopulation.OffspringPopulationIndicator;
import hh.rewarddefinition.offspringpopulation.OffspringParetoRank;
import hh.rewarddefinition.populationcontribution.EArchiveContribution;
import hh.rewarddefinition.populationcontribution.ParetoFrontContribution;
import java.util.Arrays;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.util.TypedProperties;

/**
 * Factory methods for creating instances of ICreditDefinition
 *
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
    private RewardDefFactory() {
        super();
    }

    /**
     * Returns an instance of the hyper-heuristic factory
     *
     * @return
     */
    public static RewardDefFactory getInstance() {
        if (instance == null) {
            return new RewardDefFactory();
        } else {
            return instance;
        }
    }

    public IRewardDefinition getCreditDef(String name, TypedProperties prop, Problem problem) {
        IRewardDefinition credDef = null;
        //Get values from properties or use default values
        double satisfy = prop.getDouble("satisfy", 1.0);
        double disatisfy = prop.getDouble("disatisfy", 0.0);
        double neither = prop.getDouble("neither", 0.0);
        int rank = prop.getInt("rankThresh", 5);
        //reference point used to compute hypervolume
        double[] defRef = new double[problem.getNumberOfObjectives()];
        Arrays.fill(defRef, 2.0);
        double[] refPoint = prop.getDoubleArray("ref point", defRef);
        //ideal point used in R family indicators
        double[] defIdeal = new double[problem.getNumberOfObjectives()];
        Arrays.fill(defIdeal, 0.0);
        double[] idealPoint = prop.getDoubleArray("ref point", defIdeal);
        int numVec = prop.getInt("numVec", 100);
        //kappa parameter used in IBEA
        double kappa = prop.getDouble("kappa", 0.05);
        int numObj = problem.getNumberOfObjectives();
        switch (name) {
            case "ODP": //offspring dominates parent
                credDef = new ParentDomination(satisfy, neither, disatisfy);
                break;
            case "OPIAE": //offspring parent additive epsilon indicator using pareto front
                credDef = new IBEABinaryIndicator(new BinaryAdditiveEpsilonIndicator(), kappa, new Solution(refPoint));
                break;
            case "OPIHV": //offspring parent hypervolume indicator using pareto front
                credDef = new IBEABinaryIndicator(new BinaryHypervolumeIndicator(problem), kappa, new Solution(refPoint));
                break;
            case "OPIR2": //offspring parent hypervolume indicator using pareto front
                credDef = new IBEABinaryIndicator(new BinaryR2Indicator(numObj,numVec), kappa, new Solution(idealPoint));
                break;
//            case "OPIR3": //offspring parent hypervolume indicator using pareto front
//                credDef = new IBEABinaryIndicator(new BinaryR3Indicator(numObj,numVec), kappa, new Solution(idealPoint));
//                break;
            case "OPopPF": //in pareto front
                credDef = new OffspringParetoFront(satisfy, disatisfy);
                break;
            case "OPopPR": //within pareto rank
                credDef = new OffspringParetoRank(satisfy, disatisfy, rank);
                break;
            case "OPopEA": //in epsilon archive
                credDef = new OffspringEArchive(satisfy, disatisfy);
                break;
            case "OPopIPFAE": //offpsring improvement to additive epsilon indicator value for pareto front
                credDef = new OffspringPopulationIndicator(new BinaryAdditiveEpsilonIndicator(),RewardDefinedOn.PARETOFRONT,null);
                break;
            case "OPopIPFR2": //offpsring improvement to R2 indicator value for pareto front
                credDef = new OffspringPopulationIndicator(new BinaryR2Indicator(numObj,numVec),RewardDefinedOn.PARETOFRONT,new Solution(idealPoint));
                break;
//            case "OPopIPFR3": //offpsring improvement to R3 indicator value for pareto front
//                credDef = new OffspringPopulationIndicator(new BinaryR3Indicator(numObj,numVec),RewardDefinedOn.PARETOFRONT,new Solution(idealPoint));
//                break;
            case "OPopIEAAE": //offpsring improvement to additive epsilon indicator value for epsilon archive
                credDef = new OffspringPopulationIndicator(new BinaryAdditiveEpsilonIndicator(),RewardDefinedOn.ARCHIVE,null);
                break;
            case "OPopIEAR2": //offpsring improvement to R2 indicator value for epsilon archive
                credDef = new OffspringPopulationIndicator(new BinaryR2Indicator(numObj,numVec),RewardDefinedOn.ARCHIVE,new Solution(idealPoint));
                break;
//            case "OPopIEAR3": //offpsring improvement to R3 indicator value for epsilon archive
//                credDef = new OffspringPopulationIndicator(new BinaryR3Indicator(numObj, numVec),RewardDefinedOn.ARCHIVE,new Solution(idealPoint));
//                break;
            case "CPF": //contribution to pareto front
                credDef = new ParetoFrontContribution(satisfy, disatisfy);
                break;
            case "CEA": //contribution to epsilon archive
                credDef = new EArchiveContribution(satisfy, disatisfy);
                break;
            default:
                throw new IllegalArgumentException("No such credit defintion: " + name);
        }

        return credDef;
    }
}
