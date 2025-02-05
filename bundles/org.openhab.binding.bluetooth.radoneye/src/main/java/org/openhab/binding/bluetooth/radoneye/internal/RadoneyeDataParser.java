/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.radoneye.internal;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RadoneyeDataParser} is responsible for parsing data from Wave Plus device format.
 *
 * @author Peter Obel - Initial contribution
 */
@NonNullByDefault
public class RadoneyeDataParser {
    public static final String RADON = "radon";

    private static final int EXPECTED_DATA_LEN = 20;
    private static final int EXPECTED_VER_PLUS = 1;

    private static final Logger logger = LoggerFactory.getLogger(RadoneyeDataParser.class);

    private RadoneyeDataParser() {
    }

    public static Map<String, Number> parseRd200Data(int[] data) throws RadoneyeParserException {
        logger.debug("Parsed data length: {}", data.length);
        logger.debug("Parsed data: {}", data);
        if (data.length == EXPECTED_DATA_LEN) {
            final Map<String, Number> result = new HashMap<>();

            int[] radonArray = subArray(data, 2, 6);
            result.put(RADON, new BigDecimal(readFloat(radonArray) * 37));
            return result;
        } else {
            throw new RadoneyeParserException(String.format("Illegal data structure length '%d'", data.length));
        }
    }

    private static int intFromBytes(int lowByte, int highByte) {
        return (highByte & 0xFF) << 8 | (lowByte & 0xFF);
    }

    // Little endian
    private static int fromByteArrayLE(int[] bytes) {
        int result = 0;
        for (int i = 0; i < bytes.length; i++) {
            result |= (bytes[i] & 0xFF) << (8 * i);
        }
        return result;
    }

    private static float readFloat(int[] bytes) {
        int i = fromByteArrayLE(bytes);
        return Float.intBitsToFloat(i);
    }

    private static int[] subArray(int[] array, int beg, int end) {
        return Arrays.copyOfRange(array, beg, end + 1);
    }
}
