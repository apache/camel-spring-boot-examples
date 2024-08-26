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

import java.util.Objects;
import java.util.Optional;

import jakarta.validation.constraints.NotNull;

public class RandomNumber {

	@NotNull
	Integer number;

	Constants.NUM_TYPE type;

	public RandomNumber() {
	}

	public RandomNumber(final Integer number) {
		this.number = number;
	}

	public Integer getNumber() {
		return number;
	}

	public RandomNumber setNumber(final Integer number) {
		this.number = number;
		return this;
	}

	public Constants.NUM_TYPE getType() {
		return type;
	}

	public RandomNumber setType(final Constants.NUM_TYPE type) {
		this.type = type;
		return this;
	}

	@Override
	public String toString() {
		return Optional.ofNullable(number).map(Objects::toString).orElse("");
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final RandomNumber that = (RandomNumber) o;
		return Objects.equals(getNumber(), that.getNumber());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getNumber());
	}
}
