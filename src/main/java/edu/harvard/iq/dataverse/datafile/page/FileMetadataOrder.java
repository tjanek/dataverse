package edu.harvard.iq.dataverse.datafile.page;

import edu.harvard.iq.dataverse.FileMetadata;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

class FileMetadataOrder {

    private Map<FileMetadata, Integer> offsets = new ConcurrentHashMap<>();

    public void change(FileMetadata fileMetadata, int from, int to) {
        int offset = to - from;
        if (offset != 0) {
            offsets.put(fileMetadata, offset);
        }
    }

    public List<FileMetadata> changes() {
        return offsets.keySet().stream()
                .peek(fm -> {
                    Integer offset = offsets.get(fm);
                    fm.setDisplayOrder(fm.getDisplayOrder() + offset);
                })
                .collect(Collectors.toList());
    }
}
