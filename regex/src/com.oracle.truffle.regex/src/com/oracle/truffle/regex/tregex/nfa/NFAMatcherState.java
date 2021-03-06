/*
 * Copyright (c) 2016, 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
package com.oracle.truffle.regex.tregex.nfa;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.regex.tregex.matchers.MatcherBuilder;
import com.oracle.truffle.regex.tregex.parser.ast.LookBehindAssertion;
import com.oracle.truffle.regex.tregex.parser.ast.RegexASTNode;
import com.oracle.truffle.regex.tregex.util.DebugUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NFAMatcherState extends NFAState {

    private final MatcherBuilder matcherBuilder;
    private final Set<LookBehindAssertion> finishedLookBehinds;

    public NFAMatcherState(short id,
                    ASTNodeSet<? extends RegexASTNode> stateSet,
                    MatcherBuilder matcherBuilder,
                    Set<LookBehindAssertion> finishedLookBehinds,
                    boolean hasPrefixStates) {
        this(id, stateSet, new ArrayList<>(), new ArrayList<>(), null, matcherBuilder, finishedLookBehinds, hasPrefixStates);
    }

    private NFAMatcherState(
                    short id,
                    ASTNodeSet<? extends RegexASTNode> stateSet,
                    List<NFAStateTransition> next,
                    List<NFAStateTransition> prev,
                    List<Integer> possibleResults,
                    MatcherBuilder matcherBuilder,
                    Set<LookBehindAssertion> finishedLookBehinds,
                    boolean hasPrefixStates) {
        super(id, stateSet, next, prev, possibleResults);
        this.matcherBuilder = matcherBuilder;
        this.finishedLookBehinds = finishedLookBehinds;
        setHasPrefixStates(hasPrefixStates);
    }

    public MatcherBuilder getMatcherBuilder() {
        return matcherBuilder;
    }

    public Set<LookBehindAssertion> getFinishedLookBehinds() {
        return finishedLookBehinds;
    }

    @Override
    public String toString() {
        return matcherBuilder.toString();
    }

    @Override
    @CompilerDirectives.TruffleBoundary
    public DebugUtil.Table toTable() {
        return toTable("NFAMatcherState").append(new DebugUtil.Value("matcherBuilder", toString()));
    }
}
