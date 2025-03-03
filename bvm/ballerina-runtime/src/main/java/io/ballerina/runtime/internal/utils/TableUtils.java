/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.internal.utils;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.TypeTags;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BRefValue;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.errors.ErrorCodes;
import io.ballerina.runtime.internal.errors.ErrorHelper;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.IteratorValue;
import io.ballerina.runtime.internal.values.MapValue;
import io.ballerina.runtime.internal.values.RegExpValue;
import io.ballerina.runtime.internal.values.TableValue;

import java.util.Map;

import static io.ballerina.runtime.internal.errors.ErrorReasons.TABLE_KEY_CYCLIC_VALUE_REFERENCE_ERROR;
import static io.ballerina.runtime.internal.utils.CycleUtils.Node;

/**
 * This class contains the utility methods required by the table implementation.
 *
 * @since 1.3.0
 */

public final class TableUtils {

    private TableUtils() {
    }

    /**
     * Generates a hash value which is same for the same shape.
     *
     * @param obj Ballerina value which the hash is generated from
     * @param parent Node linking to the parent object of 'obj'
     * @return The hash value
     */
    public static Long hash(Object obj, Node parent) {
        long result = 0;

        if (obj == null) {
            return 0L;
        }

        if (obj instanceof BRefValue refValue) {

            Node node = new Node(obj, parent);

            if (node.hasCyclesSoFar()) {
                throw ErrorCreator.createError(TABLE_KEY_CYCLIC_VALUE_REFERENCE_ERROR, ErrorHelper
                        .getErrorDetails(ErrorCodes.CYCLIC_VALUE_REFERENCE, TypeChecker.getType(obj)));
            }

            Type refType = TypeUtils.getImpliedType(refValue.getType());
            if (refType.getTag() == TypeTags.MAP_TAG || refType.getTag() == TypeTags.RECORD_TYPE_TAG) {
                MapValue<?, ?> mapValue = (MapValue<?, ?>) refValue;
                for (Map.Entry<?, ?> entry : mapValue.entrySet()) {
                    result = 31 * result + hash(entry.getKey(), node) +
                            (entry.getValue() == null ? 0 : hash(entry.getValue(),
                                    node));
                }
                return result;
            } else if (refType.getTag() == TypeTags.ARRAY_TAG || refType.getTag() == TypeTags.TUPLE_TAG) {
                ArrayValue arrayValue = (ArrayValue) refValue;
                IteratorValue<?> arrayIterator = arrayValue.getIterator();
                while (arrayIterator.hasNext()) {
                    result = 31 * result + hash(arrayIterator.next(), node);
                }
                return result;
            } else if (refType.getTag() == TypeTags.XML_TAG || refType.getTag() == TypeTags.XML_ELEMENT_TAG ||
                    refType.getTag() == TypeTags.XML_TEXT_TAG || refType.getTag() == TypeTags.XML_ATTRIBUTES_TAG ||
                    refType.getTag() == TypeTags.XML_COMMENT_TAG || refType.getTag() == TypeTags.XML_PI_TAG ||
                    refType.getTag() == TypeTags.XMLNS_TAG) {
                return (long) refValue.toString().hashCode();
            } else if (refType.getTag() == TypeTags.TABLE_TAG) {
                TableValue<?, ?> tableValue = (TableValue<?, ?>) refValue;
                IteratorValue<?> tableIterator = tableValue.getIterator();
                while (tableIterator.hasNext()) {
                    result = 31 * result + hash(tableIterator.next(), node);
                }
                return result;
            } else if (refValue instanceof RegExpValue) {
                return (long) refValue.toString().hashCode();
            } else {
                return (long) obj.hashCode();
            }
        } else if (obj instanceof Long l) {
            return l;
        } else {
            return (long) obj.hashCode();
        }
    }

    /**
     * Handles table insertion/store functionality.
     *
     * @param tableValue Table value which the values are inserted to
     * @param key        The key associated with the value
     * @param value      The value being inserted
     */
    public static void handleTableStore(TableValue<Object, Object> tableValue, Object key, Object value) {
        tableValue.put(key, value);
    }
}
