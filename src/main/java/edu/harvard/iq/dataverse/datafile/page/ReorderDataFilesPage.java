package edu.harvard.iq.dataverse.datafile.page;

import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.DatasetServiceBean;
import edu.harvard.iq.dataverse.DatasetVersionServiceBean;
import edu.harvard.iq.dataverse.FileMetadata;
import edu.harvard.iq.dataverse.PermissionServiceBean;
import edu.harvard.iq.dataverse.PermissionsWrapper;
import edu.harvard.iq.dataverse.authorization.Permission;
import org.primefaces.event.ReorderEvent;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@ViewScoped
@Named("ReorderDataFilesPage")
public class ReorderDataFilesPage implements java.io.Serializable {

    private static final Logger logger = Logger.getLogger(ReorderDataFilesPage.class.getCanonicalName());

    @EJB
    private DatasetServiceBean datasetService;
    @EJB
    private DatasetVersionServiceBean datasetVersionService;
    @EJB
    private PermissionServiceBean permissionService;
    @Inject
    private PermissionsWrapper permissionsWrapper;

    private Dataset dataset = new Dataset();
    private List<FileMetadata> fileMetadatas;
    private List<FileMetadata> fileMetadatasCopy;
    private List<FileMetadataEvent> orderedFilesIndex = new ArrayList<>();

    /**
     * Initializes all properties requested by frontend.
     * Like files for dataset with specific id.
     *
     * @return error if something goes wrong or null if success.
     */
    public String init() {

        Optional<Dataset> fetchedDataset = fetchDataset(dataset.getId());

        if (!fetchedDataset.isPresent() || fetchedDataset.get().isHarvested()) {
            return permissionsWrapper.notFound();
        }

        fileMetadatas = fetchedDataset.get().getLatestVersion().getFileMetadatasSorted();

        // for some reason the original fileMetadatas is causing null if used anywhere else. For
        fileMetadatasCopy = fileMetadatas;

        if (!permissionService.on(dataset).has(Permission.EditDataset)) {
            return permissionsWrapper.notAuthorized();
        }

        return null;
    }

    /**
     * Reorders files display order if any were reordered, saves the changes to the database
     * and returns to the previous page.
     *
     * @return uri to previous page
     */
    public String saveFileOrder() {

        orderedFilesIndex.forEach(orderedFileMeta -> reorderFilesDisplayOrder(orderedFileMeta, fileMetadatasCopy));

        datasetVersionService.saveFileMetadata(fileMetadatasCopy);

        return returnToPreviousPage();
    }

    /**
     * Adds the reorder action to the list.
     *
     * @param event
     */
    public void onRowReorder(ReorderEvent event) {
        int movedToIndex = event.getToIndex();

        logger.info("reorder rows at dataset files");
        logger.info("From: " + event.getFromIndex() + ", To:" + movedToIndex);

        // already moved in fileMetadataSearch before event wad fired
        // some get event.getToIndex instead of event.getFromIndex
        FileMetadata fileMetadata = fileMetadatasCopy.get(event.getToIndex());
        fileMetadata.setDisplayOrder(movedToIndex);

        orderedFilesIndex.add(new FileMetadataEvent(event, fileMetadata));
    }

    /**
     * Method responsible for retrieving dataset from database.
     *
     * @param id
     * @return optional
     */
    private Optional<Dataset> fetchDataset(Long id) {
        return Optional.ofNullable(id)
                .map(datasetId -> this.dataset = datasetService.find(datasetId));
    }

    /**
     * returns you to the dataset page.
     *
     * @return uri
     */
    private String returnToPreviousPage() {
        return "/dataset.xhtml?persistentId=" + dataset.getGlobalId().asString() + "&version=DRAFT&faces-redirect=true";
    }

    /**
     * Reorders files display order, it only reorders displayOrder for files between indexes.
     * <p></p>
     * ex. 012345 -> 3 was moved between 0 and 1 so
     * <p></p>
     * 3 -> 1;
     * 1 -> 2;
     * 2 -> 3;
     *
     * @param orderedFileMeta
     * @param originalFiles
     * @return
     */
    private List<FileMetadata> reorderFilesDisplayOrder(FileMetadataEvent orderedFileMeta,
                                                        List<FileMetadata> originalFiles) {

        int movedFrom = orderedFileMeta.getReorderEvent().getFromIndex();
        int movedTo = orderedFileMeta.getReorderEvent().getToIndex();

        if (movedTo > movedFrom) {

            originalFiles.stream()
                    .filter(fileMetadata ->
                            fileMetadata.getDisplayOrder() > movedFrom
                                    && fileMetadata.getDisplayOrder() <= movedTo
                                    && !fileMetadata.equals(orderedFileMeta.getFileMetadata()))
                    .forEach(fileMetadata -> fileMetadata.setDisplayOrder(fileMetadata.getDisplayOrder() - 1));
        } else {

            originalFiles.stream()
                    .filter(fileMetadata -> fileMetadata.getDisplayOrder() >= movedTo
                            && fileMetadata.getDisplayOrder() < movedFrom
                            && !fileMetadata.equals(orderedFileMeta.getFileMetadata()))
                    .forEach(fileMetadata -> fileMetadata.setDisplayOrder(fileMetadata.getDisplayOrder() + 1));
        }
        return originalFiles;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public List<FileMetadata> getFileMetadatas() {
        return fileMetadatas;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public void setFileMetadatas(List<FileMetadata> fileMetadatas) {
        this.fileMetadatas = fileMetadatas;
    }
}
