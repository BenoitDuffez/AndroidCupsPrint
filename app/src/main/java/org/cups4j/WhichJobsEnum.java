/**
 * Copyright (C) 2009 Harald Weyhing
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 * 
 * See the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * program; if not, see <http://www.gnu.org/licenses/>.
 */
package org.cups4j;

/**
 * Used while querying print jobs to define which jobs should be returned.
 * 
 * 
 */
public enum WhichJobsEnum {
	COMPLETED("completed"), NOT_COMPLETED("not-completed"), ALL("all");

	private String value;

	WhichJobsEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
