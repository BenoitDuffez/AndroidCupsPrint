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
 * State of print jobs
 */
public enum JobStateEnum {
  PENDING("pending"), PENDING_HELD("pending-held"), PROCESSING("processing"), PROCESSING_STOPPED("processing-stopped"), CANCELED(
      "canceled"), ABORTED("aborted"), COMPLETED("completed");

  private String value;

  JobStateEnum(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }

  public String getText() {
    return this.value;
  }

  public static JobStateEnum fromString(String value) {
    if (value != null) {
      for (JobStateEnum jobState : JobStateEnum.values()) {
        if (value.equalsIgnoreCase(jobState.value)) {
          return jobState;
        }
      }
    }
    return null;
  }
}
