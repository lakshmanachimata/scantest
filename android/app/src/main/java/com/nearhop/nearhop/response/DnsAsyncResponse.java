package com.nearhop.nearhop.response;

public interface DnsAsyncResponse {

    /**
     * Delegate to handle string outputs
     *
     * @param output
     */
    void processFinish(String output);

}
