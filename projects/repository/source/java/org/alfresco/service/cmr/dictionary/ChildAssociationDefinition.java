/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/lgpl.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.service.cmr.dictionary;

/**
 * Read-only definition of a Child Association.
 * 
 * @author David Caruana
 *
 */
public interface ChildAssociationDefinition extends AssociationDefinition
{

    /**
     * @return the required name of children (or null if none)
     */
    public String getRequiredChildName();
    
    /**
     * @return whether duplicate child names allowed within this association? 
     */
    public boolean getDuplicateChildNamesAllowed();

}
