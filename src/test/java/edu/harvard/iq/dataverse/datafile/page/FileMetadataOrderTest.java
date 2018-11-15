package edu.harvard.iq.dataverse.datafile.page;

import edu.harvard.iq.dataverse.FileMetadata;
import edu.harvard.iq.dataverse.mocks.MocksFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileMetadataOrderTest {

    private AtomicLong id;
    private FileMetadataOrder order;

    @Before
    public void setup() {
        id = new AtomicLong();
        order = new FileMetadataOrder();
    }

    @Test
    public void shouldReturnEmptyChanges() {
        // when
        List<FileMetadata> changes = order.changes();

        // then
        verifyNoChanges(changes);
    }

    @Test
    public void shouldReturnEmptyChangesWhenOrderNotChanged() {
        // given
        FileMetadata fileMetadata = makeFileMetadata(1);

        // when
        order.change(fileMetadata, 3, 3);

        // and
        List<FileMetadata> changes = order.changes();

        // then
        verifyNoChanges(changes);
    }

    @Test
    public void shouldReturnChangesWhenOrderHasChanged() {
        // given
        FileMetadata fileMetadata_1 = makeFileMetadata("file1.png", 4);
        FileMetadata fileMetadata_2 = makeFileMetadata("file2.png", 5);
        FileMetadata fileMetadata_3 = makeFileMetadata("file3.png", 6);

        // when
        order.change(fileMetadata_1, 3, 3); // 0
        order.change(fileMetadata_2, 5, 4); // -1
        order.change(fileMetadata_3, 6, 10); // +4

        // and
        List<FileMetadata> changes = order.changes();

        // then
        verifyChangesSize(changes, 2);
        verifyDisplayOrder(changes, "file2.png", 4);
        verifyDisplayOrder(changes, "file3.png", 10);
    }

    private FileMetadata makeFileMetadata(String label, int displayOrder) {
        return MocksFactory.makeFileMetadata(id.incrementAndGet(), label, displayOrder);
    }

    private FileMetadata makeFileMetadata(int displayOrder) {
        return MocksFactory.makeFileMetadata(id.incrementAndGet(), "file.png", displayOrder);
    }

    private void verifyDisplayOrder(List<FileMetadata> changes, String label, int displayOrder) {
        FileMetadata fileMetadata = changes.stream()
                .filter(fm -> fm.getLabel().equals(label))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        assertEquals(displayOrder, fileMetadata.getDisplayOrder(),"Wrong display order for: " + label);
    }

    private void verifyChangesSize(List<FileMetadata> changes, int size) {
        assertEquals(size, changes.size());
    }

    private void verifyNoChanges(List<FileMetadata> changes) {
        assertTrue(changes.isEmpty());
    }
}