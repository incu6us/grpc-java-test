package com.grpctest.rest;


import com.grpctest.repository.Storage;
import com.grpctest.repository.exception.NoRecordException;
import com.grpctest.rest.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/api/v1/storage")
public class RestService {

    private static final Logger log = LoggerFactory.getLogger(RestController.class);

    @Autowired
    private Storage storage;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageEntity> getById(@PathVariable(value = "id") int id) throws NotFoundException {
        log.info("ID: {}", id);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity(new MessageEntity(id, storage.get(id)), headers, HttpStatus.OK);
        } catch (NoRecordException e) {
            throw new NotFoundException();
        }
    }
}
