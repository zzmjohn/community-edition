/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 * This file is part of Alfresco
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.share.search;

import org.alfresco.po.share.search.LiveSearchDocumentResult;
import org.alfresco.po.share.search.LiveSearchDropdown;
import org.alfresco.po.share.search.LiveSearchPeopleResult;
import org.alfresco.po.share.search.LiveSearchSiteResult;
import org.alfresco.po.share.site.SiteDashboardPage;
import org.alfresco.po.share.site.document.ContentDetails;
import org.alfresco.po.share.site.document.ContentType;
import org.alfresco.po.share.site.document.DocumentDetailsPage;
import org.alfresco.po.share.site.document.DocumentLibraryPage;
import org.alfresco.po.share.user.MyProfilePage;
import org.alfresco.test.FailedTestListener;
import org.alfresco.po.share.util.SiteUtil;
import org.alfresco.share.util.AbstractUtils;
import org.alfresco.share.util.ShareUser;
import org.alfresco.share.util.ShareUserLiveSearch;
import org.alfresco.share.util.ShareUserSitePage;
import org.alfresco.share.util.api.CreateUserAPI;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Live search tests
 * 
 * @author jcule
 */
@Listeners(FailedTestListener.class)
public class LiveSearchTest extends AbstractUtils
{
    private static final Logger logger = Logger.getLogger(LiveSearchTest.class);

    private static String testPassword = DEFAULT_PASSWORD;
    protected String testUser;
    protected String siteName = "";

    @Override
    @BeforeClass(alwaysRun = true)
    public void setup() throws Exception
    {
        super.setup();
        testName = this.getClass().getSimpleName();
        testUser = testName + "@" + DOMAIN_FREE;
        logger.info("Starting Tests: " + testName);
    }

    /**
     * DataPreparation method - AONE_14200
     * 1) Login
     * 2) Create User
     * 3) Create Site
     * 4) Create and upload file with content
     * 
     * @throws Exception
     */

    @Test(groups = { "DataPrepLiveSearch" })
    public void dataPrep_LiveSearch_AONE_14200() throws Exception
    {
        String testName = getTestName().replace("-", "");
        String testUser = testName + "@" + DOMAIN_LIVE_SEARCH;
        String[] testUserInfo = new String[] { testUser };
        String siteName = getSiteName(testName);

        // Create user
        CreateUserAPI.createActivateUserAsTenantAdmin(drone, ADMIN_USERNAME, testUserInfo);

        // Login as created user
        ShareUser.login(drone, testUser, testPassword);

        // Create site
        SiteUtil.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);

        // Uploading files with testName as document title.
        String[] fileInfo = { testName };
        ShareUser.uploadFileInFolder(drone, fileInfo);

