package edu.harvard.iq.dataverse.datafile.page;

import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.DatasetServiceBean;
import edu.harvard.iq.dataverse.DatasetVersion;
import edu.harvard.iq.dataverse.FileMetadata;
import edu.harvard.iq.dataverse.PermissionsWrapper;
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
    @Inject
    PermissionsWrapper permissionsWrapper;

    private Dataset dataset = new Dataset();
    private List<FileMetadata> fileMetadatas;
    private List<Integer> orderedFilesIndex = new ArrayList<>();

    /**
     * Initializes all properties requested by frontend.
     * Like files for dataset with specific id.
     *
     * @return error if something goes wrong or null if success.
     */
    public String init() {
        fileMetadatas = new ArrayList<>();

        Optional<Dataset> fetchedDataset = fetchDataset(dataset.getId());

        if (!fetchedDataset.isPresent() || fetchedDataset.get().isHarvested()) {
            return permissionsWrapper.notFound();
        }

        DatasetVersion datasetVersion = fetchedDataset.get().getLatestVersion();
        fileMetadatas = datasetVersion.getFileMetadatasSorted();

        return null;
    }

    public String saveFileOrder() {
        fileMetadatas = dataset.getLatestVersion().getFileMetadatasSorted();

        orderedFilesIndex
                .forEach(orderedFileMeta -> fileMetadatas.stream()
                        .filter(fileMetadata -> fileMetadata.getDisplayOrder() >= orderedFileMeta)
                        .forEach(fileMetadata -> fileMetadata.setDisplayOrder(fileMetadata.getDisplayOrder() + 1)));

        return "/dataset.xhtml?persistentId=" + dataset.getGlobalId().asString() + "&version=DRAFT&faces-redirect=true";
    }

    public String returnToPreviousPage() {
        return "/dataset.xhtml?persistentId=" + dataset.getGlobalId().asString() + "&version=DRAFT&faces-redirect=true";
    }

    public void onRowReorder(ReorderEvent event) {
        int movedToIndex = event.getToIndex();

        logger.info("reorder rows at dataset files");
        logger.info("From: " + event.getFromIndex() + ", To:" + movedToIndex);

        // already moved in fileMetadataSearch before event wad fired
        // some get event.getToIndex instead of event.getFromIndex

        orderedFilesIndex.add(movedToIndex);

        //fileMetadataOrderService.reorderExisting(orderedFiles);
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
