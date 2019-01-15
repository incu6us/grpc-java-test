package com.grpctest.server;

import com.google.rpc.DebugInfo;
import com.grpctest.repository.Storage;
import com.grpctest.repository.exception.NoRecordException;
import com.grpctest.store.*;
import io.grpc.Metadata;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import io.grpc.Status;

@Component
public class StoreServiceImpl extends StoreServiceGrpc.StoreServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(StoreServiceImpl.class);

    private static final Metadata.Key<DebugInfo> DEBUG_INFO_TRAILER_KEY =
            ProtoUtils.keyForProto(DebugInfo.getDefaultInstance());

    private static final DebugInfo DEBUG_INFO =
            DebugInfo.newBuilder()
                    .addStackEntries("stack_entry_1")
                    .addStackEntries("stack_entry_2")
                    .addStackEntries("stack_entry_3")
                    .setDetail("detailed error info.").build();

    private static final String DEBUG_DESC = "detailed error description";


    @Autowired
    private Storage storage;

    @Override
    public void addValue(StoreRequest request, StreamObserver<StoreResponse> responseObserver) {
        log.info("id: {}; message: {}", request.getId(), request.getValue());

        StoreResponse response = StoreResponse.newBuilder()
                .setDone(storage.add(request.getId(), request.getValue()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getValue(GetRequest request, StreamObserver<GetResponse> responseObserver) {
        try {
            GetResponse response = GetResponse.newBuilder()
                    .setId(request.getId()).setValue(storage.get(request.getId()))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoRecordException e) {
            log.error("record not found with id: {}", request.getId());

            Metadata trailers = new Metadata();
            trailers.put(DEBUG_INFO_TRAILER_KEY, DEBUG_INFO);
            responseObserver.onError(Status.NOT_FOUND.withDescription(DEBUG_DESC).asRuntimeException(trailers));
        }
    }
}
