package com.jobflow.api.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public class PagedJobResponse {
    private List<JobDetailResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public PagedJobResponse(Page<com.jobflow.common.entity.Job> jobPage) {
        this.content = jobPage.getContent().stream().map(JobDetailResponse::new).toList();
        this.page = jobPage.getNumber();
        this.size = jobPage.getSize();
        this.totalElements = jobPage.getTotalElements();
        this.totalPages = jobPage.getTotalPages();
    }

    public List<JobDetailResponse> getContent() { return content; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
}