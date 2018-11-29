package ch.ethz.vppserver.ippclient

import ch.ethz.vppserver.schema.ippclient.AttributeGroup

/**
 * Copyright (C) 2008 ITS of ETH Zurich, Switzerland, Sarah Windler Burri
 *
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *
 * See the GNU Lesser General Public License for more details. You should have received a copy of
 * the GNU Lesser General Public License along with this program; if not, see
 * <http:></http:>//www.gnu.org/licenses/>.
 */

/*Notice
 * This file has been modified. It is not the original.
 * ppd op patch as suggested at
 * http://www.cups4j.org/forum/viewtopic.php?f=6&t=40
 * has been applied.
 */

class IppResult {
    var httpStatusResponse: String? = null
    var ippStatusResponse: String? = null
    var attributeGroupList: List<AttributeGroup>? = null
    var buf: ByteArray? = null
}
