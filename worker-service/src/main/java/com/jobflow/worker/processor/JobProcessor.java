package com.jobflow.worker.processor;

import com.jobflow.common.entity.Job;

public interface JobProcessor {
    void process(Job job) throws Exception;
}