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
package org.apache.camel.example.springboot.cxf.otel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.validation.constraints.NotNull;

public class Results {
	@NotNull
	Map<Constants.NUM_TYPE, List<RandomNumber>> result;

	public Results() {
		result = new ConcurrentHashMap<>(2);
	}

	public Map<Constants.NUM_TYPE, List<RandomNumber>> getResult() {
		return result;
	}

	public int getEvenCount() {
		return count(Constants.NUM_TYPE.EVEN);
	}

	public int getOddCount() {
		return count(Constants.NUM_TYPE.ODD);
	}

	public Results addNumber(RandomNumber num) {
		addNumber(num.getType(), num);
		return this;
	}

	private void addNumber(Constants.NUM_TYPE evenOddKey, RandomNumber number) {
		final List<RandomNumber> values = Optional.ofNullable(result.get(evenOddKey)).orElseGet(() -> new ArrayList<>());
		values.add(number);
		result.put(evenOddKey, values);
	}

	private Integer count(Constants.NUM_TYPE key) {
		return Optional.ofNullable(result.get(key)).map(List::size).orElse(0);
	}

}
