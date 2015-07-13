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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomAdaptor;

/**
 * Same methods as PRNG, but does not have static methods. Intended to be used
 * for applications when using multiple threads. Non-Static methods for generating
 * random or pseudo-random numbers. Any source of randomness implementing the
 * {@link Random} interface can be used as the random source.
 * <p>
 * {@code PRNG} relies on an underlying source of randomness, and inherits
 * thread safety from the underlying implementation. Unless the underlying
 * implementation is known to be thread-safe, assume that {@code PRNG} is not
 * thread-safe.
 */
public class ParallelPRNG implements Serializable{
    private static final long serialVersionUID = 3687060339895977192L;
    
    /**
     * Internal source of randomness.
     */
    private final Random random;

    /**
     * Constructor does not meet singleton pattern
     */
    public ParallelPRNG() {
        this.random  = new RandomAdaptor(new MersenneTwister());
    }

    /**
     * While the preferred method for seeding PRNGs is through the
     * {@code setRandom} method since methods providing more entropy may be
     * available to specific implementations, this method is intended to provide
     * a uniform interface for setting the seed.
     *
     * @param seed the new seed
     */
    public synchronized void setSeed(long seed) {
        random.setSeed(seed);
    }

    /**
     * Sets the source of randomness to be used.
     *
     * @param random the source of randomness to be used
     */
    public void setRandom(Random random) {
        random = random;
    }

    /**
     * Returns the source of randomness currently used.
     *
     * @return the source of randomness currently used
     */
    public Random getRandom() {
        return random;
    }

    /**
     * Returns the next random, uniformly distributed {@code float} value
     * between 0.0 and 1.0.
     *
     * @return the next random, uniformly distributed {@code float} value
     * between 0.0 and 1.0
     */
    public float nextFloat() {
        return random.nextFloat();
    }

