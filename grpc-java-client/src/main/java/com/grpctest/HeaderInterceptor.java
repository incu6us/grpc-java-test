package com.grpctest;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public class HeaderInterceptor implements ClientInterceptor {

    private final static Logger log = LoggerFactory.getLogger(HeaderInterceptor.class);
    private static final Metadata.Key<String> CUSTOM_DATA = Metadata.Key.of("custom-data", ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                    @Override
                    public void onHeaders(Metadata headers) {
                        log.info("HEADERS FROM SERVER: {}", headers);
                        log.info("CUSTOM_DATA: {}", headers.get(CUSTOM_DATA));
                        super.onHeaders(headers);
                    }
                }, headers);
            }
        };
    }
}
