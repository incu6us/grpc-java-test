package com.grpctest.server;

import com.google.protobuf.Any;
import com.grpctest.repository.Storage;
import com.grpctest.repository.exception.NoRecordException;
import com.grpctest.store.*;
import io.grpc.Status;
import io.grpc.protobuf.StatusProto;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StoreServiceImpl extends StoreServiceGrpc.StoreServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(StoreServiceImpl.class);

//    private static final Metadata.Key<com.google.protobuf.Any> DEBUG_INFO_TRAILER_KEY =
//            ProtoUtils.keyForProto(Any.getDefaultInstance());
//
//    private static final Any DEBUG_INFO = Any.pack(StringValue.newBuilder().setValue("test value").build());


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

            // without details in error
//            Metadata trailers = new Metadata();
//            trailers.put(DEBUG_INFO_TRAILER_KEY, DEBUG_INFO);
//            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).withCause(e).augmentDescription(DEBUG_INFO.toString()).asRuntimeException(trailers));

            // with details in error
            StoreError storeError = StoreError.newBuilder().setError("some error").build();
            com.google.rpc.Status status = com.google.rpc.Status.newBuilder()
                    .setCode(Status.Code.NOT_FOUND.value())
                    .setMessage(e.getMessage())
                    .addDetails(Any.pack(storeError))
                    .build();

            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
        }
    }
}
