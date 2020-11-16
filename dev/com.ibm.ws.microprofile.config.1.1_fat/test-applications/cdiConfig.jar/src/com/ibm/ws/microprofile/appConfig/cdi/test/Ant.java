/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.microprofile.appConfig.cdi.test;

/**
 *
 */
public class Ant extends Animal {

    public Ant(String name, String colour) {
        super(name, colour);
    }

    @Override
    public String toString() {
        return "A " + getColour() + " Ant called " + getName();
    }

}
