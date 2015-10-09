/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.plugin.ingest.transport.put;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.index.TransportIndexAction;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.HandledTransportAction;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugin.ingest.PipelineStore;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import java.util.function.Supplier;

public class PutPipelineTransportAction extends HandledTransportAction<PutPipelineRequest, PutPipelineResponse> {

    private final TransportIndexAction indexAction;

    @Inject
    public PutPipelineTransportAction(Settings settings, ThreadPool threadPool, TransportService transportService, ActionFilters actionFilters, IndexNameExpressionResolver indexNameExpressionResolver, TransportIndexAction indexAction) {
        super(settings, PutPipelineAction.NAME, threadPool, transportService, actionFilters, indexNameExpressionResolver, PutPipelineRequest::new);
        this.indexAction = indexAction;
    }

    @Override
    protected void doExecute(PutPipelineRequest request, ActionListener<PutPipelineResponse> listener) {
        IndexRequest indexRequest = new IndexRequest();
        indexRequest.index(PipelineStore.INDEX);
        indexRequest.type(PipelineStore.TYPE);
        indexRequest.id(request.id());
        indexRequest.source(request.source());
        indexRequest.refresh(true);
        indexAction.execute(indexRequest, new ActionListener<IndexResponse>() {
            @Override
            public void onResponse(IndexResponse indexResponse) {
                PutPipelineResponse response = new PutPipelineResponse();
                response.id(indexResponse.getId());
                response.version(indexResponse.getVersion());
                listener.onResponse(response);
            }

            @Override
            public void onFailure(Throwable e) {
                listener.onFailure(e);
            }
        });
    }
}