        // Created user logs out
        ShareUser.logout(drone);

    }

    /**
     * 1) User logs in
     * 2) Performs live search with testName as a search term
     * 3) Checks that the created document is displayed in document search results
     * 4) Checks that the created site is displayed in site search results
     * 5) Checks that the created user name is displayed in people search results
     * 6) Checks that all the links in live search results work properly
     * 7) User logs out
     */
    @Test(groups = { "TestLiveSearch","TestBug"})
    public void AONE_14200()
    {

        // live search term is document title
        testName = getTestName().replace("-", "");
        String testUser = testName + "@" + DOMAIN_LIVE_SEARCH;
        String siteName = getSiteName(testName);

        ShareUser.login(drone, testUser, testPassword);

        LiveSearchDropdown liveSearchDropdown = ShareUserLiveSearch.liveSearch(drone, testName);
        List<LiveSearchDocumentResult> liveSearchDocumentResults = ShareUserLiveSearch.getLiveSearchDocumentResults(liveSearchDropdown);

        if (liveSearchDocumentResults.size() == 0)
        {
            drone.deleteCookies();
            drone.refresh();
            drone.getCurrentPage().render();
            ShareUser.login(drone, testUser, testPassword);
            liveSearchDropdown = ShareUserLiveSearch.liveSearch(drone, testName);
            liveSearchDocumentResults = ShareUserLiveSearch.getLiveSearchDocumentResults(liveSearchDropdown);
        }

        // Checks document titles
        Assert.assertTrue(liveSearchDocumentResults.size() > 0, "Failed to find document: ISSUE ACE-3319");
        List<String> documentTitles = ShareUserLiveSearch.getLiveSearchDocumentTitles(liveSearchDocumentResults);

        if (!documentTitles.contains(testName))
        {
            drone.refresh();
            drone.getCurrentPage();
            liveSearchDropdown = ShareUserLiveSearch.liveSearch(drone, testName);
            liveSearchDocumentResults = ShareUserLiveSearch.getLiveSearchDocumentResults(liveSearchDropdown);
            documentTitles = ShareUserLiveSearch.getLiveSearchDocumentTitles(liveSearchDocumentResults);
        }

        Assert.assertTrue(documentTitles.contains(testName));

        // Checks document sites name
        List<String> documentSiteNames = ShareUserLiveSearch.getLiveSearchDocumentSiteNames(liveSearchDocumentResults);
        Assert.assertTrue(documentSiteNames.contains(siteName));

        // Checks document user names
        List<String> documentUserNames = ShareUserLiveSearch.getLiveSearchDocumentUserNames(liveSearchDocumentResults);
        Assert.assertTrue(documentUserNames.contains(testUser.toLowerCase()));

        // Checks site result
        List<LiveSearchSiteResult> liveSearchSitesResults = ShareUserLiveSearch.getLiveSearchSitesResults(liveSearchDropdown);
        List<String> sitesNames = ShareUserLiveSearch.getLiveSearchSitesTitles(liveSearchSitesResults);
        Assert.assertTrue(sitesNames.contains(siteName));

        // Checks people result
        List<LiveSearchPeopleResult> liveSearchPeopleResults = ShareUserLiveSearch.getLiveSearchPeopleResults(liveSearchDropdown);
        List<String> peopleNames = ShareUserLiveSearch.getLiveSearchUserNames(liveSearchPeopleResults);
        for (String peopleName : peopleNames)
        {
            Assert.assertTrue(peopleName.contains(testUser));
        }

        // Clicks on document title
        DocumentDetailsPage documentDetailsPage = ShareUserLiveSearch.clickOnDocumentSearchResultTitle(liveSearchDropdown, testName).render();
        Assert.assertEquals(testName, documentDetailsPage.getDocumentTitle());

        // Clicks on document site name
        liveSearchDropdown = ShareUserLiveSearch.liveSearch(drone, testName);

        DocumentLibraryPage documentLibraryPage = ShareUserLiveSearch.clickOnDocumentSiteName(liveSearchDropdown, siteName).render();
        Assert.assertTrue(documentLibraryPage.isFileVisible(testName));

        // Clicks on document user name
        liveSearchDropdown = ShareUserLiveSearch.liveSearch(drone, testName);
        MyProfilePage myProfilePage = ShareUserLiveSearch.clickOnDocumentUserName(liveSearchDropdown, testUser).render();
        Assert.assertEquals(myProfilePage.getPageTitle(), "User Profile Page");

        // Clicks on site name in sites results
        liveSearchDropdown = ShareUserLiveSearch.liveSearch(drone, testName);
        SiteDashboardPage siteDashboardPage = ShareUserLiveSearch.clickOnSiteResultSiteName(liveSearchDropdown, siteName).render();
        Assert.assertTrue(siteDashboardPage.isSiteTitle(siteName));

        // clicks on user name in people results
        liveSearchDropdown = ShareUserLiveSearch.liveSearch(drone, testName);
        myProfilePage = ShareUserLiveSearch.clickOnPeopleResultUserName(liveSearchDropdown, testUser).render();
        Assert.assertEquals(myProfilePage.getPageTitle(), "User Profile Page");

        ShareUser.logout(drone);

    }

    /**
     * DataPreparation method - AONE_14201
     * 1) Login
     * 2) Create User
     * 3) Create Site
     * 4) Create and upload different types of files
     * 
     * @throws Exception
     */

    @Test(groups = { "DataPrepLiveSearch" })
    public void dataPrep_LiveSearch_AONE_14201() throws Exception
    {
        String testName = getTestName();
        String testUser = testName + "@" + DOMAIN_LIVE_SEARCH;
        String[] testUserInfo = new String[] { testUser };
        String siteName = getSiteName(testName + 1);

        // Files
        String[] extensions = { "xlsx", "txt", "msg", "pdf", "xml", "html", "eml", "opd", "ods", "odt", "xls", "xsl", "doc", "docx", "pptx", "pot", "xsd",
                "js", "java", "css", "rtf" };
        String[] fileName = new String[extensions.length];
        for (int i = 0; i < extensions.length; i++)
        {
            fileName[i] = getFileName(testName + "." + extensions[i]);
        }

        Integer fileTypes = fileName.length - 1;

        // Create user
        CreateUserAPI.createActivateUserAsTenantAdmin(drone, ADMIN_USERNAME, testUserInfo);

        // Login as created user
        ShareUser.login(drone, testUser, testPassword);

        // Create site
        SiteUtil.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);

        // UpLoad Files
        for (int index = 0; index <= fileTypes; index++)
        {
            String[] fileInfo = { fileName[index] };
            ShareUser.uploadFileInFolder(drone, fileInfo);
        }

        // Created user logs out
        ShareUser.logout(drone);

    }

    /**
     * 1) User logs in
     * 2) Performs live search with searchTerm1 = testName + ".xml" as a search term
     * 3) Checks that the created document is displayed in document search results
     * 4) Checks that there are no sites results
     * 5) Checks that there are no people results
     * 6) Clicks on the document title in document search results and checks that document details
     * page is displayed
     * 7) Clicks on site name in document search results and checks that the site document library
     * page is displayed
     * 8) Clicks on user name in document search results and checks that user profile
     * page is displayed
     * 9) Performs live search with searchTerm2 = testName as a search term
     * 10)Clicks on the More results... icon and verifies that all created documents are
     * displayed in document search results
     * 11) User logs out
     */
    @Test(groups = { "TestLiveSearch","TestBug" })
    public void AONE_14201()
    {

        testName = getTestName();
        String testUser = testName + "@" + DOMAIN_LIVE_SEARCH;
        String siteName = getSiteName(testName + 1);

        String searchTerm1 = testName + ".xml";

        String searchTerm2 = testName;

        ShareUser.login(drone, testUser, testPassword);

        LiveSearchDropdown liveSearchDropdown = ShareUserLiveSearch.liveSearch(drone, searchTerm1);
        List<LiveSearchDocumentResult> liveSearchDocumentResults = ShareUserLiveSearch.getLiveSearchDocumentResults(liveSearchDropdown);

        if (liveSearchDocumentResults.size() != 0)
        {
            drone.deleteCookies();
            drone.refresh();
            drone.getCurrentPage().render();
            ShareUser.login(drone, testUser, testPassword);
            liveSearchDropdown = ShareUserLiveSearch.liveSearch(drone, searchTerm1);
            liveSearchDocumentResults = ShareUserLiveSearch.getLiveSearchDocumentResults(liveSearchDropdown);
        }

        // Checks document titles
        Assert.assertTrue(liveSearchDocumentResults.size() > 0);
        List<String> documentTitles = ShareUserLiveSearch.getLiveSearchDocumentTitles(liveSearchDocumentResults);

        if (documentTitles.contains(getFileName(testName + "." + "xml")))
        {
            drone.refresh();
            drone.getCurrentPage();
            liveSearchDropdown = ShareUserLiveSearch.liveSearch(drone, searchTerm1);
            liveSearchDocumentResults = ShareUserLiveSearch.getLiveSearchDocumentResults(liveSearchDropdown);
            documentTitles = ShareUserLiveSearch.getLiveSearchDocumentTitles(liveSearchDocumentResults);
        }

        Assert.assertTrue(documentTitles.contains(getFileName(testName + "." + "xml")));

        // Checks document sites name
        List<String> documentSiteNames = ShareUserLiveSearch.getLiveSearchDocumentSiteNames(liveSearchDocumentResults);
        Assert.assertTrue(documentSiteNames.contains(siteName));

        // Checks document user names
        List<String> documentUserNames = ShareUserLiveSearch.getLiveSearchDocumentUserNames(liveSearchDocumentResults);
        Assert.assertTrue(documentUserNames.contains(testUser.toLowerCase()));

        // Checks site result
        Assert.assertFalse(liveSearchDropdown.isSitesTitleVisible());

        // Checks people result
        Assert.assertFalse(liveSearchDropdown.isPeopleTitleVisible());

        // Clicks on document title
        DocumentDetailsPage documentDetailsPage = ShareUserLiveSearch.clickOnDocumentSearchResultTitle(liveSearchDropdown, getFileName(testName + "." + "xml"))
                .render();
        Assert.assertEquals(getFileName(testName + "." + "xml"), documentDetailsPage.getDocumentTitle());

        // Clicks on document site name
        liveSearchDropdown = ShareUserLiveSearch.liveSearch(drone, searchTerm1);
        DocumentLibraryPage documentLibraryPage = ShareUserLiveSearch.clickOnDocumentSiteName(liveSearchDropdown, siteName).render();
        Assert.assertTrue(documentLibraryPage.isFileVisible(getFileName(testName + "." + "xml")));

        // Clicks on document user name
        liveSearchDropdown = ShareUserLiveSearch.liveSearch(drone, searchTerm1);
        MyProfilePage myProfilePage = ShareUserLiveSearch.clickOnDocumentUserName(liveSearchDropdown, testUser).render();
        Assert.assertEquals(myProfilePage.getPageTitle(), "User Profile Page");

        // search for searchTerm2
        liveSearchDropdown = ShareUserLiveSearch.liveSearch(drone, searchTerm2);
        liveSearchDropdown.clickToSeeMoreDocumentResults();
        liveSearchDocumentResults = ShareUserLiveSearch.getLiveSearchDocumentResults(liveSearchDropdown);
        Assert.assertTrue(liveSearchDocumentResults.size() > 5);

        ShareUser.logout(drone);

    }

    /**
     * DataPreparation method - AONE_14202
     * 1) Login
     * 2) Create User
     * 3) Create Site
     * 4) Create and upload file with fileName = "}{+_)(&^%$#@!"
     * 
     * @throws Exception
     */

    @Test(groups = { "DataPrepLiveSearch" })
    public void dataPrep_LiveSearch_AONE_14202() throws Exception
    {
        String testName = getTestName();
        String testUser = testName + "@" + DOMAIN_LIVE_SEARCH;
        String siteName = getSiteName(testName);
        String fileName = "}{+_)(&^%$#@!";

        // TODO: Remove all try, catch: Redundant as Listener is being used
        try
        {
            // User is created
            String[] testUserInfo = new String[] { testUser };
            CreateUserAPI.createActivateUserAsTenantAdmin(drone, ADMIN_USERNAME, testUserInfo);

            // Created user logs in
            ShareUser.login(drone, testUser, testPassword);

            // Created user creates a site
            ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);

            // and uploads created file to site's document library
            String[] fileInfo = { fileName };
            ShareUser.uploadFileInFolder(drone, fileInfo);

            // Created user logs out
            ShareUser.logout(drone);
        }
        catch (Throwable e)
        {
            reportError(drone, testName, e);
        }
        finally
        {
            testCleanup(drone, testName);
        }
    }

    /**
     * 1) User logs in
     * 2) Performs live search with searchTerm1 = "!@#$%^&*()_+:\"|<>?;" as a search term
     * 3) Checks that there are no document results
     * 4) Checks that there are no sites results
     * 5) Checks that there are no people results
     * 6) Performs live search with searchTerm1 = "}{+_)(&^%$#@!" as a search term
     * 7) Checks that there are no document results
     * 8) Checks that there are no sites results
     * 9) Checks that there are no people results
     * 10)User logs out
     */
    @Test(groups = { "TestLiveSearch" })
    public void AONE_14202()
    {

        testName = getTestName();
        String testUser = testName + "@" + DOMAIN_LIVE_SEARCH;

        // String searchTerm1 = "!@#$%^&*()_+:\"|<>?;";
        // String searchTerm2 = "}{+_)(&^%$#@!";

        String searchTerm1 = "!@#";
        String searchTerm2 = "}{";

        ShareUser.login(drone, testUser, testPassword);

        // Check document results
        LiveSearchDropdown liveSearchDropdown = ShareUserLiveSearch.liveSearch(drone, searchTerm1);

        Assert.assertFalse(liveSearchDropdown.isDocumentsTitleVisible());
        Assert.assertFalse(liveSearchDropdown.isSitesTitleVisible());
        Assert.assertFalse(liveSearchDropdown.isPeopleTitleVisible());

        liveSearchDropdown = ShareUserLiveSearch.liveSearch(drone, searchTerm2);

        Assert.assertFalse(liveSearchDropdown.isDocumentsTitleVisible());
        Assert.assertFalse(liveSearchDropdown.isSitesTitleVisible());
        Assert.assertFalse(liveSearchDropdown.isPeopleTitleVisible());

        ShareUser.logout(drone);
    }

    /**
     * DataPreparation method - AONE_14203
     * 1) Login
     * 2) Create User
     * 3) Create Site
     * 4) Log out
     * 
     * @throws Exception
     */

    @Test(groups = { "DataPrepLiveSearch" })
    public void dataPrep_LiveSearch_AONE_14203() throws Exception
    {
        String testName = getTestName();
        String testUser = testName + "@" + DOMAIN_LIVE_SEARCH;
        String[] testUserInfo = new String[] { testUser };
        String siteName = getSiteName(testName);

        try
        {
            CreateUserAPI.createActivateUserAsTenantAdmin(drone, ADMIN_USERNAME, testUserInfo);

            ShareUser.login(drone, testUser, testPassword);

            ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);

            ShareUser.logout(drone);
        }
        catch (Throwable e)
        {
            reportError(drone, testName, e);
        }
        finally
        {
            testCleanup(drone, testName);
        }
    }

    /**
     * 1) User logs in
     * 2) Performs live search with too long searchTerm
     * 3) Checks that there are no document results
     * 4) Checks that there are no sites results
     * 5) Checks that there are no people results
     * 6) User logs out
     */
    @Test(groups = { "TestLiveSearch"})
    public void AONE_14203()
    {
        testName = getTestName();
        String testUser = testName + "@" + DOMAIN_LIVE_SEARCH;
        String searchTerm = ShareUser.getRandomStringWithNumders(1030);

        ShareUser.login(drone, testUser, testPassword);

        // Check document results
        LiveSearchDropdown liveSearchDropdown = ShareUserLiveSearch.liveSearch(drone, searchTerm);
        Assert.assertFalse(liveSearchDropdown.isDocumentsTitleVisible());
        Assert.assertFalse(liveSearchDropdown.isSitesTitleVisible());
        Assert.assertFalse(liveSearchDropdown.isPeopleTitleVisible());

        ShareUser.logout(drone);

    }

    /**
     * DataPreparation method - AONE_14204
     * 1) Login
     * 2) Create User
     * 3) Create Site
     * 4) Create folders and files
     * 4) Log out
     * 
     * @throws Exception
     */

    @Test(groups = { "DataPrepLiveSearch" })
    public void dataPrep_LiveSearch_AONE_14204() throws Exception
    {
        String testName = getTestName();
        String testUser = testName + "@" + DOMAIN_LIVE_SEARCH;
        String[] testUserInfo = new String[] { testUser };
        String siteName = getSiteName(testName);

        String[] folders = { "H0us8 1", "H0us8 2", "H0us8 3" };
        String[] folderTitles_Descriptions = { "H0us8", "T3chn0" };
        String[] filesWithTitle = { "H0us8 my 11", "H0us8 my 21", "H0us8 my 31", "T3chn0 my" };

        try
        {
            CreateUserAPI.createActivateUserAsTenantAdmin(drone, ADMIN_USERNAME, testUserInfo);

            ShareUser.login(drone, testUser, testPassword);

            ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);

            // Creating files with given Title.
            ContentDetails contentDetails = new ContentDetails();
            contentDetails.setName(filesWithTitle[0]);
            contentDetails.setTitle(folderTitles_Descriptions[0]);
            contentDetails.setDescription(folderTitles_Descriptions[0]);
            ShareUser.createContent(drone, contentDetails, ContentType.PLAINTEXT);

            contentDetails.setName(filesWithTitle[1]);
            contentDetails.setTitle(folderTitles_Descriptions[1]);
            contentDetails.setDescription(folderTitles_Descriptions[0]);
            ShareUser.createContent(drone, contentDetails, ContentType.PLAINTEXT);

            contentDetails.setName(filesWithTitle[2]);
            contentDetails.setTitle(folderTitles_Descriptions[0]);
            contentDetails.setDescription(folderTitles_Descriptions[1]);
            ShareUser.createContent(drone, contentDetails, ContentType.PLAINTEXT);

            contentDetails.setName(filesWithTitle[3]);
            contentDetails.setTitle(folderTitles_Descriptions[0]);
            contentDetails.setDescription(folderTitles_Descriptions[0]);
            ShareUser.createContent(drone, contentDetails, ContentType.PLAINTEXT);

            // Creating folders
            ShareUserSitePage.createFolder(drone, folders[0], folderTitles_Descriptions[0], folderTitles_Descriptions[0]);
            ShareUserSitePage.createFolder(drone, folders[1], folderTitles_Descriptions[1], folderTitles_Descriptions[0]);
            ShareUserSitePage.createFolder(drone, folders[2], folderTitles_Descriptions[0], folderTitles_Descriptions[1]);

        }
        catch (Throwable e)
        {
            reportError(drone, testName, e);
        }
        finally
        {
            testCleanup(drone, testName);
        }
    }

    /**
     * 1) User logs in
     * 2) Performs live search with search term "house my 31"
     * 3) Checks that document search results return document with title "House my 31"
     * 4) Checks that document search results don't return document with title "Techno my"
     * 5) Checks that there are no sites and people results returned
     * 6) User logs out
     */
    @Test(groups = { "TestLiveSearch","TestBug" })
    public void AONE_14204()
    {
        testName = getTestName();
        String testUser = testName + "@" + DOMAIN_LIVE_SEARCH;
        String searchTerm = "H0us8 my 31";

        ShareUser.login(drone, testUser, testPassword);

        // Check document results
        LiveSearchDropdown liveSearchDropdown = ShareUserLiveSearch.liveSearch(drone, searchTerm);
        List<LiveSearchDocumentResult> liveSearchDocumentResults = ShareUserLiveSearch.getLiveSearchDocumentResults(liveSearchDropdown);
        List<String> documentTitles = ShareUserLiveSearch.getLiveSearchDocumentTitles(liveSearchDocumentResults);

        if (!documentTitles.contains("H0us8 my 31"))
        {
            drone.deleteCookies();
            drone.refresh();
            drone.getCurrentPage().render();
            ShareUser.login(drone, testUser, testPassword);
            liveSearchDropdown = ShareUserLiveSearch.liveSearch(drone, searchTerm);
            liveSearchDocumentResults = ShareUserLiveSearch.getLiveSearchDocumentResults(liveSearchDropdown);
            documentTitles = ShareUserLiveSearch.getLiveSearchDocumentTitles(liveSearchDocumentResults);
        }

        Assert.assertTrue(documentTitles.contains("H0us8 my 31"));
        Assert.assertFalse(documentTitles.contains("T3chn0 my"));

        Assert.assertFalse(liveSearchDropdown.isSitesTitleVisible());
        Assert.assertFalse(liveSearchDropdown.isPeopleTitleVisible());

        ShareUser.logout(drone);
    }

    /**
     * DataPreparation method - AONE_14208
     * 1) Login
     * 2) Create User
     * 3) Create Site
     * 4) Create folders and files
     * 4) Log out
     * 
     * @throws Exception
     */
    @Test(groups = { "DataPrepLiveSearch" })
    public void dataPrep_LiveSearch_AONE_14208() throws Exception
    {
        String testName = getTestName();
        String testUser = testName + "@" + DOMAIN_LIVE_SEARCH;
        String[] testUserInfo = new String[] { testUser };
        String siteName = getSiteName(testName);

        String[] folders = { "H0us8 1", "H0us8 2", "H0us8 3" };
        String[] folderTitles_Descriptions = { "H0us8", "T3chn0" };
        String[] filesWithTitle = { "H0us8 my 11", "H0us8 my 21", "H0us8 my 31", "T3chn0 my" };

        try
        {
            CreateUserAPI.createActivateUserAsTenantAdmin(drone, ADMIN_USERNAME, testUserInfo);

            ShareUser.login(drone, testUser, testPassword);

            ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);

            // Creating files with given Title.
            ContentDetails contentDetails = new ContentDetails();
            contentDetails.setName(filesWithTitle[0]);
            contentDetails.setTitle(folderTitles_Descriptions[0]);
            contentDetails.setDescription(folderTitles_Descriptions[0]);
            ShareUser.createContent(drone, contentDetails, ContentType.PLAINTEXT);

            contentDetails.setName(filesWithTitle[1]);
            contentDetails.setTitle(folderTitles_Descriptions[1]);
            contentDetails.setDescription(folderTitles_Descriptions[0]);
            ShareUser.createContent(drone, contentDetails, ContentType.PLAINTEXT);

            contentDetails.setName(filesWithTitle[2]);
            contentDetails.setTitle(folderTitles_Descriptions[0]);
            contentDetails.setDescription(folderTitles_Descriptions[1]);
            ShareUser.createContent(drone, contentDetails, ContentType.PLAINTEXT);

            contentDetails.setName(filesWithTitle[3]);
            contentDetails.setTitle(folderTitles_Descriptions[0]);
            contentDetails.setDescription(folderTitles_Descriptions[0]);
            ShareUser.createContent(drone, contentDetails, ContentType.PLAINTEXT);

            // Creating folders
            ShareUserSitePage.createFolder(drone, folders[0], folderTitles_Descriptions[0], folderTitles_Descriptions[0]);
            ShareUserSitePage.createFolder(drone, folders[1], folderTitles_Descriptions[1], folderTitles_Descriptions[0]);
            ShareUserSitePage.createFolder(drone, folders[2], folderTitles_Descriptions[0], folderTitles_Descriptions[1]);

        }
        catch (Throwable e)
        {
            reportError(drone, testName, e);
        }
        finally
        {
            testCleanup(drone, testName);
        }
    }

    /**
     * 1) User logs in
     * 2) Performs live search with search term "h0us8 31"
     * 3) Checks that document search results return document with title "House my 31"
     * 4) Checks that document search results don't return document with title "T3chn0 my"
     * 5) Checks that there are no sites and people results returned
     * 6) User logs out
     */
    @Test(groups = { "TestLiveSearch","TestBug" })
    public void AONE_14208()
    {
        testName = getTestName();
        String testUser = testName + "@" + DOMAIN_LIVE_SEARCH;
        String searchTerm = "H0us8 31";

        ShareUser.login(drone, testUser, testPassword);

        // Check document results
        LiveSearchDropdown liveSearchDropdown = ShareUserLiveSearch.liveSearch(drone, searchTerm);

        // Check document results
        List<LiveSearchDocumentResult> liveSearchDocumentResults = ShareUserLiveSearch.getLiveSearchDocumentResults(liveSearchDropdown);
        List<String> documentTitles = ShareUserLiveSearch.getLiveSearchDocumentTitles(liveSearchDocumentResults);

        if (!documentTitles.contains("H0us8 my 31"))
        {
            drone.deleteCookies();
            drone.refresh();
            drone.getCurrentPage().render();
            ShareUser.login(drone, testUser, testPassword);
            liveSearchDropdown = ShareUserLiveSearch.liveSearch(drone, searchTerm);
            liveSearchDocumentResults = ShareUserLiveSearch.getLiveSearchDocumentResults(liveSearchDropdown);
            documentTitles = ShareUserLiveSearch.getLiveSearchDocumentTitles(liveSearchDocumentResults);
        }

        Assert.assertTrue(documentTitles.contains("H0us8 my 31"));
        Assert.assertFalse(documentTitles.contains("T3chn0 my"));

        // Checks site result
        Assert.assertFalse(liveSearchDropdown.isSitesTitleVisible());

        // Checks people result
        Assert.assertFalse(liveSearchDropdown.isPeopleTitleVisible());

        ShareUser.logout(drone);
    }

    /**
     * DataPreparation method - AONE_13893
     * 1) Login
     * 2) Create User
     * 3) Create Site
     * 4) Create folders and files
     * 4) Log out
     * 
     * @throws Exception
     */
    @Test(groups = { "DataPrepLiveSearch" })
    public void dataPrep_LiveSearch_AONE_14209() throws Exception
    {
        String testName = getTestName();
        String testUser = testName + "@" + DOMAIN_LIVE_SEARCH;
        String[] testUserInfo = new String[] { testUser };
        String siteName = getSiteName(testName);

        String[] folders = { "H0us8 1", "H0us8 2", "H0us8 3" };
        String[] folderTitles_Descriptions = { "H0us8", "T3chn0" };
        String[] filesWithTitle = { "H0us8 my 11", "H0us8 my 21", "H0us8 my 31", "T3chn0 my" };

        try
        {
            CreateUserAPI.createActivateUserAsTenantAdmin(drone, ADMIN_USERNAME, testUserInfo);

            ShareUser.login(drone, testUser, testPassword);

            ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);

            // Creating files with given Title.
            ContentDetails contentDetails = new ContentDetails();
            contentDetails.setName(filesWithTitle[0]);
            contentDetails.setTitle(folderTitles_Descriptions[0]);
            contentDetails.setDescription(folderTitles_Descriptions[0]);
            ShareUser.createContent(drone, contentDetails, ContentType.PLAINTEXT);

            contentDetails.setName(filesWithTitle[1]);
            contentDetails.setTitle(folderTitles_Descriptions[1]);
            contentDetails.setDescription(folderTitles_Descriptions[0]);
            ShareUser.createContent(drone, contentDetails, ContentType.PLAINTEXT);

            contentDetails.setName(filesWithTitle[2]);
            contentDetails.setTitle(folderTitles_Descriptions[0]);
            contentDetails.setDescription(folderTitles_Descriptions[1]);
            ShareUser.createContent(drone, contentDetails, ContentType.PLAINTEXT);

            contentDetails.setName(filesWithTitle[3]);
            contentDetails.setTitle(folderTitles_Descriptions[0]);
            contentDetails.setDescription(folderTitles_Descriptions[0]);
            ShareUser.createContent(drone, contentDetails, ContentType.PLAINTEXT);

            // Creating folders
            ShareUserSitePage.createFolder(drone, folders[0], folderTitles_Descriptions[0], folderTitles_Descriptions[0]);
            ShareUserSitePage.createFolder(drone, folders[1], folderTitles_Descriptions[1], folderTitles_Descriptions[0]);
            ShareUserSitePage.createFolder(drone, folders[2], folderTitles_Descriptions[0], folderTitles_Descriptions[1]);

        }
        catch (Throwable e)
        {
            reportError(drone, testName, e);
        }
        finally
        {
            testCleanup(drone, testName);
        }
    }

    /**
     * 1) User logs in
     * 2) Performs live search with search term "T3chn0"
     * 3) Checks that document search results return document with title "H0us8 my 21"
     * 4) Checks that there are no sites and people results returned
     * 5) User logs out
     */
    @Test(groups = { "TestLiveSearch","TestBug" })
    public void AONE_14209()
    {
        testName = getTestName();
        String testUser = testName + "@" + DOMAIN_LIVE_SEARCH;
        String searchTerm = "T3chn0";

        ShareUser.login(drone, testUser, testPassword);

        // Check document results
        LiveSearchDropdown liveSearchDropdown = ShareUserLiveSearch.liveSearch(drone, searchTerm);

        // Check document results
        List<LiveSearchDocumentResult> liveSearchDocumentResults = ShareUserLiveSearch.getLiveSearchDocumentResults(liveSearchDropdown);
        List<String> documentTitles = ShareUserLiveSearch.getLiveSearchDocumentTitles(liveSearchDocumentResults);

        if (!documentTitles.contains("H0us8 my 21"))
        {
            drone.deleteCookies();
            drone.refresh();
            drone.getCurrentPage().render();
            ShareUser.login(drone, testUser, testPassword);
            liveSearchDropdown = ShareUserLiveSearch.liveSearch(drone, searchTerm);
            liveSearchDocumentResults = ShareUserLiveSearch.getLiveSearchDocumentResults(liveSearchDropdown);
            documentTitles = ShareUserLiveSearch.getLiveSearchDocumentTitles(liveSearchDocumentResults);
        }

        Assert.assertTrue(documentTitles.contains("H0us8 my 21"));

        // Checks site result
        Assert.assertFalse(liveSearchDropdown.isSitesTitleVisible());

        // Checks people result
        Assert.assertFalse(liveSearchDropdown.isPeopleTitleVisible());

        ShareUser.logout(drone);
    }

    /**
     * DataPreparation method - AONE_13892
     * 1) Login
     * 2) Create User that is not system tenant i.e. not alfresco.com user
     * 3) Create Site
     * 4) Create and upload file with content
     * 
     * @throws Exception
     */

    /**
     * DataPreparation method - AONE_14205
     * 1) Login
     * 2) Create User that is not system tenant i.e. not alfresco.com user
     * 3) Create site with site name = "n3w s1t3 creat3ed 88"
     * 4) Create site with site name = "n3w s1t3 creat3ed 99"
     * 
     * @throws Exception
     */

    @Test(groups = { "DataPrepLiveSearch" })
    public void dataPrep_LiveSearch_AONE_14205() throws Exception
    {
        String testName = getTestName();
        String testUser = testName + "@" + DOMAIN_LIVE_SEARCH;
        String[] testUserInfo = new String[] { testUser };
        String siteName1 = "n3w s1t3 creat3ed 88";
        String siteName2 = "n3w s1t3 creat3ed 99";

        try
        {
            // Create user
            CreateUserAPI.createActivateUserAsTenantAdmin(drone, ADMIN_USERNAME, testUserInfo);

            // Login as created user
            ShareUser.login(drone, testUser, testPassword);

            // Create sites
            SiteUtil.createSite(drone, siteName1, AbstractUtils.SITE_VISIBILITY_PUBLIC);
            SiteUtil.createSite(drone, siteName2, AbstractUtils.SITE_VISIBILITY_PUBLIC);

            // Created user logs out
            ShareUser.logout(drone);

        }
        catch (Throwable e)
        {
            reportError(drone, testName, e);
        }
        finally
        {
            testCleanup(drone, testName);
        }

    }

    /**
     * 1) User logs in
     * 2) Performs live search with searchterm1 = siteName1 = "n3w s1t3 88"
     * 4) Checks that the site with name "n3w s1t3 creat3ed 88" is returned in live search sites results
     * 5) Checks that the site with name "n3w s1t3 creat3ed 99" is not returned in live search sites results
     */

    @Test(groups = { "TestLiveSearch" ,"TestBug"})
    public void AONE_14205()
    {

        // live search term is document title
        testName = getTestName();
        String testUser = testName + "@" + DOMAIN_LIVE_SEARCH;
        ShareUser.login(drone, testUser, testPassword);

        String searchTerm1 = "n3w s1t3 88";

        // Checks document sites name
        LiveSearchDropdown liveSearchDropdown = ShareUserLiveSearch.liveSearch(drone, searchTerm1);
        List<LiveSearchSiteResult> liveSearchSitesResults = ShareUserLiveSearch.getLiveSearchSitesResults(liveSearchDropdown);
        List<String> sitesNames = ShareUserLiveSearch.getLiveSearchSitesTitles(liveSearchSitesResults);

        if (!sitesNames.contains("n3w s1t3 creat3ed 88"))
        {
            drone.deleteCookies();
            drone.refresh();
            drone.getCurrentPage().render();
            ShareUser.login(drone, testUser, testPassword);
            liveSearchDropdown = ShareUserLiveSearch.liveSearch(drone, searchTerm1);
            liveSearchSitesResults = ShareUserLiveSearch.getLiveSearchSitesResults(liveSearchDropdown);
            sitesNames = ShareUserLiveSearch.getLiveSearchSitesTitles(liveSearchSitesResults);
        }

        Assert.assertFalse(sitesNames.contains("n3w s1t3 creat3ed 99"));

        // Query issue to fix? !!!!!!
        Assert.assertTrue(sitesNames.contains("n3w s1t3 creat3ed 88"));

    }

    /**
     * DataPreparation method - AONE_14206
     * 1) Login
     * 2) Create user that is not system tenant i.e. not alfresco.com user with user name = "n3w us3r creat3ed 77"
     * 3) Create user that is not system tenant i.e. not alfresco.com user with user name = "n3w us3r creat3ed 55"
     * 4) Create site with site name = "n3w s1t3 creat3ed 99"
     * 
     * @throws Exception
     */

    @Test(groups = { "DataPrepLiveSearch" })
    public void dataPrep_LiveSearch_AONE_14206() throws Exception
    {
        String testName = getTestName();
        String testUser1 = "n3w.us3r.77" + "@" + DOMAIN_LIVE_SEARCH;
        String[] testUserInfo1 = new String[] { testUser1 };

        String testUser2 = "n3w.us3r.55" + "@" + DOMAIN_LIVE_SEARCH;
        String[] testUserInfo2 = new String[] { testUser2 };

        try
        {
            // Create users
            CreateUserAPI.createActivateUserAsTenantAdmin(drone, ADMIN_USERNAME, testUserInfo1);
            CreateUserAPI.createActivateUserAsTenantAdmin(drone, ADMIN_USERNAME, testUserInfo2);

            // user logs out
            ShareUser.logout(drone);

        }
        catch (Throwable e)
        {
            reportError(drone, testName, e);
        }
        finally
        {
            testCleanup(drone, testName);
        }

    }

    /**
     * 1) User logs in
     * 2) Performs live search with searchterm1 = siteName1 = "n3w us3r 77"
     * 4) Checks that the user with name "n3w.us3r.77" is returned in live search sites results
     * 5) Checks that the user with name "n3w.us3r.55" is not returned in live search sites results
     */

    @Test(groups = { "TestLiveSearch","TestBug" })
    public void AONE_14206()
    {

        testName = getTestName();
        String testUser1 = "n3w.us3r.77" + "@" + DOMAIN_LIVE_SEARCH;
        String testUser2 = "n3w.us3r.55" + "@" + DOMAIN_LIVE_SEARCH;
        ShareUser.login(drone, testUser1, testPassword);

        String searchTerm = "n3w us3r 77";

        LiveSearchDropdown liveSearchDropdown = ShareUserLiveSearch.liveSearch(drone, searchTerm);
        List<LiveSearchPeopleResult> liveSearchPeopleResults = ShareUserLiveSearch.getLiveSearchPeopleResults(liveSearchDropdown);
        List<String> peopleNames = ShareUserLiveSearch.getLiveSearchUserNames(liveSearchPeopleResults);

        if (!peopleNames.get(0).contains(testUser1))
        {
            drone.deleteCookies();
            drone.refresh();
            drone.getCurrentPage().render();
            ShareUser.login(drone, testUser1, testPassword);
            liveSearchDropdown = ShareUserLiveSearch.liveSearch(drone, searchTerm);
            liveSearchPeopleResults = ShareUserLiveSearch.getLiveSearchPeopleResults(liveSearchDropdown);
            peopleNames = ShareUserLiveSearch.getLiveSearchUserNames(liveSearchPeopleResults);
        }

        for (String peopleName : peopleNames)
        {
            Assert.assertTrue(peopleName.contains(testUser1));
            Assert.assertTrue(!peopleName.contains(testUser2));
        }

    }

    /**
     * DataPreparation method - AONE_15127
     * 1) Login
     * 2) Create User
     * 3) Create Site
     * 4) Create two files: fileName1=file-1234-0 and fileContent1 = "w0rd1 w0rd3 w0rd5"
     * fileName2=file-1234-1 and fileContent1 = "w0rd1 w0rd7 w0rd9"
     * 4) Log out
     * 
     * @throws Exception
     */

    @Test(groups = { "DataPrepLiveSearch", "EnterpriseOnly" })
    public void dataPrep_LiveSearch_AONE_15127() throws Exception
    {
        String testName = getTestName();
        String testUser = testName + "@" + DOMAIN_LIVE_SEARCH;
        String[] testUserInfo = new String[] { testUser };
        String siteName = getSiteName(testName);

        String fileName1 = "file-1234-0";
        String fileName2 = "file-1234-1";
        String fileContent1 = "w0rd1 w0rd3 w0rd5";
        String fileContent2 = "w0rd1 w0rd7 w0rd9";

        try
        {
            CreateUserAPI.createActivateUserAsTenantAdmin(drone, ADMIN_USERNAME, testUserInfo);

            ShareUser.login(drone, testUser, testPassword);

            ShareUser.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);

            // Creating files with given Title.
            ContentDetails contentDetails = new ContentDetails();
            contentDetails.setName(fileName1);
            contentDetails.setContent(fileContent1);
            ShareUser.createContent(drone, contentDetails, ContentType.PLAINTEXT);

            contentDetails.setName(fileName2);
            contentDetails.setContent(fileContent2);
            ShareUser.createContent(drone, contentDetails, ContentType.PLAINTEXT);

        }
        catch (Throwable e)
        {
            reportError(drone, testName, e);
        }
        finally
        {
            testCleanup(drone, testName);
        }
    }

    /**
     * 1) User logs in
     * 2) Performs live search with searchTerm = "w0rd1 w0rd5"
     * 3) Verify document with title file-1234-0 is displayed in document search results
     * 4) Verify document with title file-1234-1 my is not displayed in document search results
     * 5) Verify there are no sites and people results
     */
    @Test(groups = { "TestLiveSearch", "EnterpriseOnly" ,"TestBug"})
    public void AONE_15127()
    {
        // live search term is document title
        testName = getTestName();
        String testUser = testName + "@" + DOMAIN_LIVE_SEARCH;
        // String siteName = getSiteName(testName);

        String searchTerm = "w0rd1 w0rd5";
        String fileName1 = "file-1234-0";
        String fileName2 = "file-1234-1";

        ShareUser.login(drone, testUser, testPassword);

        LiveSearchDropdown liveSearchDropdown = ShareUserLiveSearch.liveSearch(drone, searchTerm);
        List<LiveSearchDocumentResult> liveSearchDocumentResults = ShareUserLiveSearch.getLiveSearchDocumentResults(liveSearchDropdown);

        if (liveSearchDocumentResults.size() == 0)
        {
            drone.deleteCookies();
            drone.refresh();
            drone.getCurrentPage().render();
            ShareUser.login(drone, testUser, testPassword);
            liveSearchDropdown = ShareUserLiveSearch.liveSearch(drone, searchTerm);
            liveSearchDocumentResults = ShareUserLiveSearch.getLiveSearchDocumentResults(liveSearchDropdown);
        }

        // Checks document titles
        Assert.assertTrue(liveSearchDocumentResults.size() > 0);
        List<String> documentTitles = ShareUserLiveSearch.getLiveSearchDocumentTitles(liveSearchDocumentResults);

        Assert.assertTrue(documentTitles.contains(fileName1));
        Assert.assertFalse(documentTitles.contains(fileName2));

        // Checks site result
        Assert.assertFalse(liveSearchDropdown.isSitesTitleVisible());

        // Checks people result
        Assert.assertFalse(liveSearchDropdown.isPeopleTitleVisible());

    }

    /**
     * DataPreparation method - AONE_14207
     * 1) Login
     * 2) Create User
     * 3) Create Site
     * 4) Create and upload file with content
     * 
     * @throws Exception
     */

    @Test(groups = { "DataPrepLiveSearch" })
    public void dataPrep_LiveSearch_AONE_14207() throws Exception
    {
        String testName = getTestName();
        String testUser = testName + "@" + DOMAIN_LIVE_SEARCH;
        String[] testUserInfo = new String[] { testUser };
        String siteName = getSiteName(testName);

        try
        {
            // Create user
            CreateUserAPI.createActivateUserAsTenantAdmin(drone, ADMIN_USERNAME, testUserInfo);

            // Login as created user
            ShareUser.login(drone, testUser, testPassword);

            // Create site
            SiteUtil.createSite(drone, siteName, AbstractUtils.SITE_VISIBILITY_PUBLIC);

            // Uploading files with testName as document title.

            String[] fileInfo = { testName };
            ShareUser.uploadFileInFolder(drone, fileInfo);

            // Created user logs out
            ShareUser.logout(drone);

        }
        catch (Throwable e)
        {
            reportError(drone, testName, e);
        }
        finally
        {
            testCleanup(drone, testName);
        }

    }

    /**
     * 1) User logs in
     * 2) Performs live search with testName as a search term
     * 3) Checks that the created document is displayed in document search results
     * 4) Checks that the number of documents displayed is less than five and that More link is not displayed
     * 5) User logs out
     */
    @Test(groups = { "TestLiveSearch","TestBug" })
    public void AONE_14207()
    {

        // live search term is document title
        testName = getTestName();
        String testUser = testName + "@" + DOMAIN_LIVE_SEARCH;

        ShareUser.login(drone, testUser, testPassword);

        LiveSearchDropdown liveSearchDropdown = ShareUserLiveSearch.liveSearch(drone, testName);

        List<LiveSearchDocumentResult> liveSearchDocumentResults = ShareUserLiveSearch.getLiveSearchDocumentResults(liveSearchDropdown);

        if (liveSearchDocumentResults.size() != 0)
        {
            drone.deleteCookies();
            drone.refresh();
            drone.getCurrentPage().render();
            ShareUser.login(drone, testUser, testPassword);
            liveSearchDropdown = ShareUserLiveSearch.liveSearch(drone, testName);
            liveSearchDocumentResults = ShareUserLiveSearch.getLiveSearchDocumentResults(liveSearchDropdown);
        }

        // Checks document titles
        Assert.assertTrue(liveSearchDocumentResults.size() > 0, "Failed to find document: ISSUE ACE-3319");
        List<String> documentTitles = ShareUserLiveSearch.getLiveSearchDocumentTitles(liveSearchDocumentResults);
        Assert.assertTrue(documentTitles.contains(testName));

        // Checks number of results and more link
        Assert.assertTrue(liveSearchDocumentResults.size() < 5);
        Assert.assertFalse(liveSearchDropdown.isMoreResultsVisible());
        ShareUser.logout(drone);

    }

}
