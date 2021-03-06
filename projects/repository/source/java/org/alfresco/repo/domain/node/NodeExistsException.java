/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.domain.node;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

/**
 * Exception generated when a live node already exists for a given {@link NodeRef node reference}.
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class NodeExistsException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = -2122408334209855947L;
    
    private final Pair<Long, NodeRef> nodePair;

    public NodeExistsException(Pair<Long, NodeRef> nodePair, Throwable e)
    {
        super("Node already exists: " + nodePair, e);
        this.nodePair = nodePair;
    }

    public Pair<Long, NodeRef> getNodePair()
    {
        return nodePair;
    }
}
