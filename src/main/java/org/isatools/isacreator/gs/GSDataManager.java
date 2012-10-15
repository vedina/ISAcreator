package org.isatools.isacreator.gs;

import org.apache.log4j.Logger;
import org.genomespace.client.DataManagerClient;
import org.genomespace.client.GsSession;
import org.genomespace.client.exceptions.NotFoundException;
import org.genomespace.client.utils.WebClientBuilder;
import org.genomespace.datamanager.core.GSDirectoryListing;
import org.genomespace.datamanager.core.GSFileMetadata;
import org.isatools.errorreporter.model.ErrorLevel;
import org.isatools.errorreporter.model.ErrorMessage;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 26/09/2012
 * Time: 14:41
 *
 * Data Manager for GenomeSpace
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class GSDataManager {

    private static Logger log = Logger.getLogger(GSDataManager.class);

    private List<ErrorMessage> messages;

    private GsSession gsSession = null;

    /***
     * Constructor. The data manager works for a particular GS session.
     *
     * @param session
     */
    public GSDataManager(GsSession session){
        gsSession = session;
        messages = new ArrayList<ErrorMessage>();
    }


    /**
     * List files in given directory
     *
     * @param dirPath
     */
    public List<String> ls(String dirPath){
        try{
            DataManagerClient dmClient = gsSession.getDataManagerClient();
            GSDirectoryListing dirListing = dmClient.list(dirPath);
            List<GSFileMetadata> fileMetadataList = dirListing.getContents();
            List<String> listing = new ArrayList<String>();
            for(GSFileMetadata fileMetadata:fileMetadataList){
                listing.add(fileMetadata.getName());
            }
            return listing;
        }catch(NotFoundException e){
           System.err.println("The directory path "+dirPath+" was not found in Genome Space.");
           System.exit(-1);
        }
        return null;
    }

    /**
     * Get InputStreams for all the files in a directory
     *
     * @param dirPath
     * @return
     */
    public List<InputStream> lsInputStreams(String dirPath) {
        //setting the max number of concurrent connections
        WebClientBuilder.setDefaultMaxConnectionsPerHost(10);
        DataManagerClient dmClient = gsSession.getDataManagerClient();
        GSDirectoryListing dirListing = dmClient.list(dirPath);
        List<GSFileMetadata> fileMetadataList = dirListing.getContents();
        List<InputStream> listing = new ArrayList<InputStream>();
        for(GSFileMetadata fileMetadata:fileMetadataList){
            System.out.println("fileMetadata="+fileMetadata);
            InputStream is = dmClient.getInputStream(fileMetadata);
            listing.add(is);
        }
        return listing;
    }

    /**
     * List files in home directory
     *
     * @param username
     */
    public void lsHome(String username){
        DataManagerClient dmClient = gsSession.getDataManagerClient();
        GSDirectoryListing homeDirInfo = dmClient.listDefaultDirectory();
    }


    public GSFileMetadata getFileMetadata(String filePath){
        DataManagerClient dmClient = gsSession.getDataManagerClient();
        GSFileMetadata fileMetadata = dmClient.getMetadata(filePath);
        return fileMetadata;
    }

    public boolean uploadFiles() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Download a file to a given directory
     *
     * @param fileToDownload
     * @param localDirPath
     * @return
     */
    public boolean downloadFile(String fileToDownload, String localDirPath) {
        //try{
            log.debug("fileToDownload="+fileToDownload);
            fileToDownload = transformURLtoFilePath(fileToDownload);
            DataManagerClient dmClient = gsSession.getDataManagerClient();
            GSFileMetadata fileToDownloadMetadata = dmClient.getMetadata(fileToDownload);
            System.out.println("remote file ="+fileToDownloadMetadata);
            String localFilePath = localDirPath+fileToDownloadMetadata.getName();
            System.out.println("local file = "+localFilePath);
            File localTargetFile = new File(localFilePath);
            dmClient.downloadFile(fileToDownloadMetadata, localTargetFile, true);
            return true;
        //}catch(){

        //}

    }

    /*
    public String getFilePath(String url){
        System.out.println("url="+url);
        DataManagerClient dmClient = gsSession.getDataManagerClient();
        GSFileMetadata fileMetadata = dmClient.getMetadata(url);
        System.out.println("fileMetadata="+fileMetadata);
        System.out.println("NAme="+fileMetadata.getName());
        return fileMetadata.getName();
    }
    */

    /**
     * Given a directory path in GS and a local directory path, it downloads all the files in the GS directory to the local directory.
     *
     * @param dirPath
     * @param localDirPath
     * @return
     */
    public boolean downloadAllFilesFromDirectory(String dirPath, String localDirPath) {

        DataManagerClient dmClient = gsSession.getDataManagerClient();
        System.out.println("dirPath="+dirPath);
        dirPath = transformURLtoFilePath(dirPath);

        System.out.println("dirPath="+dirPath);

        if (dirPath==null){
             System.out.println("dirPath is null!!!");
        }
        GSDirectoryListing dirListing = null;
        try{
            dirListing = dmClient.list(dirPath);
        }catch(NotFoundException ex){
            ex.printStackTrace();
            messages.add(new ErrorMessage(ErrorLevel.ERROR, "The directory "+dirPath+" was not found"));
            return false;
        }

        List<GSFileMetadata> fileMetadataList = dirListing.getContents();
        for(GSFileMetadata fileToDownload: fileMetadataList){
             String localFilePath = localDirPath+fileToDownload.getName();
             File localTargetFile = new File(localFilePath);
             dmClient.downloadFile(fileToDownload, localTargetFile,true);
        }
        return true;
    }

    private String transformURLtoFilePath(String url){
        if (url==null) return null;
        Pattern HOME = Pattern.compile("/Home/");
        Matcher m = HOME.matcher(url);
        while (m.find()) {
            return url.substring(m.start());
        }
        return null;
    }

    public boolean mkDir() {
        return false;
    }

    public void ls() {

    }

    public List<ErrorMessage> getMessages() {
        return messages;
    }


}
