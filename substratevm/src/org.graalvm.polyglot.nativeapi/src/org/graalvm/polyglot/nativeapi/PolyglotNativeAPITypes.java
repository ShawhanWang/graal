/*
 * Copyright (c) 2018, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.graalvm.polyglot.nativeapi;

import org.graalvm.nativeimage.ObjectHandle;
import org.graalvm.nativeimage.c.CContext;
import org.graalvm.nativeimage.c.constant.CEnum;
import org.graalvm.nativeimage.c.constant.CEnumValue;
import org.graalvm.nativeimage.c.function.CFunctionPointer;
import org.graalvm.nativeimage.c.function.InvokeCFunctionPointer;
import org.graalvm.nativeimage.c.struct.CField;
import org.graalvm.nativeimage.c.struct.CPointerTo;
import org.graalvm.nativeimage.c.struct.CStruct;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.polyglot.nativeapi.PolyglotNativeAPI.PolyglotIsolateThread;
import org.graalvm.word.PointerBase;
import org.graalvm.word.UnsignedWord;

import com.oracle.svm.core.c.CTypedef;

@CContext(PolyglotNativeAPICContext.class)
class PolyglotNativeAPITypes {

    @CEnum("poly_status")
    enum PolyglotStatus {
        poly_ok,

        poly_invalid_arg,

        poly_object_expected,

        poly_string_expected,

        poly_name_expected,

        poly_function_expected,

        poly_number_expected,

        poly_boolean_expected,

        poly_array_expected,

        poly_generic_failure,

        poly_pending_exception,

        poly_cancelled,

        poly_status_last;

        @CEnumValue
        public native int getCValue();
    }

    @CStruct("poly_extended_error_info")
    interface PolyglotExtendedErrorInfo extends PointerBase {

        @CField("error_code")
        void setErrorCode(int errorCode);

        @CField("error_message")
        void setErrorMessage(CCharPointer errorMessage);

    }

    @CPointerTo(value = PolyglotExtendedErrorInfo.class)
    interface ExtendedErrorInfoPointer extends PointerBase {

        void write(PolyglotExtendedErrorInfo value);

    }

    @CPointerTo(nameOfCType = "size_t")
    interface SizeTPointer extends PointerBase {

        UnsignedWord read();

        void write(UnsignedWord value);

    }

    @CPointerTo(nameOfCType = "poly_engine")
    @CTypedef(name = "poly_engine")
    interface PolyglotEnginePointer extends PointerBase, ObjectHandle {
    }

    @CPointerTo(nameOfCType = "poly_engine")
    interface PolyglotEnginePointerPointer extends PointerBase, ObjectHandle {

        void write(ObjectHandle value);

    }

    @CPointerTo(nameOfCType = "poly_handle")
    @CTypedef(name = "poly_handle")
    interface PolyglotHandlePointer extends PointerBase, ObjectHandle {

    }

    @CPointerTo(nameOfCType = "poly_context")
    @CTypedef(name = "poly_context")
    interface PolyglotContextPointer extends PointerBase, ObjectHandle {
    }

    @CPointerTo(nameOfCType = "poly_context")
    interface PolyglotContextPointerPointer extends PointerBase, ObjectHandle {

        void write(ObjectHandle value);

    }

    @CPointerTo(nameOfCType = "poly_value")
    @CTypedef(name = "poly_value")
    interface PolyglotValuePointer extends PointerBase, ObjectHandle {
    }

    @CPointerTo(nameOfCType = "poly_value")
    interface PolyglotValuePointerPointer extends PointerBase, ObjectHandle {

        PolyglotValuePointer read(int index);

        void write(ObjectHandle value);

        void write(int index, ObjectHandle value);
    }

    @CPointerTo(nameOfCType = "poly_callback_info")
    @CTypedef(name = "poly_callback_info")
    interface PolyglotCallbackInfo extends ObjectHandle, PointerBase {
    }

    @CTypedef(name = "poly_callback")
    interface PolyglotCallbackPointer extends CFunctionPointer {
        @InvokeCFunctionPointer
        PolyglotValuePointer invoke(PolyglotIsolateThread ithread, PolyglotCallbackInfo info);
    }

}
