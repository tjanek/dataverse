package edu.harvard.iq.dataverse.datafile.page;

import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.DatasetServiceBean;
import edu.harvard.iq.dataverse.DatasetVersion;
import edu.harvard.iq.dataverse.FileMetadata;
import edu.harvard.iq.dataverse.PermissionsWrapper;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Optional;

@ViewScoped
@Named("ReorderDataFilesPage")
public class ReorderDataFilesPage implements java.io.Serializable {

    @EJB
    private DatasetServiceBean datasetService;
    @Inject
    PermissionsWrapper permissionsWrapper;

    private Dataset dataset = new Dataset();
    private List<FileMetadata> fileMetadatas;

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

        DatasetVersion datasetVersion = fetchedDataset.get().getLatestVersion();
        fileMetadatas = datasetVersion.getFileMetadatas();

        return null;
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
