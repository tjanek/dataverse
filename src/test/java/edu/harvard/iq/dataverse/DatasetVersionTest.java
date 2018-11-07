package edu.harvard.iq.dataverse;

import edu.harvard.iq.dataverse.mocks.MocksFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static edu.harvard.iq.dataverse.mocks.MocksFactory.makeFileMetadata;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author michael
 * @author tjanek
 */
public class DatasetVersionTest {

    public DatasetVersionTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testComparator() {
        DatasetVersion ds1_0 = new DatasetVersion();
        ds1_0.setId(0l);
        ds1_0.setVersionNumber( 1l );
        ds1_0.setMinorVersionNumber( 0l );
        ds1_0.setVersionState(DatasetVersion.VersionState.RELEASED);
        
        DatasetVersion ds1_1 = new DatasetVersion();
        ds1_1.setId(1l);
        ds1_1.setVersionNumber( 1l );
        ds1_1.setMinorVersionNumber( 1l );
        ds1_1.setVersionState(DatasetVersion.VersionState.RELEASED);
        
        DatasetVersion ds2_0 = new DatasetVersion();
        ds2_0.setId(2l);
        ds2_0.setVersionNumber( 2l );
        ds2_0.setMinorVersionNumber( 0l );
        ds2_0.setVersionState(DatasetVersion.VersionState.RELEASED);
        
        DatasetVersion ds_draft = new DatasetVersion();
        ds_draft.setId(3l);
        ds_draft.setVersionState(DatasetVersion.VersionState.DRAFT);
        
        List<DatasetVersion> expected = Arrays.asList( ds1_0, ds1_1, ds2_0, ds_draft );
        List<DatasetVersion> actual = Arrays.asList( ds2_0, ds1_0, ds_draft, ds1_1 );
        Collections.sort(actual, DatasetVersion.compareByVersion);
        assertEquals( expected, actual );
    }

    @Test
    public void testIsInReview() {
        Dataset ds = MocksFactory.makeDataset();
        
        DatasetVersion draft = ds.getCreateVersion();
        draft.setVersionState(DatasetVersion.VersionState.DRAFT);
        ds.addLock(new DatasetLock(DatasetLock.Reason.InReview, MocksFactory.makeAuthenticatedUser("Lauren", "Ipsumowitch")));
        assertTrue(draft.isInReview());

        DatasetVersion nonDraft = new DatasetVersion();
        nonDraft.setVersionState(DatasetVersion.VersionState.RELEASED);
        assertEquals(false, nonDraft.isInReview());
        
        ds.addLock(null);
        assertFalse(nonDraft.isInReview());
    }

    @Test
    public void testGetJsonLd() throws ParseException {
        Dataset dataset = new Dataset();
        dataset.setProtocol("doi");
        dataset.setAuthority("10.5072/FK2");
        dataset.setIdentifier("LK0D1H");
        DatasetVersion datasetVersion = new DatasetVersion();
        datasetVersion.setDataset(dataset);
        datasetVersion.setVersionState(DatasetVersion.VersionState.DRAFT);
        assertEquals("", datasetVersion.getPublicationDateAsString());
        // Only published datasets return any JSON.
        assertEquals("", datasetVersion.getJsonLd());
        datasetVersion.setVersionState(DatasetVersion.VersionState.RELEASED);
        datasetVersion.setVersionNumber(1L);
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyyMMdd");
        Date publicationDate = dateFmt.parse("19551105");
        datasetVersion.setReleaseTime(publicationDate);
        dataset.setPublicationDate(new Timestamp(publicationDate.getTime()));
        Dataverse dataverse = new Dataverse();
        dataverse.setName("LibraScholar");
        dataset.setOwner(dataverse);
        String jsonLd = datasetVersion.getJsonLd();
        System.out.println("jsonLd: " + jsonLd);
        JsonReader jsonReader = Json.createReader(new StringReader(jsonLd));
        JsonObject obj = jsonReader.readObject();
        assertEquals("http://schema.org", obj.getString("@context"));
        assertEquals("Dataset", obj.getString("@type"));
        assertEquals("https://doi.org/10.5072/FK2/LK0D1H", obj.getString("identifier"));
        assertEquals("https://schema.org/version/3.3", obj.getString("schemaVersion"));
        assertEquals("1955-11-05", obj.getString("dateModified"));
        assertEquals("1955-11-05", obj.getString("datePublished"));
        assertEquals("1", obj.getString("version"));
        // TODO: if it ever becomes easier to mock a dataset title, test it.
        assertEquals("", obj.getString("name"));
        // TODO: If it ever becomes easier to mock authors, test them.
        JsonArray emptyArray = Json.createArrayBuilder().build();
        assertEquals(emptyArray, obj.getJsonArray("author"));
        // TODO: If it ever becomes easier to mock subjects, test them.
        assertEquals(emptyArray, obj.getJsonArray("keywords"));
        assertEquals("Dataverse", obj.getJsonObject("provider").getString("name"));
        assertEquals("LibraScholar", obj.getJsonObject("includedInDataCatalog").getString("name"));
    }

    @Test
    public void shouldSortFileMetadataByDisplayOrder() {
        // given
        DatasetVersion version = withUnSortedFiles();

        // when
        List<FileMetadata> orderedMetadatas = version.getFileMetadatasSorted();

        // then
        verifySortOrder(orderedMetadatas, "file4.png", 0);
        verifySortOrder(orderedMetadatas, "file3.png", 1);
        verifySortOrder(orderedMetadatas, "file5.png", 2);
        verifySortOrder(orderedMetadatas, "file2.png", 3);
        verifySortOrder(orderedMetadatas, "file6.png", 4);
        verifySortOrder(orderedMetadatas, "file1.png", 5);
    }

    private void verifySortOrder(List<FileMetadata> metadatas, String label, int expectedOrderIndex) {
        assertEquals(label, metadatas.get(expectedOrderIndex).getLabel());
    }

    private DatasetVersion withUnSortedFiles() {
        DatasetVersion datasetVersion = new DatasetVersion();

        datasetVersion.setFileMetadatas(newArrayList(
                makeFileMetadata("file2.png", 3),
                makeFileMetadata("file1.png", 5),
                makeFileMetadata("file3.png", 1),
                makeFileMetadata("file4.png", 0),
                makeFileMetadata("file5.png", 2),
                makeFileMetadata("file6.png", 4)
        ));

        return datasetVersion;
    }

}
