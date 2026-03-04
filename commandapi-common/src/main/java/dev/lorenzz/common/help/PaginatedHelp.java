package dev.lorenzz.common.help;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PaginatedHelp {

    private final List<String> entries;
    private final int pageSize;

    public PaginatedHelp(List<String> entries, int pageSize) {
        this.entries = Collections.unmodifiableList(new ArrayList<>(entries));
        this.pageSize = pageSize;
    }

    public int pages() {
        return Math.max(1, (int) Math.ceil(entries.size() / (double) pageSize));
    }

    public List<String> page(int index) {
        int page = Math.max(1, Math.min(index, pages()));
        int start = (page - 1) * pageSize;
        int end = Math.min(entries.size(), start + pageSize);
        return entries.subList(start, end);
    }
}
