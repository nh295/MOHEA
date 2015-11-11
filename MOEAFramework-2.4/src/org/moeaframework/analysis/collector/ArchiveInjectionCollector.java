/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.moeaframework.analysis.collector;

import org.moeaframework.core.EpsilonBoxDominanceArchive;
import org.moeaframework.core.EpsilonBoxEvolutionaryAlgorithm;

/**
 *
 * @author SEAK2
 */
public class ArchiveInjectionCollector implements Collector{
    
    /**
	 * The algorithm instance used by this collector; or {@code null} if this 
	 * collector has not yet been attached.
	 */
	private final EpsilonBoxEvolutionaryAlgorithm algorithm;

	/**
	 * Constructs an unattached collector for recording the number of 
	 * Archival injections detected by collector
	 */
	public ArchiveInjectionCollector() {
		this(null);
	}
	
	/**
	 * Constructs a collector for recording the number of archive injections
	 * 
	 * @param algorithm the algorithm this collector records data from
	 */
	public ArchiveInjectionCollector(
			EpsilonBoxEvolutionaryAlgorithm algorithm) {
		super();
		this.algorithm = algorithm;
	}

	@Override
	public void collect(Accumulator accumulator) {
		EpsilonBoxDominanceArchive archive = algorithm.getArchive();

		if (archive != null) {
			accumulator.add("Number of Injections", archive
					.getInjectionCount());
		}
	}

	@Override
	public AttachPoint getAttachPoint() {
		return AttachPoint.isSubclass(EpsilonBoxEvolutionaryAlgorithm.class);
	}

	@Override
	public Collector attach(Object object) {
		return new ArchiveInjectionCollector(
				(EpsilonBoxEvolutionaryAlgorithm)object);
	}
    
}
