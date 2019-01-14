package com.grpctest.server;

import com.grpctest.Constants;
import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeaderInterceptor implements ServerInterceptor {

    private final static Logger log = LoggerFactory.getLogger(HeaderInterceptor.class);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        return next.startCall(new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
            @Override
            public void sendHeaders(Metadata responseHeaders) {
                responseHeaders.put(Constants.CUSTOM_DATA, "some useful data");
                responseHeaders.merge(headers);
                log.info("headers: {}", responseHeaders);
                super.sendHeaders(responseHeaders);
            }
        }, headers);
    }
}
