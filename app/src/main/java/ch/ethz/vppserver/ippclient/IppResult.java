package ch.ethz.vppserver.ippclient;

import android.support.annotation.Nullable;

import java.util.List;

import ch.ethz.vppserver.schema.ippclient.AttributeGroup;

/**
 * Copyright (C) 2008 ITS of ETH Zurich, Switzerland, Sarah Windler Burri
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * See the GNU Lesser General Public License for more details. You should have received a copy of
 * the GNU Lesser General Public License along with this program; if not, see
 * <http://www.gnu.org/licenses/>.
 */

/*Notice
 * This file has been modified. It is not the original. 
 * ppd op patch as suggested at 
 * http://www.cups4j.org/forum/viewtopic.php?f=6&t=40
 * has been applied.
 */

public class IppResult {
    String _httpStatusResponse = null;

    String _ippStatusResponse = null;

    List<AttributeGroup> _attributeGroupList = null;

    byte buf[];

    public IppResult() {
    }

    /**
     * @return
     */
    @Nullable
    public String getHttpStatusResponse() {
        return _httpStatusResponse;
    }

    /**
     * @param statusResponse
     */
    public void setHttpStatusResponse(String statusResponse) {
        _httpStatusResponse = statusResponse;
    }

    /**
     * @return
     */
    @Nullable
    public String getIppStatusResponse() {
        return _ippStatusResponse;
    }

    /**
     * @param statusResponse
     */
    public void setIppStatusResponse(String statusResponse) {
        _ippStatusResponse = statusResponse;
    }

    /**
     * @return
     */
    @Nullable
    public List<AttributeGroup> getAttributeGroupList() {
        return _attributeGroupList;
    }

    /**
     * @param group
     */
    public void setAttributeGroupList(List<AttributeGroup> group) {
        _attributeGroupList = group;
    }

    @Nullable
    public byte[] getBuf() {
        return buf;
    }

    public void setBuf(byte[] buffer) {
        this.buf = buffer;
    }
}
