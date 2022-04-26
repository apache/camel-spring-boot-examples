/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.example.springboot.numbers.participants;

import org.apache.camel.Exchange;
import org.apache.camel.Predicate;

import java.util.function.BiFunction;

/**
 * Provides various {@link Predicate}s that routing participants can send to the
 * dynamic router as rules to determine exchange suitability.
 */
public abstract class PredicateConstants {

    /**
     * Gets the message body as an integer and determines if the number is evenly
     * divided by the supplied integer.
     */
    public static final BiFunction<Exchange, Integer, Boolean> noRemainder = (e, m) ->
            e.getIn().getBody(Integer.class) % m == 0;

    /**
     * Determines if the message body is a number that is even.
     */
    public static final Predicate PREDICATE_EVEN = e -> noRemainder.apply(e, 2);

    /**
     * Determines if the message body is a number that is odd.
     */
    public static final Predicate PREDICATE_ODD = e -> e.getIn().getBody(Integer.class) % 2 != 0;

    /**
     * Determines if the message body is a number that is a multiple of 3.
     */
    public static final Predicate PREDICATE_THREES = e -> noRemainder.apply(e, 3);

    /**
     * Determines if the message body is a number that is a multiple of 4.
     */
    public static final Predicate PREDICATE_FOURS = e -> noRemainder.apply(e, 4);

    /**
     * Determines if the message body is a number that is a multiple of 5.
     */
    public static final Predicate PREDICATE_FIVES =  e -> noRemainder.apply(e, 5);

    /**
     * Determines if the message body is a number that is a multiple of 6.
     */
    public static final Predicate PREDICATE_SIXES = e -> noRemainder.apply(e, 6);

    /**
     * Determines if the message body is a number that is a multiple of 7.
     */
    public static final Predicate PREDICATE_SEVENS = e -> noRemainder.apply(e, 7);

    /**
     * Determines if the message body is a number that is a multiple of 8.
     */
    public static final Predicate PREDICATE_EIGHTS = e -> noRemainder.apply(e, 8);

    /**
     * Determines if the message body is a number that is a multiple of 9.
     */
    public static final Predicate PREDICATE_NINES = e -> noRemainder.apply(e, 9);

    /**
     * Determines if the message body is a number that is a multiple of 10.
     */
    public static final Predicate PREDICATE_TENS = e -> noRemainder.apply(e, 10);

    /**
     * If this predicate is prioritized with a higher number than {@link #PREDICATE_SEVENS}
     * or {@link #PREDICATE_THREES} or {@link #PREDICATE_EVEN}, then this will miss 7, 3,
     * and 2 in the accumulated list of prime numbers.
     */
    public static final Predicate PREDICATE_PRIMES = e -> {
        int n = e.getIn().getBody(Integer.class);
        // 2 is the first prime number
        if (n <= 2) {
            return n == 2;
        }
        // no other even numbers are prime
        if (n % 2 == 0) {
            return false;
        }
        // only some odd numbers might be prime
        int max = (int) Math.sqrt(n) + 1;
        for (int i = 3; i < max; i += 2) {
            if (n % i == 0)
                return false;
        }
        return true;
    };
}
