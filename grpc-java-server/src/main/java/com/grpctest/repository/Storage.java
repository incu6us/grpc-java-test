package com.grpctest.repository;

import com.grpctest.repository.exception.NoRecordException;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class Storage {
    private static Map<Integer, String> storage = new HashMap();

    public boolean add(Integer id, String message) {
        if (storage.containsKey(id)) {
            return false;
        }
        storage.put(id, message);
        return true;
    }

    public String get(Integer id) throws NoRecordException {
        String storedValue = storage.get(id);
        if (storedValue == null) {
            throw new NoRecordException();
        }

        return storedValue;
    }
}
