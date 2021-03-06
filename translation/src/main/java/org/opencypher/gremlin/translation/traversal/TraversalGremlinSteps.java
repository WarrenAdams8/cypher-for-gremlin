/*
 * Copyright (c) 2018-2019 "Neo4j, Inc." [https://neo4j.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opencypher.gremlin.translation.traversal;

import java.util.stream.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Pop;
import org.apache.tinkerpop.gremlin.process.traversal.Scope;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal.Symbols;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.AddVertexStartStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.GraphStep;
import org.apache.tinkerpop.gremlin.structure.Column;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality;
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph;
import org.opencypher.gremlin.translation.GremlinSteps;
import org.opencypher.gremlin.traversal.CustomFunction;

@SuppressWarnings("unchecked")
public class TraversalGremlinSteps implements GremlinSteps<GraphTraversal, P> {

    private final GraphTraversal g;

    public TraversalGremlinSteps(GraphTraversal g) {
        this.g = g;
    }

    @Override
    public GraphTraversal current() {
        return g.asAdmin().clone();
    }

    private boolean isStartedOrSubTraversal() {
        boolean hasSteps = g.asAdmin().getSteps().size() > 0;
        boolean isSubTraversal = g.asAdmin().getGraph()
            .filter(graph -> graph instanceof EmptyGraph)
            .isPresent();
        return hasSteps || isSubTraversal;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> start() {
        GraphTraversal g = __.start();
        return new TraversalGremlinSteps(g);
    }

    @Override
    public GremlinSteps<GraphTraversal, P> V() {
        if (isStartedOrSubTraversal()) {
            g.V();
        } else {
            // Workaround for constructing `GraphStep` with `isStart == true`
            g.asAdmin().getBytecode().addStep(Symbols.V);
            g.asAdmin().addStep(new GraphStep<>(g.asAdmin(), Vertex.class, true));
        }
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> E() {
        if (isStartedOrSubTraversal()) {
            throw new IllegalStateException("Edge graph step can only be at the start of traversal");
        } else {
            // Workaround for constructing `EdgeStep` with `isStart == true`
            g.asAdmin().getBytecode().addStep(Symbols.E);
            g.asAdmin().addStep(new GraphStep<>(g.asAdmin(), Edge.class, true));
        }
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> addE(String edgeLabel) {
        g.addE(edgeLabel);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> addV() {
        if (isStartedOrSubTraversal()) {
            g.addV();
        } else {
            // Workaround for constructing `GraphStep` with `isStart == true`
            g.asAdmin().getBytecode().addStep(Symbols.addV);
            g.asAdmin().addStep(new AddVertexStartStep(g.asAdmin(), (String) null));
        }
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> addV(String vertexLabel) {
        if (isStartedOrSubTraversal()) {
            g.addV(vertexLabel);
        } else {
            // Workaround for constructing `GraphStep` with `isStart == true`
            g.asAdmin().getBytecode().addStep(Symbols.addV, vertexLabel);
            g.asAdmin().addStep(new AddVertexStartStep(g.asAdmin(), vertexLabel));
        }
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> aggregate(String sideEffectKey) {
        g.aggregate(sideEffectKey);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> and(GremlinSteps<GraphTraversal, P>... andTraversals) {
        g.and(traversals(andTraversals));
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> as(String stepLabel) {
        g.as(stepLabel);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> barrier() {
        g.barrier();
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> bothE(String... edgeLabels) {
        g.bothE(edgeLabels);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> by(GremlinSteps<GraphTraversal, P> traversal, Order order) {
        g.by(traversal.current(), order);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> by(GremlinSteps<GraphTraversal, P> traversal) {
        g.by(traversal.current());
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> cap(String sideEffectKey) {
        g.cap(sideEffectKey);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> choose(final GremlinSteps<GraphTraversal, P> choiceTraversal) {
        g.choose(choiceTraversal.current());
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> choose(final GremlinSteps<GraphTraversal, P> traversalPredicate,
                                                  GremlinSteps<GraphTraversal, P> trueChoice) {
        g.choose(traversalPredicate.current(), trueChoice.current());
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> choose(final GremlinSteps<GraphTraversal, P> traversalPredicate,
                                                  GremlinSteps<GraphTraversal, P> trueChoice,
                                                  GremlinSteps<GraphTraversal, P> falseChoice) {
        g.choose(traversalPredicate.current(), trueChoice.current(), falseChoice.current());
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> choose(P predicate,
                                                  GremlinSteps<GraphTraversal, P> trueChoice,
                                                  GremlinSteps<GraphTraversal, P> falseChoice) {
        g.choose(predicate, trueChoice.current(), falseChoice.current());
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> choose(P predicate, GremlinSteps<GraphTraversal, P> trueChoice) {
        g.choose(predicate, trueChoice.current());
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> coalesce(GremlinSteps<GraphTraversal, P>... coalesceTraversals) {
        g.coalesce(traversals(coalesceTraversals));
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> constant(Object e) {
        g.constant(e);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> count() {
        g.count();
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> count(Scope scope) {
        g.count(scope);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> dedup(String... dedupLabels) {
        g.dedup(dedupLabels);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> drop() {
        g.drop();
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> emit() {
        g.emit();
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> emit(GremlinSteps<GraphTraversal, P> traversal) {
        g.emit(traversal.current());
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> flatMap(GremlinSteps<GraphTraversal, P> traversal) {
        g.flatMap(traversal.current());
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> fold() {
        g.fold();
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> from(String fromStepLabel) {
        g.from(fromStepLabel);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> group() {
        g.group();
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> has(String propertyKey) {
        g.has(propertyKey);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> has(String propertyKey, P predicate) {
        g.has(propertyKey, predicate);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> hasKey(String... labels) {
        if (labels.length >= 1) {
            g.hasKey(labels[0], argumentsSlice(labels, 1));
        }
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> hasLabel(String... labels) {
        if (labels.length >= 1) {
            g.hasLabel(labels[0], argumentsSlice(labels, 1));
        }
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> hasNot(String propertyKey) {
        g.hasNot(propertyKey);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> id() {
        g.id();
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> identity() {
        g.identity();
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> inE(String... edgeLabels) {
        g.inE(edgeLabels);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> inV() {
        g.inV();
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> index() {
        g.index();
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> inject(Object... injections) {
        g.inject(injections);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> is(P predicate) {
        g.is(predicate);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> key() {
        g.key();
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> label() {
        g.label();
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> limit(long limit) {
        g.limit(limit);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> limit(Scope scope, long limit) {
        g.limit(scope, limit);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> local(GremlinSteps<GraphTraversal, P> localTraversal) {
        g.local(localTraversal.current());
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> loops() {
        g.loops();
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> map(CustomFunction function) {
        g.map(function.getImplementation());
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> map(GremlinSteps<GraphTraversal, P> traversal) {
        g.map(traversal.current());
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> math(String expression) {
        g.math(expression);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> max() {
        g.max();
        return this;
    }


    @Override
    public GremlinSteps<GraphTraversal, P> max(Scope scope) {
        g.max(scope);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> mean() {
        g.mean();
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> mean(Scope scope) {
        g.mean(scope);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> min() {
        g.min();
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> min(Scope scope) {
        g.min(scope);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> not(GremlinSteps<GraphTraversal, P> notTraversal) {
        g.not(notTraversal.current());
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> option(Object pickToken, GremlinSteps<GraphTraversal, P> traversalOption) {
        g.option(pickToken, traversalOption.current());
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> optional(GremlinSteps<GraphTraversal, P> optionalTraversal) {
        g.optional(optionalTraversal.current());
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> or(GremlinSteps<GraphTraversal, P>... orTraversals) {
        g.or(traversals(orTraversals));
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> order() {
        g.order();
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> otherV() {
        g.otherV();
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> outE(String... edgeLabels) {
        g.outE(edgeLabels);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> outV() {
        g.outV();
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> path() {
        g.path();
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> properties(String... propertyKeys) {
        g.properties(propertyKeys);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> property(org.apache.tinkerpop.gremlin.structure.T token, Object value) {
        g.property(token, value);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> property(String key, Object value) {
        g.property(key, value);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> property(Cardinality cardinality, String key, Object value) {
        g.property(cardinality, key, value);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> property(String key, GremlinSteps<GraphTraversal, P> traversal) {
        return property(key, traversal.current());
    }

    @Override
    public GremlinSteps<GraphTraversal, P> property(GremlinSteps<GraphTraversal, P> keyTraversal, GremlinSteps<GraphTraversal, P> valueTraversal) {
        g.property(keyTraversal.current(), valueTraversal.current());
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> property(Cardinality cardinality, String key, GremlinSteps<GraphTraversal, P> traversal) {
        g.property(cardinality, key, traversal.current());
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> project(String... keys) {
        if (keys.length >= 1) {
            g.project(keys[0], argumentsSlice(keys, 1));
        } else {
            throw new IllegalArgumentException("`project()` step requires keys");
        }
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> range(Scope scope, long low, long high) {
        g.range(scope, low, high);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> repeat(GremlinSteps<GraphTraversal, P> repeatTraversal) {
        g.repeat(repeatTraversal.current());
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> select(final Pop pop, String selectKey) {
        g.select(pop, selectKey);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> select(String... selectKeys) {
        if (selectKeys.length >= 2) {
            g.select(selectKeys[0], selectKeys[1], argumentsSlice(selectKeys, 2));
        } else if (selectKeys.length == 1) {
            g.select(selectKeys[0]);
        } else {
            throw new IllegalArgumentException("Select step should have arguments");
        }
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> select(Column column) {
        g.select(column);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> sideEffect(GremlinSteps<GraphTraversal, P> sideEffectTraversal) {
        g.sideEffect(sideEffectTraversal.current());
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> simplePath() {
        g.simplePath();
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> skip(long skip) {
        g.skip(skip);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> sum() {
        g.sum();
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> sum(Scope scope) {
        g.sum(scope);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> tail(Scope scope, long limit) {
        g.tail(scope, limit);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> times(int maxLoops) {
        g.times(maxLoops);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> to(String toStepLabel) {
        g.to(toStepLabel);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> unfold() {
        g.unfold();
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> union(GremlinSteps<GraphTraversal, P>... unionTraversals) {
        g.union(traversals(unionTraversals));
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> until(GremlinSteps<GraphTraversal, P> untilTraversal) {
        g.until(untilTraversal.current());
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> valueMap() {
        g.valueMap();
        return this;
    }

    @Override
    @SuppressWarnings( "deprecation" )
    public GremlinSteps<GraphTraversal, P> valueMap(boolean includeTokens) {
        g.valueMap(includeTokens);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> value() {
        g.value();
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> values(String... propertyKeys) {
        g.values(propertyKeys);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> where(GremlinSteps<GraphTraversal, P> whereTraversal) {
        g.where(whereTraversal.current());
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> where(P predicate) {
        g.where(predicate);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> with(String key) {
        g.with(key);
        return this;
    }

    @Override
    public GremlinSteps<GraphTraversal, P> with(String name, Object value) {
        g.with(name, value);
        return this;
    }

    private static String[] argumentsSlice(String[] arguments, int start) {
        String[] dest = new String[arguments.length - start];
        System.arraycopy(arguments, start, dest, 0, arguments.length - start);
        return dest;
    }

    private static GraphTraversal[] traversals(GremlinSteps<GraphTraversal, P>[] gremlinSteps) {
        return Stream.of(gremlinSteps)
            .map(GremlinSteps::current)
            .toArray(GraphTraversal[]::new);
    }

    @Override
    public String toString() {
        return g.asAdmin().getBytecode().toString();
    }
}
