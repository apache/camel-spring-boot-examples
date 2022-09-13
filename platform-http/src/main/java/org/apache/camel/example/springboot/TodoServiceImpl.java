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
package org.apache.camel.example.springboot;

import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service("todoService")
public class TodoServiceImpl implements TodoService {

	private Map<Long, Todo> todos = new ConcurrentHashMap<>();
	private AtomicLong counter = new AtomicLong(1);

	@Override
	public Collection<Todo> findNotCompleted() {
		return todos.values().stream().filter(todo -> !todo.isCompleted()).collect(Collectors.toList());
	}

	@Override
	public Collection<Todo> findCompleted() {
		return todos.values().stream().filter(Todo::isCompleted).collect(Collectors.toList());
	}

	@Override
	public long deleteCompleted() {
		long completed = todos.values().stream().filter(Todo::isCompleted).count();

		todos.entrySet().removeIf(entry -> entry.getValue().isCompleted());

		return completed;
	}

	@Override
	public Todo findById(long id) {
		return todos.get(id);
	}

	@Override
	public void create(Todo todo) {
		todo.setId(counter.getAndIncrement());
		todos.put(todo.getId(), todo);
	}

	@Override
	public Todo update(Todo todo, long id) {
		Todo persistedTodo = todos.get(id);
		if (persistedTodo != null) {
			persistedTodo.setCompleted(todo.isCompleted());
			persistedTodo.setTitle(todo.getTitle());
			persistedTodo.setOrder(todo.getOrder());
			persistedTodo.setUrl(todo.getUrl());

			return persistedTodo;
		}
		return null;
	}

	@Override
	public void deleteOne(long id) {
		todos.remove(id);
	}

	@Override
	public Collection<Todo> listAll() {
		return todos.values();
	}
}
