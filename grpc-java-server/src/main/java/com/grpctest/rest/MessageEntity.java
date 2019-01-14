package com.grpctest.rest;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class MessageEntity {
    @NonNull
    private int id;

    @NonNull
    private String message;
}
