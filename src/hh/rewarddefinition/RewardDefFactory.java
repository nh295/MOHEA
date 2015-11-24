/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.rewarddefinition;

import hh.rewarddefinition.fitnessindicator.AdditiveEpsilonIndicator;
import hh.rewarddefinition.fitnessindicator.HypervolumeIndicator;
import hh.rewarddefinition.fitnessindicator.R2Indicator;
import hh.rewarddefinition.offspringparent.OPBinaryIndicator;
import hh.rewarddefinition.offspringparent.ParentDomination;
import hh.rewarddefinition.offspringpopulation.OffspringEArchive;
import hh.rewarddefinition.offspringpopulation.OffspringParetoFront;
import hh.rewarddefinition.offspringpopulation.OffspringPopulationIndicator;
import hh.rewarddefinition.populationcontribution.EArchiveContribution;
import hh.rewarddefinition.populationcontribution.IndicatorContribution;
import hh.rewarddefinition.populationcontribution.ParetoFrontContribution;
import java.util.Arrays;
import org.moeaframework.core.Problem;
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
                credDef = new OPBinaryIndicator(new AdditiveEpsilonIndicator(), kappa,problem);
                break;
            case "OPIHV": //offspring parent hypervolume indicator using pareto front
                credDef = new OPBinaryIndicator(new HypervolumeIndicator(problem), kappa,problem);
                break;
            case "OPIR2": //offspring parent hypervolume indicator using pareto front
                credDef = new OPBinaryIndicator(new R2Indicator(numObj,numVec), kappa,problem);
                break;
//            case "OPIR3": //offspring parent hypervolume indicator using pareto front
//                credDef = new IBEABinaryIndicator(new BinaryR3Indicator(numObj,numVec), kappa, new Solution(idealPoint));
//                break;
            case "OPopPF": //in pareto front
                credDef = new OffspringParetoFront(satisfy, disatisfy);
                break;
            case "OPopEA": //in epsilon archive
                credDef = new OffspringEArchive(satisfy, disatisfy);
                break;
            case "OPopIPFAE": //offpsring improvement to additive epsilon indicator value for pareto front
                credDef = new OffspringPopulationIndicator(new AdditiveEpsilonIndicator(),RewardDefinedOn.PARETOFRONT);
                break;
            case "OPopIPFHV":
                //offpsring improvement to hypervolume of pareto front
                credDef = new OffspringPopulationIndicator(new HypervolumeIndicator(problem),RewardDefinedOn.PARETOFRONT);
                break;
            case "OPopIPFR2": //offpsring improvement to R2 indicator value for pareto front
                credDef = new OffspringPopulationIndicator(new R2Indicator(numObj,numVec),RewardDefinedOn.PARETOFRONT);
                break;
//            case "OPopIPFR3": //offpsring improvement to R3 indicator value for pareto front
//                credDef = new OffspringPopulationIndicator(new BinaryR3Indicator(numObj,numVec),RewardDefinedOn.PARETOFRONT,new Solution(idealPoint));
//                break;
            case "OPopIEAAE": //offpsring improvement to additive epsilon indicator value for epsilon archive
                credDef = new OffspringPopulationIndicator(new AdditiveEpsilonIndicator(),RewardDefinedOn.ARCHIVE);
                break;
            case "OPopIEAHV":
                //offpsring improvement to hypervolume of pareto front
                credDef = new OffspringPopulationIndicator(new HypervolumeIndicator(problem), RewardDefinedOn.ARCHIVE);
                break;
            case "OPopIEAR2": //offpsring improvement to R2 indicator value for epsilon archive
                credDef = new OffspringPopulationIndicator(new R2Indicator(numObj,numVec),RewardDefinedOn.ARCHIVE);
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
            case "CHVPF": //Contribution to HV of Pareto front
                credDef = new IndicatorContribution(new HypervolumeIndicator(problem), RewardDefinedOn.PARETOFRONT);
                break;
            case "CHVEA": //Contribution to HV of epsilon archive
                credDef = new IndicatorContribution(new HypervolumeIndicator(problem), RewardDefinedOn.ARCHIVE);
                break;
            case "CR2PF": //Contribution to R2 of Pareto front
                credDef = new IndicatorContribution(new R2Indicator(numObj,numVec), RewardDefinedOn.PARETOFRONT);
                break;
            case "CR2EA": //Contribution to R2 of epsilon archive
                credDef = new IndicatorContribution(new R2Indicator(numObj,numVec), RewardDefinedOn.ARCHIVE);
                break;
            default:
                throw new IllegalArgumentException("No such credit defintion: " + name);
        }

        return credDef;
    }
}
