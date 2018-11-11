package edu.harvard.iq.dataverse;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.logging.Logger;

/**
 *
 * @author tjanek
 */
@Stateless
@Named
public class FileMetadataOrderServiceBean implements java.io.Serializable {

    private static final Logger logger = Logger.getLogger(FileMetadataOrderServiceBean.class.getCanonicalName());

    @PersistenceContext(unitName = "VDCNet-ejbPU")
    private EntityManager em;

    public void reorderExisting(Collection<FileMetadata> fileMetadatas) {
        fileMetadatas.forEach(this::reorder);
    }

    private void reorder(FileMetadata fileMetadata) {
        Long fileMetadataId = fileMetadata.getId();
        int oldOrder = displayOrderOf(fileMetadataId);
        int newOrder = fileMetadata.getDisplayOrder();
        updateOrder(oldOrder, newOrder, fileMetadata);
    }

    private int displayOrderOf(Long fileMetadataId) {
        return (int) em.createNativeQuery("SELECT displayorder from filemetadata where id = ?")
                .setParameter(1, fileMetadataId)
                .getSingleResult();
    }

    private void updateOrder(int oldOrder, int newOrder, FileMetadata fileMetadata) {
        Long fileMetadataId = fileMetadata.getId();
        if (newOrder > oldOrder) {
            em.createNativeQuery("update filemetadata set displayorder = displayorder - 1" +
                    " where displayorder >= ? and displayorder <= ? and datasetversion_id = ?")
                    .setParameter(1, oldOrder)
                    .setParameter(2, newOrder)
                    .setParameter(3, fileMetadata.getDatasetVersion().getId())
                    .executeUpdate();
        } else if (newOrder < oldOrder) {
            em.createNativeQuery("update filemetadata set displayorder = displayorder + 1" +
                    " where displayorder >= ? and datasetversion_id = ?")
                    .setParameter(1, newOrder)
                    .setParameter(2, fileMetadata.getDatasetVersion().getId())
                    .executeUpdate();
        }

        em.createNativeQuery("update filemetadata set displayorder = ?" +
                " where id = ?")
                .setParameter(1, newOrder)
                .setParameter(2, fileMetadataId)
                .executeUpdate();
    }

}
