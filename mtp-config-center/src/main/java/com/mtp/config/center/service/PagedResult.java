package com.mtp.config.center.service;

import java.util.List;

public class PagedResult<T> {
    private List<T> content;
    private int total;
    private int page;
    private int size;

    public PagedResult(List<T> content, int total, int page, int size) {
        this.content = content;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public List<T> getContent() { return content; }
    public int getTotal() { return total; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalElements() { return total; }
}