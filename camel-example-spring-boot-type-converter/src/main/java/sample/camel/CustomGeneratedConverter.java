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
package sample.camel;

import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.apache.camel.TypeConverter;
import org.apache.camel.TypeConverters;

@Converter(generateLoader = true)
public class CustomGeneratedConverter implements TypeConverters {

	@Converter
	public Person toPerson(byte[] data, Exchange exchange){
		TypeConverter converter = exchange.getContext().getTypeConverter();

		String s = converter.convertTo(String.class, data);

		final String[] splitData = s.split(" ");
		final String firstName = splitData[0];
		final String lastName = splitData[1];
		int age = Integer.parseInt(splitData[2]);

		return new Person(firstName, lastName, age);
	}

}
