package com.example.yornshakeit;

import java.util.List;
import java.util.Map;

public class Response {

    private Map<String, List<String>> headers;
    private String text;

    public Response(Map<String, List<String>> headers, String text) {
        this.headers = headers;
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public List<String> getHeader(String name) {
        return this.headers.get(name);
    }
}
