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

import org.apache.camel.ProducerTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class OddServiceImpl implements OddService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OddServiceImpl.class);

	@Autowired
	ProducerTemplate producerTemplate;

	@Override
	public RandomNumber check(RandomNumber number) {
		if (number.getNumber() % 2 == 1) {
			//register
			producerTemplate.sendBody("direct:register", new RandomNumber().setNumber(number.getNumber()).setType(Constants.NUM_TYPE.ODD));
		} else {
			LOGGER.info("skip {}, it is even", number);
		}
		return number;
	}
}
