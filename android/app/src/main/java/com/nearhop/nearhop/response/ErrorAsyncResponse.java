package com.nearhop.nearhop.response;

interface ErrorAsyncResponse {

    /**
     * Delegate to bubble up errors
     *
     * @param output
     */
    <T extends Throwable> void processFinish(T output);
}
