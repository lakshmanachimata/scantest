package com.nearhop.nearhop.response;

import android.util.SparseArray;

import com.nearhop.nearhop.utilities.Host;

interface LanHostAsyncResponse {

    /**
     * Delegate to handle integer outputs
     *
     * @param output
     */
    void processFinish(int output);

    /**
     * Delegate to handle boolean outputs
     *
     * @param output
     */
    void processFinish(boolean output);

    /**
     * Delegate to handle Map outputs
     *
     * @param output
     */
    void processFinish(Host host, SparseArray<String> output);
}
