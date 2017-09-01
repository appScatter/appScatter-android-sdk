package com.mindera.metrics.statful;

import com.statful.client.domain.api.Aggregation;
import com.statful.client.domain.api.AggregationFrequency;
import com.statful.client.domain.api.Aggregations;
import com.statful.client.domain.api.ClientConfiguration;
import com.statful.client.domain.api.SenderAPI;
import com.statful.client.domain.api.SenderFacade;
import com.statful.client.domain.api.Tags;
import com.statful.sdk.domain.StatfulSDK;
import com.statful.sdk.domain.configuration.MetricsCollectionConfiguration;

public class NoOpStatfulSDK implements StatfulSDK {
    private SenderAPI noOpSenderApi = new SenderAPI() {
        @Override
        public SenderAPI with() {
            return this;
        }

        @Override
        public SenderAPI name(String name) {
            return this;
        }

        @Override
        public SenderAPI value(String value) {
            return this;
        }

        @Override
        public SenderAPI configuration(ClientConfiguration configuration) {
            return this;
        }

        @Override
        public SenderAPI sampleRate(Integer sampleRate) {
            return this;
        }

        @Override
        public SenderAPI tag(String type, String value) {
            return this;
        }

        @Override
        public SenderAPI tags(Tags tags) {
            return this;
        }

        @Override
        public SenderAPI aggregation(Aggregation aggregation) {
            return this;
        }

        @Override
        public SenderAPI aggregations(Aggregation... aggregations) {
            return this;
        }

        @Override
        public SenderAPI aggregations(Aggregations aggregations) {
            return this;
        }

        @Override
        public SenderAPI aggregationFrequency(AggregationFrequency aggregationFrequency) {
            return this;
        }

        @Override
        public SenderAPI namespace(String namespace) {
            return this;
        }

        @Override
        public SenderAPI timestamp(Long timestamp) {
            return this;
        }

        @Override
        public void send() {

        }
    };
    
    private SenderFacade noOpSenderFacade = new SenderFacade() {
        @Override
        public SenderAPI with() {
            return noOpSenderApi;
        }

        @Override
        public void send() {

        }
    };
    
    @Override
    public SenderFacade timer(String s, long l) {
        return noOpSenderFacade;
    }

    @Override
    public SenderFacade counter(String s) {
        return noOpSenderFacade;
    }

    @Override
    public SenderFacade counter(String s, int i) {
        return noOpSenderFacade;
    }

    @Override
    public SenderFacade gauge(String s, Long aLong) {
        return noOpSenderFacade;
    }

    @Override
    public SenderFacade gauge(String s, Double aDouble) {
        return noOpSenderFacade;
    }

    @Override
    public SenderFacade gauge(String s, Float aFloat) {
        return noOpSenderFacade;
    }

    @Override
    public SenderFacade gauge(String s, Integer integer) {
        return noOpSenderFacade;
    }

    @Override
    public SenderFacade put(String s, Long aLong) {
        return noOpSenderFacade;
    }

    @Override
    public SenderFacade put(String s, Double aDouble) {
        return noOpSenderFacade;
    }

    @Override
    public SenderFacade put(String s, Float aFloat) {
        return noOpSenderFacade;
    }

    @Override
    public SenderFacade put(String s, Integer integer) {
        return noOpSenderFacade;
    }

    @Override
    public SenderFacade aggregatedTimer(String s, long l, Aggregation aggregation, AggregationFrequency aggregationFrequency) {
        return noOpSenderFacade;
    }

    @Override
    public SenderFacade aggregatedCounter(String s, int i, Aggregation aggregation, AggregationFrequency aggregationFrequency) {
        return noOpSenderFacade;
    }

    @Override
    public SenderFacade aggregatedGauge(String s, Long aLong, Aggregation aggregation, AggregationFrequency aggregationFrequency) {
        return noOpSenderFacade;
    }

    @Override
    public SenderFacade aggregatedGauge(String s, Double aDouble, Aggregation aggregation, AggregationFrequency aggregationFrequency) {
        return noOpSenderFacade;
    }

    @Override
    public SenderFacade aggregatedGauge(String s, Float aFloat, Aggregation aggregation, AggregationFrequency aggregationFrequency) {
        return noOpSenderFacade;
    }

    @Override
    public SenderFacade aggregatedGauge(String s, Integer integer, Aggregation aggregation, AggregationFrequency aggregationFrequency) {
        return noOpSenderFacade;
    }

    @Override
    public SenderFacade aggregatedPut(String s, Long aLong, Aggregation aggregation, AggregationFrequency aggregationFrequency) {
        return noOpSenderFacade;
    }

    @Override
    public SenderFacade aggregatedPut(String s, Double aDouble, Aggregation aggregation, AggregationFrequency aggregationFrequency) {
        return noOpSenderFacade;
    }

    @Override
    public SenderFacade aggregatedPut(String s, Float aFloat, Aggregation aggregation, AggregationFrequency aggregationFrequency) {
        return noOpSenderFacade;
    }

    @Override
    public SenderFacade aggregatedPut(String s, Integer integer, Aggregation aggregation, AggregationFrequency aggregationFrequency) {
        return noOpSenderFacade;
    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {

    }

    @Override
    public void onMetricsCollectionConfigurationChanged(MetricsCollectionConfiguration metricsCollectionConfiguration) {

    }
}
