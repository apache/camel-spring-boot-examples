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

import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

/**
 * Provides various {@link Predicate}s that routing participants can send to the
 * dynamic router as rules to determine exchange suitability.
 */
public final class PredicateConstants {

    private PredicateConstants() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    /**
     * Gets the message body as an integer and determines if the number is evenly
     * divided by the supplied integer.
     */
    public static final BiPredicate<Integer, Integer> noRemainder = (n, m) -> n % m == 0;

    /**
     * Extracts the number from the exchange header.
     */
    public static final ToIntFunction<Exchange> extractValue = e ->
            e.getMessage().getHeader("number", Integer.class);

    public static final IntFunction<Predicate> noRemainderCurried = m ->
            e -> noRemainder.test(extractValue.applyAsInt(e), m);

    /**
     * Determines if the message body is a number that is even.
     */
    public static final Predicate PREDICATE_EVEN = e -> noRemainderCurried.apply(2).matches(e);

    /**
     * Determines if the message body is a number that is odd.
     */
    public static final Predicate PREDICATE_ODD = e -> !noRemainderCurried.apply(2).matches(e);

    /**
     * Determines if the message body is a number that is a multiple of 3.
     */
    public static final Predicate PREDICATE_THREES = e -> noRemainderCurried.apply(3).matches(e);

    /**
     * Determines if the message body is a number that is a multiple of 4.
     */
    public static final Predicate PREDICATE_FOURS = e -> noRemainderCurried.apply(4).matches(e);

    /**
     * Determines if the message body is a number that is a multiple of 5.
     */
    public static final Predicate PREDICATE_FIVES = e -> noRemainderCurried.apply(5).matches(e);

    /**
     * Determines if the message body is a number that is a multiple of 6.
     */
    public static final Predicate PREDICATE_SIXES = e -> noRemainderCurried.apply(6).matches(e);

    /**
     * Determines if the message body is a number that is a multiple of 7.
     */
    public static final Predicate PREDICATE_SEVENS = e -> noRemainderCurried.apply(7).matches(e);

    /**
     * Determines if the message body is a number that is a multiple of 8.
     */
    public static final Predicate PREDICATE_EIGHTS = e -> noRemainderCurried.apply(8).matches(e);

    /**
     * Determines if the message body is a number that is a multiple of 9.
     */
    public static final Predicate PREDICATE_NINES = e -> noRemainderCurried.apply(9).matches(e);

    /**
     * Determines if the message body is a number that is a multiple of 10.
     */
    public static final Predicate PREDICATE_TENS = e -> noRemainderCurried.apply(10).matches(e);

    /**
     * If this predicate is prioritized with a higher number than {@link #PREDICATE_SEVENS}
     * or {@link #PREDICATE_THREES} or {@link #PREDICATE_EVEN}, then this will miss 7, 3,
     * and 2 in the accumulated list of prime numbers.
     */
    public static final Predicate PREDICATE_PRIMES = e -> {
        int n = extractValue.applyAsInt(e);
        if (n <= 0) {
            return false;
        }
        // handle cases for 1, 2 and 3
        if (n <= 3) {
            return n > 1;
        }
        // check if number is divisible by 2 or 3
        if (noRemainder.test(n, 2) || noRemainder.test(n, 3)) {
            return false;
        }
        int i = 5;
        while (i * i <= n) {
            if (noRemainder.test(n, i) || noRemainder.test(n, (i + 2))) {
                return false;
            }
            i += 6;
        }
        return true;
    };
}
