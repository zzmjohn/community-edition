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
package org.alfresco.web.ui.common.component;

import java.io.Serializable;

/**
 * @author Kevin Roast
 */
public interface IBreadcrumbHandler extends Serializable
{
   /**
    * Override Object.toString()
    * 
    * @return the element display label for this handler instance.
    */
   public String toString();
   
   /**
    * Perform appropriate processing logic and then return a JSF navigation outcome.
    * This method will be called by the framework when the handler instance is selected by the user.
    * 
    * @param breadcrumb    The UIBreadcrumb component that caused the navigation
    * 
    * @return JSF navigation outcome
    */
   public String navigationOutcome(UIBreadcrumb breadcrumb);
}
