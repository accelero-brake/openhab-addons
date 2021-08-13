/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.bh1750.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link BH1750BindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author accelero-brake - Initial contribution
 */
@NonNullByDefault
public class BH1750BindingConstants {

    private static final String BINDING_ID = "bh1750";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_LIGHT_SENSOR = new ThingTypeUID(BINDING_ID, "lightSensor");

    // List of all Channel ids
    public static final String CHANNEL_LIGHT_MEASURE = "lightMeasure";
}
