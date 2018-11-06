/*
 * Copyright © 2018 Apple Inc. and the ServiceTalk project authors
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
package io.servicetalk.concurrent.api;

import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.servicetalk.concurrent.internal.EmptySubscription.EMPTY_SUBSCRIPTION;

final class EmptyPublisher<T> extends AbstractSynchronousPublisher<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmptyPublisher.class);
    private static final EmptyPublisher EMPTY_PUBLISHER = new EmptyPublisher();

    private EmptyPublisher() {
        // singleton
    }

    @Override
    void doSubscribe(Subscriber<? super T> subscriber) {
        try {
            subscriber.onSubscribe(EMPTY_SUBSCRIPTION);
        } catch (Throwable t) {
            LOGGER.debug("Ignoring exception from onSubscribe of Subscriber {}.", subscriber, t);
            return;
        }
        try {
            subscriber.onComplete();
        } catch (Throwable t) {
            LOGGER.debug("Ignoring exception from onComplete of Subscriber {}.", subscriber, t);
        }
    }

    @SuppressWarnings("unchecked")
    static <T> Publisher<T> emptyPublisher() {
        return (Publisher<T>) EMPTY_PUBLISHER;
    }
}
