/*
 * Copyright (c) 2013, 2017, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.svm.hosted.c.codegen;

import static com.oracle.svm.core.util.VMError.shouldNotReachHere;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.graalvm.nativeimage.c.function.CFunctionPointer;
import org.graalvm.nativeimage.c.function.InvokeCFunctionPointer;
import org.graalvm.word.SignedWord;
import org.graalvm.word.UnsignedWord;

import com.oracle.svm.core.util.InterruptImageBuilding;
import com.oracle.svm.core.util.UserError;
import com.oracle.svm.hosted.NativeImageOptions;
import com.oracle.svm.hosted.NativeImageOptions.CStandards;
import com.oracle.svm.hosted.c.NativeLibraries;
import com.oracle.svm.hosted.c.info.ElementInfo;
import com.oracle.svm.hosted.c.info.EnumInfo;
import com.oracle.svm.hosted.c.info.InfoTreeBuilder;
import com.oracle.svm.hosted.c.info.PointerToInfo;
import com.oracle.svm.hosted.c.info.StructInfo;

import jdk.vm.ci.meta.MetaAccessProvider;
import jdk.vm.ci.meta.ResolvedJavaType;

public class CSourceCodeWriter {

    private static final String CHARSET = "US-ASCII";
    private static final String INDENT4 = "    ";
    public static final String C_SOURCE_FILE_EXTENSION = ".c";

    private final List<String> lines;
    private final StringBuilder currentLine;

    private int indentLevel = 0;
    protected final Path tempDirectory;

    public CSourceCodeWriter(Path tempDirectory) {
        this.tempDirectory = tempDirectory;
        this.lines = new ArrayList<>();
        this.currentLine = new StringBuilder(100);
    }

    public int currentLineNumber() {
        return lines.size() + 1;
    }

    public String getLine(int lineNumber) {
        int index = lineNumber - 1;
        if (index >= 0 && index < lines.size()) {
            return lines.get(index);
        }
        return "";
    }

    public void includeFiles(List<String> headerFiles) {
        for (String headerFile : headerFiles) {
            if (!((headerFile.startsWith("<") && headerFile.endsWith(">")) || (headerFile.startsWith("\"") && headerFile.endsWith("\"")))) {
                throw UserError.abort("header file name must be surrounded by <...> or \"...\": " + headerFile);
            }
            Path headerFilePath = Paths.get(headerFile);
            appendln("#include " + headerFilePath.toString());
        }
    }

    public CSourceCodeWriter printf(String firstArg, String secondArg) {
        append("printf(\"" + firstArg + "\\n\", " + secondArg + ")");
        return this;
    }

    public CSourceCodeWriter printf(String firstArg, String secondArg, String thirdArg) {
        append("printf(\"" + firstArg + "\\n\", " + secondArg + ", " + thirdArg + ")");
        return this;
    }

    public CSourceCodeWriter indents() {
        assert currentLine.length() == 0 : "indenting in the middle of a line";
        for (int i = 0; i < indentLevel; i++) {
            append(INDENT4);
        }
        return this;
    }

    public void indent() {
        indentLevel++;
    }

    public void outdent() {
        indentLevel--;
    }

    public void semicolon() {
        appendln(";");
    }

    public void appendln(String str) {
        append(str);
        appendln();
    }

    public void appendln() {
        assert currentLine.indexOf("\n") == -1 : "line must not contain newline character";
        lines.add(currentLine.toString());
        currentLine.delete(0, currentLine.length());
    }

    public CSourceCodeWriter append(String str) {
        assert !str.contains("\n") : "line must not contain newline character";
        currentLine.append(str);
        return this;
    }

    public Path writeFile(String fileName) {
        return writeFile(fileName, true);
    }

    public Path writeFile(String fileName, boolean ensureCorrectExtension) {
        assert currentLine.length() == 0 : "last line not finished";

        String fixedFileName = fileName;
        if (!fileName.endsWith(C_SOURCE_FILE_EXTENSION) && ensureCorrectExtension) {
            fixedFileName = fileName.concat(C_SOURCE_FILE_EXTENSION);
        }

        Path outputFile = tempDirectory.resolve(fixedFileName);
        Charset charset = Charset.forName(CHARSET);

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile, charset)) {
            for (String line : lines) {
                writer.write(line);
                writer.write("\n");
            }
        } catch (ClosedByInterruptException ex) {
            throw new InterruptImageBuilding();
        } catch (IOException ex) {
            throw shouldNotReachHere(ex);
        }

        return outputFile;
    }

    public static String findCTypeName(MetaAccessProvider metaAccess, NativeLibraries nativeLibs, ResolvedJavaType type) {
        switch (type.getJavaKind()) {
            case Boolean:
                if (NativeImageOptions.getCStandard() == CStandards.C89) {
                    return "int";
                } else {
                    return "bool";
                }
            case Byte:
                return "char";
            case Char:
                return "short";
            case Double:
                return "double";
            case Float:
                return "float";
            case Int:
                return "int"; // will be at least 32 bits; use uint32_t instead?
            case Long:
                return "long long int";
            case Short:
                return "short";
            case Void:
                return "void";
            case Object:
                ElementInfo elementInfo = nativeLibs.findElementInfo(type);
                if (elementInfo instanceof PointerToInfo) {
                    PointerToInfo pointerToInfo = (PointerToInfo) elementInfo;
                    return pointerToInfo.getTypedefName() != null ? pointerToInfo.getTypedefName() : pointerToInfo.getName() + "*";
                } else if (elementInfo instanceof StructInfo) {
                    StructInfo structInfo = (StructInfo) elementInfo;
                    return structInfo.getTypedefName() != null ? structInfo.getTypedefName() : structInfo.getName() + "*";
                } else if (elementInfo instanceof EnumInfo) {
                    return elementInfo.getName();
                } else if (metaAccess.lookupJavaType(UnsignedWord.class).isAssignableFrom(type)) {
                    return "size_t";
                } else if (metaAccess.lookupJavaType(SignedWord.class).isAssignableFrom(type)) {
                    return "ssize_t";
                } else if (isFunctionPointer(metaAccess, type)) {
                    return InfoTreeBuilder.getTypedefName(type) != null ? InfoTreeBuilder.getTypedefName(type) : "void *";
                }
                return "void *";
            default:
                throw shouldNotReachHere();
        }
    }

    private static boolean isFunctionPointer(MetaAccessProvider metaAccess, ResolvedJavaType type) {
        boolean functionPointer = metaAccess.lookupJavaType(CFunctionPointer.class).isAssignableFrom(type);
        return functionPointer &&
                Arrays.stream(type.getDeclaredMethods()).anyMatch(v -> v.getDeclaredAnnotation(InvokeCFunctionPointer.class) != null);
    }

    /**
     * Appends definition of "flags" like macro.
     *
     * @param preDefine
     */
    public void appendMacroDefinition(String preDefine) {
        appendln("#define " + preDefine);
    }
}
