package edu.harvard.iq.dataverse.datafile.page;

import edu.harvard.iq.dataverse.FileMetadata;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

class FileMetadataOrder {

    private final List<FileMetadata> storage;

    FileMetadataOrder(List<FileMetadata> storage) {
        this.storage = storage;
    }

    List<FileMetadata> changes() {
        List<FileMetadata> changes = newArrayList();

        for (int i = 0; i < storage.size(); i++) {
            FileMetadata fileMetadata = storage.get(i);
            if (fileMetadata.getDisplayOrder() != i) {
                fileMetadata.setDisplayOrder(i);
                changes.add(fileMetadata);
            }
        }

        return changes;
    }

    public List<FileMetadata> getStorage() {
        return storage;
    }
}
