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

/**
 * The {@link BH1750Configuration} class contains fields mapping thing configuration parameters.
 *
 * @author accelero-brake - Initial contribution
 */
public class BH1750Configuration {

    public String address;
    public int busNumber;
    public int refresh;

    public String getAddress() {
        return address;
    }

    public int getAddressHex() {
        return Integer.parseInt(address, 16);
    }

    public int getBusNumber() {
        return busNumber;
    }

    public int getRefresh() {
        return refresh;
    }
}