    /**
     * Returns the next random, uniformly distributed {@code float} value
     * between {@code min} and {@code max}.
     *
     * @return the next random, uniformly distributed {@code float} value
     * between {@code min} and {@code max}
     */
    public float nextFloat(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    /**
     * Returns the next random, uniformly distributed {@code double} value
     * between 0.0 and 1.0.
     *
     * @return the next random, uniformly distributed {@code double} value
     * between 0.0 and 1.0
     */
    public double nextDouble() {
        return random.nextDouble();
    }

    /**
     * Returns the next random, uniformly distributed {@code double} value
     * between {@code min} and {@code max}.
     *
     * @return the next random, uniformly distributed {@code double} value
     * between {@code min} and {@code max}
     */
    public double nextDouble(double min, double max) {
        return min + random.nextDouble() * (max - min);
    }

    /**
     * Returns the next random, uniformly distributed {@code int} value between
     * {@code Integer.MIN_VALUE} and {@code Integer.MAX_VALUE} .
     *
     * @return the next random, uniformly distributed {@code int} value between
     * {@code Integer.MIN_VALUE} and {@code Integer.MAX_VALUE}.
     */
    public int nextInt() {
        return random.nextInt();
    }

    /**
     * Returns the next random, uniformly distributed {@code int} value between
     * {@code 0} (inclusive) and {@code n} (exclusive).
     *
     * @return the next random, uniformly distributed {@code int} value between
     * {@code 0} (inclusive) and {@code n} (exclusive).
     */
    public int nextInt(int n) {
        return random.nextInt(n);
    }

    /**
     * Returns the next random, uniformly distributed {@code int} value between
     * {@code min} and {@code max} (both inclusive).
     *
     * @return the next random, uniformly distributed {@code int} value between
     * {@code min} and {@code max} (both inclusive).
     */
    public int nextInt(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    /**
     * Returns the next random, uniformly distributed {@code boolean} value.
     *
     * @return the next random, uniformly distributed {@code boolean} value.
     */
    public boolean nextBoolean() {
        return random.nextBoolean();
    }

    /**
     * Returns the next random, Gaussian distributed {@code double} value with
     * mean {@code 0.0} and standard deviation {@code 1.0}.
     *
     * @return the next random, Gaussian distributed {@code double} value with
     * mean {@code 0.0} and standard deviation {@code 1.0}.
     */
    public double nextGaussian() {
        return random.nextGaussian();
    }

    /**
     * Returns the next random, Gaussian distributed {@code double} value with
     * mean {@code mean} and standard deviation {@code stdev}.
     *
     * @return the next random, Gaussian distributed {@code double} value with
     * mean {@code mean} and standard deviation {@code stdev}.
     */
    public double nextGaussian(double mean, double stdev) {
        return stdev * random.nextGaussian() + mean;
    }

    /**
     * Shuffles the elements of the specified array using the same algorithm as
     * {@link Collections#shuffle}.
     *
     * @param <T> the type of element stored in the array
     * @param array the array to be shuffled
     */
    public <T> void shuffle(T[] array) {
        for (int i = array.length - 1; i >= 1; i--) {
            int j = nextInt(i + 1);

            if (i != j) {
                T temp = array[i];
                array[i] = array[j];
                array[j] = temp;
            }
        }
    }

    /**
     * Shuffles the elements of the specified array using the same algorithm as
     * {@link Collections#shuffle}.
     *
     * @param array the array to be shuffled
     */
    public void shuffle(double[] array) {
        for (int i = array.length - 1; i >= 1; i--) {
            int j = nextInt(i + 1);

            if (i != j) {
                double temp = array[i];
                array[i] = array[j];
                array[j] = temp;
            }
        }
    }

    /**
     * Shuffles the elements of the specified array using the same algorithm as
     * {@link Collections#shuffle}.
     *
     * @param array the array to be shuffled
     */
    public void shuffle(float[] array) {
        for (int i = array.length - 1; i >= 1; i--) {
            int j = nextInt(i + 1);

            if (i != j) {
                float temp = array[i];
                array[i] = array[j];
                array[j] = temp;
            }
        }
    }

    /**
     * Shuffles the elements of the specified array using the same algorithm as
     * {@link Collections#shuffle}.
     *
     * @param array the array to be shuffled
     */
    public void shuffle(long[] array) {
        for (int i = array.length - 1; i >= 1; i--) {
            int j = nextInt(i + 1);

            if (i != j) {
                long temp = array[i];
                array[i] = array[j];
                array[j] = temp;
            }
        }
    }

    /**
     * Shuffles the elements of the specified array using the same algorithm as
     * {@link Collections#shuffle}.
     *
     * @param array the array to be shuffled
     */
    public void shuffle(int[] array) {
        for (int i = array.length - 1; i >= 1; i--) {
            int j = nextInt(i + 1);

            if (i != j) {
                int temp = array[i];
                array[i] = array[j];
                array[j] = temp;
            }
        }
    }

    /**
     * Shuffles the elements of the specified array using the same algorithm as
     * {@link Collections#shuffle}.
     *
     * @param array the array to be shuffled
     */
    public void shuffle(short[] array) {
        for (int i = array.length - 1; i >= 1; i--) {
            int j = nextInt(i + 1);

            if (i != j) {
                short temp = array[i];
                array[i] = array[j];
                array[j] = temp;
            }
        }
    }

    /**
     * Shuffles the elements of the specified array using the same algorithm as
     * {@link Collections#shuffle}.
     *
     * @param array the array to be shuffled
     */
    public void shuffle(byte[] array) {
        for (int i = array.length - 1; i >= 1; i--) {
            int j = nextInt(i + 1);

            if (i != j) {
                byte temp = array[i];
                array[i] = array[j];
                array[j] = temp;
            }
        }
    }

    /**
     * Shuffles the elements of the specified array using the same algorithm as
     * {@link Collections#shuffle}.
     *
     * @param array the array to be shuffled
     */
    public void shuffle(boolean[] array) {
        for (int i = array.length - 1; i >= 1; i--) {
            int j = nextInt(i + 1);

            if (i != j) {
                boolean temp = array[i];
                array[i] = array[j];
                array[j] = temp;
            }
        }
    }

    /**
     * Shuffles the elements of the specified list by invoking the
     * {@link Collections#shuffle} method with the internal {@link Random} in
     * this PRNG.
     *
     * @param <T> the type of elements stored in the list
     * @param list the list to be shuffled
     */
    public <T> void shuffle(List<T> list) {
        Collections.shuffle(list, random);
    }

    /**
     * Returns a randomly selected item from the specified list.
     *
     * @param <T> the type of the elements stored in the list
     * @param list the list from which the item is randomly selected
     * @return a randomly selected item from the specified list
     */
    public <T> T nextItem(List<T> list) {
        return list.get(nextInt(list.size()));
    }

}
