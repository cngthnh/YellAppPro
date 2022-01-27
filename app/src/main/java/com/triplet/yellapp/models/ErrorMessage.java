package com.triplet.yellapp.models;

import com.triplet.yellapp.utils.Client;

import java.io.IOException;
import java.lang.annotation.Annotation;

import okhttp3.ResponseBody;
import retrofit2.Converter;

public class ErrorMessage {
    public String message;
    public static ErrorMessage convertErrors(ResponseBody response){
        Converter<ResponseBody, ErrorMessage> converter = Client.getInstance().responseBodyConverter(ErrorMessage.class, new Annotation[0]);

        ErrorMessage errorMessage = null;

        try {
            errorMessage = converter.convert(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return errorMessage;
    }

    public String getMessage() {
        return message;
    }
}
