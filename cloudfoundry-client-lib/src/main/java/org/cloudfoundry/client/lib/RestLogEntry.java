/*
 * Copyright 2009-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cloudfoundry.client.lib;

import java.net.URI;
import java.time.LocalTime;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

/**
 * RestLogEntry containing the values logged for each REST call
 *
 * @author: Thomas Risberg
 */
public class RestLogEntry {

	private HttpMethod method;
	private URI uri;
	private String status;
	private HttpStatus httpStatus;
	private String message;
	private final long duration;
	private final String time;
	
	public RestLogEntry(HttpMethod method, URI uri, String status, HttpStatus httpStatus, String message, long duration) {
		this.method = method;
		this.uri = uri;
		this.status = status;
		this.httpStatus = httpStatus;
		this.message = message;
		this.duration = duration;
		this.time = LocalTime.now().toString();
	}

	public HttpMethod getMethod() {
		return method;
	}

	public URI getUri() {
		return uri;
	}

	public String getStatus() {
		return status;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	public String getMessage() {
		return message;
	}
	
	public long getDuration() {
		return duration;
	}
	
	public String getTime() {
		return time;
	}
}
