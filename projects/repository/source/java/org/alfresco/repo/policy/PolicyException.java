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
package org.alfresco.repo.policy;


/**
 * Base Policy Exception.
 * 
 * @author David Caruana
 */
public class PolicyException extends RuntimeException
{
    private static final long serialVersionUID = 3761122726173290550L;

    
    public PolicyException(String msg)
    {
       super(msg);
    }
    
    public PolicyException(String msg, Throwable cause)
    {
       super(msg, cause);
    }
}
