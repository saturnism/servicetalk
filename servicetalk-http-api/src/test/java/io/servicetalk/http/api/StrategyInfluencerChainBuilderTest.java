/*
 * Copyright © 2019, 2021 Apple Inc. and the ServiceTalk project authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.servicetalk.http.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;

import javax.annotation.Nonnull;

import static io.servicetalk.http.api.HttpExecutionStrategies.defaultStrategy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class StrategyInfluencerChainBuilderTest {

    @Test
    void deepCopy() {
        StrategyInfluencerChainBuilder chain1 = new StrategyInfluencerChainBuilder();
        HttpExecutionStrategyInfluencer influencer1 = newNoInfluenceInfluencer();
        chain1.append(influencer1);

        StrategyInfluencerChainBuilder chain2 = chain1.copy();
        HttpExecutionStrategyInfluencer influencer2 = newNoInfluenceInfluencer();
        chain2.append(influencer2);

        defaultStrategy().merge(chain1.build().requiredOffloads());

        verifyNoInteractions(influencer2);
    }

    @Test
    void buildWithStrategy() {
        StrategyInfluencerChainBuilder chain = new StrategyInfluencerChainBuilder();
        HttpExecutionStrategy transportStrategy = HttpExecutionStrategies.customStrategyBuilder().offloadSend().build();
        HttpExecutionStrategyInfluencer influencer = chain.build(transportStrategy);
        HttpExecutionStrategy influenced = influencer.requiredOffloads();
        assertThat("Unexpected influenced strategy", influenced, sameInstance(transportStrategy));
    }

    @Test
    void buildWithDefaultStrategy() {
        StrategyInfluencerChainBuilder chain = new StrategyInfluencerChainBuilder();
        HttpExecutionStrategy transportStrategy = defaultStrategy();
        HttpExecutionStrategyInfluencer influencer = chain.build(transportStrategy);
        HttpExecutionStrategy influenced = influencer.requiredOffloads();
        assertThat("Unexpected influenced strategy", influenced, sameInstance(defaultStrategy()));
    }

    @ParameterizedTest(name = "conditional? = {0}")
    @ValueSource(booleans = {false, true})
    void appendAndPrepend(boolean conditional) {
        StrategyInfluencerChainBuilder chain = new StrategyInfluencerChainBuilder();
        HttpExecutionStrategyInfluencer influencer1 = newNoInfluenceInfluencer();
        HttpExecutionStrategyInfluencer influencer2 = newNoInfluenceInfluencer();
        HttpExecutionStrategyInfluencer influencer3 = newNoInfluenceInfluencer();

        if (conditional) {
            chain.appendIfInfluencer(influencer2);
            chain.prependIfInfluencer(influencer1);
            chain.appendIfInfluencer(influencer3);
        } else {
            chain.append(influencer2);
            chain.prepend(influencer1);
            chain.append(influencer3);
        }

        defaultStrategy().merge(chain.build().requiredOffloads());

        InOrder inOrder = inOrder(influencer1, influencer2, influencer3);
        inOrder.verify(influencer1).requiredOffloads();
        inOrder.verify(influencer2).requiredOffloads();
        inOrder.verify(influencer3).requiredOffloads();
    }

    @Nonnull
    private HttpExecutionStrategyInfluencer newNoInfluenceInfluencer() {
        HttpExecutionStrategyInfluencer influencer1 = mock(HttpExecutionStrategyInfluencer.class);
        when(influencer1.requiredOffloads()).thenReturn(HttpExecutionStrategies.offloadNone());
        when(influencer1.influenceStrategy(any(HttpExecutionStrategy.class)))
                .then(invocation -> invocation.getArgument(0));
        return influencer1;
    }
}
